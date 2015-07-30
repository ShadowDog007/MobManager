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

package ninja.mcknight.bukkit.mobmanager.abilities.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.P;
import ninja.mcknight.bukkit.mobmanager.abilities.abilities.AbilitySet;
import ninja.mcknight.bukkit.mobmanager.common.config.AbstractConfig;
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;

public class AbilityConfig extends AbstractConfig
{
	private static AbilityConfig abilityCfg = null;
	public static AbilityConfig i()
	{
		return abilityCfg;
	}
	
	protected final static String ABILITY_CONFIG_NAME = "abilities.yml";
	
	private final HashMap<String, AbilityWorldConfig> worlds = new HashMap<String, AbilityWorldConfig>();
	
	public final HashSet<String> enabledWorlds = new HashSet<String>();
	
	public final boolean limitBonusSpawns;
	public final int bonusSpawnRange;
	public final int bonusSpawnHeightRange;
	public final int commandPSpawnMinRange;
	
	public final boolean useCircleLocationGeneration;
	public final boolean radiusBonusSpawn;
	public final boolean commandSpawnUseRadius;
	public final boolean commandPSpawnUseRadius;
	public final boolean commandPSpawnRadiusAllowCenter;
	
	public final AbilityWorldConfig globalCfg;
	
	public AbilityConfig()
	{
		abilityCfg = this;
		FileConfiguration cfg = getConfig("", AbilityConfig.ABILITY_CONFIG_NAME);
		
		/* ################ EnabledWorlds ################ */
		List<String> list = cfg.getStringList("EnabledWorlds");
		
		if (list == null || list.size() == 0)
		{
			list = new ArrayList<String>();
			
			for (World world : P.p().getServer().getWorlds())
			{
				list.add(world.getName());
			}
		}
		
		for (String world : list)
		{
			enabledWorlds.add(world.toLowerCase());
		}
		
		set(cfg, "EnabledWorlds", list);
		
		/* ################ LimitBonusSpawns ################ */
		limitBonusSpawns = cfg.getBoolean("LimitBonusSpawns", true);
		set(cfg, "LimitBonusSpawns", limitBonusSpawns);
		
		/* ################ SpawnRanges ################ */
		bonusSpawnRange = cfg.getInt("BonusSpawnRange", 16);
		set(cfg, "BonusSpawnRange", bonusSpawnRange);
		
		bonusSpawnHeightRange = cfg.getInt("BonusSpawnHeightRange", 4);
		set(cfg, "BonusSpawnHeightRange", bonusSpawnHeightRange);
		
		commandPSpawnMinRange = cfg.getInt("CommandPSpawnMinRange", 8);
		set(cfg, "CommandPSpawnMinRange", commandPSpawnMinRange);
		
		useCircleLocationGeneration = cfg.getBoolean("UseCircleLocationGeneration", false);
		set(cfg, "UseCircleLocationGeneration", useCircleLocationGeneration);
		
		// Radius usage toggles
		radiusBonusSpawn = cfg.getBoolean("BonusSpawnUseRadius", false);
		set(cfg, "BonusSpawnUseRadius", radiusBonusSpawn);
		
		commandSpawnUseRadius = cfg.getBoolean("CommandSpawnUseRadius", false);
		set(cfg, "CommandSpawnUseRadius", commandSpawnUseRadius);
		
		commandPSpawnUseRadius = cfg.getBoolean("CommandPSpawnUseRadius", true);
		set(cfg, "CommandPSpawnUseRadius", commandPSpawnUseRadius);
		
		// Accept center spawn
		commandPSpawnRadiusAllowCenter = cfg.getBoolean("CommandPSpawnRadiusAllowCenter", false);
		set(cfg, "CommandPSpawnRadiusAllowCenter", commandPSpawnRadiusAllowCenter);
		
		/* ################ AbilitySets ################ */
		AbilitySet.resetAbilitySets();
		
		ConfigurationSection abilitySets = cfg.getConfigurationSection("AbilitySets");
		if (abilitySets == null)
			abilitySets = cfg.createSection("AbilitySets");
		set(cfg, "AbilitySets", abilitySets);
			
		for (String setName : abilitySets.getKeys(false))
		{
			ConfigurationSection as = abilitySets.getConfigurationSection(setName);
			if (as == null)
				as = abilitySets.createSection(setName);
			AbilitySet.createAbilitySet(as);
		}
		
		/* ################ Ability Global Config ################ */
		globalCfg = new AbilityWorldConfig(cfg, "");
		
		String worldHeader = getResourceAsString("Abilities_WorldConfigHeader.txt");
		
		copyHeader("Ability Global Config\n" + getResourceAsString("Abilities_ConfigHeader.txt") + worldHeader, cfg);
		saveConfig("", ABILITY_CONFIG_NAME, cfg);
		
		/* ################ Ability World Config's ################ */
		for (String worldName : list)
		{
			boolean found = false;
			for (World world : P.p().getServer().getWorlds())
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
				MMComponent.getAbilities().warning("Failed to find world " + worldName);
				continue;
			}
			
			cfg = getConfig(WORLDS_FOLDER + File.separator + worldName, ABILITY_CONFIG_NAME);
			AbilityWorldConfig worldCfg = new AbilityWorldConfig(cfg, WORLDS_FOLDER + File.separator + worldName);
			
			if (worldCfg.worldSettingsEnabled())
				worlds.put(worldName.toLowerCase(), worldCfg);
			
			copyHeader(cfg, "Abilities_WorldConfigHeader.txt", "Ability World Config\n");
			saveConfig(WORLDS_FOLDER + File.separator + worldName, ABILITY_CONFIG_NAME, cfg);
		}
	}
	
	public AbilityWorldConfig getWorldConfig(String world)
	{
		world = world.toLowerCase();
		if (!enabledWorlds.contains(world))
			return null;
		
		AbilityWorldConfig worldCfg = worlds.get(world);
		
		return worldCfg != null && worldCfg.worldSettingsEnabled() ? worldCfg : globalCfg;
	}
	
	public MobAbilityConfig getMobConfig(String world, ExtendedEntityType mobType, SpawnReason spawnReason)
	{
		AbilityWorldConfig worldCfg = getWorldConfig(world);
		
		if (worldCfg == null)
			return null;
		
		if (spawnReason != null && !worldCfg.enabledSpawnReasons.containsValue(spawnReason.toString()))
			return null;
		
		MobAbilityConfig mobCfg = worldCfg.mobs.get(mobType);
		
		return mobCfg != null ? mobCfg : globalCfg.mobs.get(mobType);
	}
}
