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

package ninja.mcknight.bukkit.mobmanager.limiter.world;

import java.util.List;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.P;
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;
import ninja.mcknight.bukkit.mobmanager.limiter.config.LimiterConfig;
import ninja.mcknight.bukkit.mobmanager.limiter.config.WorldConfig;
import ninja.mcknight.bukkit.mobmanager.limiter.util.MobType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;

/**
 * Keeps track of the number of mobs of different types within a world
 * 
 * @author Michael McKnight (ShadowDog007)
 *
 */
public class MMWorld
{	
	private static long ticksPerRecount = LimiterConfig.ticksPerRecount;
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
	 * Stores all mob type counts
	 */
	private int[] mobCounts;
	
	/**
	 * Stores mob counts of individual mobs (Only if they have limits)
	 */
	private int[] individualMobCounts;
	
	public MMWorld(final World world, WorldConfig worldConf)
	{
		this.world = world;
		this.worldConf = worldConf;
		
		mobCounts = new int[worldConf.maximums.length];
		
		List<ExtendedEntityType> types = worldConf.getIndividualMobs();
		
		// Create the array to store individual mob counts
		individualMobCounts = new int[ExtendedEntityType.values().length];
		// Initialise the array counts
		for (int i = 0; i < individualMobCounts.length; ++i)
		{
			individualMobCounts[i] = -1;
		}
		
		// Initialise counts of mobs we want to keep track of
		for (ExtendedEntityType type : types)
		{
			individualMobCounts[type.ordinal()] = 0;
		}
		
		updateMobCounts();

		final int maxMonsters = worldConf.maximums[MobType.MONSTER.ordinal()];
		final int maxAnimals = worldConf.maximums[MobType.ANIMAL.ordinal()];
		final int maxWater = worldConf.maximums[MobType.WATER_ANIMAL.ordinal()];
		final int maxAmbient = worldConf.maximums[MobType.AMBIENT.ordinal()];
		final int maxVillagers = worldConf.maximums[MobType.VILLAGER.ordinal()];

		MMComponent.getLimiter().info(String.format("[%s] Limits M:%d, A:%d, W:%d, Am:%d, V:%d", world.getName(), maxMonsters, maxAnimals, maxWater, maxAmbient, maxVillagers));
	}
	
	public short getSearchDistanceSquared(short y)
	{
		return y <= worldConf.groundHeight ? worldConf.undergroundSearchDistanceSquared : getSearchDistanceSquared();
	}
	
	public short getSearchDistanceSquared()
	{
		return worldConf.despawnSearchDistanceSquared > 0 ? worldConf.despawnSearchDistanceSquared : LimiterConfig.despawnSearchDistanceSquared;
	}
	
	public short getSearchDistance(short y)
	{
		return y <= worldConf.groundHeight ? worldConf.undergroundSearchDistance : getSearchDistance();
	}
	
	public short getSearchDistance()
	{
		return worldConf.despawnSearchDistance > 0 ? worldConf.despawnSearchDistance : LimiterConfig.despawnSearchDistance;
	}
	
	public short getSearchHeight()
	{
		return worldConf.despawnSearchHeight > 0 ? worldConf.despawnSearchHeight : LimiterConfig.despawnSearchHeight;
	}
	
