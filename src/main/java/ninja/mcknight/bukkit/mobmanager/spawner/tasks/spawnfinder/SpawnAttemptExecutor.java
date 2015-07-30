/*
 * Copyright 2013 Michael McKnight. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package ninja.mcknight.bukkit.mobmanager.spawner.tasks.spawnfinder;

import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.common.util.LocationCache;
import ninja.mcknight.bukkit.mobmanager.common.util.MMThreadFactory;
import ninja.mcknight.bukkit.mobmanager.limiter.world.MMWorld;
import ninja.mcknight.bukkit.mobmanager.spawner.config.Region;
import ninja.mcknight.bukkit.mobmanager.spawner.config.SpawnerConfig;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import ninja.mcknight.bukkit.mobmanager.common.util.ThreadCache;

/**
 * Handles initialisation and execution of individual spawn attempts
 */
public class SpawnAttemptExecutor implements Runnable
{
	protected final SpawnFinder spawnFinder;
	protected final SpawnerConfig cfg;
	private final Queue<Player> playerQueue;
	
	private final MMThreadFactory threadFactory;
	private ThreadPoolExecutor executor;
	protected final ThreadCache<SpawnAttemptCache> threadCache;
	
	private int currentThreads;
	
	private final PriorityBlockingQueue<Runnable> syncWorkerQueue;
	
	private int ticksLeft;

	public SpawnAttemptExecutor(SpawnFinder spawnFinder, Queue<Player> playerQueue)
	{
		this.spawnFinder = spawnFinder;
		this.cfg = MMComponent.getSpawner().getConfig();
		this.playerQueue = playerQueue;
		
		threadFactory = new MMThreadFactory(MMComponent.Component.SPAWNER, "SpawnFinder");
		this.threadCache = new ThreadCache<SpawnAttemptCache>(SpawnAttemptCache.class);
		
		int c = cfg.spawnFinderThreads;
		Comparator<Object> taskComparator = new Comparator<Object>()
		{
			@Override
			public int compare(Object arg0, Object arg1)
			{
				return arg0.hashCode() > arg1.hashCode() ? -1 : 1;
			}
			
		};
		this.syncWorkerQueue =  new PriorityBlockingQueue<Runnable>(20, taskComparator);
		this.executor = new ThreadPoolExecutor(c, c, 0L, TimeUnit.NANOSECONDS, new PriorityBlockingQueue<Runnable>(20, taskComparator), threadFactory);
	}
	
	/**
	 * Starts the SpawnAttemptExecutor
	 * 
	 * @param ticksLeft The number of ticks left before the next spawn
	 */
	public void execute(int ticksLeft)
	{
		boolean runMain = !playerQueue.isEmpty();
		
		if (runMain)
		{
			// Set ticks left
			this.ticksLeft = ticksLeft;
			
			// Fetch the max number of threads
			currentThreads = cfg.spawnFinderThreads;
			
			// Find a reasonable number of threads to run the main task in
			for (; currentThreads > 1 && playerQueue.size() / currentThreads <= 2; --currentThreads);
			
			// Execute the main tasks
			for (int i = 0; i < currentThreads; ++i)
				executor.execute(this);
		}
		else
			currentThreads = 0;
		
		// Run any tasks in the worker queue (Probably full)
		Runnable task;
		while ((task = syncWorkerQueue.poll()) != null)
		{
			task.run();
		}
	}
	
	@Override
	public void run()
	{
		int checkPlayers = ticksLeft != 0 ? playerQueue.size() / ticksLeft / currentThreads : playerQueue.size();
		if (checkPlayers == 0)
			checkPlayers = playerQueue.size();
		
		Location playerLoc = LocationCache.getCachedLocation();
		
		Player player;
		while (checkPlayers-- > 0 && (player = playerQueue.poll()) != null)
		{
			if (!player.isValid())
				continue;
			
			// Check if the player is in creative mode
			if (cfg.ignoreCreativePlayers && player.getGameMode() == GameMode.CREATIVE)
				continue;
			
			// Find the region the player is in
			player.getLocation(playerLoc);
			Region playerRegion = cfg.getRegion(playerLoc);
			
			// Check if we can spawn in this region
			if (playerRegion == null || playerRegion.spawnAttempts <= 0)
				continue;
			
			// Check if the player already has too many mobs spawned around them
			boolean outsideSpawnLimits = playerRegion.maxPlayerMobs > 0 && spawnFinder.getMobCount(player, playerRegion.playerMobCooldown) >= playerRegion.maxPlayerMobs;

			// If we are outside of spawn limits continue
			// Unless the region has mobs which can ignore the spawn limits
			if (outsideSpawnLimits && !playerRegion.ignoreMobLimits())
				continue;
			
			// Find the max spawn range for the world
			MMWorld world = MMComponent.getLimiter().getWorld(playerLoc.getWorld());
			int yHeight = world.getWorld().getEnvironment() == Environment.NORMAL ? playerLoc.getBlockY() : Short.MAX_VALUE;
			
			int maxRange = Math.min(world != null ? world.getSearchDistance((short) yHeight) : Integer.MAX_VALUE, playerRegion.getMaxBlockRange(yHeight));
			int minRange = Math.min(playerRegion.getMinBlockRange(yHeight), maxRange);
			int heightRange = world != null ? world.getSearchHeight() : 24;
			
			// Attempt 'X' spawns
			for (int i = 0; i < playerRegion.spawnAttempts; ++i)
				executor.execute(new SpawnAttempt(this, player, maxRange, minRange, heightRange, outsideSpawnLimits));
		}
	}
	
	protected void addTask(Runnable task, boolean sync)
	{
		if (sync)
			syncWorkerQueue.add(task);
		else
			executor.execute(task);
	}
}
