package ninja.mcknight.bukkit.mobmanager.plugin;

import java.util.Arrays;

import org.bukkit.command.PluginCommand;

import lombok.Getter;
import ninja.mcknight.bukkit.bknightcore.command.BKCommandManager;
import ninja.mcknight.bukkit.bknightcore.plugin.BKPlugin;
import ninja.mcknight.bukkit.mobmanager.module.abilities.Abilities;
import ninja.mcknight.bukkit.mobmanager.module.bounty.Bounty;
import ninja.mcknight.bukkit.mobmanager.module.limiter.Limiter;
import ninja.mcknight.bukkit.mobmanager.module.spawner.Spawner;

public class MobManager extends BKPlugin {

	@Getter public BKCommandManager commandManager;
	
	public MobManager() {
		this.commandManager = new BKCommandManager();
		
		super.registerModule(new Limiter(this));
		super.registerModule(new Abilities(this));
		super.registerModule(new Bounty(this));
		super.registerModule(new Spawner(this));
	}
	
	@Override
	public void beforeEnable() {
		PluginCommand command = super.getCommand("mobmanager");
		command.setExecutor(this.commandManager = new BKCommandManager());
		command.setAliases(Arrays.asList("mm"));
	}

}
