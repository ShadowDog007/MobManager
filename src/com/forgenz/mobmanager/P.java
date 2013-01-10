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

package com.forgenz.mobmanager;

import java.io.IOException;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.forgenz.mobmanager.listeners.ChunkListener;
import com.forgenz.mobmanager.listeners.MobListener;
import com.forgenz.mobmanager.listeners.PlayerListener;
import com.forgenz.mobmanager.listeners.commands.MMCommandListener;
import com.forgenz.mobmanager.world.MMWorld;

/**
 * <b>MobManager</b> </br>
 * MobManager aims to reduce the number of unnecessary mob spawns </br>
 * 
 * @author Michael McKnight (ShadowDog007)
 *
 */
public class P extends JavaPlugin
{
	public static P p = null;
	public static FileConfiguration cfg = null;
	
	public static HashMap<String, MMWorld> worlds = null;
	
	private MobDespawnTask despawner = null;

	@Override
	public void onLoad()
	{
	}

	@Override
	public void onEnable()
	{
		p = this;
		cfg = getConfig();
		
		// Start Metrics gathering
		try
		{
			Metrics metrics = new Metrics(this);
			metrics.start();
		}
		catch (IOException e)
		{
			getLogger().info("Failed to start metrics gathering..  :(");
		}
		
		// Load config
		Config config = new Config();

		// Setup worlds
		worlds = new HashMap<String, MMWorld>();
		if (config.setupWorlds() == 0)
		{
			getLogger().warning("No valid worlds found");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		// Register Mob event listener
		getServer().getPluginManager().registerEvents(new MobListener(), this);
		// Register Player event listener
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		// Register Chunk event listener
		getServer().getPluginManager().registerEvents(new ChunkListener(), this);
		
		// Register MobManager command
		getCommand("mm").setExecutor(new MMCommandListener());
		
		// Start the despawner task
		despawner = new MobDespawnTask();
		despawner.runTaskTimer(this, Config.ticksPerDespawnScan, Config.ticksPerDespawnScan);
		
		getLogger().info("v" + getDescription().getVersion() + " ennabled with " + worlds.size() + " worlds");
		// And we are done :D
	}

	@Override
	public void onDisable()
	{
		// This has not worked for me in the past..
		getServer().getScheduler().cancelTasks(this);
		// Soo....
		despawner.cancel();
		
		p = null;
		cfg = null;
		worlds = null;
	}
}
