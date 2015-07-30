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
import java.util.Map;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.common.config.AbstractConfig;
import ninja.mcknight.bukkit.mobmanager.common.util.MiscUtil;
import ninja.mcknight.bukkit.mobmanager.common.util.RandomUtil;
import ninja.mcknight.bukkit.mobmanager.spawner.config.regions.GlobalRegion;
import ninja.mcknight.bukkit.mobmanager.spawner.config.regions.PointCircleRegion;
import ninja.mcknight.bukkit.mobmanager.spawner.util.MobCounter;
import ninja.mcknight.bukkit.mobmanager.spawner.util.MobReference;
import ninja.mcknight.bukkit.mobmanager.spawner.util.MobSpawner;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import ninja.mcknight.bukkit.mobmanager.spawner.config.regions.PointSquareRegion;

/**
 * Represents an area which mobs can be spawned
 */
public abstract class Region extends AbstractConfig
{
	public final String name;
	public final int priority;
	
	public final int spawnAttempts;
	public final float spawnAttemptChance;
	private final int maxBlockRange, minBlockRange;
	private final int maxUndergroundBlockRange, minUndergroundBlockRange, undergroundHeight;
	public final int maxPlayerMobs;
	public final int playerMobCooldown;
	
	private MobCounter maxAliveLimiter;
	private HashMap<String, MobCounter> groupedMaxAliveLimiters;
	private HashMap<String, Integer> groupedPlayerMobLimits;
	
	
	private final List<Mob> mobs;
	
	public final boolean hasRegionLimitBypass;
	
	public Region(ConfigurationSection cfg, RegionType type)
	{
		super.setCfg(cfg);
		
		name = cfg.getName();
		int priority = getAndSet("Priority", type != RegionType.GLOBAL ? 1 : 0);
		this.priority = priority > 0 ? priority : type != RegionType.GLOBAL ? 1 : 0;
		set("Priority", priority);
		
		spawnAttempts = getAndSet("SpawnAttempts", 1);
		spawnAttemptChance = getAndSet("SpawnAttemptChance", 100.0F) / 100.0F;
		maxBlockRange = getAndSet("MaxBlockRange", 48);
		minBlockRange = getAndSet("MinBlockRange", 24);
		maxUndergroundBlockRange = getAndSet("MaxUndergroundBlockRange", 32);
		minUndergroundBlockRange = getAndSet("MinUndergroundBlockRange", 16);
		undergroundHeight = getAndSet("UndergroundHeight", 55);
		maxPlayerMobs = getAndSet("MaxPlayerMobs", 15);
		ConfigurationSection limitCfg = getConfigurationSection("MaxPlayerMobGroups");
		for (String key : limitCfg.getKeys(false))
		{
			int limit = limitCfg.getInt(key);
			
			// Limits above 0 are valid
			if (limit > 0)
			{
				if (groupedPlayerMobLimits == null)
					groupedPlayerMobLimits = new HashMap<String, Integer>();
				groupedPlayerMobLimits.put(key.toLowerCase(), limit);
			}
		}
		
		playerMobCooldown = getAndSet("PlayerMobCooldown", 60) * 1000;
		
		int maxRegionMobs = getAndSet("MaxRegionMobs", 0);
		
		limitCfg = getConfigurationSection("RegionMaxMobGroups");
		
		int regionMobCooldown = getAndSet("RegionMobCooldown", 60) * 1000;
		boolean enforceAllRemovalConditions = getAndSet("EnforceAllCooldownConditions", false);
		if (maxRegionMobs > 0)
			maxAliveLimiter = new MobCounter(maxRegionMobs, regionMobCooldown, enforceAllRemovalConditions);
		
		for (String key : limitCfg.getKeys(false))
		{
			int limit = limitCfg.getInt(key);
			
			// Limits above 0 are valid
			if (limit > 0)
			{
				if (groupedMaxAliveLimiters == null)
					groupedMaxAliveLimiters = new HashMap<String, MobCounter>();
				groupedMaxAliveLimiters.put(key.toLowerCase(), new MobCounter(maxRegionMobs, regionMobCooldown, enforceAllRemovalConditions));
			}
		}
		
		initialise();
		
		ArrayList<Mob> mobs = new ArrayList<Mob>();
		
		boolean hasRegionLimitBypass = false;
		
		// Fetch mob configs
		List<Object> mobConfigs = MiscUtil.getList(getAndSet("Mobs", new ArrayList<Map<String, Object>>()));
		// Iterate through each config
		for (int i = 0; i < mobConfigs.size(); ++i)
		{
			// Fetch the config
			Map<String, Object> mobConfig = MiscUtil.copyConfigMap(mobConfigs.get(i));
			// Update the map
			mobConfigs.set(i, mobConfig);
			
			// Parse the mob config
			Mob mob = Mob.setup(mobConfig, this);
			if (mob != null)
				mobs.add(mob);
			
			if (mob.bypassSpawnLimits)
				hasRegionLimitBypass = true;
		}
		set("Mobs", mobConfigs);
		
		this.hasRegionLimitBypass = hasRegionLimitBypass;
		
		this.mobs = Collections.unmodifiableList(mobs);
		
		super.clearCfg();
	}
	
