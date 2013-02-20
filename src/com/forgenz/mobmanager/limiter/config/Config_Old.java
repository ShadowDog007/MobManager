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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.common.config.EnumSettingContainer;
import com.forgenz.mobmanager.common.config.TSettingContainer;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;
import com.forgenz.mobmanager.limiter.world.MMWorld;

/**
 * Config_Old moves the old config settings (From v2.1b) into limiter.yml
 * and converts layers to their new format
 * 
 * @author Michael McKnight (ShadowDog007)
 *
 */
public class Config_Old extends Config
{
	
	public Config_Old()
	{
		FileConfiguration cfg = getConfig("", LIMITER_CONFIG_NAME);
		FileConfiguration oldCfg = P.p.getConfig();
		
		List<String> activeWorlds = oldCfg.getStringList("EnabledWorlds");
		
		if (activeWorlds == null || activeWorlds.size() == 0)
		{
			activeWorlds = new ArrayList<String>();
			for (World world : P.p.getServer().getWorlds())
			{
				activeWorlds.add(world.getName());
			}
		}
		for (String world : activeWorlds)
			enabledWorlds.add(world);
		set(cfg, "EnabledWorlds", activeWorlds);
		oldCfg.set("EnabledWorlds", null);
		
		/* ################ DisableWarnings ################ */
		disableWarnings = oldCfg.getBoolean("DisableWarnings", true);
		set(cfg, "DisableWarnings", disableWarnings);
		oldCfg.set("DisableWarnings", null);
		
		/* ################ UseAsyncDespawnScanner ################ */
		useAsyncDespawnScanner = oldCfg.getBoolean("UseAsyncDespawnScanner", false);
		set(cfg, "UseAsyncDespawnScanner", useAsyncDespawnScanner);
		oldCfg.set("UseAsyncDespawnScanner", null);
		
		/* ################ IgnoreCreativePlayers ################ */
		ignoreCreativePlayers = oldCfg.getBoolean("IgnoreCreativePlayers", false);
		set(cfg, "IgnoreCreativePlayers", ignoreCreativePlayers);
		oldCfg.set("IgnoreCreativePlayers", null);
		
		/* ################ TamedAnimals ################ */
		removeTamedAnimals = oldCfg.getBoolean("RemoveTamedAnimals", false);
		set(cfg, "RemoveTamedAnimals", removeTamedAnimals);
		oldCfg.set("RemoveTamedAnimals", null);
		
		countTamedAnimals = oldCfg.getBoolean("CountTamedAnimals", true);
		set(cfg, "CountTamedAnimals", countTamedAnimals);
		oldCfg.set("CountTamedAnimals", null);
		
		/* ################ Animal Despawning Stuff ################ */
		enableAnimalDespawning = oldCfg.getBoolean("EnableAnimalDespawning", true);
		set(cfg, "EnableAnimalDespawning", enableAnimalDespawning);
		oldCfg.set("EnableAnimalDespawning", null);
		
		daysTillFarmAnimalCleanup = oldCfg.getDouble("DaysTillFarmAnimalCleanup", 15.0D);
		set(cfg, "DaysTillFarmAnimalCleanup", daysTillFarmAnimalCleanup);
		oldCfg.set("DaysTillFarmAnimalCleanup", null);
		
		protectedFarmAnimalSaveInterval = oldCfg.getInt("ProtectedFarmAnimalSaveInterval", 6000);
		set(cfg, "ProtectedFarmAnimalSaveInterval", protectedFarmAnimalSaveInterval);
		oldCfg.set("ProtectedFarmAnimalSaveInterval", null);
		
		/* ################ SpawnChunkSearchDistance ################ */
		spawnChunkSearchDistance = (short) Math.abs(oldCfg.getInt("SpawnChunkSearchDistance", 5));
		// Validate SpawnChunkSearchDistance
		if (spawnChunkSearchDistance == 0)
			spawnChunkSearchDistance = 1;
		set(cfg, "SpawnChunkSearchDistance", spawnChunkSearchDistance);
		oldCfg.set("SpawnChunkSearchDistance", null);
		
		/* ################ FlyingMobAditionalLayerDepth ################ */
		flyingMobAditionalLayerDepth = (short) oldCfg.getInt("FlyingMobAditionalLayerDepth", 2);
		set(cfg, "FlyingMobAditionalLayerDepth", flyingMobAditionalLayerDepth);
		oldCfg.set("FlyingMobAditionalLayerDepth", null);
		
		/* ################ TicksPerRecount ################ */
		ticksPerRecount = oldCfg.getInt("TicksPerRecount", 40);
		set(cfg, "TicksPerRecount", ticksPerRecount);
		oldCfg.set("TicksPerRecount", null);
		
		/* ################ TicksPerDespawnScan ################ */
		ticksPerDespawnScan = oldCfg.getInt("TicksPerDespawnScan", 100);
		set(cfg, "TicksPerDespawnScan", ticksPerDespawnScan);
		oldCfg.set("TicksPerDespawnScan", null);
		
		/* ################ MinTicksLivedForDespawn ################ */
		minTicksLivedForDespawn = oldCfg.getInt("MinTicksLivedForDespawn", 100);
		set(cfg, "MinTicksLivedForDespawn", minTicksLivedForDespawn);
		oldCfg.set("MinTicksLivedForDespawn", null);
		
		/* ################ IgnoredMobs ################ */
		ignoredMobs =new TSettingContainer<ExtendedEntityType>(ExtendedEntityType.values(), cfg.getList("IgnoredMobs"), "IgnoredMobs");
		ignoredMobs.addDefaults(ExtendedEntityType.get(EntityType.WITHER), ExtendedEntityType.get(EntityType.VILLAGER));
		List<String> ignoredList = ignoredMobs.getList();
		set(cfg, "IgnoredMobs", ignoredList);
		String strList = ignoredMobs.toString();
		if (strList.length() != 0)
			P.p.getLogger().info("IgnoredMobs: " + strList);
		
		
		/* ################ DisabledMobs ################ */
		disabledMobs = new TSettingContainer<ExtendedEntityType>(ExtendedEntityType.values(), cfg.getList("DisabledMobs"), "DisabledMobs");
		List<String> disabledList = disabledMobs.getList();
		set(cfg, "DisabledMobs", disabledList);
		strList = disabledMobs.toString();
		if (strList.length() != 0)
			P.p.getLogger().info("DisabledMobs: " + strList);
		
		/* ################ EnabledSpawnReasons ################ */
		enabledSpawnReasons = new EnumSettingContainer(SpawnReason.class, oldCfg.getList("EnabledSpawnReasons", null), "The Spawn Reason '%s' is invalid");
		enabledSpawnReasons.addDefaults(SpawnReason.DEFAULT,
				SpawnReason.NATURAL,
				SpawnReason.SPAWNER,
				SpawnReason.CHUNK_GEN,
				SpawnReason.VILLAGE_DEFENSE,
				SpawnReason.VILLAGE_INVASION,
				SpawnReason.BUILD_IRONGOLEM,
				SpawnReason.BUILD_SNOWMAN,
				SpawnReason.BREEDING,
				SpawnReason.EGG);
		List<String> srList = enabledSpawnReasons.getList();
		set(cfg, "EnabledSpawnReasons", srList);
		oldCfg.set("EnabledSpawnReasons", null);
		strList = enabledSpawnReasons.toString();
		if (strList.length() != 0)
			P.p.getLogger().info("EnabledSpawnReasons: " + strList);
		
		Pattern layerPattern = Pattern.compile("^\\d+:{1}\\d+$");
		Pattern layerSplitPattern = Pattern.compile(":{1}");
		
		/* ################ Layers ################ */
		layers = new ArrayList<String>();
		layerBoundaries = new HashSet<Integer>();
		for (String layer : oldCfg.getStringList("Layers"))
		{
			if (!layerPattern.matcher(layer).matches())
			{
				P.p.getLogger().info("The layer '" + layer + "' is invalid");
				continue;
			}
			
			// Splits the range string for the layer
			final String[] range = layerSplitPattern.split(layer);

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
			
			layer = miny + "/" + maxy;
			// Add the boundaries of the layer
			layerBoundaries.add(miny);
			layerBoundaries.add(maxy);
			layers.add(layer);
		}
		
		if (layers.size() == 0)
		{
			for (int i = 0; i <= 240; i += 8)
			{
				layers.add(i + "/" + (i + 16));
			}
		}
		
		set(cfg, "Layers", layers);
		oldCfg.set("Layers", null);
		P.p.getLogger().info(layers.size() + " layers found");
		
		oldCfg.set("MobAbilities", null);
		oldCfg.set("MobAttributes", null);
		
		// Copy the header to the file
		copyHeader(cfg, "Limiter_ConfigHeader.txt", P.p.getDescription().getName() + " Limiter Global Config " + P.p.getDescription().getVersion() + "\n");
		saveConfig("", LIMITER_CONFIG_NAME, cfg);
	}
	
	public int setupWorlds()
	{
		int numWorlds = 0;
		worldConfigs = new HashMap<String, WorldConfig>();
		
		for (String worldName : enabledWorlds)
		{
			World world = P.p.getServer().getWorld(worldName);
			
			if (world == null)
				continue;
			
			WorldConfig wc = new WorldConfig_Old(world);
			
			worldConfigs.put(world.getName(), wc);
			P.worlds.put(world.getName(), new MMWorld(world, wc));
			
			++numWorlds;
		}
		
		return numWorlds;
	}
}
