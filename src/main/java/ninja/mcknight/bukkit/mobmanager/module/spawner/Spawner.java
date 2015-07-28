package ninja.mcknight.bukkit.mobmanager.module.spawner;

import ninja.mcknight.bukkit.bknightcore.plugin.BKModule;
import ninja.mcknight.bukkit.bknightcore.plugin.config.BKModuleConfig;
import ninja.mcknight.bukkit.mobmanager.plugin.MobManager;

public class Spawner extends BKModule {

	public Spawner(MobManager plugin) {
		super(plugin, "Spawner");
	}
	
	@Override
	public BKModuleConfig getDefaultModuleConfig() {
		BKModuleConfig cfg = new BKModuleConfig();
		cfg.setEnabled(true);
		return cfg;
	}
	
	@Override
	protected void onEnable() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onDisable() {
		// TODO Auto-generated method stub
		
	}

}