	public abstract void initialise();

	public abstract boolean withinRegion(Location location);
	
	public int getMaxBlockRange(int yHeight)
	{
		return yHeight > undergroundHeight ? maxBlockRange : maxUndergroundBlockRange;
	}	
	
	public int getMinBlockRange(int yHeight)
	{
		return yHeight > undergroundHeight ? minBlockRange : minUndergroundBlockRange;
	}
	
	/**
	 * Checks if the region has mobs which can ignore spawn limits
	 * 
	 * @return true if the region has at least one mob which can ignore region spawn limits
	 */
	public boolean ignoreMobLimits()
	{
		if (!hasRegionLimitBypass)
			return false;
		
		for (Mob mob : mobs)
		{
			// Check if the mob can bypass region spawn limits and also is within its own spawn limit
			if (mob.bypassSpawnLimits && mob.withinAliveLimit())
			{
				return true;
			}
		}
		return false;
	}
	
	public int getPlayerGroupMobLimit(String playerLimitGroup)
	{
		if (groupedPlayerMobLimits == null)
			return 0;
		
		Integer limit = groupedPlayerMobLimits.get(playerLimitGroup);
		
		return limit != null ? limit : 0;
	}
	
	/**
	 * Returns the list of mobs
	 */
	public List<Mob> getMobs(int y, Environment environment, Biome biome)
	{
		List<Mob> mobList = new ArrayList<Mob>(mobs.size());
		
		for (Mob mob : mobs)
		{
			SpawnRequirements r = mob.getRequirements();
			if (r == null || (r.meetsHeightRequirements(y) && r.meetsEnvironmentRequirements(environment) && r.meetsBiomeRequirements(biome)))
				mobList.add(mob);
		}
		
		return mobList;
	}
	
	public boolean withinAliveLimit()
	{
		return maxAliveLimiter == null || maxAliveLimiter.withinLimit();
	}

	/**
	 * Adds the placeholder to spawn limits to prepare for spawning a mob
	 * 
	 * @param mob The mob settings which were used to spawn this entity
	 * @param mobRef The entity which was spawned
	 * 
	 * @return True if the mob is allowed to spawn
	 */
	public boolean addSpawnedMob(Mob mob, MobReference mobRef)
	{
		if (!mobRef.isValid())
			return false;
		
		// Add the mob to the regions limit
		if (maxAliveLimiter != null && !maxAliveLimiter.add(mobRef))
			return false;
		
		// Add the mob to its grouped limit
		if (groupedMaxAliveLimiters != null && mob.regionLimitGroup.length() > 0)
		{
			MobCounter limiter = groupedMaxAliveLimiters.get(mob.regionLimitGroup);
			
			if (limiter != null && !limiter.add(mobRef))
				return false;
		}
		return true;
	}
	
	/**
	 * Attempts to spawn a mob within the region
	 * <p>
	 * Note: Not essential that the location is within the region but it should be :3
	 * 
	 * @param player The player the mob will be spawning for
	 * @param spawnLoc The location the mob will be spawning at
	 * @param lightLevel The light level at the given spawn location
	 * @param biome The biome type at the given spawn location
	 * @param materialBelow The material of the block below the given spawn location
	 * @param environment The environment of the world the given location is in
	 * @param outsideSpawnLimits True if either player or region spawn limits have been met
	 * 
	 * @return A mob spawner if a mob should be spawned
	 */
	public MobSpawner spawnMob(Player player, int playerY, int heightRange, Location spawnLoc, boolean wideLoc, boolean tallLoc, int time, int lightLevel, Biome biome, Material materialBelow, Environment environment, boolean outsideSpawnLimits)
	{
		// Fetch all the mobs which we can spawn in this location 
		ArrayList<Mob> spawnableMobs = getSpawnableMobs(player, spawnLoc.getWorld(), spawnLoc, wideLoc, tallLoc, time, lightLevel, biome, materialBelow, environment, outsideSpawnLimits);
		
		// If no mobs can spawn here return false :'(
		if (spawnableMobs.isEmpty())
			return null;
		
		// Fetch a random mob from the list of spawnable mobs
		Mob mob = getMob(spawnableMobs, getTotalChance(spawnableMobs));
		
		// If the mob is null, or the entity type is invalid return false :'(
		if (mob == null || mob.getMobType().getBukkitEntityType() == null)
			return null;
		
		// If this mobs requirements check was delayed check it now
		if (mob.delayRequirementsCheck && !mob.requirementsMet(true, spawnLoc.getWorld(), spawnLoc, time, lightLevel, biome, materialBelow, environment))
			return null;
		
		// Return the mob spawner
		return new MobSpawner(this, player, spawnLoc, mob, playerY, heightRange);
	}
	
