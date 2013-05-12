package com.forgenz.mobmanager.spawner;

import com.forgenz.mobmanager.MMComponent;
import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.common.config.AbstractConfig;
import com.forgenz.mobmanager.spawner.config.SpawnerConfig;

public class SpawnerComponent extends MMComponent
{
	private SpawnerConfig config;
	private boolean enabled = false;
	
	public SpawnerComponent(Component c)
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
		
		boolean shouldEnable = P.p().getConfig().getBoolean("EnableSpawner", true);
		//AbstractConfig.set(P.p().getConfig(), "EnableSpawner", shouldEnable);
		
		return shouldEnable;
	}
	
	@Override
	public void enable(boolean force)
	{		
		// Make sure MobManager is enabled
		if (P.p() == null || !P.p().isEnabled())
		{
			throw new IllegalStateException("MobManager must be enabled to enable the Spawner");
		}
		
		// Check if the Spawner is already enabled
		if (this.isEnabled())
		{
			throw new IllegalStateException("MobManager-Spawner was already enabled");
		}
		
		// Check if we should enable this component
		if (!force && !P.p().getConfig().getBoolean("EnableSpawner", true))
		{
			return;
		}
		
		if (null == null)
		{
			//info("Spawner component is not implemented, disabled");
			return;
		}
		
		info("Enabling");
		enabled = true;
		
		// Make sure the Limiter Component is enabled
		if (!Component.LIMITER.i().isEnabled())
		{
			Component.SPAWNER.warning("Limiter must be enabled first");
			return;
		}

		// Load the config
		config = new SpawnerConfig();
	}

	@Override
	public void disable(boolean force)
	{
		// Check if the Spawner was already disabled
		if (!this.isEnabled())
		{
			throw new IllegalStateException("MobManager-Spawner was already disabled");
		}
	}
	
	@Override
	public SpawnerConfig getConfig()
	{
		if (!this.isEnabled())
		{
			Component.SPAWNER.warning("Config should not be fetched when Spawner is disabled");
		}
		
		return config;
	}
}
