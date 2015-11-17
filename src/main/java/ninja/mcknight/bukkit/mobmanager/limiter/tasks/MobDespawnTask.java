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

package ninja.mcknight.bukkit.mobmanager.limiter.tasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;
import ninja.mcknight.bukkit.mobmanager.limiter.config.LimiterConfig;
import ninja.mcknight.bukkit.mobmanager.limiter.util.MobDespawnCheck;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.P;
import ninja.mcknight.bukkit.mobmanager.limiter.world.MMWorld;


public class MobDespawnTask extends BukkitRunnable
{	
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
		
		// Make sure the task is not run more than once at a time
		if (!running.compareAndSet(false, true))
			return;
		
		// Setups up each world, one per tick then starts the despawn scanner
		(new DespawnSetup(running)).runTaskTimer(P.p(), 1L, 1L);
	}
}

/**
 * This class is responsible for making sure the iterators are accessed while still knowing which world they belong to</br>
 * Used to make it easier to spread the task over multiple ticks
 * @author Michael McKnight (ShadowDog007)
 *
 */
class EntityIterator
{
	private int currentIndex = 0;
	private final ArrayList<MMWorld> worlds;
	private final ArrayList<Iterator<LivingEntity>> iterators;
	
	/**
	 * Sets up the iterators required
	 */
	EntityIterator()
	{
		// Fetch the worlds to be checked
		MMWorld[] mmWorlds = MMComponent.getLimiter().getWorlds();
		
		worlds = new ArrayList<MMWorld>(mmWorlds.length);
		iterators = new ArrayList<Iterator<LivingEntity>>(mmWorlds.length);
		
		// Add each world to the list
		for (MMWorld world : mmWorlds)
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
		// Fetch the current world we are setting up
		MMWorld world = worlds.get(currentIndex);
		
		// Fetch a list of entities in the world
		List<LivingEntity> entities = world.getWorld().getLivingEntities();
		
		// Add the iterator for the entity list to our list
		iterators.add(entities.iterator());
		
		// Update the mob counts of the world so we know they are up to date
		world.updateMobCounts(entities);
		
		// Increment the index and return false if we are finished
		if (++currentIndex >= worlds.size())
		{
			// Reset the current index for the iterator
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
		// If the index is too high we have no more iterators left
		if (currentIndex >= iterators.size())
			return false;
		
		// If the iterator at the current index is null we skip it
		if (iterators.get(currentIndex) == null)
		{
			++currentIndex;
			return hasNext();
		}
		
		// If the current iterator has another entity we return true
		if (iterators.get(currentIndex).hasNext())
			return true;
		
		// If there are no more iterators we are done
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
	
	public void empty()
	{
		currentIndex = iterators.size();
	}
}

class DespawnSetup extends BukkitRunnable
{
	private final AtomicBoolean running;
	private final EntityIterator it;
	
	DespawnSetup(AtomicBoolean running)
	{
		this.running = running;
		this.it = new EntityIterator();
	}
	
	public void run()
	{
		if (!it.setupNextWorld())
		{
			// Stop the setup task
			cancel();
			
			DespawnTask task = new DespawnTask(running, it);
			// Run the despawner task
			if (LimiterConfig.useAsyncDespawnScanner)
				task.runTaskTimerAsynchronously(P.p(), 1L, 1L);
			else
				task.runTaskTimer(P.p(), 1L, 1L);
		}	
	}
}

class DespawnTask extends BukkitRunnable
{
	private boolean warning = false;
	private final AtomicBoolean running;
	private final EntityIterator it;
	
	DespawnTask(AtomicBoolean running, EntityIterator it)
	{
		this.running = running;
		this.it = it;
	}
	
	
	/**
	 * Scans for and removes entities which are not required
	 */
	@Override
	public void run()
	{
		try
		{
			// Note the time we start
			long start = System.nanoTime();
			LivingEntity entity;

			// Iterate through each entity until there are none left or the task has run for 0.5ms
			while ((entity = it.next()) != null && (System.nanoTime() - start) < 500000L)
			{
				// Check if the mob should be despawned
				if (MobDespawnCheck.shouldDespawn(it.getWorld(), entity, true))
				{
					// try/catch just in case Bukkit decide to add an event for removing entities
					try
					{
						entity.remove();
					}
					catch (Exception e)
					{
						// Make sure this isn't spamed
						if (!warning)
						{
							warning = true;
							MMComponent.getLimiter().warning("Please disable \"UseAsyncDespawnScanner\" it needs to be fixed. Please notify ShadowDog007");
							MMComponent.getLimiter().warning("Automatically switching to Synchronous Despawn Scanner");
							LimiterConfig.useAsyncDespawnScanner = false;

							// Empty the iterator
							it.empty();
							break;
						}
					}

					it.getWorld().decrementMobCount(ExtendedEntityType.valueOf(entity), entity);
				}
			}

			boolean finished = !it.hasNext() || P.p() == null;

			// The task is finished, and allow a new one to start
			if (finished)
			{
				/* ######## END TASK ######## */
				cancel();
				running.compareAndSet(true, false);
			}
		}
		catch (Throwable e)
		{
			P.p().getLogger().severe("Something unexpected happened: " + e.getMessage());
			e.printStackTrace();

			/* ######## END TASK ######## */
			cancel();
			running.compareAndSet(true, false);
		}
	}
}

