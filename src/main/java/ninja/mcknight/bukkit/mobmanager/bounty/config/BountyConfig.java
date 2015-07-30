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

package ninja.mcknight.bukkit.mobmanager.bounty.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ninja.mcknight.bukkit.mobmanager.bounty.config.multipliers.TimeMultiplier;
import ninja.mcknight.bukkit.mobmanager.common.config.AbstractConfig;
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.abilities.abilities.AbilitySet;
import ninja.mcknight.bukkit.mobmanager.bounty.config.multipliers.PermissionMultiplier;
import ninja.mcknight.bukkit.mobmanager.bounty.config.util.ExploitsPlayerData;
import ninja.mcknight.bukkit.mobmanager.common.util.LocationCache;

public class BountyConfig extends AbstractConfig
{
	private static final String BOUNTY_CONFIG_NAME = "bounty.yml";
	private static final double DOUBLE_TOO_SMALL = 0.0001;
	
	public final BountyType bountyType;
	public final Material itemDrop;
	
	private final HashSet<String> enabledWorlds = new HashSet<String>();
	
	private final BountyWorldConfig globalCfg;
	private final HashMap<String, BountyWorldConfig> worldConfigs = new HashMap<String, BountyWorldConfig>();
	
	// Multiplier data
	private final HashMap<String, Double> worldMultipliers = new HashMap<String, Double>();
	private final double[] timeMultipliers = new double[TimeMultiplier.values().length];
	private final double[] biomeMultipliers = new double[Biome.values().length];
	private final ArrayList<PermissionMultiplier> permissionMultipliers;
	private final HashMap<String, Double> abilitySetMultipliers = new HashMap<String, Double>();
	
	private boolean usingTimeMulti, usingBiomeMulti;
	
	// Exploits
	public final boolean useCooldown, useLoginTimer, useSpawnerProtection, useNearbyMobCap, useDepreciativeReturn;
	private final double depreciativeReturnMulti, nearbyCappedMulti, spawnerCappedMulti;
	private final int cooldownTimeout, loginTimeout, nearbyRangeSquared, nearbyMobCap, spawnerCap;
	private final HashMap<String, ExploitsPlayerData> exploitsPlayerData;
	private final HashMap<String, Long> loginTimer;
	
	public final String rewardPlayerMessage;
	public final String finePlayerMessage;
	
