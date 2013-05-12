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

package com.forgenz.mobmanager.limiter.tasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import com.forgenz.mobmanager.MMComponent;
import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.limiter.config.LimiterConfig;
import com.forgenz.mobmanager.limiter.util.MobDespawnCheck;
import com.forgenz.mobmanager.limiter.util.MobType;
import com.forgenz.mobmanager.limiter.world.MMWorld;


public class MobDespawnTask extends BukkitRunnable
{
	/**
	 * This class is responsible for making sure the iterators are accessed while still knowing which world they belong to</br>
	 * Used to make it easier to spread the task over multiple ticks
	 * @author Michael McKnight (ShadowDog007)
	 *
	 */
	private class EntityIterator
	{
		private int currentIndex = 0;
		private final ArrayList<MMWorld> worlds;
		private final ArrayList<Iterator<LivingEntity>> iterators;
		
		/**
		 * Sets up the iterators required
		 */
		EntityIterator()
		{
			worlds = new ArrayList<MMWorld>(MMComponent.getLimiter().getWorlds().size());
			iterators = new ArrayList<Iterator<LivingEntity>>(MMComponent.getLimiter().getWorlds().size());
			
			for (MMWorld world : MMComponent.getLimiter().getWorlds().values())
			{
				worlds.add(world);
			}
		}
		
		/**
		 * Sets up iterators for entities in each world one by one
		 * @return False when there are no more worlds to setup
		 */
		public boolean setupNextWorld()
		{			
			MMWorld world = worlds.get(currentIndex);
			
			List<LivingEntity> entities = world.getWorld().getLivingEntities();
			
			iterators.add(entities.iterator());
			
			world.updateMobCounts(entities);
			
			if (++currentIndex >= worlds.size())
			{
				currentIndex = 0;
				return false;
			}
			
			return true;
		}
		
		/**
		 * Fetches the world we are currently iterating over
		 */
		public MMWorld getWorld()
		{
			return worlds.get(currentIndex);
		}
		
		/**
		 * Checks if there are more entities to check</br>
		 * Also iterates to the next iterator when the current one is finished
		 */
		public boolean hasNext()
		{
			if (currentIndex >= iterators.size())
				return false;
			
			if (iterators.get(currentIndex) == null)
			{
				++currentIndex;
				return hasNext();
			}
			
			if (iterators.get(currentIndex).hasNext())
				return true;
			
			if (++currentIndex >= iterators.size())
				return false;
			
			return hasNext();
		}
		
		/**
		 * Fetches the next entity
		 */
		public LivingEntity next()
		{
			if (!hasNext())
				return null;
			
			return iterators.get(currentIndex).next();
		}
	}
	
	private AtomicBoolean running = new AtomicBoolean(false);
	/**
	 * Sets up the despawn scan
	 */
	@Override
	public void run()
	{
		if (MMComponent.getLimiter().getWorlds() == null)
		{
			cancel();
			return;
		}
		
		/* ######## START TASK ######## */
		// Make sure the task is not run more than once at a time
		if (!running.compareAndSet(false, true))
			return;
		
		// Create the entity iterator object
		final EntityIterator it = new EntityIterator();
		
		final List<LivingEntity> mobsToDespawn = LimiterConfig.useAsyncDespawnScanner ? new ArrayList<LivingEntity>() : null;
		
		/* ######## SCANNER CREATION ######## */
		// Create the despawner task
		final Runnable removeQueueFillTask = new Runnable()
		{
			
			/**
			 * Scans for and removes entities which are not required
			 */
			@Override
			public void run()
			{
				// Note the time we start
				long start = System.nanoTime();
				LivingEntity entity;
				
				// Iterate through each entity until there are none left or the task has run for 0.4ms
				while ((entity = it.next()) != null && (System.nanoTime() - start) < 400000L)
				{
					// Check if the mob should be despawned
					if (MobDespawnCheck.shouldDespawn(it.getWorld(), entity))
					{
						// Make sure we don't use bukkit methods in async
						if (!LimiterConfig.useAsyncDespawnScanner)
						{
							entity.remove();
							it.getWorld().decrementMobCount(MobType.valueOf(entity));
						}
						else
						{
							mobsToDespawn.add(entity);
						}
					}
				}
				
				// Check if there is more to go
				if (it.hasNext() && P.p() != null)
				{
					// Schedule the task to run later
					if (LimiterConfig.useAsyncDespawnScanner)
						P.p().getServer().getScheduler().runTaskLaterAsynchronously(P.p(), this, 1L);
					else
						P.p().getServer().getScheduler().runTaskLater(P.p(), this, 1L);
				}
				// If we are in async we need to schedule a new task to remove the entities
				else if (P.p() != null)
				{
					if (LimiterConfig.useAsyncDespawnScanner)
					{
						final LivingEntity[] entities = mobsToDespawn.toArray(new LivingEntity[0]);
						mobsToDespawn.clear();
						
						P.p().getServer().getScheduler().runTaskLater(P.p(), new Runnable()
						{

							@Override
							public void run()
							{
								for (int i = 0; i < entities.length; ++i)
								{
									if (entities[i].isValid())
									{
										entities[i].remove();
									}
								}
							}
							
						}, 1L);
					}
					
					/* ######## END TASK ######## */
					running.compareAndSet(true, false);
				}
			}
		};
		
		/* ######## SETUP TASK START ######## */
		// Setups up each world, one per tick then starts the despawn scanner
		P.p().getServer().getScheduler().runTaskLater(P.p(), new Runnable()
		{
			@Override
			public void run()
			{
				if (it.setupNextWorld())
					P.p().getServer().getScheduler().runTaskLater(P.p(), this, 1L);
				else
				{
					/* ######## SCAN START ######## */
					// Run the despawner task
					if (LimiterConfig.useAsyncDespawnScanner)
						P.p().getServer().getScheduler().runTaskLaterAsynchronously(P.p(), removeQueueFillTask, 1L);
					else
						P.p().getServer().getScheduler().runTaskLater(P.p(), removeQueueFillTask, 1L);
				}	
			}
		}, 1L);
	}
}
