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

import java.util.LinkedHashMap;
import java.util.Map;

import ninja.mcknight.bukkit.mobmanager.common.config.AbstractConfig;
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;
import ninja.mcknight.bukkit.mobmanager.common.util.RandomLocationGen;
import ninja.mcknight.bukkit.mobmanager.spawner.util.MobCounter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.abilities.abilities.AbilitySet;
import ninja.mcknight.bukkit.mobmanager.common.util.MiscUtil;
import ninja.mcknight.bukkit.mobmanager.limiter.world.MMWorld;
import ninja.mcknight.bukkit.mobmanager.spawner.util.MobReference;

/**
 * Represents a mob which can be spawned and the chance of it being spawned</br>
 * relative to other mobs which are to be spawned
 */
public class Mob extends AbstractConfig
{
	public final int spawnChance;
	
	private ExtendedEntityType mobType;
	private AbilitySet abilitySet;
	
	private SpawnRequirements requirements;
	private Action action;
	
	private MobCounter maxAliveLimiter;

	public final String playerLimitGroup, regionLimitGroup;
	public final int heightOffset;
	public final boolean bypassMobManagerLimit;
	public final boolean bypassSpawnLimits;
	public final boolean delayRequirementsCheck;
	
	private Mob(Map<String, Object> cfg)
	{
		super.setMapCfg(cfg);
		
		spawnChance = getAndSet("SpawnChance", 1);
		int maxAlive = getAndSet("MaxAlive", 0);
		int mobCooldown = getAndSet("MobCooldown", 60) * 1000;
		boolean enforceAllRemovalConditions = getAndSet("EnforceAllCooldownConditions", false);
		
		if (maxAlive > 0)
			maxAliveLimiter = new MobCounter(maxAlive, mobCooldown, enforceAllRemovalConditions);
		
		playerLimitGroup = getAndSet("PlayerLimitGroup", "").toLowerCase();
		regionLimitGroup = getAndSet("RegionLimitGroup", "").toLowerCase();
		heightOffset = getAndSet("HeightOffset", 0);
		bypassMobManagerLimit = getAndSet("BypassMobManagerLimit", false);
		bypassSpawnLimits = getAndSet("BypassPlayerAndRegionMobLimit", false);
		delayRequirementsCheck = getAndSet("DelayRequirementsCheck", false);
		
		mobType = ExtendedEntityType.valueOf(getAndSet("MobType", "UNKNOWN"));
		if (MMComponent.getAbilities().isEnabled())
		{
			String abilitySetName = getAndSet("AbilitySet", "default");
			abilitySet = abilitySetName.equalsIgnoreCase("default") ? null : AbilitySet.getAbilitySet(abilitySetName);
			if (abilitySetName.equalsIgnoreCase("default") && abilitySet == null)
				MMComponent.getSpawner().warning("Missing AbilitySet: " + abilitySetName);
			if (abilitySet != null && mobType == ExtendedEntityType.UNKNOWN)
				mobType = abilitySet.getAbilitySetsEntityType();
		}
		
		if (mobType == ExtendedEntityType.UNKNOWN)
		{
			super.clearCfg();
			return;
		}
		
		Map<String, Object> cfgMap = MiscUtil.copyConfigMap(getAndSet("Requirements", new LinkedHashMap<String, Object>()));
		requirements = new SpawnRequirements(cfgMap);
		set("Requirements", cfgMap);
		if (!requirements.required())
			requirements = null;
		
		cfgMap = MiscUtil.copyConfigMap(getAndSet("Action", new LinkedHashMap<String, Object>()));
		action = new Action(cfgMap);
		set("Action", cfgMap);
		
		super.clearCfg();
	}
	
	public boolean withinAliveLimit()
	{
		return maxAliveLimiter == null || maxAliveLimiter.withinLimit();
	}
	
	public ExtendedEntityType getMobType()
	{
		return mobType;
	}
	
