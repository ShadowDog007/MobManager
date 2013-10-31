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

package com.forgenz.mobmanager.spawner.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.forgenz.mobmanager.MMComponent;
import com.forgenz.mobmanager.common.config.AbstractConfig;
import com.forgenz.mobmanager.spawner.SpawnerComponent;
import com.forgenz.mobmanager.spawner.config.Region.RegionType;

public class SpawnerWorldConfig extends AbstractConfig
{
	public final boolean spawnMobs;
	
	private final ArrayList<Region> regions;
	
	protected SpawnerWorldConfig(World world)
	{
		FileConfiguration cfg = getConfig(WORLDS_FOLDER + File.separatorChar + world.getName(), SpawnerComponent.SPAWNER_CONFIG_NAME);
		
		super.setCfg(cfg);
		
		spawnMobs = getAndSet("SpawnMobs", true);
		
		if (spawnMobs)
		{
			if (world.getTicksPerMonsterSpawns() > 0)
				MMComponent.getSpawner().warning("The world %s should have vanilla monster spawning disabled via bukkit.yml, or 'SpawnMobs' should be disabled for this world.", world.getName());
			if (world.getTicksPerAnimalSpawns() > 0)
				MMComponent.getSpawner().warning("The world %s should have vanilla animal spawning disabled via bukkit.yml, or 'SpawnMobs' should be disabled for this world.", world.getName());
		}
		
		ConfigurationSection regionsCfg = getConfigurationSection("Regions");
		regions = new ArrayList<Region>();
		
		for (RegionType type : RegionType.values())
		{
			if (type == RegionType.GLOBAL)
				continue;
			
			ConfigurationSection regionCfg = getConfigurationSection(regionsCfg, type.toString());
			
			for (String regionName : regionCfg.getKeys(false))
			{
				try
				{
					Region region = type.createRegion(getConfigurationSection(regionCfg, regionName));
					regions.add(region);
				}
				catch (Exception e)
				{
					MMComponent.getSpawner().warning("Error occured when creating a region. RegionType=%s, RegionName=%s", type, regionName);
				}
			}
		}		
		
		super.clearCfg();
		
		copyHeader(cfg, "Spawner_WorldConfigHeader.txt", "Spawner World Config\n");
		saveConfig(WORLDS_FOLDER + File.separatorChar + world.getName(), SpawnerComponent.SPAWNER_CONFIG_NAME, cfg);
	}
	
	/**
	 * Fetches all regions which this location is within and adds them to the given list
	 * 
	 * @param location The given location
	 * @param regionList List to add regions to
	 */
	public void getRegions(Location location, List<Region> regionList)
	{
		// Iterate through every region
		for (Region region : regions)
		{
			// Check if the location is within the given region
			if (region.withinRegion(location))
				// Add the region to the list
				regionList.add(region);
		}
	}
}
