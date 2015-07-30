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

package ninja.mcknight.bukkit.mobmanager.limiter;

import java.util.concurrent.ConcurrentHashMap;

import ninja.mcknight.bukkit.mobmanager.limiter.config.LimiterConfig;
import ninja.mcknight.bukkit.mobmanager.limiter.listeners.MobListener;
import org.bukkit.Bukkit;
import org.bukkit.World;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.P;
import ninja.mcknight.bukkit.mobmanager.common.config.AbstractConfig;
import ninja.mcknight.bukkit.mobmanager.limiter.listeners.ChunkListener;
import ninja.mcknight.bukkit.mobmanager.limiter.tasks.MobDespawnTask;
import ninja.mcknight.bukkit.mobmanager.limiter.util.AnimalProtection;
import ninja.mcknight.bukkit.mobmanager.limiter.world.MMWorld;

public class LimiterComponent extends MMComponent
{
	private LimiterConfig config;
	private boolean enabled = false;
	
	public static ConcurrentHashMap<String, MMWorld> worlds = null;
	
	private MobDespawnTask despawner = null;
	
	public AnimalProtection animalProtection = null;
	
	public LimiterComponent(Component c)
	{
		super(c);
	}
	
	@Override
	public boolean isEnabled()
	{
		return enabled;
	}
	
	@Override
	protected boolean initializeConfig()
	{
		if (P.p() == null || !P.p().isEnabled())
		{
			return false;
		}
		
		boolean shouldEnable = P.p().getConfig().getBoolean("EnableLimiter", true);
		AbstractConfig.set(P.p().getConfig(), "EnableLimiter", shouldEnable);
		
		return shouldEnable;
	}
	
	@Override
	public void enable(boolean force) throws IllegalStateException
	{
		// Make sure MobManager is enabled
		if (P.p() == null || !P.p().isEnabled())
		{
			throw new IllegalStateException("MobManager must be enabled to enable the Limiter");
		}
		
		// Check if the Spawner is already enabled
		if (this.isEnabled())
		{
			throw new IllegalStateException("MobManager-Limiter was already enabled");
		}
		
		// Check if we should enable this component
		if (!force && !P.p().getConfig().getBoolean("EnableLimiter", true))
		{
			return;
		}
		
		info("Enabling");
		enabled = true;

		// Load Config
		LimiterConfig config = new LimiterConfig();

		// Setup worlds
		worlds = new ConcurrentHashMap<String, MMWorld>(2, 0.75F, 2);
		if (config.setupWorlds() == 0)
		{
			warning("No valid worlds found");
			return;
		}

		// Register Mob event listeners
		Bukkit.getPluginManager().registerEvents(new MobListener(), P.p());
		// Register Chunk event listener
		Bukkit.getPluginManager().registerEvents(new ChunkListener(), P.p());

		// Start the despawner task
		despawner = new MobDespawnTask();
		despawner.runTaskTimer(P.p(), 1L, LimiterConfig.ticksPerDespawnScan);

		// Setup animal protection
		if (LimiterConfig.enableAnimalDespawning || LimiterConfig.enableAnimalTracking)
		{
			animalProtection = new AnimalProtection();

			Bukkit.getPluginManager().registerEvents(animalProtection, P.p());
			animalProtection.runTaskTimerAsynchronously(P.p(), LimiterConfig.protectedFarmAnimalSaveInterval, LimiterConfig.protectedFarmAnimalSaveInterval);
		}

		info(String.format("Enabled with %d worlds", worlds.size()));
	}
	
	@Override
	public void disable(boolean force) throws IllegalStateException
	{
		// Check if the Spawner was already disabled
		if (!this.isEnabled())
		{
			throw new IllegalStateException("MobManager-Limiter was already disabled");
		}
		
		if (despawner != null)
			despawner.cancel();
		
		if (animalProtection != null)
		{
			animalProtection.cancel();
			try
			{
				animalProtection.run();
			}
			catch (Exception e)
			{
				
			}
		}
		
		info("Disabled");
		enabled = false;
	}
	
	@Override
	public LimiterConfig getConfig() throws IllegalStateException
	{
		if (!this.isEnabled())
		{
			throw new IllegalStateException("Config should not be fetched when Limiter is disabled");
		}
		
		return config;
	}
	
	public void addWorld(MMWorld world)
	{
		worlds.put(world.getWorld().getName().toLowerCase(), world);
	}
	
	public MMWorld[] getWorlds()
	{
		return worlds.values().toArray(new MMWorld[worlds.size()]);
	}
	
	public MMWorld getWorld(World world)
	{
		return getWorld(world.getName());
	}
	
	public MMWorld getWorld(String world)
	{
		return worlds.get(world.toLowerCase());
	}
}
