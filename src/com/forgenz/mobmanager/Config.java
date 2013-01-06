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
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import com.forgenz.mobmanager.world.MMWorld;

public class Config
{
	public final static Pattern layerPattern = Pattern.compile("^\\d+:{1}\\d+$");
	public final static Pattern layerSplitPattern = Pattern.compile(":{1}");
	final static String worldsFolder = "worlds";
	
	public static boolean disableWarnings;
	
	public static short spawnChunkSearchDistance;
	public static short flyingMobAditionalLayerDepth;
	public static int ticksPerRecount;
	public static int ticksPerDespawnScan;
	
	public static List<EntityType> ignoredMobs;
	
	public static List<String> layers;
	
	public static HashMap<String, WorldConf> worldConfigs;
	
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
			
			// Attempt to fetch settings for the world
			for (MobType mob : mobs)
			{
				maximums[mob.index] = (short) Math.abs(cfg.getInt("WorldMaximum." + mob.cPath, mob.getDefaultMax(world.getEnvironment())));
				dynMultis[mob.index] = (short) Math.abs(cfg.getInt("ChunkCalculatedMaximum." + mob.cPath, mob.getDefaultDynMulti(world.getEnvironment())));
			}
			
			breedingLimit = (short) cfg.getInt("BreedingMaximumPerChunk", 15);
			numAnimalsForFarm = (short) cfg.getInt("NumAnimalsForFarm", 3);
			spawnChunkSearchDistance = (short) cfg.getInt("SpawnChunkSearchDistance", -1);
			undergroundSpawnChunkSearchDistance = cfg.getInt("UndergroundSpawnChunkSearchDistance", 2);
			
			int defaultHeight = world.getEnvironment() == Environment.NORMAL ? 55 : (world.getEnvironment() == Environment.NETHER ? 32 : -1);
			groundHeight = cfg.getInt("GroundHeight", defaultHeight);
			
			
			
			// Save settings, adding any missing settings with defaults
			for (MobType mob : mobs)
			{
				cfg.set("WorldMaximum." + mob.cPath, maximums[mob.index]);
				cfg.set("ChunkCalculatedMaximum." + mob.cPath, dynMultis[mob.index]);
			}
			
			cfg.set("BreedingMaximumPerChunk", breedingLimit);
			cfg.set("NumAnimalsForFarm", numAnimalsForFarm);
			cfg.set("SpawnSearchChunkDistance", spawnChunkSearchDistance);
			cfg.set("UndergroundSpawnChunkSearchDistance", undergroundSpawnChunkSearchDistance);
			cfg.set("GroundHeight", groundHeight);
			
			copyHeader(cfg, "worldConfigHeader.txt");
			
			saveConfig(worldsFolder, worldName + ".yml", cfg);
		}
	}
	
	Config()
	{
		List<String> activeWorlds = P.cfg.getStringList("EnabledWorlds");
		
		if (activeWorlds == null || activeWorlds.size() == 0)
		{
			activeWorlds = new ArrayList<String>();
			for (World world : P.p.getServer().getWorlds())
			{
				activeWorlds.add(world.getName());
			}
		}
		
		disableWarnings = P.cfg.getBoolean("DisableWarnings", false);
		spawnChunkSearchDistance = (short) Math.abs(P.cfg.getInt("SpawnChunkSearchDistance", 5));
		flyingMobAditionalLayerDepth = (short) P.cfg.getInt("FlyingMobAditionalLayerDepth", 2);
		ticksPerRecount = P.cfg.getInt("TicksPerRecount", 40);
		ticksPerDespawnScan = P.cfg.getInt("TicksPerDespawnScan", 100);
		
		ignoredMobs = new ArrayList<EntityType>();
		List<?> stringIgnoredMobs = P.cfg.getList("IgnoredMobs", null);
		if (stringIgnoredMobs != null)
		{
			P.p.getLogger().info("Found");
			for (Object obj : stringIgnoredMobs)
			{
				if (obj instanceof String == false)
					continue;
				
				String entity = (String) obj;
				
				// Get the entities type
				EntityType e = EntityType.valueOf(entity.toUpperCase());
				
				// Check if the given entity is valid
				if (e == null)
				{
					P.p.getLogger().warning("The ignoredMob '" + entity + "' is invalid");
					continue;
				}
				
				// Check if the given entity is a LivingEntity
				if (!LivingEntity.class.isAssignableFrom(e.getEntityClass()))
					continue;
				
				// Add the entity type to the list of ignored mobs
				if (!ignoredMobs.contains(e))
					ignoredMobs.add(e);
			}
		}
		else
		{
			// Add default ignored mobs
			ignoredMobs.add(EntityType.WITHER);
		}
		
		
		layers = new ArrayList<String>();
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
			layers.add(layer);
		}
		
		if (layers.size() == 0)
		{
			for (int i = 0; i <= 240; i += 8)
			{
				layers.add(i + ":" + (i + 16));
			}
		}
		
		// Validate SpawnChunkSearchDistance
		if (spawnChunkSearchDistance == 0)
			spawnChunkSearchDistance = 1;
		
		// Set and save the config values
		P.cfg.set("EnabledWorlds", activeWorlds);
		
		P.cfg.set("DisableWarnings", disableWarnings);
		P.cfg.set("SpawnChunkSearchDistance", spawnChunkSearchDistance);
		P.cfg.set("FlyingMobAditionalLayerDepth", flyingMobAditionalLayerDepth);
		P.cfg.set("TicksPerRecount", ticksPerRecount);
		P.cfg.set("TicksPerDespawnScan", ticksPerDespawnScan);
		
		String im = "";
		ArrayList<String> ignoredMobs_Strings = new ArrayList<String>(ignoredMobs.size());
		for (EntityType e : ignoredMobs)
		{
			if (im.length() != 0)
				im += ",";
			im += e.toString();
			ignoredMobs_Strings.add(e.toString());
		}
		
		if (im.length() != 0);
			P.p.getLogger().info("IgnoredMobs: " + im);
		
		P.cfg.set("IgnoredMobs", ignoredMobs_Strings);
		
		
		P.cfg.set("Layers", layers);
		
		copyHeader(P.cfg, "configHeader.txt");
		P.p.saveConfig();
		
		P.p.getLogger().info(layers.size() + " layers found");
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
		cfg.options().header(getResourceAsString(resource));
		cfg.options().copyHeader(true);
	}
}
