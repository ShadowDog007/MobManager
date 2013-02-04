package com.forgenz.mobmanager.config;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;

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
		
		
		copyHeader(cfg, "worldConfigHeader.txt", P.p.getDescription().getName() + " Config " + P.p.getDescription().getVersion() + "\n");
		saveConfig(worldsFolder, worldName + ".yml", cfg);
	}
}