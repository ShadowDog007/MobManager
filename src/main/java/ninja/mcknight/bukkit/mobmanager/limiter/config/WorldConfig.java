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

package ninja.mcknight.bukkit.mobmanager.limiter.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ninja.mcknight.bukkit.mobmanager.limiter.util.MobType;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;

import ninja.mcknight.bukkit.mobmanager.common.config.AbstractConfig;
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;

public class WorldConfig extends AbstractConfig
{
	public final String worldName;
	
	public final boolean limiterEnabled;
	
	public final short[] maximums;
	public final short[] dynMultis;
	
	private final short[] mobMaximums;
	private final short[] dynMobMultis;
	
	public final short breedingLimit;
	public final short numAnimalsForFarm;
	
	public final short despawnSearchDistance, despawnSearchDistanceSquared;
	public final short undergroundSearchDistance, undergroundSearchDistanceSquared;
	public final short despawnSearchHeight;
	
	public final int groundHeight;
	
	public WorldConfig(World world)
	{
		this(world, getConfig(WORLDS_FOLDER + File.separator + world.getName(), LIMITER_CONFIG_NAME));
	}
	public WorldConfig(World world, FileConfiguration cfg)
	{
		this.worldName = world.getName();
		
		limiterEnabled = true;
		
		MobType[] mobs = MobType.values();
		
		maximums = new short[mobs.length];
		dynMultis = new short[mobs.length];
		
		/* ################ MobTypeLimits ################ */
		for (MobType mob : mobs)
		{
			maximums[mob.ordinal()] = (short) Math.abs(cfg.getInt("WorldMaximum." + mob.cPath, mob.getDefaultMax(world.getEnvironment())));
			dynMultis[mob.ordinal()] = (short) Math.abs(cfg.getInt("ChunkCalculatedMaximum." + mob.cPath, mob.getDefaultDynMulti(world.getEnvironment())));
			
			// Limit dynamic multi so to prevent ending up with -ve limits
			if (dynMultis[mob.ordinal()] > 1000)
				dynMultis[mob.ordinal()] = 1000;
			
			set(cfg, "WorldMaximum." + mob.cPath, maximums[mob.ordinal()]);
			set(cfg, "ChunkCalculatedMaximum." + mob.cPath, dynMultis[mob.ordinal()]);
		}
		
		set(cfg, "WorldMaximum", cfg.getConfigurationSection("WorldMaximum"));
		set(cfg, "ChunkCalculatedMaximum", cfg.getConfigurationSection("ChunkCalculatedMaximum"));
		
		/* ################ BreedingMaximumPerChunk ################ */
		breedingLimit = (short) cfg.getInt("BreedingMaximumPerChunk", 15);
		set(cfg, "BreedingMaximumPerChunk", breedingLimit);
		
		/* ################ NumAnimalsForFarm ################ */
		numAnimalsForFarm = (short) cfg.getInt("NumAnimalsForFarm", 3);
		set(cfg, "NumAnimalsForFarm", numAnimalsForFarm);
		
		/* ################ DespawnSearchDistance ################ */
		short despawnSearchDistance = (short) cfg.getInt("DespawnSearchDistance", -1);
		this.despawnSearchDistance = despawnSearchDistance <= 0 ? -1 : despawnSearchDistance;
		this.despawnSearchDistanceSquared = despawnSearchDistance <= 0 ? -1 : (short) (despawnSearchDistance * despawnSearchDistance);
		set(cfg, "DespawnSearchDistance", despawnSearchDistance);
		

		/* ################ UndergroundSearchDistance ################ */
		this.undergroundSearchDistance = (short) cfg.getInt("UndergroundSearchDistance", 32);
		this.undergroundSearchDistanceSquared = (short) (undergroundSearchDistance * undergroundSearchDistance);
		set(cfg, "UndergroundSearchDistance", undergroundSearchDistance);
		
		/* ################ DespawnSearchHeight ################ */
		short despawnSearchHeight = (short) cfg.getInt("DespawnSearchHeight", -1);
		this.despawnSearchHeight = despawnSearchHeight <= 0 ? -1 : despawnSearchHeight;
		set(cfg, "DespawnSearchHeight", despawnSearchHeight);
		
		/* ################ GroundHeight ################ */
		int defaultHeight = world.getEnvironment() == Environment.NORMAL ? 55 : (world.getEnvironment() == Environment.NETHER ? 32 : -1);
		groundHeight = cfg.getInt("GroundHeight", defaultHeight);
		set(cfg, "GroundHeight", groundHeight);
		
		/* ################ MobsLimits ################ */
		int size = ExtendedEntityType.values().length;
		mobMaximums = new short[size];
		dynMobMultis = new short[size];
		
		for (int i = 0; i < size; ++i)
		{
			mobMaximums[i] = dynMobMultis[i] = Short.MAX_VALUE;
		}
		
		for (ExtendedEntityType type : ExtendedEntityType.values())
		{
			if (type.getMobType() == null)
			{
				continue;
			}
			
			String wm = String.format("Mobs.WorldMaximum.%s", type.getTypeData());
			String dm = String.format("Mobs.ChunkCalculatedMaximum.%s", type.getTypeData());
			
			short max = (short) cfg.getInt(wm, -1);
			short dynMulti = (short) cfg.getInt(dm, -1);
			
			if (max < -1)
				max = -1;
			if (dynMulti < -1)
				dynMulti = -1;
			
			if (max != -1)
			{
				mobMaximums[type.ordinal()] =  max;
			}
			if (dynMulti != -1)
			{
				dynMobMultis[type.ordinal()] = dynMulti;
			}
			
			set(cfg, wm, max);
			set(cfg, dm, dynMulti);
		}
		
		set(cfg, "Mobs.WorldMaximum", cfg.getConfigurationSection("Mobs.WorldMaximum"));
		set(cfg, "Mobs.ChunkCalculatedMaximum", cfg.getConfigurationSection("Mobs.ChunkCalculatedMaximum"));
		set(cfg, "Mobs", cfg.getConfigurationSection("Mobs"));
		
		
		// Remove old Settings
		cfg.set("SpawnChunkSearchDistance", null);
		cfg.set("UndergroundSpawnChunkSearchDistance", null);
		
		copyHeader(cfg, "Limiter_WorldConfigHeader.txt", "Limiter World Config\n");
		saveConfig(WORLDS_FOLDER + File.separator + world.getName(), LIMITER_CONFIG_NAME, cfg);
	}
	
	public int getMaximum(ExtendedEntityType type, int chunks)
	{
		short max = mobMaximums[type.ordinal()];
		int dynCount = dynMobMultis[type.ordinal()];
		
		// If the dynMulti more than max we just return max
		if (dynCount >= max)
			return max;
		
		dynCount = (dynCount * chunks) >> 8;
		
		return dynCount < max ? dynCount : max;
	}
	
	public List<ExtendedEntityType> getIndividualMobs()
	{
		List<ExtendedEntityType> types = new ArrayList<ExtendedEntityType>();
		
		if (mobMaximums != null)
		{
			for (int i = 0; i < mobMaximums.length; ++i)
			{
				if (mobMaximums[i] != Short.MAX_VALUE)
				{
					types.add(ExtendedEntityType.valueOf(i));
				}
			}
		}
		
		if (dynMobMultis != null)
		{
			for (int i = 0; i < dynMobMultis.length; ++i)
			{
				if (dynMobMultis[i] == Short.MAX_VALUE)
					continue;
				ExtendedEntityType type = ExtendedEntityType.valueOf(i);
				
				// Make sure we haven't already added the entity type
				if (!types.contains(type))
					types.add(type);
			}
		}
		
		return types;
	}
}