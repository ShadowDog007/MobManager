package ninja.mcknight.bukkit.mobmanager.module.limiter.config;

import lombok.Data;
import ninja.mcknight.bukkit.bknightcore.config.IBKConfig;

@Data
public class LimiterConfig implements IBKConfig {

	private int ticksPerRecount, ticksPerDespawnScan;
	
	@Override
	public void validate() {
		// TODO Auto-generated method stub
		
	}

}
