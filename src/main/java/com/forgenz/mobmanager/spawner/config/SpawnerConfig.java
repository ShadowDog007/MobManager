package com.forgenz.mobmanager.spawner.config;

import org.bukkit.configuration.file.FileConfiguration;

import com.forgenz.mobmanager.common.config.AbstractConfig;

public class SpawnerConfig extends AbstractConfig
{
	
	private static final String SPAWNER_CONFIG_NAME = "spawner.yml";
	
	private static SpawnerConfig spawnerCfg;
	public static SpawnerConfig i()
	{
		return spawnerCfg;
	}
	
	public final boolean useCircleLocationGeneration;
	
	public SpawnerConfig()
	{
		spawnerCfg = this;
		FileConfiguration cfg = getConfig("", SpawnerConfig.SPAWNER_CONFIG_NAME);
		
		this.useCircleLocationGeneration = cfg.getBoolean("UseCircleLocationGeneration", false);
		set(cfg, "UseCircleLocationGeneration", this.useCircleLocationGeneration);
	}
}
