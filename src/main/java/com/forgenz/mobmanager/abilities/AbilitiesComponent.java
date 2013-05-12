package com.forgenz.mobmanager.abilities;

import org.bukkit.Bukkit;

import com.forgenz.mobmanager.MMComponent;
import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.abilities.abilities.AbilitySet;
import com.forgenz.mobmanager.abilities.config.AbilityConfig;
import com.forgenz.mobmanager.abilities.listeners.AbilitiesMobListener;
import com.forgenz.mobmanager.common.config.AbstractConfig;

public class AbilitiesComponent extends MMComponent
{
	private AbilityConfig config;
	private boolean enabled = false;
	
	public AbilitiesComponent(Component c)
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
		
		boolean shouldEnable = P.p().getConfig().getBoolean("EnableAbilities", true);
		AbstractConfig.set(P.p().getConfig(), "EnableAbilities", shouldEnable);
		
		return shouldEnable;
	}
	
	@Override
	public void enable(boolean force)
	{
		// Make sure MobManager is enabled
		if (P.p() == null || !P.p().isEnabled())
		{
			throw new IllegalStateException("MobManager must be enabled to enable Abilities");
		}
		
		// Check if the Spawner is already enabled
		if (this.isEnabled())
		{
			throw new IllegalStateException("MobManager-Abilities was already enabled");
		}
		
		// Check if we should enable this component
		if (!force && !P.p().getConfig().getBoolean("EnableAbilities", false))
		{
			return;
		}
		
		info("Enabling");
		enabled = true;

		// Load the configuration
		config = new AbilityConfig();
		
		// Register Mob event listeners
		Bukkit.getPluginManager().registerEvents(new AbilitiesMobListener(), P.p());
		
		info(String.format("Enabled with %d worlds and %d AbilitySets", config.enabledWorlds.size(), AbilitySet.getNumAbilitySets() - 1));
	}

	@Override
	public void disable(boolean force)
	{
		// Check if the Spawner was already disabled
		if (!this.isEnabled())
		{
			throw new IllegalStateException("MobManager-Abilities was already disabled");
		}
		
		info("Disabled");
		enabled = false;
	}
	
	@Override
	public AbilityConfig getConfig()
	{
		if (!this.isEnabled())
		{
			throw new IllegalStateException("Config should not be fetched when Abilities is disabled");
		}
		
		return config;
	}
}
