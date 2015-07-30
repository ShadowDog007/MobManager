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

package ninja.mcknight.bukkit.mobmanager.limiter.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.P;
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;
import ninja.mcknight.bukkit.mobmanager.common.util.LocationCache;
import ninja.mcknight.bukkit.mobmanager.common.util.PlayerFinder;
import ninja.mcknight.bukkit.mobmanager.limiter.config.LimiterConfig;
import ninja.mcknight.bukkit.mobmanager.limiter.util.MobType;
import ninja.mcknight.bukkit.mobmanager.limiter.world.MMWorld;

/**
 * Listens for and counts mob spawns </br>
 * Prevents mob spawns if mob limits have been hit
 * 
 * @author Michael McKnight (ShadowDog007)
 *
 */
public class MobListener implements Listener
{	
	// Event listener methods
	
	/**
	 * Checks mob limits to determine if the mob can spawn </br>
	 * Only prevents natural spawns (Including for disabled mobs)
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onCreatureSpawn(final CreatureSpawnEvent event)
	{
		// Check if we should ignore the following spawn
		if (P.p().shouldIgnoreNextSpawn() || P.p().shouldLimiterIgnoreNextSpawn())
			return;
		
		if (!P.p().getPluginIntegration().canDespawn(event.getEntity()))
			return;
		
		// Checks for spawn reasons we want to limit
		if (!LimiterConfig.enabledSpawnReasons.containsValue(event.getSpawnReason().toString()))
			return;
		
		ExtendedEntityType eMobType = ExtendedEntityType.valueOf(event.getEntity());
		// Check if the entity is disabled
		if (LimiterConfig.disabledMobs.contains(eMobType) || eMobType.hasParent() && LimiterConfig.disabledMobs.contains(eMobType.getParent()))
		{
			// Prevent the entity from spawning
			event.setCancelled(true);
			return;
		}
		
		// Checks if we can ignore the creature spawn
		MobType mob = eMobType.getMobType(event.getEntity());
		if (mob == null || LimiterConfig.ignoredMobs.contains(eMobType) || eMobType.hasParent() && LimiterConfig.ignoredMobs.contains(eMobType.getParent()))
		{
			return;
		}

		final MMWorld world = MMComponent.getLimiter().getWorld(event.getLocation().getWorld());
		// If the world is not found we ignore the spawn
		if (world == null)
		{
			return;
		}
		
		
		// Animals need to be counted per chunk as well
		if (mob == MobType.ANIMAL)
		{
			if (event.getSpawnReason() == SpawnReason.BREEDING || event.getSpawnReason() == SpawnReason.EGG)
			{
				// If breeding limit is invalid (-ve) it is disabled
				if (world.worldConf.breedingLimit >= 0)
				{
					int animalCount = 0;
					// If the limit is 0 there is no point counting the mobs
					if (world.worldConf.breedingLimit != 0)
					{
						for (Entity entity : LocationCache.getCachedLocation(event.getEntity()).getChunk().getEntities())
						{
							if (MobType.ANIMAL.belongs(entity))
								++animalCount;
						}
					}
					// Cancels the event if the chunk is not within breeding limits
					if (animalCount >= world.worldConf.breedingLimit)
					{
						event.setCancelled(true);
					}
				}
				
				// There is probably going to be a player there? So don't bother checking for one?
				return;
			}
		}

		// Try to update the number of mobs in this world
		
		// Check if we are within spawn limits
		if (!world.withinMobLimit(eMobType, event.getEntity()))
		{
			event.setCancelled(true);
			return;
		}
		
		// Checks that there is a player within range of the creature spawn
		if (!PlayerFinder.playerNear(world, event.getEntity(), PlayerFinder.mobFlys(event.getEntity())))
		{
			event.setCancelled(true);
		}
	}
	
	/**
	 * Counts the mobs which spawn
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void countCreatureSpawns(CreatureSpawnEvent event)
	{
		// Fetch the world the creature spawned in
		MMWorld world = MMComponent.getLimiter().getWorld(event.getLocation().getWorld());
		// Do nothing if the world is inactive
		if (world == null)
		{
			return;
		}
		
		// Fetch the entitys type
		ExtendedEntityType eType = ExtendedEntityType.valueOf(event.getEntity());
		
		// If the mob is being ignored it is not counted towards the limits
		if (LimiterConfig.ignoredMobs.contains(eType))
			return;
		
		// Increment counts for the mob
		world.incrementMobCount(eType, event.getEntity());
	}
	
	/**
	 * Decrements mob counts when a mob dies. </br>
	 * <b>Note: <i>Does not seem to catch mob despawns so world.updateMobCount() recounts every tick</i></b>
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDeath(final EntityDeathEvent event)
	{
		// Fetch the world the spawn occurred in
		final MMWorld world = MMComponent.getLimiter().getWorld(event.getEntity().getWorld());
		// Do nothing if the world is inactive
		if (world == null)
		{
			return;
		}
		
		// Fetch the entity type
		ExtendedEntityType eType = ExtendedEntityType.valueOf(event.getEntity());
		// If the mob is being ignored it was not counted towards the limits
		if (LimiterConfig.ignoredMobs.contains(eType) || eType.hasParent() && LimiterConfig.ignoredMobs.contains(eType.getParent()))
			return;
		
		// Decrement counts for the entity
		world.decrementMobCount(eType, event.getEntity());
	}
}