	/**
	 * Fetches all mobs which can spawn in the given location and returns a list of them
	 * 
	 * @param world The world the mob is spawning in
	 * @param y The Y coordinate at which the mob will be spawning at
	 * @param lightLevel The light level at the spawn location
	 * @param biome The Biome type at the spawn location
	 * @param materialBelow The Material of the block below the spawn location
	 * @param environment The environment in the world the spawn location is in
	 * @param outsideSpawnLimits True if player or region limits have been met
	 * 
	 * @return A list of spawnable mobs
	 */
	private ArrayList<Mob> getSpawnableMobs(Player player, World world, Location sLoc, boolean wideLoc, boolean tallLoc, int time, int lightLevel, Biome biome, Material materialBelow, Environment environment, boolean outsideSpawnLimits)
	{
		// Initialise the list
		ArrayList<Mob> spawnableMobs = MMComponent.getSpawner().getConfig().getCachedList();
		
		for (Mob mob : mobs)
		{
			// If we are operating outside spawn limits the mob must be able to bypass those limits
			if (outsideSpawnLimits && !mob.bypassSpawnLimits)
				continue;
			
			// Don't allow wide mobs to spawn in a tight location
			if (mob.getMobType().isWide() && !wideLoc)
				continue;
			
			// Don't allow tall mobs to spawn in a tight location
			if (mob.getMobType().isTall() && !tallLoc)
				continue;
			
			// Check if the requirements are met
			if (!mob.requirementsMet(false, world, sLoc, time, lightLevel, biome, materialBelow, environment))
				continue;
			
			// Check if the mob is assigned to a player spawn limit
			if (!MMComponent.getSpawner().getSpawnFinder().withinGroupedLimit(player, this, mob))
				continue;
			
			// Check if the mob is assigned to another region spawn limit
			if (mob.regionLimitGroup.length() > 0)
			{
				// Fetch the limiter for the given group
				MobCounter limiter = groupedMaxAliveLimiters.get(mob.regionLimitGroup);
				
				// Check if the group limit has not been reached
				if (limiter != null && !limiter.withinLimit())
					continue;
			}
			spawnableMobs.add(mob);
		}
		
		return spawnableMobs;
	}
	
	/**
	 * Calculates the total of chances
	 * 
	 * @param mobs List of mobs to total the chances of
	 * 
	 * @return Sum of all chances
	 */
	private int getTotalChance(final List<Mob> mobs)
	{
		int chance = 0;
		for (Mob mob : mobs)
			chance += mob.spawnChance;
		return chance;
	}
	
	
	/**
	 * Fetches a random mob from the given list</br>
	 * Uses the mobs chances to pick mobs
	 * 
	 * @param mobs List of mobs to pick from
	 * @param totalChance Sum of all mob chances in the list
	 * 
	 * @return A single mob
	 */
	private Mob getMob(final List<Mob> mobs, int totalChance)
	{
		// Get a random number between 0 and the 
		int chance = RandomUtil.i.nextInt(totalChance);
		
		for (Mob mob : mobs)
		{
			// Minus the chance of the mob from the total
			chance -= mob.spawnChance;
			
			// Once 'chance' reduces below 0 we have found our mob
			if (chance < 0)
				return mob;
		}
		
		// Waht
		MMComponent.getSpawner().warning("Mob chances failed (That should never happen)");
		return null;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
	
	/**
	 * Represents a type of region
	 */
	public enum RegionType
	{
		/** Global Region */
		GLOBAL(GlobalRegion.class),
		/** Cylindrical region centered around a point */
		POINT_CIRCLE(PointCircleRegion.class),
		/** Rectangular region centered around a point */
		POINT_SQUARE(PointSquareRegion.class);
		
		private final Class<? extends Region> clazz;
		
		private RegionType(Class<? extends Region> clazz)
		{
			this.clazz = clazz;
		}
		
		public Region createRegion(ConfigurationSection cfg)
		{
			try
			{
				return clazz.getConstructor(ConfigurationSection.class).newInstance(cfg);
			}
			catch (Exception e)
			{
				MMComponent.getSpawner().severe("Error occured when creating region. RegionType=%s, RegionName=%s", e, this, cfg.getName());
				return null;
			}
		}
	}
}
