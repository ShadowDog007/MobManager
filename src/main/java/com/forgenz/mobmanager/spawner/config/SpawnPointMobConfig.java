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
