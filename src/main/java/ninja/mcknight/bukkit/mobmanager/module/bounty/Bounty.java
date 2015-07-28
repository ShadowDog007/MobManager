package ninja.mcknight.bukkit.mobmanager.module.bounty;

import ninja.mcknight.bukkit.bknightcore.plugin.BKModule;
import ninja.mcknight.bukkit.bknightcore.plugin.config.BKModuleConfig;
import ninja.mcknight.bukkit.mobmanager.plugin.MobManager;

public class Bounty extends BKModule {

	public Bounty(MobManager plugin) {
		super(plugin, "Bounty");
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
