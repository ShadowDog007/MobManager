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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;

import ninja.mcknight.bukkit.mobmanager.common.config.AbstractConfig;
import ninja.mcknight.bukkit.mobmanager.common.util.MiscUtil;

/**
 * Represents a set of requirements which a spawn must meet in order to spawn a mob
 */
public class SpawnRequirements extends AbstractConfig
{
	private static boolean first = false;
	
	private final int minLight, maxLight;
	private final int minY, maxY;
	private final int minTime, maxTime;
	
	private final long slimeLikeSpawnSeed;
	
	public final boolean requireOpaqueBlock;

	private final Set<Material> blockSet;
	private final boolean blockWhitelist;
	
	private final Set<Biome> biomeSet;
	private final boolean biomeWhitelist;
	
	private final Set<Environment> environmentSet;
	private final boolean environmentWhitelist;
	
	@SuppressWarnings("unchecked")
	public SpawnRequirements(Map<String, Object> cfg)
	{
		super.setMapCfg(cfg);
		
		minLight = getAndSet("MinLight", 0);
		maxLight = getAndSet("MaxLight", 15);
		
		minY = getAndSet("MinY", 0);
		maxY = getAndSet("MaxY", 256);
		
		minTime = getAndSet("MinTime", 0);
		maxTime = getAndSet("MaxTime", 24000);
		
		slimeLikeSpawnSeed = getAndSet("SlimeLikeSpawnSeed", 0L);
		
		requireOpaqueBlock = getAndSet("RequireOpaqueBlock", true);
		
		List<String> tmp = MiscUtil.getStringList(getAndSet("BlockList", new ArrayList<String>()));
		if (tmp.isEmpty())
		{
			blockSet = Collections.emptySet();
		}
		else
		{
			Set<Material> blockSet = new HashSet<Material>();
				
			for (Object obj : tmp)
			{
				String material = obj.toString();
				
				for (Material m : Material.values())
				{
					if (!m.isBlock())
						continue;
					if (m.toString().equalsIgnoreCase(material))
						blockSet.add(m);
				}
			}
			
			this.blockSet = (Set<Material>) (blockSet.isEmpty() ? Collections.emptySet(): blockSet);
		}
		blockWhitelist = getAndSet("BlockWhitelist", false);
		
		tmp = MiscUtil.getStringList(getAndSet("BiomeList", new ArrayList<String>()));
		if (tmp.isEmpty())
		{
			biomeSet = Collections.emptySet();
		}
		else
		{
			Set<Biome> biomeSet = new HashSet<Biome>();
				
			for (Object obj : tmp)
			{
				String biome = obj.toString();
				
				for (Biome b : Biome.values())
				{
					if (b.toString().equalsIgnoreCase(biome))
						biomeSet.add(b);
				}
			}
			
			this.biomeSet = (Set<Biome>) (biomeSet.isEmpty() ? Collections.emptySet(): biomeSet);
		}
		biomeWhitelist = getAndSet("BiomeWhitelist", false);
		
		tmp = MiscUtil.getStringList(getAndSet("EnvironmentList", new ArrayList<String>()));
		if (tmp.isEmpty())
		{
			environmentSet = Collections.emptySet();
		}
		else
		{
			Set<Environment> environmentSet = new HashSet<Environment>();
				
			for (Object obj : tmp)
			{
				String environment = obj.toString();
				
				for (Environment e : Environment.values())
				{
					if (e.toString().equalsIgnoreCase(environment))
						environmentSet.add(e);
				}
			}
			
			this.environmentSet = (Set<Environment>) (environmentSet.isEmpty() ? Collections.emptySet(): environmentSet);
		}
		environmentWhitelist = getAndSet("EnvironmentWhitelist", false);
		
		super.clearCfg();
		
		if (!first)
		{
			if (!required())
				cfg.clear();
		}
		else
			first = false;
	}
	
	/**
	 * Check if the requirements are needed
	 * 
	 * @return True if it is required
	 */
	public boolean required()
	{
		return !ignoreLight()
				|| maxY < 256
				|| minY > 0
				|| maxTime < 24000
				|| minTime > 0
				|| slimeLikeSpawnSeed != 0
				|| requireOpaqueBlock
				|| !blockSet.isEmpty()
				|| !biomeSet.isEmpty()
				|| !environmentSet.isEmpty();
	}
	
