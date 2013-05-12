package com.forgenz.mobmanager.limiter;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.forgenz.mobmanager.MMComponent;
import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.common.config.AbstractConfig;
import com.forgenz.mobmanager.limiter.config.LimiterConfig;
import com.forgenz.mobmanager.limiter.listeners.ChunkListener;
import com.forgenz.mobmanager.limiter.listeners.MobListener;
import com.forgenz.mobmanager.limiter.tasks.MobDespawnTask;
import com.forgenz.mobmanager.limiter.util.AnimalProtection;
import com.forgenz.mobmanager.limiter.world.MMWorld;

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
	public void enable(boolean force)
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
		if (LimiterConfig.enableAnimalDespawning)
		{
			animalProtection = new AnimalProtection();

			Bukkit.getPluginManager().registerEvents(animalProtection, P.p());
			animalProtection.runTaskTimerAsynchronously(P.p(), LimiterConfig.protectedFarmAnimalSaveInterval, LimiterConfig.protectedFarmAnimalSaveInterval);
		}

		info(String.format("Enabled with %d worlds", worlds.size()));
	}
	
	@Override
	public void disable(boolean force)
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
			animalProtection.run();
		}
		
		info("Disabled");
		enabled = false;
	}
	
	@Override
	public LimiterConfig getConfig()
	{
		if (!this.isEnabled())
		{
			throw new IllegalStateException("Config should not be fetched when Limiter is disabled");
		}
		
		return config;
	}
	
	public ConcurrentHashMap<String, MMWorld> getWorlds()
	{
		return worlds;
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
