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

package com.forgenz.mobmanager.limiter.world;

import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;
import com.forgenz.mobmanager.limiter.config.Config;
import com.forgenz.mobmanager.limiter.config.WorldConfig;
import com.forgenz.mobmanager.limiter.util.MobType;

/**
 * Keeps track of the number of mobs of different types within a world
 * 
 * @author Michael McKnight (ShadowDog007)
 *
 */
public class MMWorld
{	
	private static long ticksPerRecount = Config.ticksPerRecount;
	/**
	 * Bukkit world this object as affiliated with
	 */
	private final World world;
	
	/**
	 * World settings
	 */
	public final WorldConfig worldConf;
	
	/**
	 * Count of loaded chunks in the world
	 */
	private int numChunks = 0;

	/**
	 * Used to check if we should update mob counts
	 */
	private boolean needsUpdate = true;
	
	
	/**
	 * Stores all mob counts
	 */
	private int[] mobCounts;
	
	
	public MMWorld(final World world, WorldConfig worldConf)
	{
		this.world = world;
		this.worldConf = worldConf;
		
		mobCounts = new int[worldConf.maximums.length];
		
		updateMobCounts();

		final int maxMonsters = worldConf.maximums[MobType.MONSTER.index];
		final int maxAnimals = worldConf.maximums[MobType.ANIMAL.index];
		final int maxWater = worldConf.maximums[MobType.WATER_ANIMAL.index];
		final int maxAmbient = worldConf.maximums[MobType.AMBIENT.index];
		final int maxVillagers = worldConf.maximums[MobType.VILLAGER.index];

		P.p().getLogger().info(String.format("[%s] Limits M:%d, A:%d, W:%d, Am:%d, V:%d", world.getName(), maxMonsters, maxAnimals, maxWater, maxAmbient, maxVillagers));
	}
	
	public short getSearchDistance(short y)
	{
		return y <= worldConf.groundHeight ? worldConf.undergroundSearchDistance : getSearchDistance();
	}
	
	public short getSearchDistance()
	{
		return worldConf.despawnSearchDistance > 0 ? worldConf.despawnSearchDistance : Config.despawnSearchDistance;
	}
	
	public short getSearchHeight()
	{
		return worldConf.despawnSearchHeight > 0 ? worldConf.despawnSearchHeight : Config.despawnSearchHeight;
	}
	
	private void resetMobCounts()
	{
		for (MobType mob : MobType.values())
		{
			mobCounts[mob.index] = 0;
		}
	}
	
	public boolean updateMobCounts()
	{
		return updateMobCounts(null);
	}
	
	/**
	 * Used in the despawn task so an entity list does not need to be generated twice
	 * @param entities - List of entities in the world
	 */
	public boolean updateMobCounts(List<LivingEntity> entities)
	{
		if (needsUpdate)
		{
			resetMobCounts();
			
			numChunks = world.getLoadedChunks().length;
			
			// Fetches the list of entities if it was not given
			if (entities == null)
				entities = world.getLivingEntities();
			
			// Loop through each loaded chunk in the world
			for (final LivingEntity entity : entities)
			{
				// Check if the mob should be ignored
				if (Config.ignoredMobs.contains(ExtendedEntityType.get(entity)))
					continue;

				// Fetch mob type
				MobType mob = MobType.valueOf(entity);
				// If the mob type is null ignore the entity
				if (mob == null)
					continue;


				if (mob == MobType.ANIMAL)
				{
					// Make sure tameable animals are not counted if it is set to false
					if (!Config.countTamedAnimals && entity instanceof Tameable)
					{
						Tameable tameable = (Tameable) entity;

						if (tameable.isTamed())
							continue;
					}
				}

				// Increment counter
				++mobCounts[mob.index];
			}
			
			// Reset 'updatedThisTick' so updates can be run again later
			P.p().getServer().getScheduler().runTaskLater(P.p(),
				new Runnable()
				{
					public void run()
					{
						needsUpdate = true;
					}
				}
				, ticksPerRecount);
			
			needsUpdate = false;
			return true;
		}
		return false;
	}

	public World getWorld()
	{
		return world;
	}
	
	public int getNumChunks()
	{
		return numChunks;
	}

	public int getMobCount(MobType mob)
	{
		if (mob == null)
			return 0;
		
		updateMobCounts();
		
		return mobCounts[mob.index];
	}
	
	/**
	 * Calculates the maximum number of monsters currently allowed in the world
	 * @return The max number of monsters
	 */
	public short maxMobs(final MobType mob)
	{
		if (mob == null)
			return Short.MAX_VALUE;

		short dynMax = (short) (worldConf.dynMultis[mob.index] * numChunks >> 8);
			
		return worldConf.maximums[mob.index] < dynMax ? worldConf.maximums[mob.index] : dynMax;
	}

	public boolean withinMobLimit(MobType mob)
	{
		if (mob == null)
			return true;
		
		updateMobCounts();
		
		return maxMobs(mob) > mobCounts[mob.index];
	}

	public void incrementMobCount(MobType mob)
	{
		if (mob == null)
			return;
		++mobCounts[mob.index];
	}
	
	public void decrementMobCount(MobType mob)
	{
		if (mob == null)
			return;
		--mobCounts[mob.index];
	}

	public void incrementChunkCount()
	{
		++numChunks;
	}

	public void decrementChunkCount()
	{
		--numChunks;
	}
}
