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

package com.forgenz.mobmanager;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.scheduler.BukkitRunnable;

import com.forgenz.mobmanager.listeners.MobListener;
import com.forgenz.mobmanager.world.MMChunk;
import com.forgenz.mobmanager.world.MMCoord;
import com.forgenz.mobmanager.world.MMWorld;


public class MobDespawnTask extends BukkitRunnable
{
	@Override
	public void run()
	{
		if (P.worlds == null)
		{
			cancel();
			return;
		}
		
		for (MMWorld world : P.worlds.values())
		{
			world.updateMobCounts();
			
			for (LivingEntity entity : world.getWorld().getLivingEntities())
			{
				// Make sure the entity is alive and valid
				if (!entity.isValid())
					continue;
				
				// Check if the mob has lived long enough
				if (entity.getTicksLived() <= Config.minTicksLivedForDespawn)
					continue;
				
				// Check if the mob is being ignored
				if (Config.ignoredMobs.containsValue(entity.getType().toString()))
					continue;
				
				MobType mob = MobType.valueOf(entity);
				// If MobManager does not recognize the entity ignore it
				if (mob == null)
					continue;
				
				// Check if the mob is an animal
				if (mob == MobType.ANIMAL)
				{
					// If the chunk has more than 'numAnimalsForFarm' then animals are not despawned
					MMChunk chunk = world.getChunk(entity.getLocation().getChunk());
					if (chunk.getAnimalCount() >= world.worldConf.numAnimalsForFarm)
					{
						continue;
					}
				}
				// Only despawn villagers if they are over their limits
				else if (mob == MobType.VILLAGER)
				{
					if (world.withinMobLimit(mob))
						continue;
				}
				// Does not despawn the entity if it carries players items
				else if (entity instanceof Zombie || entity instanceof Skeleton)
				{
					EntityEquipment equipment = entity.getEquipment();
					
					// If any of these statements pass then the the mob carries an item dropped from a player
					if (equipment.getBootsDropChance() == 1F)
						continue;
					if (equipment.getChestplateDropChance() == 1F)
						continue;
					if (equipment.getHelmetDropChance() == 1F)
						continue;
					if (equipment.getItemInHandDropChance() == 1F)
						continue;
					if (equipment.getLeggingsDropChance() == 1F)
						continue;
				}
				
				// Search for a nearby player
				if (!MobListener.i.playerNear(world, new MMCoord(entity.getLocation().getChunk()), entity.getLocation().getBlockY(), MobListener.i.mobFlys(entity)))
				{
					entity.remove();
					world.decrementMobCount(MobType.valueOf(entity));
				}
			}
		}
	}
}
