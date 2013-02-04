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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.world.MMWorld;

public class Config extends AbstractConfig
{
	public final static Random rand = new Random();
	
	public final static Pattern layerPattern = Pattern.compile("^\\d+:{1}\\d+$");
	public final static Pattern layerSplitPattern = Pattern.compile(":{1}");
	
	public static boolean disableWarnings;
	public static boolean ignoreCreativePlayers;
	public static boolean useAsyncDespawnScanner;
	
	public static boolean removeTamedAnimals;
	public static boolean countTamedAnimals;
	
	public static boolean enableAnimalDespawning;
	public static double daysTillFarmAnimalCleanup;
	public static int protectedFarmAnimalSaveInterval;
	
	public static short spawnChunkSearchDistance;
	public static short flyingMobAditionalLayerDepth;
	public static int ticksPerRecount;
	public static int ticksPerDespawnScan;
	public static int minTicksLivedForDespawn;
	
	public static EnumSettingContainer ignoredMobs;
	public static EnumSettingContainer disabledMobs;
	
	public static EnumSettingContainer enabledSpawnReasons;
	
	public static List<String> layers;
	public static HashSet<Integer> layerBoundaries;
	
	public static HashMap<EntityType, MobAttributes> mobAttributes;
	
	public static HashMap<String, WorldConfig> worldConfigs;
	