	private void resetMobCounts()
	{
		for (MobType mob : MobType.values())
		{
			mobCounts[mob.ordinal()] = 0;
		}
		
		for (int i = 0; i < individualMobCounts.length; ++i)
		{
			if (individualMobCounts[i] != -1)
				individualMobCounts[i] = 0;
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
		if (Bukkit.isPrimaryThread() && needsUpdate)
		{
			resetMobCounts();
			
			numChunks = world.getLoadedChunks().length;
			
			// Fetches the list of entities if it was not given
			if (entities == null)
				entities = world.getLivingEntities();
			
			// Loop through each loaded chunk in the world
			for (final LivingEntity entity : entities)
			{
				ExtendedEntityType eType = ExtendedEntityType.valueOf(entity);
				
				// Check if the mob should be ignored
				if (LimiterConfig.ignoredMobs.contains(eType))
					continue;
				
				// Add individual mob counts
				if (individualMobCounts != null)
				{
					// Check to see if the mob should be counted
					if (individualMobCounts[eType.ordinal()] != -1)
					{
						++individualMobCounts[eType.ordinal()];
					}
				}

				// Fetch mob type
				MobType mob = eType.getMobType(entity);
				// If the mob type is null ignore the entity
				if (mob == null)
					continue;


				if (mob == MobType.ANIMAL)
				{
					// Make sure tameable animals are not counted if it is set to false
					if (!LimiterConfig.countTamedAnimals && entity instanceof Tameable)
					{
						Tameable tameable = (Tameable) entity;

						if (tameable.isTamed())
							continue;
					}
				}

				// Increment counter
				++mobCounts[mob.ordinal()];
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
		
		return mobCounts[mob.ordinal()];
	}
	
	/**
	 * Calculates the maximum number of one mobtype currently allowed in the world
	 * @return The max number of monsters
	 */
	public short maxMobs(final MobType mob)
	{
		if (mob == null)
			return Short.MAX_VALUE;

		short dynMax = (short) (worldConf.dynMultis[mob.ordinal()] * numChunks >> 8);
			
		return worldConf.maximums[mob.ordinal()] < dynMax ? worldConf.maximums[mob.ordinal()] : dynMax;
	}
	
	/**
	 * Checks to see if the specific mob is within its limits
	 */
	public boolean withinMobLimit(ExtendedEntityType mob, LivingEntity entity)
	{
		if (mob == null)
		{
			mob = ExtendedEntityType.valueOf(entity);
		}
		
		// Check if the mobs parent is within the limits first
		if (mob.hasParent() && !withinMobLimit(mob.getParent(), null))
			return false;
		
		// If the mob is not within its types counts return false
		if (!withinMobLimit(mob.getMobType()))
			return false;
		
		// If the count is -1 we are not counting this mob
		if (individualMobCounts[mob.ordinal()] == -1)
			return true;
		
		// Return true if the count is under the maximum for the mob
		return worldConf.getMaximum(mob, numChunks) > individualMobCounts[mob.ordinal()];
	}

	private boolean withinMobLimit(MobType mob)
	{
		if (mob == null)
			return true;
		
		updateMobCounts();
		
		return maxMobs(mob) > mobCounts[mob.ordinal()];
	}
	
	/**
	 * Increments the mob counts for this mob
	 * @param mob The mob type which is being counted
	 * @param entity The actual mob being counted 
	 */
	public void incrementMobCount(ExtendedEntityType mob, LivingEntity entity)
	{
		if (mob == null)
			return;
		
		if (mob.hasParent())
			incrementMobCount(mob.getParent(), entity);
		
		// Increment the MobTypes count
		incrementMobCount(mob.getMobType(entity));
		
		if (individualMobCounts == null)
			return;
		
		// Increment the mobs count if we should be counting it
		if (individualMobCounts[mob.ordinal()] != -1)
			++individualMobCounts[mob.ordinal()];
	}
	
	/**
	 * Decrements the mob counts for this mob
	 * @param mob The mob type which is being counted
	 * @param entity The actual mob being counted 
	 */
	public void decrementMobCount(ExtendedEntityType mob, LivingEntity entity)
	{
		if (mob == null)
			return;
		
		if (mob.hasParent())
			decrementMobCount(mob.getParent(), entity);
		
		// Decrement the MobTypes count
		decrementMobCount(mob.getMobType(entity));
		
		if (individualMobCounts == null)
			return;
		
		// Decrement the mobs count if we should be counting it
		if (individualMobCounts[mob.ordinal()] != -1)
			--individualMobCounts[mob.ordinal()];
	}

	private void incrementMobCount(MobType mob)
	{
		if (mob == null)
			return;
		
		++mobCounts[mob.ordinal()];
	}
	
	private void decrementMobCount(MobType mob)
	{
		if (mob == null)
			return;
		--mobCounts[mob.ordinal()];
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
