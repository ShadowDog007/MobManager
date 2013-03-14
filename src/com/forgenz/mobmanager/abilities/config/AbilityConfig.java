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

package com.forgenz.mobmanager.abilities.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.abilities.abilities.AbilitySet;
import com.forgenz.mobmanager.abilities.util.MiscUtil;
import com.forgenz.mobmanager.common.config.AbstractConfig;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;

public class AbilityConfig extends AbstractConfig
{
	protected final static String WORLDS_FOLDER = "worlds";
	protected final static String ABILITY_CONFIG_NAME = "abilities.yml";
	
	private final HashMap<String, WorldAbilityConfig> worlds = new HashMap<String, WorldAbilityConfig>();
	
	public final HashSet<String> enabledWorlds = new HashSet<String>();
	
	public final boolean limitBonusSpawns;
	
	public final WorldAbilityConfig globalCfg;
	
	public AbilityConfig()
	{	
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
			enabledWorlds.add(world.toUpperCase());
		}
		
		set(cfg, "EnabledWorlds", list);
		
		/* ################ AbilitySets ################ */
		AbilitySet.resetAbilitySets();
		
		List<?> abilitySets = cfg.getList("AbilitySets");
		if (abilitySets == null)
			abilitySets = new ArrayList<Object>();
		set(cfg, "AbilitySets", abilitySets);
			
		for (Object obj : abilitySets)
			AbilitySet.createAbilitySet(MiscUtil.getConfigMap(obj));
		
		/* ################ LimitBonusSpawns ################ */
		limitBonusSpawns = cfg.getBoolean("LimitBonusSpawns");
		set(cfg, "LimitBonusSpawns", limitBonusSpawns);
		
		/* ################ Ability Global Config ################ */
		globalCfg = new WorldAbilityConfig(cfg, "");
		
		String headerVersion = P.p().getDescription().getName() + " Ability %s " + P.p().getDescription().getVersion() + "\n";
		String worldHeader = getResourceAsString("Abilities_WorldConfigHeader.txt");
		
		copyHeader(String.format(headerVersion, "Global Config") + getResourceAsString("Abilities_ConfigHeader.txt") + worldHeader, cfg);
		saveConfig("", ABILITY_CONFIG_NAME, cfg);
		
		/* ################ Ability World Config's ################ */
		headerVersion = String.format(headerVersion, "World Configs");
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
				continue;
			
			cfg = getConfig(WORLDS_FOLDER + File.separator + worldName, ABILITY_CONFIG_NAME);
			WorldAbilityConfig worldCfg = new WorldAbilityConfig(cfg, WORLDS_FOLDER + File.separator + worldName);
			
			if (worldCfg.worldSettingsEnabled())
				worlds.put(worldName, worldCfg);
			
			copyHeader(cfg, "Abilities_WorldConfigHeader.txt", P.p().getDescription().getName() + " Config " + P.p().getDescription().getVersion() + "\n");
			saveConfig(WORLDS_FOLDER + File.separator + worldName, ABILITY_CONFIG_NAME, cfg);
		}
	}
	
	public WorldAbilityConfig getWorldConfig(String world)
	{
		if (!enabledWorlds.contains(world.toUpperCase()))
			return null;
		
		WorldAbilityConfig worldCfg = worlds.get(world);
		
		return worldCfg != null && worldCfg.worldSettingsEnabled() ? worldCfg : globalCfg;
	}
	
	public MobAbilityConfig getMobConfig(String world, ExtendedEntityType mobType, SpawnReason spawnReason)
	{
		WorldAbilityConfig worldCfg = getWorldConfig(world);
		
		if (worldCfg == null)
			return null;
		
		if (spawnReason != null && !worldCfg.enabledSpawnReasons.containsValue(spawnReason.toString()))
			return null;
		
		MobAbilityConfig mobCfg = worldCfg.mobs.get(mobType);
		
		return mobCfg != null ? mobCfg : globalCfg.mobs.get(mobType);
	}
}
