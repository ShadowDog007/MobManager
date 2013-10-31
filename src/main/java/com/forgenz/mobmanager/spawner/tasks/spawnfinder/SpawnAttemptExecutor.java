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

package com.forgenz.mobmanager.spawner.tasks.spawnfinder;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.forgenz.mobmanager.MMComponent;
import com.forgenz.mobmanager.MMComponent.Component;
import com.forgenz.mobmanager.common.util.LocationCache;
import com.forgenz.mobmanager.common.util.MMThreadFactory;
import com.forgenz.mobmanager.common.util.ThreadCache;
import com.forgenz.mobmanager.limiter.world.MMWorld;
import com.forgenz.mobmanager.spawner.config.Region;
import com.forgenz.mobmanager.spawner.config.SpawnerConfig;

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
	
	private final LinkedBlockingQueue<Runnable> workerQueue = new LinkedBlockingQueue<Runnable>();
	
	private int ticksLeft;
	
	public SpawnAttemptExecutor(SpawnFinder spawnFinder, Queue<Player> playerQueue)
	{
		this.spawnFinder = spawnFinder;
		this.cfg = MMComponent.getSpawner().getConfig();
		this.playerQueue = playerQueue;
		
		threadFactory = new MMThreadFactory(Component.SPAWNER, "SpawnFinder");
		this.threadCache = new ThreadCache<SpawnAttemptCache>(SpawnAttemptCache.class);
		
		int c = cfg.spawnFinderThreads;
		this.executor = new ThreadPoolExecutor(c, c, 0L, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
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
			
			// Find a resonable number of threads to run the main task in
			for (; currentThreads > 1 && playerQueue.size() / currentThreads <= 2; --currentThreads);
			
			// Execute the main tasks
			for (int i = 0; i < currentThreads; ++i)
				executor.execute(this);
		}
		else
			currentThreads = 0;
		
		// Run any tasks in the worker queue (Probably full)
		Runnable task;
		while ((task = workerQueue.poll()) != null)
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
			
			// Find the region the player is in
			player.getLocation(playerLoc);
			Region playerRegion = cfg.getRegion(playerLoc);
			
			// Check if we can spawn in this region
			if (playerRegion == null || playerRegion.spawnAttempts <= 0)
				continue;

			// Check if the player already has too many mobs spawned around them
			if (playerRegion.maxPlayerMobs > 0 && spawnFinder.getMobCount(player, playerRegion.mobLimitTimeout) >= playerRegion.maxPlayerMobs)
				continue;
			
			// Find the max spawn range for the world
			MMWorld world = MMComponent.getLimiter().getWorld(playerLoc.getWorld());
			int maxRange = world != null ? world.getSearchDistance((short) playerLoc.getBlockY()) : playerRegion.maxBlockRange;
			if (playerRegion.maxBlockRange < maxRange)
				maxRange = playerRegion.maxBlockRange;
			int minRange = playerRegion.minBlockRange < maxRange ? playerRegion.minBlockRange : maxRange;
			int heightRange = world != null ? world.getSearchHeight() : 24;
			
			// Attempt 'X' spawns
			for (int i = 0; i < playerRegion.spawnAttempts; ++i)
				executor.execute(new SpawnAttempt(this, player, maxRange, minRange, heightRange));
		}
	}
	
	protected void addTask(Runnable task, boolean sync)
	{
		if (sync)
			workerQueue.add(task);
		else
			executor.execute(task);
	}
}
