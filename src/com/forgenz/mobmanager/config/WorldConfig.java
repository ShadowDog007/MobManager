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

package com.forgenz.mobmanager.config;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import com.forgenz.mobmanager.MobType;
import com.forgenz.mobmanager.P;

public class WorldConfig extends AbstractConfig
{
	final static String worldsFolder = "worlds";
	
	public final FileConfiguration cfg;
	public final String worldName;
	
	public final boolean limiterEnabled;
	public final short[] maximums;
	public final short[] dynMultis;
	public final short breedingLimit;
	public final short numAnimalsForFarm;
	public final short spawnChunkSearchDistance;
	public final int undergroundSpawnChunkSearchDistance;
	public final int groundHeight;
	
	public final HashMap<EntityType, MobAttributes> mobAttributes;
	
	public WorldConfig(World world)
	{
		cfg = getConfig(worldsFolder, world.getName() + ".yml");
		this.worldName = world.getName();
		
		limiterEnabled = true;
		
		MobType[] mobs = MobType.values();
		
		maximums = new short[mobs.length];
		dynMultis = new short[mobs.length];
		
		/* ################ MobLimits ################ */
		for (MobType mob : mobs)
		{
			maximums[mob.index] = (short) Math.abs(cfg.getInt("WorldMaximum." + mob.cPath, mob.getDefaultMax(world.getEnvironment())));
			dynMultis[mob.index] = (short) Math.abs(cfg.getInt("ChunkCalculatedMaximum." + mob.cPath, mob.getDefaultDynMulti(world.getEnvironment())));
			
			cfg.set("WorldMaximum." + mob.cPath, maximums[mob.index]);
			cfg.set("ChunkCalculatedMaximum." + mob.cPath, dynMultis[mob.index]);
		}
		
		/* ################ BreedingMaximumPerChunk ################ */
		breedingLimit = (short) cfg.getInt("BreedingMaximumPerChunk", 15);
		cfg.set("BreedingMaximumPerChunk", breedingLimit);
		
		/* ################ NumAnimalsForFarm ################ */
		numAnimalsForFarm = (short) cfg.getInt("NumAnimalsForFarm", 3);
		cfg.set("NumAnimalsForFarm", numAnimalsForFarm);
		
		/* ################ SpawnChunkSearchDistance ################ */
		spawnChunkSearchDistance = (short) cfg.getInt("SpawnChunkSearchDistance", -1);
		cfg.set("SpawnChunkSearchDistance", spawnChunkSearchDistance);
		
		/* ################ UndergroundSpawnChunkSearchDistance ################ */
		undergroundSpawnChunkSearchDistance = cfg.getInt("UndergroundSpawnChunkSearchDistance", 2);
		cfg.set("UndergroundSpawnChunkSearchDistance", undergroundSpawnChunkSearchDistance);
		
		
		/* ################ GroundHeight ################ */
		int defaultHeight = world.getEnvironment() == Environment.NORMAL ? 55 : (world.getEnvironment() == Environment.NETHER ? 32 : -1);
		groundHeight = cfg.getInt("GroundHeight", defaultHeight);
		cfg.set("GroundHeight", groundHeight);
		
		/* ######## World Mob Attributes ######## */
		mobAttributes = new HashMap<EntityType, MobAttributes>();
		ConfigurationSection mobsCfg = cfg.getConfigurationSection("MobAttributes");
		
		if (mobsCfg == null)
			mobsCfg = cfg.createSection("MobAttributes");
		
		Set<String> keys = mobsCfg.getKeys(false);
		
		for (String key : keys)
		{
			EntityType mob = null; 
			try
			{
				mob = EntityType.valueOf(key.toUpperCase());
			}
			catch (IllegalArgumentException e)
			{
				P.p.getLogger().warning("The mob " + key + " is invalid for the MobAttributes");
				continue;
			}
			if (mob == null || !LivingEntity.class.isAssignableFrom(mob.getEntityClass()))
			{
				P.p.getLogger().warning("The mob " + key + " is invalid for the MobAttributes");
				continue;
			}
			
			ConfigurationSection mobCfg = mobsCfg.getConfigurationSection(key);
			if (mobCfg == null)
			{
				P.p.getLogger().warning("Error loading MobAttributes for " + key);
				continue;
			}
			mobAttributes.put(mob, new MobAttributes(mob, mobCfg));
		}
		
		
		copyHeader(cfg, "worldConfigHeader.txt", P.p.getDescription().getName() + " Config " + P.p.getDescription().getVersion() + "\n");
		saveConfig(worldsFolder, worldName + ".yml", cfg);
	}
}