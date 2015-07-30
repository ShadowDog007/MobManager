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

package ninja.mcknight.bukkit.mobmanager.bounty;

import net.milkbowl.vault.economy.Economy;

import ninja.mcknight.bukkit.mobmanager.P;
import ninja.mcknight.bukkit.mobmanager.bounty.config.BountyConfig;
import ninja.mcknight.bukkit.mobmanager.bounty.config.BountyType;
import ninja.mcknight.bukkit.mobmanager.bounty.listeners.BountyDeathListener;
import ninja.mcknight.bukkit.mobmanager.bounty.listeners.BountyLoginListener;
import ninja.mcknight.bukkit.mobmanager.bounty.listeners.BountySpawnListener;
import ninja.mcknight.bukkit.mobmanager.common.config.AbstractConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import ninja.mcknight.bukkit.mobmanager.MMComponent;

public class BountyComponent extends MMComponent
{
	private Economy economy;
	private BountyConfig config;
	private boolean enabled = false;
	
	public BountyComponent(Component c)
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
		
		boolean shouldEnable = P.p().getConfig().getBoolean("EnableBounty", false);
		AbstractConfig.set(P.p().getConfig(), "EnableBounty", shouldEnable);
		
		return shouldEnable;
	}

	@Override
	public void enable(boolean force) throws IllegalStateException
	{
		// Make sure MobManager is enabled
		if (P.p() == null || !P.p().isEnabled())
		{
			throw new IllegalStateException("MobManager must be enabled to enable Bounties");
		}
		
		// Check if the Spawner is already enabled
		if (this.isEnabled())
		{
			throw new IllegalStateException("MobManager-Bounty was already enabled");
		}
		
		// Check if we should enable this component
		if (!force && !P.p().getConfig().getBoolean("EnableBounty", true))
		{
			return;
		}
		
		info("Enabling");
		enabled = true;
		
		config = new BountyConfig();
		
		if (config.bountyType == BountyType.MONEY && !setupEconomy())
		{
			warning("Requires Vault to use BountyType: MONEY");
			disable(true);
		}

		// Register event listeners
		Bukkit.getPluginManager().registerEvents(new BountyDeathListener(), P.p());
		if (config.useSpawnerProtection)
			Bukkit.getPluginManager().registerEvents(new BountySpawnListener(), P.p());
		if (config.useLoginTimer)
			Bukkit.getPluginManager().registerEvents(new BountyLoginListener(), P.p());

		info(String.format("Enabled with %d worlds", config.getWorldCount()));
	}

	@Override
	public void disable(boolean force) throws IllegalStateException
	{
		if (!this.isEnabled())
		{
			throw new IllegalStateException("MobManager-Bounty was already disabled");
		}
		
		economy = null;
		
		info("Disabled");
		enabled = false;
	}

	@Override
	public BountyConfig getConfig() throws IllegalStateException
	{
		if (!this.isEnabled())
		{
			throw new IllegalStateException("Config should nor be fetched when Bounty is disabled");
		}
		
		return config;
	}
	
	public Economy getEconomy()
	{
		return economy;
	}
	
	private boolean setupEconomy()
	{
		try
		{
			RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

			if (economyProvider != null)
			{
				economy = economyProvider.getProvider();
			}

			return (economy != null);
		}
		catch (NoClassDefFoundError e)
		{
			return false;
		}
	}
}
