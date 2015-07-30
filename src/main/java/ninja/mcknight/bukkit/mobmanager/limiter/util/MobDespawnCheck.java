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

package ninja.mcknight.bukkit.mobmanager.limiter.util;

import ninja.mcknight.bukkit.mobmanager.common.util.PlayerFinder;
import ninja.mcknight.bukkit.mobmanager.limiter.config.LimiterConfig;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.EntityEquipment;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.P;
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;
import ninja.mcknight.bukkit.mobmanager.limiter.world.MMWorld;

public class MobDespawnCheck
{
	/**
	 * Checks the given mob to see if it should be despawned
	 * @param entity The entity to be checked
	 * @param findPlayer Whether or not to try find a player (False if you know there are no nearby players)
	 * @return True if the entity should be despawned
	 */
	public static boolean shouldDespawn(LivingEntity entity, boolean findPlayer)
	{
		if (entity == null)
			return false;
		
		return shouldDespawn(MMComponent.getLimiter().getWorld(entity.getWorld()), entity);
	}
	
	/**
	 * Checks the given mob to see if it should be despawned
	 * @param world The world the entity resides in (Given for performance, even if its only slight)
	 * @param entity The entity to be checked
	 * @param findPlayer Whether or not to try find a player (False if you know there are no nearby players)
	 * @return True if the entity should be despawned
	 */
	public static boolean shouldDespawn(MMWorld world, LivingEntity entity, boolean findPlayer)
	{		
		if (world == null || entity == null)
		{
			if (!LimiterConfig.disableWarnings)
			{
				MMComponent.getLimiter().warning("Error when checking whether to despawn a mob");
				NullPointerException e = new NullPointerException();
				
				e.printStackTrace();
			}
			return false;
		}
		
		// Despawn a player? I don't think so..
		if (entity.getType() == EntityType.PLAYER)
			return false;
		
		// Make sure the entity is alive and valid
		if (!entity.isValid())
			return false;

		// Check if the mob has lived long enough
		if (entity.getTicksLived() <= LimiterConfig.minTicksLivedForDespawn)
			return false;
		
		// Check if other plugins will allow the mob to be despawned
		if (!P.p().getPluginIntegration().canDespawn(entity))
			return false;

		// Fetch the entities type
		ExtendedEntityType eType = ExtendedEntityType.valueOf(entity);

		MobType mob = eType.getMobType(entity);
		// If MobManager does not recognize the entity ignore it
		if (mob == null)
			return false;
		
		// Check if the mob is being ignored
		if (LimiterConfig.ignoredMobs.contains(eType))
			return false;
		
		// Check if the mob is an animal
		if (mob == MobType.ANIMAL)
		{
			// If animal protection is off then despawning of animals is disabled
			if (!LimiterConfig.enableAnimalDespawning || MMComponent.getLimiter().animalProtection == null)
				return false;
			
			// Check if the animal is tamed
			if (!LimiterConfig.removeTamedAnimals && entity instanceof Tameable)
			{
				Tameable tameable = (Tameable) entity;
				
				if (tameable.isTamed())
					return false;
			}
			
			// Check if the animal is being protected
			if (MMComponent.getLimiter().animalProtection.checkUUID(entity.getUniqueId()))
				return false;

			// If the chunk has more than 'numAnimalsForFarm' then animals are not despawned
			int animalCount = 0;
			for (Entity e : entity.getLocation().getChunk().getEntities())
			{
				if (MobType.ANIMAL.belongs(e))
					++animalCount;
			}
			if (animalCount >= world.worldConf.numAnimalsForFarm)
				return false;
		}
		// Only despawn villagers if they are over their limits
		else if (mob == MobType.VILLAGER)
		{
			if (world.withinMobLimit(eType, entity))
				return false;
		}
		// Does not despawn the entity if it carries players items
		else if (hasEquipment(entity.getType()))
		{
			EntityEquipment equipment = entity.getEquipment();
			
			// If any of these statements pass then the the mob carries an item dropped from a player
			if (equipment.getItemInHandDropChance() >= 1F
					|| equipment.getBootsDropChance() >= 1F
					|| equipment.getChestplateDropChance() >= 1F
					|| equipment.getHelmetDropChance() >= 1F
					|| equipment.getLeggingsDropChance() >= 1F)
				return false;
		}
		
		// If we are not looking for a player the mob can be despawned
		if (!findPlayer)
			return true;
		
		// Search for a nearby player
		return !PlayerFinder.playerNear(world, entity, PlayerFinder.mobFlys(entity));
	}
	
	public static boolean shouldDespawn(LivingEntity entity)
	{
		return shouldDespawn(entity, true);
	}
	
	public static boolean shouldDespawn(MMWorld world, LivingEntity entity)
	{
		return shouldDespawn(world, entity, true);
	}
	
	private static boolean hasEquipment(EntityType type)
	{
		switch (type)
		{
		case ZOMBIE:
		case PIG_ZOMBIE:
		case SKELETON:
			return true;
		default:
			return false;
		}
	}
}
