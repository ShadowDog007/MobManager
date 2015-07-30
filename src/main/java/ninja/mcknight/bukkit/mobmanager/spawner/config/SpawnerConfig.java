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

package ninja.mcknight.bukkit.mobmanager.spawner.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import ninja.mcknight.bukkit.mobmanager.common.config.AbstractConfig;
import ninja.mcknight.bukkit.mobmanager.common.util.RandomUtil;
import ninja.mcknight.bukkit.mobmanager.common.util.ThreadCache;
import ninja.mcknight.bukkit.mobmanager.spawner.SpawnerComponent;
import ninja.mcknight.bukkit.mobmanager.spawner.config.Region.RegionType;

public class SpawnerConfig extends AbstractConfig
{	
	private static SpawnerConfig spawnerCfg;
	public static SpawnerConfig i()
	{
		return spawnerCfg;
	}
	
	private final HashMap<String, SpawnerWorldConfig> worldConfigs = new HashMap<String, SpawnerWorldConfig>();
	private final Region globalRegion;
	
	private final ThreadCache<ArrayList<?>> regionListCache = new ThreadCache<ArrayList<?>>();
	
	public final boolean removePlayersMobOnDisconnect;
	public final boolean ignoreCreativePlayers;
	public final int spawnFinderThreads;
	public final int ticksPerSpawn;
	public final int spawnGenerationAttempts;
	public final int mobDistanceForLimitRemoval;
	
	public SpawnerConfig()
	{
		spawnerCfg = this;
		FileConfiguration cfg = getConfig("", SpawnerComponent.SPAWNER_CONFIG_NAME, SpawnerComponent.SPAWNER_CONFIG_NAME);
		
		super.setCfg(cfg);
		
		spawnFinderThreads = getAndSet("SpawnFinderThreads", 1);
		ticksPerSpawn = getAndSet("TicksPerSpawn", 100);
		
		spawnGenerationAttempts = getAndSet("SpawnGenerateAttempts", 3);
		mobDistanceForLimitRemoval = (int) Math.pow(getAndSet("MobDistanceForLimitRemoval", 64), 2);
		
		removePlayersMobOnDisconnect = getAndSet("RemovePlayerMobsOnDisconnect", true);
		ignoreCreativePlayers = getAndSet("IgnoreCreativePlayers", true);
		
		ConfigurationSection regionsCfg = getConfigurationSection("Regions");
		globalRegion = RegionType.GLOBAL.createRegion(getConfigurationSection(regionsCfg, "GlobalRegion"));
		
		for (World world : Bukkit.getWorlds())
		{
			worldConfigs.put(world.getName(), new SpawnerWorldConfig(world));
		}
		
		super.clearCfg();
		
		copyHeader("Spawner Global Config\n" + getResourceAsString("Spawner_ConfigHeader.txt") + getResourceAsString("Spawner_WorldConfigHeader.txt"), cfg);
		saveConfig("", SpawnerComponent.SPAWNER_CONFIG_NAME, cfg);
	}
	
	@SuppressWarnings("unchecked")
	protected <T> ArrayList<T> getCachedList()
	{
		ArrayList<?> list = regionListCache.get();
		if (list == null)
		{
			list = new ArrayList<Object>();
			regionListCache.set(list);
		}
		else
			list.clear();
		
		return (ArrayList<T>) list;
	}
	
	/**
	 * Fetches the world config for the given world
	 * 
	 * @param world The world we want a config for
	 * 
	 * @return A world config
	 */
	public SpawnerWorldConfig getWorldConfig(World world)
	{
		return worldConfigs.get(world.getName());
	}
	
	/**
	 * Fetches a list of regions which exist at the location
	 * 
	 * @param location The given location
	 * 
	 * @return List of regions which the location is within
	 */
	public List<Region> getSpawnableRegions(Location location)
	{
		// Fetch the worlds configuration
		SpawnerWorldConfig cfg = getWorldConfig(location.getWorld());
		
		// If we can't spawn mobs in this world we return an empty list 
		if (cfg != null && !cfg.spawnMobs)
			return Collections.emptyList();
		
		// Create a list to store regions in
		List<Region> regionList = new ArrayList<Region>();
		
		// Check if there was a world config
		if (cfg == null)
		{
			// Add the global region and return
			regionList.add(globalRegion);
			return regionList;
		}
		
		// Fetch all the world regions
		cfg.getRegions(location, regionList);
		
		// Add the global region if possible
		if (regionList.isEmpty() || globalRegion.priority > 0)
			regionList.add(globalRegion);
		
		return regionList;
	}
	
	/**
	 * Fetches a single region which contains the given region
	 * 
	 * @param location The location at which to search for regions
	 * 
	 * @return A single region, GlobalRegion if no others exist
	 */
	public Region getRegion(Location location)
	{
		// Fetch the worlds configuration
		SpawnerWorldConfig cfg = getWorldConfig(location.getWorld());
		
		// If the config doesn't exist just use the global region
		if (cfg == null)
			return globalRegion;
		
		// If mobs can't be spawned here don't do anything.
		if (!cfg.spawnMobs)
			return null;
		
		// Fetch a cached list for speed? (TODO: Test performance difference)
		ArrayList<Region> regionList = getCachedList();
		
		// Fetch all regions which match the location
		cfg.getRegions(location, regionList);
		
		// If the global region has a positive priority we need to add it to the list
		if (globalRegion.priority > 0)
			regionList.add(globalRegion);
		
		// Select a random region region
		Region region = pickRegion(regionList);
		// Clear the regions, we don't need them anymore :)
		regionList.clear();
		return region;
	}
	
	/**
	 * Given a list of regions this picks a random one
	 * based on each regions priority
	 * 
	 * @param regionList List of regions to pick from
	 * @return A single region
	 */
	private Region pickRegion(ArrayList<Region> regionList)
	{
		// Fetch a random value between 0 and the total priority
		int chance = getTotalRegionPriority(regionList);
		
		if (chance == 0)
			return globalRegion;
		
		int val = RandomUtil.i.nextInt(getTotalRegionPriority(regionList));
		
		for (Region region : regionList)
		{
			// Remove the current regions priority until val is less than 0
			val -= region.priority;
			// Once val is less than 0 we have found our region
			if (val < 0)
				return region;
		}
		
		// Should NEVER reach this point. Return global region just in case.
		return globalRegion;
	}
	
	/**
	 * Calculates the sum of all the given regions priorities
	 * 
	 * @param regionList List of regions
	 * @return Total priority
	 */
	private int getTotalRegionPriority(ArrayList<Region> regionList)
	{
		int pri = 0;
		// Add the priorities of each region
		for (Region region : regionList)
			pri += region.priority;
		// Return the total
		return pri;
	}
}