	/**
	 * Checks if the given y coordinate meets the requirements of this object
	 * 
	 * @return True if hight requirements are met
	 */
	public boolean meetsHeightRequirements(int y)
	{
		return maxY >= y && minY <= y;
	}
	
	public boolean meetsTimeRequirements(int time)
	{
		return maxTime >= time && minTime <= time;
	}
	
	public boolean meetsSlimeLikeSpawnRequirements(int chunkX, int chunkZ)
	{
		if (slimeLikeSpawnSeed == 0)
			return true;
		
		Random rnd = new Random(slimeLikeSpawnSeed +
				(long) (chunkX * chunkX * 0x4c1906) +
				(long) (chunkX * 0x5ac0db) +
				(long) (chunkZ * chunkZ) * 0x4307a7L +
				(long) (chunkZ * 0x5f24f) ^ 0x3ad8025f);
		return rnd.nextInt(10) == 0;
	}
	
	public boolean meetsEnvironmentRequirements(Environment environment)
	{
		return environmentWhitelist == environmentSet.contains(environment);
	}
	
	public boolean meetsBiomeRequirements(Biome biome)
	{
		return biomeWhitelist == biomeSet.contains(biome);
	}
	
	public boolean meetsBlockRequirements(Material materialBelow)
	{
		return blockWhitelist == blockSet.contains(materialBelow);
	}
	
	/**
	 * Checks if light needs to be checked
	 * 
	 * @return True if light can be ignored
	 */
	public boolean ignoreLight()
	{
		return minLight <= 0 && maxLight >= 15;
	}
	
	/**
	 * Checks if the requirements are met at the given location
	 * 
	 * @return True if the requirements are met
	 */
	public boolean met(int chunkX, int chunkZ, int y, int time, int lightLevel, Biome biome, Material materialBelow, Environment environment)
	{
		if (minLight > lightLevel || maxLight < lightLevel)
				return false;
		
		if (!meetsHeightRequirements(y) || !meetsTimeRequirements(time))
			return false;
		
		if (requireOpaqueBlock && !materialBelow.isSolid())
			return false;
		
		if (!meetsSlimeLikeSpawnRequirements(chunkX, chunkZ))
			return false;
		
		return meetsEnvironmentRequirements(environment) 
				&& meetsBiomeRequirements(biome)
				&& meetsBlockRequirements(materialBelow);
	}

	public static void resetConfigFlag()
	{
		first = true;
	}
	
	@Override
	public String toString()
	{
		return toString(false);
	}
	
	public String toString(boolean ignoreSome)
	{
		StringBuilder bldr = new StringBuilder();
		
		if (!ignoreSome)
		{
			if (maxY < 256)
				bldr.append("MaxY:").append(maxY);
			if (minY > 0)
				addSeperator(bldr).append("MinY:").append(minY);
		}
		
		if (maxLight < 15)
			addSeperator(bldr).append("MaxLight:").append(maxLight);
		
		if (minLight > 0)
			addSeperator(bldr).append("MinLight:").append(minLight);
		
		if (requireOpaqueBlock)
			addSeperator(bldr).append("RequiresOpaqueBlock");
		
		if (!ignoreSome && !environmentSet.isEmpty())
			addSeperator(bldr).append("Environment").append(environmentWhitelist ? "White" : "Black").append("List:").append(environmentSet);
		
		if (!ignoreSome && !biomeSet.isEmpty())
			addSeperator(bldr).append("Biome").append(environmentWhitelist ? "White" : "Black").append("List:").append(biomeSet);
		
		if (!ignoreSome && !blockSet.isEmpty())
			addSeperator(bldr).append("Block").append(environmentWhitelist ? "White" : "Black").append("List:").append(blockSet);
		
		return bldr.toString();
	}
	
	private StringBuilder addSeperator(StringBuilder builder)
	{
		if (builder.length() > 0)
			builder.append(',');
		return builder;
	}
}