	public BountyConfig()
	{
		FileConfiguration cfg = getConfig("", BOUNTY_CONFIG_NAME);
		ConfigurationSection sect;
		
		/* ################ EnabledWorlds ################ */
		List<String> enabledWorlds = cfg.getStringList("EnabledWorlds");
		
		if (enabledWorlds == null || enabledWorlds.size() == 0)
		{
			enabledWorlds = new ArrayList<String>();
			for (World world : Bukkit.getWorlds())
			{
				enabledWorlds.add(world.getName());
			}
		}
		
		for (String world : enabledWorlds)
		{
			this.enabledWorlds.add(world.toLowerCase());
		}
		set(cfg, "EnabledWorlds", enabledWorlds);
		
		/* ################ BountyType ################ */
		String bountyTypeString = cfg.getString("BountyType", "MONEY");
		BountyType type = null;
		for (int i = 0; i < BountyType.values().length; ++i)
		{
			type = BountyType.values()[i];
			
			if (type.toString().equalsIgnoreCase(bountyTypeString))
			{
				break;
			}
		}
		if (type == null)
			type = BountyType.MONEY;
		bountyType = type;
		set(cfg, "BountyType", bountyType.toString());
		
		/* ################ PlayerMessages ################ */
		sect = getConfigurationSection(cfg, "PlayerMessages");
		
		String str;
		// Reward
		str = sect.getString("Reward", "Earned %amount% for killing a %mob%");
		set(sect, "Reward", str);
		str = str.replace("%amount%", bountyType.amount);
		str = str.replace("%mob%", bountyType.mob);
		rewardPlayerMessage = ChatColor.translateAlternateColorCodes('&',str);
		
		// Fine
		str = sect.getString("Fine", "Fined %amount% for killing a %mob%");
		set(sect, "Fine", str);
		str = str.replace("%amount%", bountyType.amount);
		str = str.replace("%mob%", bountyType.mob);
		finePlayerMessage = ChatColor.translateAlternateColorCodes('&', str);
		
		/* ################ ItemDrop ################ */
		String materialName = cfg.getString("ItemDrop", Material.EMERALD.toString());
		Material material = Material.getMaterial(materialName.toUpperCase());
		if (bountyType == BountyType.ITEM && material == null)
		{
			MMComponent.getBounties().warning(String.format("The material %s does not exist, defaulting ItemDrop to emerald"));
			material = Material.EMERALD;
		}
		
		if (material == null || material == Material.AIR)
		{
			material = Material.EMERALD;
		}
		itemDrop = material;
		set(cfg, "ItemDrop", material.toString());
		
		/* ################ Multipliers ################ */
		{
			ConfigurationSection multiSect;
			sect = getConfigurationSection(cfg, "Multipliers");
			
			/* ################ World Multipliers ################ */
			multiSect = getConfigurationSection(sect, "World");
			
			for (String world : this.enabledWorlds)
			{
				double multi = multiSect.getDouble(world, 1.0);
				
				if (multi != 1.0)
				{
					worldMultipliers.put(world, multi);
				}
				
				set(multiSect, world, multi);
			}
			
			/* ################ Time Multipliers ################ */
			multiSect = getConfigurationSection(sect, "Time");
			
			for (TimeMultiplier time : TimeMultiplier.values())
			{
				timeMultipliers[time.ordinal()] = multiSect.getDouble(time.toString(), 1.0);
				if (timeMultipliers[time.ordinal()] != 1.0)
					usingTimeMulti = true;
				set(multiSect, time.toString(), timeMultipliers[time.ordinal()]);
			}
			
			/* ################ Biome Multipliers ################ */
			multiSect = getConfigurationSection(sect, "Biome");
			
			for (Biome biome : Biome.values())
			{
				biomeMultipliers[biome.ordinal()] = multiSect.getDouble(biome.toString(), 1.0);
				
				if (biomeMultipliers[biome.ordinal()] != 1.0)
					usingBiomeMulti = true;
				
				set(multiSect, biome.toString(), biomeMultipliers[biome.ordinal()]);
			}
			
			/* ################ Permission Multipliers ################ */
			multiSect = getConfigurationSection(sect, "Permission");
			
			Set<String> keys = multiSect.getKeys(false);
			// Setup defaults as an example
			if (keys.size() == 0)
			{
				set(multiSect, "mobmanager_bounty_multiplier_double", 2.0);
				set(multiSect, "mobmanager_bounty_multiplier_triple", 3.0);
				keys = multiSect.getKeys(false);
			}
			
			permissionMultipliers = new ArrayList<PermissionMultiplier>(keys.size());
			for (String perm : keys)
			{
				PermissionMultiplier multi = new PermissionMultiplier(perm.replaceAll("_", "."), multiSect.getDouble(perm, 1.0));
				
				int i;			
				// Make sure the list is ordered from highest to lowest multi
				for (i = 0; i < permissionMultipliers.size(); ++i)
				{
					if (permissionMultipliers.get(i).getMultiplier() < multi.getMultiplier())
					{
						break;
					}
				}
				permissionMultipliers.add(i, multi);
			}
			
			// Set each multiplier from lowest to highest
			for (int i = permissionMultipliers.size() - 1; i >= 0; --i)
			{
				PermissionMultiplier permMulti = permissionMultipliers.get(i);
				
				set(multiSect, permMulti.getPermission().replaceAll("\\.", "_"), permMulti.getMultiplier());
			}
			
			/* ################ AbilitySet Multipliers ################ */
			if (MMComponent.getAbilities().isEnabled())
			{
				multiSect = getConfigurationSection(sect, "AbilitySets");
				
				for (String setName : AbilitySet.getAbilitySetNames())
				{
					double multi = multiSect.getDouble(setName, 1.0);
					set(multiSect, setName, multi);
					
					if (multi != 1.0)
					{
						abilitySetMultipliers.put(setName, multi);
					}
				}		
			}
		}
		
		/* ################ Exploits ################ */
		{
			ConfigurationSection exSect;
			sect = getConfigurationSection(cfg, "Exploits");
			
			/* ################ Cooldown ################ */
			exSect = getConfigurationSection(sect, "Cooldown");
			useCooldown = exSect.getBoolean("Use", false);
			cooldownTimeout = (int) (1000 * Math.abs(exSect.getDouble("Time", 5.0)));
			
			set(exSect, "Use", useCooldown);
			set(exSect, "Time", cooldownTimeout / 1000.0);
		
			/* ################ LoginTimer ################ */
			exSect = getConfigurationSection(sect, "LoginTimer");
			useLoginTimer = exSect.getBoolean("Use", false);
			loginTimeout = (int) (1000 * Math.abs(exSect.getDouble("Time", 10.0)));
			
			set(exSect, "Use", useLoginTimer);
			set(exSect, "Time", loginTimeout / 1000.0);
			
			/* ################ SpawnerProtection ################ */
			exSect = getConfigurationSection(sect, "SpawnerProtection");
			useSpawnerProtection = exSect.getBoolean("Use", false);
			spawnerCap = exSect.getInt("Cap", 2);
			spawnerCappedMulti = exSect.getDouble("Multi", 0.25);
			
			set(exSect, "Use", useSpawnerProtection);
			set(exSect, "Cap", spawnerCap);
			set(exSect, "Multi", spawnerCappedMulti);
			
			/* ################ NearbyMobCap ################ */
			exSect = getConfigurationSection(sect, "NearbyMobCap");
			useNearbyMobCap = exSect.getBoolean("Use", false);
			int range = Math.abs(exSect.getInt("Range", 10));
			nearbyRangeSquared = range * range;
			nearbyMobCap = exSect.getInt("Cap", 10);
			nearbyCappedMulti = exSect.getDouble("Multi", 0.25);
			
			set(exSect, "Use", useNearbyMobCap);
			set(exSect, "Range", range);
			set(exSect, "Cap", nearbyMobCap);
			set(exSect, "Multi", nearbyCappedMulti);
			
			/* ################ DepreciativeReturn ################ */
			exSect = getConfigurationSection(sect, "DepreciativeReturn");
			depreciativeReturnMulti = exSect.getDouble("Multi", 0.5);
			boolean useDepreciativeReturn = exSect.getBoolean("Use", false);
			this.useDepreciativeReturn = depreciativeReturnMulti != 1.0 ? useDepreciativeReturn : false;
			
			set(exSect, "Use", useDepreciativeReturn);
			set(exSect, "Multi", depreciativeReturnMulti);
			
			
			// Setup Exploits data storage
			if (useCooldown || useNearbyMobCap || useSpawnerProtection || useDepreciativeReturn)
			{
				exploitsPlayerData = new HashMap<String, ExploitsPlayerData>();
			}
			else
			{
				exploitsPlayerData = null;
			}
			
			if (useLoginTimer)
			{
				loginTimer = new HashMap<String, Long>();
			}
			else
			{
				loginTimer = null;
			}
		}
		
		/* ################ Bounty Global Config ################ */
		globalCfg = new BountyWorldConfig(cfg, "");
		
		String worldHeader = getResourceAsString("Bounty_WorldConfigHeader.txt");
		
		copyHeader("Bounty GlobalConfig\n" + getResourceAsString("Bounty_ConfigHeader.txt") + worldHeader, cfg);
		saveConfig("", BOUNTY_CONFIG_NAME, cfg);
		
		/* ################ Bounty World Configs ################ */
		for (String worldName : enabledWorlds)
		{
			boolean found = false;
			for (World world : Bukkit.getWorlds())
			{
				if (world.getName().equalsIgnoreCase(worldName))
				{
					found = true;
					worldName = world.getName();
					break;
				}
			}
			
			if (!found)
			{
				MMComponent.getBounties().warning("Failed to find world " + worldName);
				continue;
			}
			
			cfg = getConfig(WORLDS_FOLDER + File.separator + worldName, BOUNTY_CONFIG_NAME);
			BountyWorldConfig worldCfg = new BountyWorldConfig(cfg, WORLDS_FOLDER + File.separator + worldName);
			
			if (worldCfg.useWorldSettings)
				worldConfigs.put(worldName.toLowerCase(), worldCfg);
			
			copyHeader(cfg, "Bounty_WorldConfigHeader.txt", "Bounty World Config\n");
			saveConfig(WORLDS_FOLDER + File.separator + worldName, BOUNTY_CONFIG_NAME, cfg);
		}
	}
	
