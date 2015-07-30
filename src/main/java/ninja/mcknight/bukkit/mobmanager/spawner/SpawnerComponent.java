package ninja.mcknight.bukkit.mobmanager.spawner;

import ninja.mcknight.bukkit.mobmanager.common.config.AbstractConfig;
import ninja.mcknight.bukkit.mobmanager.spawner.config.Action;
import ninja.mcknight.bukkit.mobmanager.spawner.config.SpawnRequirements;
import ninja.mcknight.bukkit.mobmanager.spawner.config.SpawnerConfig;
import ninja.mcknight.bukkit.mobmanager.spawner.listener.PlayerListener;
import ninja.mcknight.bukkit.mobmanager.spawner.tasks.spawnfinder.SpawnFinder;
import org.bukkit.Bukkit;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.P;

public class SpawnerComponent extends MMComponent
{
	public static final String SPAWNER_CONFIG_NAME = "spawner.yml";
	
	private SpawnerConfig config;
	private boolean enabled = false;
	
	private SpawnFinder spawnFinder;
	
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
		
		boolean shouldEnable = P.p().getConfig().getBoolean("EnableSpawner", false);
		AbstractConfig.set(P.p().getConfig(), "EnableSpawner", shouldEnable);
		
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
		
		info("Enabling");
		
		// Make sure the Limiter Component is enabled
		if (!Component.LIMITER.i().isEnabled())
		{
			Component.SPAWNER.warning("Limiter must be enabled first");
			return;
		}
		
		enabled = true;
		
		// Make sure Requirements/Actions appear in the config at least once
		SpawnRequirements.resetConfigFlag();
		Action.resetConfigFlag();

		// Load the config
		config = new SpawnerConfig();
		
		// Create the spawn finder
		spawnFinder = new SpawnFinder();
		
		// Register the player listener
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), P.p());
	}

	@Override
	public void disable(boolean force)
	{
		// Check if the Spawner was already disabled
		if (!this.isEnabled())
		{
			throw new IllegalStateException("MobManager-Spawner was already disabled");
		}
		
		config = null;
		
		spawnFinder.cancel();
		spawnFinder = null;
		
		enabled = false;
		info("Disabled");
	}
	
	@Override
	public SpawnerConfig getConfig()
	{
		if (!this.isEnabled())
		{
			throw new IllegalStateException("Config should not be fetched when Spawner is disabled");
		}
		
		return config;
	}
	
	public SpawnFinder getSpawnFinder()
	{
		return spawnFinder;
	}
}
