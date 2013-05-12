package com.forgenz.mobmanager.spawner.config;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import com.forgenz.mobmanager.common.config.AbstractConfig;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;
import com.forgenz.mobmanager.common.util.RandomLocationGen;

public class SpawnPointMobConfig extends AbstractConfig
{
	
	private static Random rand = new Random();
	
	private int tick = 0;
	
	public final String name;
	public final Location location;
	public final ExtendedEntityType type;
	public final int ticksPerSpawn;
	public final int maxGroupSize;
	public final int radius;
	public final int minLight;
	public final int maxLight;
	
	protected SpawnPointMobConfig(ConfigurationSection cfg, String name, World world)
	{
		this.name = name;
		
		double x = cfg.getDouble("X", 0.0);
		double y = cfg.getDouble("Y", 0.0);
		double z = cfg.getDouble("Z", 0.0);
		set(cfg, "X", x);
		set(cfg, "Y", y);
		set(cfg, "Z", z);
		this.location = new Location(world, x, y, z);
		
		String typeStr = cfg.getString("Type", "");
		ExtendedEntityType type = null;
		for (ExtendedEntityType t : ExtendedEntityType.values())
		{
			if (t.toString().equalsIgnoreCase(typeStr))
			{
				type = t;
				break;
			}
		}
		this.type = type;
		set(cfg, "Type", type != null ? type.toString() : "");
		
		
		int tps = cfg.getInt("Rate", 1);
		if (tps < 0)
			tps = 0;
		this.ticksPerSpawn = tps;
		set(cfg, "Rate", tps);
		
		int maxGroupSize = cfg.getInt("MaxGroupSize");
		if (maxGroupSize < 1)
			maxGroupSize = 1;
		this.maxGroupSize = maxGroupSize;
		set(cfg, "MaxGroupSize", maxGroupSize);
		
		this.radius = cfg.getInt("Radius", 0);
		set(cfg, "Radius", this.radius);
		
		this.minLight = cfg.getInt("MinLight", 0);
		set(cfg, "MinLight", this.minLight);
		
		this.maxLight = cfg.getInt("MaxLight", 15);
		set(cfg, "MaxLight", this.maxLight);
	}
	
	public boolean isValid()
	{
		return this.type != null && location != null && location.getWorld() != null && this.ticksPerSpawn != 0;
	}
	
	public void tick()
	{
		if (++this.tick == this.ticksPerSpawn)
		{
			spawnMobs();
			tick = 0;
		}
	}
	
	public void spawnMobs()
	{
		int mobCount = rand.nextInt(maxGroupSize);
		
		for (int i = 0; i < mobCount; ++i)
		{
			Location loc;
			if (radius != 0)
			{
				loc = RandomLocationGen.getLocation(false, this.location, this.radius, 0, 4);
			}
			else
			{
				loc = this.location;
			}
		}
	}
	
}
