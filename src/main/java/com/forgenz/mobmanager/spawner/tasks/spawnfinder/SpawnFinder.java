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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.forgenz.mobmanager.MMComponent;
import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.common.util.LocationCache;
import com.forgenz.mobmanager.common.util.PlayerFinder.FinderMode;
import com.forgenz.mobmanager.spawner.config.SpawnerConfig;

/**
 * Initiates the finding of spawns for mobs
 */
public class SpawnFinder extends BukkitRunnable
{
	private final SpawnerConfig cfg;
	
	private final Queue<Player> playerQueue = new ConcurrentLinkedQueue<Player>();
	private int ticksLeft;
	
	private final SpawnAttemptExecutor spawnAttemptExecutor = new SpawnAttemptExecutor(this, playerQueue);
	
	private final ConcurrentHashMap<String, List<WeakReference<LivingEntity>>> playerMobs; 
	
	public SpawnFinder()
	{
		// Fetch spawner config
		cfg = MMComponent.getSpawner().getConfig();
		
		playerMobs = new ConcurrentHashMap<String, List<WeakReference<LivingEntity>>>(cfg.spawnFinderThreads);
		
		this.ticksLeft = cfg.ticksPerSpawn;
		
		runTaskTimer(P.p(), 1L, 2L);
	}
	
	@Override
	public void run()
	{
		// Initialise the task
		if (ticksLeft-- == cfg.ticksPerSpawn)
		{
			for (Player player : Bukkit.getOnlinePlayers())
				playerQueue.add(player);
			return;
		}
		
		spawnAttemptExecutor.execute(ticksLeft);
		
		// Reset everything
		if (ticksLeft == 0)
		{
			playerQueue.clear();
			ticksLeft = cfg.ticksPerSpawn;
		}
	}
	
	/**
	 * Fetches the number of mobs which the player has spawned
	 * 
	 * @param player The player involved
	 * @param mobLimitTimeout The timeout for when a mob is removed
	 * 
	 * @return The number of mobs which the player has spawned
	 */
	public int getMobCount(Player player, int mobLimitTimeout)
	{
		synchronized (player)
		{
			// Fetch the list of mobs
			List<WeakReference<LivingEntity>> playersList = playerMobs.get(player.getName());
			
			// If the list doesn't exist the mob count is
			if (playersList != null)
			{
				// Fetch the iterator for the list
				Iterator<WeakReference<LivingEntity>> it = playersList.iterator();
				while (it.hasNext())
				{
					// Fetch the entity
					LivingEntity e = it.next().get();
					
					// If the entity is invalid or the timeout has passed remove them from the list
					if (e == null || !e.isValid() || (mobLimitTimeout > 0 && e.getTicksLived() > mobLimitTimeout) || playerOutOfRange(player, e))
						it.remove();
				}
				
				// Return the new size of the list
				return playersList.size();
			}
			// List doesn't exist so mobcount is 0
			return 0;
		}
	}
	
	/**
	 * Checks if the player is out of range of the given entity
	 * 
	 * @param player The player
	 * @param entity The entity
	 * 
	 * @return True if the player and entity are too far away from each other to count towards the player mob limit
	 */
	private boolean playerOutOfRange(Player player, LivingEntity entity)
	{
		// If the automatic removal is disabled we do nothing
		if (cfg.mobDistanceForLimitRemoval <= 0)
			return false;
		
		// If the entity and player are in different worlds they are too far away
		if (player.getWorld() != entity.getWorld())
			return true;
		
		// Fetch player and mob locations
		Location playerLoc = LocationCache.getCachedLocation(player);
		Location mobLoc = LocationCache.getCachedLocation(entity);
		
		// Check if the locations are out of range
		return !FinderMode.CYLINDER.withinRange(playerLoc, mobLoc, cfg.mobDistanceForLimitRemoval, 32);
	}
	
	/**
	 * Clears mobs which count towards the players limit
	 * 
	 * @param player
	 */
	public void removeMobs(Player player)
	{
		synchronized (player)
		{
			playerMobs.remove(player.getName());
		}
	}

	/**
	 * Add a mob to the given players mob count
	 * 
	 * @param player The player involved
	 * @param entity The entity to add
	 */
	public void addMob(Player player, LivingEntity entity)
	{
		synchronized (player)
		{
			// Fetch the players mob list
			List<WeakReference<LivingEntity>> playersList = playerMobs.get(player.getName());
			
			// If the list doesn't exist create it
			if (playersList == null)
			{
				playersList = new ArrayList<WeakReference<LivingEntity>>();
				playerMobs.put(player.getName(), playersList);
			}
			
			// Add a reference to the mob
			playersList.add(new WeakReference<LivingEntity>(entity));
		}
	}
}
