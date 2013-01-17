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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.forgenz.mobmanager.world.MMWorld;

public class Config
{
	public final static Pattern layerPattern = Pattern.compile("^\\d+:{1}\\d+$");
	public final static Pattern layerSplitPattern = Pattern.compile(":{1}");
	final static String worldsFolder = "worlds";
	
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
	
	public static EnumConfig ignoredMobs;
	public static EnumConfig disabledMobs;
	
	public static EnumConfig enabledSpawnReasons;
	
	public static List<String> layers;
	public static HashSet<Integer> layerBoundaries;
	
	public static HashMap<String, WorldConf> worldConfigs;
	
	public class EnumConfig
	{
		private ArrayList<String> contains = null;
		
		public EnumConfig(Class<?> enumClass, List<?> objectList, String missingEnumError)
		{
			if (objectList == null)
				return;
			
			this.contains = new ArrayList<String>();
			
			Object[] enumValues = enumClass.getEnumConstants();
			
			for (Object obj : objectList)
			{
				if (obj instanceof String == false)
					continue;
				
				String string = (String) obj;
				
				boolean found = false;
				
				for (Object value : enumValues)
				{
					if (value.toString().equalsIgnoreCase(string))
					{
						contains.add(value.toString());
						found = true;
					}
				}
				
				if (!found)
					P.p.getLogger().info(String.format(missingEnumError, string));
			}
		}
		
		public List<String> getList()
		{
			if (contains == null)
				return new ArrayList<String>();
			
			return contains;
		}
		
		public boolean containsValue(String string)
		{
			if (contains == null)
				return false;
			return contains.contains(string);
		}
		
		public void addDefaults(String ...defaults)
		{
			if (contains != null)
				return;
			
			if (defaults.length != 0)
				contains = new ArrayList<String>();
			
			for (String str : defaults)
			{
				contains.add(str);
			}
		}
		
		public String toString()
		{
			if (contains == null)
				return "";
			
			String str = "";
			
			for (String s : contains)
			{
				if (str.length() != 0)
					str += ",";
				str += s;
			}
			return str;
		}
	}
	
	public class WorldConf
	{
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
		
		public WorldConf(World world)
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
			cfg.set("SpawnSearchChunkDistance", spawnChunkSearchDistance);
			
			/* ################ UndergroundSpawnChunkSearchDistance ################ */
			undergroundSpawnChunkSearchDistance = cfg.getInt("UndergroundSpawnChunkSearchDistance", 2);
			cfg.set("UndergroundSpawnChunkSearchDistance", undergroundSpawnChunkSearchDistance);
			
			
			/* ################ GroundHeight ################ */
			int defaultHeight = world.getEnvironment() == Environment.NORMAL ? 55 : (world.getEnvironment() == Environment.NETHER ? 32 : -1);
			groundHeight = cfg.getInt("GroundHeight", defaultHeight);
			cfg.set("GroundHeight", groundHeight);
			
			
			copyHeader(cfg, "worldConfigHeader.txt", P.p.getDescription().getName() + " Config " + P.p.getDescription().getVersion() + "\n");
			saveConfig(worldsFolder, worldName + ".yml", cfg);
		}
	}
	
	Config()
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
		ignoredMobs = new EnumConfig(EntityType.class, P.cfg.getList("IgnoredMobs", null), "The Ignored Mob '%s' is invalid");
		ignoredMobs.addDefaults(EntityType.WITHER.toString(), EntityType.VILLAGER.toString());
		List<String> ignoredList = ignoredMobs.getList();
		P.cfg.set("IgnoredMobs", ignoredList);
		String strList = ignoredMobs.toString();
		if (strList.length() != 0)
			P.p.getLogger().info("IgnoredMobs: " + strList);
		
		
		/* ################ DisabledMobs ################ */
		disabledMobs = new EnumConfig(EntityType.class, P.cfg.getList("DisabledMobs", null), "The Disabled Mob '%s' is invalid");
		List<String> disabledList = disabledMobs.getList();
		P.cfg.set("DisabledMobs", disabledList);
		strList = disabledMobs.toString();
		if (strList.length() != 0)
			P.p.getLogger().info("DisabledMobs: " + strList);
		
		/* ################ EnabledSpawnReasons ################ */
		enabledSpawnReasons = new EnumConfig(SpawnReason.class, P.cfg.getList("EnabledSpawnReasons", null), "The Spawn Reason '%s' is invalid");
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
		
		// Copy the header to the file
		copyHeader(P.cfg, "configHeader.txt", P.p.getDescription().getName() + " Config " + P.p.getDescription().getVersion() + "\n");
		P.p.saveConfig();
	}
	
	public FileConfiguration getConfig(String folder, String config)
	{
		return YamlConfiguration.loadConfiguration(new File(P.p.getDataFolder(), folder + File.separator + config));
	}
	
	public void saveConfig(String folder, String config, FileConfiguration cfg)
	{
		try
		{
			cfg.save(new File(P.p.getDataFolder(), folder + File.separator + config));
		}
		catch (IOException exception)
		{
			P.p.getLogger().severe("Unable to write to config file at \"" + folder + File.separator + config + "\"");
		}
	}
	
	int setupWorlds()
	{
		int numWorlds = 0;
		worldConfigs = new HashMap<String, WorldConf>();
		
		for (String worldName : P.cfg.getStringList("EnabledWorlds"))
		{
			World world = P.p.getServer().getWorld(worldName);
			
			if (world == null)
				continue;
			
			WorldConf wc = new WorldConf(world);
			
			worldConfigs.put(world.getName(), wc);
			P.worlds.put(world.getName(), new MMWorld(world, wc));
			
			++numWorlds;
		}
		
		return numWorlds;
	}
	
	public String getResourceAsString(String resource)
	{
		InputStream headerStream = P.p.getResource(resource);
		if (headerStream == null)
			return "";
		
		String header = "";
		int numBytes = 1;
		while (numBytes > 0)
		{
			byte[] bytes = new byte[64];
			
			try
			{
				numBytes = headerStream.read(bytes);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			for (int i = 0; i < numBytes; ++i)
			{
				header += (char) bytes[i];
			}
		}
		
		return header;
	}
	public void copyHeader(FileConfiguration cfg, String resource)
	{		
		copyHeader(cfg, resource, "");
	}
	
	public void copyHeader(FileConfiguration cfg, String resource, String add)
	{
		cfg.options().header(add + getResourceAsString(resource));
		cfg.options().copyHeader(true);
	}
}