	public AbilitySet getAbilitySet()
	{
		return abilitySet;
	}
	
	public SpawnRequirements getRequirements()
	{
		return requirements;
	}
	
	public boolean addHeightOffset(Location spawnLoc, int playerHeight, int heightRange)
	{
		if (heightOffset == 0)
			return true;
		if (!RandomLocationGen.findSafeY(spawnLoc, playerHeight + heightOffset, heightRange, requirements == null || requirements.requireOpaqueBlock))
			return false;
		
		Block b = spawnLoc.getBlock();
		return (!getMobType().isTall() || RandomLocationGen.isTallLocation(b)) && (!getMobType().isWide() || RandomLocationGen.isWideLocation(b));
	}
	
	/**
	 * Checks if requirements are met at the given location to spawn this entity
	 * 
	 * @param delayed True if this check has already been delayed
	 * @param world The world which the mob is being spawned in
	 * @param y The Y Location where the mob is being spawned
	 * @param lightLevel The light level at the given location
	 * @param biome The biome at the given location
	 * @param materialBelow The material which the mob would be standing on
	 * @param environment The environment of the world
	 * @return True if all the mobs requirements are met
	 */
	public boolean requirementsMet(boolean delayed, World world, Location sLoc, int time, int lightLevel, Biome biome, Material materialBelow, Environment environment)
	{
		if (!delayed && delayRequirementsCheck)
			return true;
		
		// If the mobs alive limit is reached we can't spawn any more of this mob
		if (!withinAliveLimit())
			return false;
		
		// Do we need to check limiter spawn limits?
		if (!bypassMobManagerLimit)
		{
			// Fetch the Limiter world
			MMWorld mmWorld = MMComponent.getLimiter().getWorld(world);
			
			// If the world exists and has met the mob limit for the given mob we can't spawn this mob
			if (mmWorld != null && !mmWorld.withinMobLimit(getMobType(), null))
				return false;
		}
		
		// Check if we have more requirements and that they are met
		return requirements == null
				|| requirements.met(sLoc.getBlockX() >> 4, sLoc.getBlockZ() >> 4, sLoc.getBlockY(), time, lightLevel, biome, materialBelow, environment);
	}
	
	public boolean addSpawnedMob(MobReference mobRef)
	{
		return !mobRef.isValid() || maxAliveLimiter == null || maxAliveLimiter.add(mobRef);
	}
	
	/**
	 * Executes the action which occurs upon the spawning of this mob
	 * 
	 * @param location The location at which to execute the action
	 */
	public void executeAction(Location location)
	{
		// If the action exists we execute it
		if (action != null)
			action.execute(location);
	}
	
	/**
	 * Checks if the mob is valid
	 * 
	 * The mob is invalid if the spawnChance is less than or equal to 0 (So it can't spawn)
	 * 
	 * @return True if the mob is valid
	 */
	private boolean isValid()
	{
		return spawnChance > 0;
	}
	
	/**
	 * Creates a mob given the configuration map
	 * 
	 * @param cfg The config map to use to create this mob
	 * @param region The region this mob will belong to
	 * @return The mob if was created successfully
	 */
	public static Mob setup(Map<String, Object> cfg, Region region)
	{
		Mob mob = null;
		try
		{
			// Create the new mob
			mob = new Mob(cfg);
		}
		catch (Exception e)
		{
			// If an error occured print it to the screen and return null
			MMComponent.getSpawner().severe("Error setting up Mob: Region=%s, ConfigurationSection=%s", e, region, cfg.toString());
			return null;
		}
		
		// If the mob is invalid print an error and return null
		if (!mob.isValid())
		{
			MMComponent.getSpawner().severe("Error setting up Mob: Region=%s, MobType=%s, AbilitySet=%s, Chance=%d", region, mob.getMobType(), mob.getAbilitySet(), mob.spawnChance);
			return null;
		}
		
		// Return the mob :3
		return mob;
	}
}
