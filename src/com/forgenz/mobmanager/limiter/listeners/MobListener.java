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

package com.forgenz.mobmanager.limiter.listeners;

import java.util.List;

import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Flying;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.common.integration.PluginIntegration;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;
import com.forgenz.mobmanager.limiter.config.Config;
import com.forgenz.mobmanager.limiter.util.MobType;
import com.forgenz.mobmanager.limiter.util.Spiral;
import com.forgenz.mobmanager.limiter.world.MMChunk;
import com.forgenz.mobmanager.limiter.world.MMCoord;
import com.forgenz.mobmanager.limiter.world.MMLayer;
import com.forgenz.mobmanager.limiter.world.MMWorld;

/**
 * Listens for and counts mob spawns </br>
 * Prevents mob spawns if mob limits have been hit
 * 
 * @author Michael McKnight (ShadowDog007)
 *
 */
public class MobListener implements Listener
{
	public static MobListener i = null;
	
	public MobListener()
	{
		i = this;
	}
	
	public boolean mobFlys(Entity entity)
	{
		if (entity instanceof Flying || entity instanceof Bat)
			return true;
		return false;
	}
	
	/**
	 * Scans nearby chunks for players on layers which cross a given height
	 * 
	 * @param world The World the chunk is in
	 * @param chunk The coordinate of the center chunk
	 * @param y The height to search for players at
	 * @param flying If true layers further down will be checked for player to allow for more flying mobs
	 * 
	 * @return True if there is a player within range of the center chunk and in
	 *         a layer which overlaps the height 'y'
	 */
	public boolean playerNear(final MMWorld world, final MMCoord center, final int y, boolean flying)
	{
		int searchDist = world.getSearchDistance();
		
		if (y <= world.worldConf.groundHeight)
			searchDist = world.worldConf.undergroundSpawnChunkSearchDistance;
		
		// Creates a spiral generator
		final Spiral spiral = new Spiral(center, searchDist);
		
		MMCoord coord;
		// Loop through until the entire circle has been generated
		while ((coord = spiral.run()) != null)
		{
			// Fetch the given chunk
			final MMChunk chunk = world.getChunk(coord);
			
			// If the chunk is not loaded continue
			if (chunk == null)
				continue;
			
			// If the chunk has no players in it continue
			if (!chunk.hasPlayers())
				continue;
			
			// Fetch layers to check for players
			List<MMLayer> layers = flying ? chunk.getLayersAtAndBelow(y) : chunk.getLayersAt(y);
			
			// Loop through each layer which overlaps the height 'y'
			for (final MMLayer layer : layers)
			{
				// If the layer has a player in it then there is a player close
				// to the center near Y = 'y'
				if (!layer.isEmpty())
				{
					return true;
				}
			}
		}
		return false;
	}
	
	// Event listener methods
	/**
	 * Checks mob limits to determine if the mob can spawn </br>
	 * Only prevents natural spawns (Including for disabled mobs)
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onCreatureSpawn(final CreatureSpawnEvent event)
	{
		// Check if we should ignore the following spawn
		if (P.p.shouldIgnoreNextSpawn() || P.p.shouldLimiterIgnoreNextSpawn())
			return;
		
		if (!PluginIntegration.getInstance().canDespawn(event.getEntity()))
			return;
		
		// Checks for spawn reasons we want to limit
		if (!Config.enabledSpawnReasons.containsValue(event.getSpawnReason().toString()))
			return;
		
		// Check if the entity is disabled
		if (Config.disabledMobs.contains(ExtendedEntityType.get(event.getEntity())))
		{
			// Prevent the entity from spawning
			event.setCancelled(true);
			return;
		}
		
		// Checks if we can ignore the creature spawn
		MobType mob = MobType.valueOf(event.getEntity());
		if (mob == null || Config.ignoredMobs.contains(ExtendedEntityType.get(event.getEntity())))
		{
			return;
		}

		final MMWorld world = P.worlds.get(event.getLocation().getWorld().getName());
		// If the world is not found we ignore the spawn
		if (world == null)
		{
			return;
		}
		
		MMChunk chunk = null;
		
		
		// Animals need to be counted per chunk as well
		if (mob == MobType.ANIMAL)
		{
			if (event.getSpawnReason() == SpawnReason.BREEDING || event.getSpawnReason() == SpawnReason.EGG)
			{
				// Try update mob counts
				world.updateMobCounts();
				
				chunk = world.getChunk(event.getLocation().getChunk());
				if (chunk == null)
				{
					if (!Config.disableWarnings)
						P.p.getLogger().warning(mob + " spawn was allowed because chunk was missing");
					return;
				}
				
				// Cancels the event if the chunk is not within breeding limits
				if (!chunk.withinBreedingLimits())
				{
					event.setCancelled(true);
				}
				
				// There is probably going to be a player there? So don't bother checking for one?
				return;
			}
		}

		// Try to update the number of mobs in this world
		world.updateMobCounts();
		// Check if we are within spawn limits
		if (!world.withinMobLimit(mob))
		{
			event.setCancelled(true);
			return;
		}
		
		// Fetches the chunk if it has not been fetched already
		if (chunk == null)
			chunk = world.getChunk(event.getLocation().getChunk());
		
		if (chunk == null)
		{
			if (!Config.disableWarnings)
				P.p.getLogger().warning(mob + " spawn was allowed because chunk was missing");
			return;
		}
		
		// Checks that there is a player within range of the creature spawn
		if (!playerNear(world, chunk.getCoord(), event.getLocation().getBlockY(), mobFlys(event.getEntity())))
		{
			event.setCancelled(true);
			return;
		}
	}
	
	/**
	 * Counts the mobs which spawn
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void countCreatureSpawns(CreatureSpawnEvent event)
	{
		// Check if we don't need to count the mob
		MobType mob = MobType.valueOf(event.getEntity());
		if (mob == null || Config.ignoredMobs.contains(ExtendedEntityType.get(event.getEntity())))
		{
			return;
		}
		
		// Fetch the world the creature spawned in
		MMWorld world = P.worlds.get(event.getLocation().getWorld().getName());
		// Do nothing if the world is inactive
		if (world == null)
		{
			return;
		}
		
		// Counts the mob
		// Must count animals inside of chunks too
		if (mob == MobType.ANIMAL)
		{
			MMChunk chunk = world.getChunk(event.getLocation().getChunk());
			if (chunk != null)
			{
				chunk.changeNumAnimals(true);
			}
		}
		
		world.incrementMobCount(mob);
	}
	
	/**
	 * Decrements mob counts when a mob dies. </br>
	 * <b>Note: <i>Does not seem to catch mob despawns so world.updateMobCount() recounts every tick</i></b>
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDeath(final EntityDeathEvent event)
	{
		// Check if we can ignore the mob spawn
		MobType mob = MobType.valueOf(event.getEntity());
		if (mob == null)
		{
			return;
		}

		// Fetch the world the spawn occurred in
		final MMWorld world = P.worlds.get(event.getEntity().getLocation().getWorld().getName());
		// Do nothing if the world is inactive
		if (world == null)
		{
			return;
		}
		
		// Animals must be counted in chunks as well
		if (mob == MobType.ANIMAL)
		{
			// Fetch chunk the animal died in
			MMChunk chunk = world.getChunk(event.getEntity().getLocation().getChunk());
			if (chunk != null)
			{
				chunk.changeNumAnimals(false);
			}
		}
		
		world.decrementMobCount(mob);
	}
}
