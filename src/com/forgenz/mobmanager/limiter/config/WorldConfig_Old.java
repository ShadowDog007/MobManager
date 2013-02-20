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

package com.forgenz.mobmanager.limiter.config;

import java.io.File;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.limiter.util.MobType;

/**
 * This class copies settings from old configs into new configs and then removes the old world configurations
 * @author Michael McKnight (ShadowDog007)
 *
 */
public class WorldConfig_Old extends WorldConfig
{
	public WorldConfig_Old(World world)
	{
		this(world, getConfig(WORLDS_FOLDER + File.separator, world.getName() + ".yml"));
	}
	
	public WorldConfig_Old(World world, FileConfiguration cfg)
	{
		super(world, cfg);
		
		FileConfiguration newCfg = getConfig(WORLDS_FOLDER + File.separator + world.getName(), LIMITER_CONFIG_NAME);
		
		MobType[] mobs = MobType.values();
		
		/* ################ MobLimits ################ */
		for (MobType mob : mobs)
		{			
			set(newCfg, "WorldMaximum." + mob.cPath, maximums[mob.index]);
			set(newCfg, "ChunkCalculatedMaximum." + mob.cPath, dynMultis[mob.index]);
		}
		
		/* ################ BreedingMaximumPerChunk ################ */
		set(newCfg, "BreedingMaximumPerChunk", breedingLimit);
		
		/* ################ NumAnimalsForFarm ################ */
		set(newCfg, "NumAnimalsForFarm", numAnimalsForFarm);
		
		/* ################ SpawnChunkSearchDistance ################ */
		set(newCfg, "SpawnChunkSearchDistance", spawnChunkSearchDistance);
		
		/* ################ UndergroundSpawnChunkSearchDistance ################ */
		set(newCfg, "UndergroundSpawnChunkSearchDistance", undergroundSpawnChunkSearchDistance);
		
		
		/* ################ GroundHeight ################ */
		set(newCfg, "GroundHeight", groundHeight);
		
		
		copyHeader(newCfg, "Limiter_WorldConfigHeader.txt", P.p.getDescription().getName() + " Limiter World Config " + P.p.getDescription().getVersion() + "\n");
		saveConfig(WORLDS_FOLDER + File.separator + world.getName(), LIMITER_CONFIG_NAME, newCfg);
		
		File oldConfigFile = new File(P.p.getDataFolder(), WORLDS_FOLDER + File.separator + world.getName() + ".yml");
		
		try
		{
			oldConfigFile.delete();
		}
		catch (SecurityException e)
		{
			P.p.getLogger().warning("Failed to delete old world config file:" + e.getMessage());
		}
	}
}