	public Config()
	{		
		/* ################ ActiveWorlds ################ */
		List<String> activeWorlds = P.cfg.getStringList("EnabledWorlds");
		
		if (activeWorlds == null || activeWorlds.size() == 0)
		{
			activeWorlds = new ArrayList<String>();
			for (World world : P.p.getServer().getWorlds())
			{
				activeWorlds.add(world.getName());
			}
		}
		P.cfg.set("EnabledWorlds", activeWorlds);
		
		/* ################ DisableWarnings ################ */
		disableWarnings = P.cfg.getBoolean("DisableWarnings", true);
		P.cfg.set("DisableWarnings", disableWarnings);
		
		/* ################ UseAsyncDespawnScanner ################ */
		useAsyncDespawnScanner = P.cfg.getBoolean("UseAsyncDespawnScanner", false);
		P.cfg.set("UseAsyncDespawnScanner", useAsyncDespawnScanner);
		
		/* ################ IgnoreCreativePlayers ################ */
		ignoreCreativePlayers = P.cfg.getBoolean("IgnoreCreativePlayers", false);
		P.cfg.set("IgnoreCreativePlayers", ignoreCreativePlayers);
		
		/* ################ TamedAnimals ################ */
		removeTamedAnimals = P.cfg.getBoolean("RemoveTamedAnimals", false);
		P.cfg.set("RemoveTamedAnimals", removeTamedAnimals);
		
		countTamedAnimals = P.cfg.getBoolean("CountTamedAnimals", true);
		P.cfg.set("CountTamedAnimals", countTamedAnimals);
		
		/* ################ Animal Despawning Stuff ################ */
		enableAnimalDespawning = P.cfg.getBoolean("EnableAnimalDespawning", true);
		P.cfg.set("EnableAnimalDespawning", enableAnimalDespawning);
		
		daysTillFarmAnimalCleanup = P.cfg.getDouble("DaysTillFarmAnimalCleanup", 15.0D);
		P.cfg.set("DaysTillFarmAnimalCleanup", daysTillFarmAnimalCleanup);
		
		protectedFarmAnimalSaveInterval = P.cfg.getInt("ProtectedFarmAnimalSaveInterval", 6000);
		P.cfg.set("ProtectedFarmAnimalSaveInterval", protectedFarmAnimalSaveInterval);
		
		/* ################ SpawnChunkSearchDistance ################ */
		spawnChunkSearchDistance = (short) Math.abs(P.cfg.getInt("SpawnChunkSearchDistance", 5));
		// Validate SpawnChunkSearchDistance
		if (spawnChunkSearchDistance == 0)
			spawnChunkSearchDistance = 1;
		P.cfg.set("SpawnChunkSearchDistance", spawnChunkSearchDistance);
		
		/* ################ FlyingMobAditionalLayerDepth ################ */
		flyingMobAditionalLayerDepth = (short) P.cfg.getInt("FlyingMobAditionalLayerDepth", 2);
		P.cfg.set("FlyingMobAditionalLayerDepth", flyingMobAditionalLayerDepth);
		
		/* ################ TicksPerRecount ################ */
		ticksPerRecount = P.cfg.getInt("TicksPerRecount", 40);
		P.cfg.set("TicksPerRecount", ticksPerRecount);
		
		/* ################ TicksPerDespawnScan ################ */
		ticksPerDespawnScan = P.cfg.getInt("TicksPerDespawnScan", 100);
		P.cfg.set("TicksPerDespawnScan", ticksPerDespawnScan);
		
		/* ################ MinTicksLivedForDespawn ################ */
		minTicksLivedForDespawn = P.cfg.getInt("MinTicksLivedForDespawn", 100);
		P.cfg.set("MinTicksLivedForDespawn", minTicksLivedForDespawn);
		
		/* ################ IgnoredMobs ################ */
		ignoredMobs = new EnumSettingContainer(EntityType.class, P.cfg.getList("IgnoredMobs", null), "The Ignored Mob '%s' is invalid");
		ignoredMobs.addDefaults(EntityType.WITHER.toString(), EntityType.VILLAGER.toString());
		List<String> ignoredList = ignoredMobs.getList();
		P.cfg.set("IgnoredMobs", ignoredList);
		String strList = ignoredMobs.toString();
		if (strList.length() != 0)
			P.p.getLogger().info("IgnoredMobs: " + strList);
		
		
		/* ################ DisabledMobs ################ */
		disabledMobs = new EnumSettingContainer(EntityType.class, P.cfg.getList("DisabledMobs", null), "The Disabled Mob '%s' is invalid");
		List<String> disabledList = disabledMobs.getList();
		P.cfg.set("DisabledMobs", disabledList);
		strList = disabledMobs.toString();
		if (strList.length() != 0)
			P.p.getLogger().info("DisabledMobs: " + strList);
		
		/* ################ EnabledSpawnReasons ################ */
		enabledSpawnReasons = new EnumSettingContainer(SpawnReason.class, P.cfg.getList("EnabledSpawnReasons", null), "The Spawn Reason '%s' is invalid");
		enabledSpawnReasons.addDefaults(SpawnReason.DEFAULT.toString(),
				SpawnReason.NATURAL.toString(),
				SpawnReason.SPAWNER.toString(),
				SpawnReason.CHUNK_GEN.toString(),
				SpawnReason.VILLAGE_DEFENSE.toString(),
				SpawnReason.VILLAGE_INVASION.toString(),
				SpawnReason.BUILD_IRONGOLEM.toString(),
				SpawnReason.BUILD_SNOWMAN.toString(),
				SpawnReason.BREEDING.toString(),
				SpawnReason.EGG.toString());
		List<String> srList = enabledSpawnReasons.getList();
		P.cfg.set("EnabledSpawnReasons", srList);
		strList = enabledSpawnReasons.toString();
		if (strList.length() != 0)
			P.p.getLogger().info("EnabledSpawnReasons: " + strList);
		
		
		/* ################ Layers ################ */
		layers = new ArrayList<String>();
		layerBoundaries = new HashSet<Integer>();
		for (String layer : P.cfg.getStringList("Layers"))
		{
			if (!layerPattern.matcher(layer).matches())
			{
				P.p.getLogger().info("The layer '" + layer + "' is invalid");
				continue;
			}
			
			// Splits the range string for the layer
			final String[] range = Config.layerSplitPattern.split(layer);

			// Converts range into integers
			int miny = Integer.valueOf(range[0]);
			int maxy = Integer.valueOf(range[1]);

			// Makes sure miny is actually the lower value
			if (maxy < miny)
			{
				miny = miny ^ maxy;
				maxy = miny ^ maxy;
				miny = miny ^ maxy;
			}
			
			layer = miny + ":" + maxy;
			// Add the boundaries of the layer
			layerBoundaries.add(miny);
			layerBoundaries.add(maxy);
			layers.add(layer);
		}
		
		if (layers.size() == 0)
		{
			for (int i = 0; i <= 240; i += 8)
			{
				layers.add(i + ":" + (i + 16));
			}
		}
		
		P.cfg.set("Layers", layers);
		P.p.getLogger().info(layers.size() + " layers found");
		
		/* ######## Global Mob Attributes ######## */
		mobAttributes = new HashMap<EntityType, MobAttributes>();
		ConfigurationSection cfg = P.cfg.getConfigurationSection("MobAttributes");
		
		if (cfg == null)
			cfg = P.cfg.createSection("MobAttributes");
		
		Set<String> keys = cfg.getKeys(false);
		
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
			
			ConfigurationSection mobCfg = cfg.getConfigurationSection(key);
			if (mobCfg == null)
			{
				P.p.getLogger().warning("Error loading MobAttributes for " + key);
				continue;
			}
			mobAttributes.put(mob, new MobAttributes(mob, mobCfg));
		}
		
		// Copy the header to the file
		copyHeader(P.cfg, "configHeader.txt", P.p.getDescription().getName() + " Config " + P.p.getDescription().getVersion() + "\n");
		P.p.saveConfig();
	}
	
	public int setupWorlds()
	{
		int numWorlds = 0;
		worldConfigs = new HashMap<String, WorldConfig>();
		
		for (String worldName : P.cfg.getStringList("EnabledWorlds"))
		{
			World world = P.p.getServer().getWorld(worldName);
			
			if (world == null)
				continue;
			
			WorldConfig wc = new WorldConfig(world);
			
			worldConfigs.put(world.getName(), wc);
			P.worlds.put(world.getName(), new MMWorld(world, wc));
			
			++numWorlds;
		}
		
		return numWorlds;
	}
}