	public int getWorldCount()
	{
		return enabledWorlds.size();
	}
	
	public BountyWorldConfig getWorldConfig(World world)
	{
		return getWorldConfig(world.getName());
	}
	
	public BountyWorldConfig getWorldConfig(String world)
	{
		world = world.toLowerCase();
		
		if (!enabledWorlds.contains(world))
		{
			return null;
		}
		
		BountyWorldConfig config = worldConfigs.get(world);
		
		return config != null ? config : globalCfg;
	}
	
	public ExploitsPlayerData getPlayerData(Player player)
	{
		ExploitsPlayerData playerData = exploitsPlayerData.get(player.getName());
		
		if (playerData == null)
		{
			playerData = new ExploitsPlayerData();
			exploitsPlayerData.put(player.getName(), playerData);
		}
		
		return playerData;
	}
	
	public void playedLoggedIn(Player player)
	{
		loginTimer.put(player.getName(), System.currentTimeMillis());
	}
	
	public double applyMultipliers(double reward, Player player, LivingEntity entity, ExtendedEntityType type)
	{
		Double multi;
		
		// Prevent exploit and permission multipliers from changing fines
		if (reward > 0.0)
		{
			ExploitsPlayerData playerData = null;
			
			if (useCooldown || useNearbyMobCap || useSpawnerProtection || useDepreciativeReturn)
			{
				playerData = getPlayerData(player);
			
				// If the cooldown has not expired we return 0.0
				if (useCooldown && System.currentTimeMillis() - playerData.getLastKillTime() <= cooldownTimeout)
				{
					return 0.0;
				}
				
				// If the players login cooldown has not expired we return 0.0
				if (useLoginTimer)
				{
					Long loginTime = loginTimer.get(player.getName());
					
					if (loginTime != null)
					{
						if (System.currentTimeMillis() - loginTime <= loginTimeout)
						{
							return 0.0;
						}
						else
						{
							loginTimer.remove(player.getName());
						}
					}
				}
				
	
				
				if (useSpawnerProtection)
				{
					if (spawnerCap < playerData.getSpawnedMobKillCount(entity))
						reward *= spawnerCappedMulti;
				}
				
				if (useNearbyMobCap)
				{
					if (nearbyMobCap < playerData.getNewNearbyKillCount(entity, nearbyRangeSquared))
						reward *= nearbyCappedMulti;
				}
				
				// Apply Depreciative Return
				if (useDepreciativeReturn)
				{
					int count = playerData.getNewKillCount(type);
					while (--count >= 0)
					{
						reward *= depreciativeReturnMulti;
					}
				}
			}
			
			// Apply Permission Multipliers
			for (int i = 0; i < permissionMultipliers.size(); ++i)
			{
				PermissionMultiplier permMulti = permissionMultipliers.get(i);
				
				if (permMulti.hasPermission(player))
				{
					reward *= permMulti.getMultiplier();
					break;
				}
			}
		}
		
		// Apply world multiplier
		if ((multi = worldMultipliers.get(entity.getWorld().getName().toLowerCase())) != null)
		{
			reward *= multi;
		}
		
		// Apply time multiplier
		if (usingTimeMulti)
		{
			reward *= timeMultipliers[TimeMultiplier.valueOf((int) entity.getWorld().getTime()).ordinal()];
		}
		
		// Apply Biome multiplier
		if (usingBiomeMulti)
		{
			Location loc = entity.getLocation(LocationCache.getCachedLocation());
			Biome biome = entity.getWorld().getBiome(loc.getBlockX(), loc.getBlockZ());
			
			reward *= biomeMultipliers[biome.ordinal()];
		}
		
		// Apply AbilitySet Multipliers 
		if (MMComponent.getAbilities().isEnabled())
		{
			String setName = AbilitySet.getMeta(entity);
			if (setName != null)
			{
				if ((multi = abilitySetMultipliers.get(setName)) != null)
				{
					reward *= multi;
				}
			}
		}
		
		if (Math.abs(reward) <= DOUBLE_TOO_SMALL)
			return 0.0;
		
		return reward;
	}
}
