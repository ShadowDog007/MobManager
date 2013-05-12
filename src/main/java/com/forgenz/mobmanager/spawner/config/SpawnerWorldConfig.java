package com.forgenz.mobmanager.spawner.config;

import java.util.HashSet;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.forgenz.mobmanager.common.config.AbstractConfig;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;

public class SpawnerWorldConfig extends AbstractConfig
{
	private final HashSet<SpawnPointMobConfig> mobConfig;
	
	protected SpawnerWorldConfig(FileConfiguration cfg, String folder)
	{
		mobConfig = new HashSet<SpawnPointMobConfig>();
		
		ConfigurationSection mobCfg = cfg.getConfigurationSection("Mobs");
		if (mobCfg == null)
		{
			mobCfg = cfg.createSection("Mobs");
		}
		
		ExtendedEntityType[] values = ExtendedEntityType.values();
		for (String key : mobCfg.getKeys(false))
		{
			if (!mobCfg.isConfigurationSection(key))
				continue;
			
			for (ExtendedEntityType type : values)
			{
				if (type.toString().equalsIgnoreCase(key))
				{
					SpawnPointMobConfig mob = null; //new SpawnPointMobConfig(type, mobCfg.getConfigurationSection(key));
					
					if (mob.isValid())
					{
						mobConfig.add(mob);
					}
					break;
				}
			}
		}
	}
	
}
