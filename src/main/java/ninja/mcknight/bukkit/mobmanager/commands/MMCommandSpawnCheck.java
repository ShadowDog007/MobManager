package ninja.mcknight.bukkit.mobmanager.commands;

import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.common.util.LocationCache;
import ninja.mcknight.bukkit.mobmanager.spawner.config.Mob;
import ninja.mcknight.bukkit.mobmanager.spawner.config.Region;

public class MMCommandSpawnCheck extends MMCommand
{
	MMCommandSpawnCheck()
	{
		super(Pattern.compile("checkmobs", Pattern.CASE_INSENSITIVE), Pattern.compile("^.*$"),
				0, 0);
	}

	@Override
	public void run(CommandSender sender, String maincmd, String[] args)
	{
		if (sender instanceof Player && !sender.hasPermission("mobmanager.checkmobs"))
		{
			sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use /mm checkmobs");
			return;
		}
		
		if (!(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.DARK_RED + "You can not use this command from console");
			return;
		}
		
		if (!MMComponent.getSpawner().isEnabled())
		{
			sender.sendMessage(ChatColor.DARK_RED + "The spawner component must be enabled");
			return;
		}
		
		Player player = (Player) sender;
		Location playerLoc = LocationCache.getCachedLocation(player);
		
		List<Region> regionList = MMComponent.getSpawner().getConfig().getSpawnableRegions(playerLoc);
		
		if (regionList.isEmpty())
		{
			player.sendMessage(ChatColor.RED + "Spawning is disabled in this world");
			return;
		}
		
		StringBuilder bldr = new StringBuilder();
		
		Environment worldEnv = playerLoc.getWorld().getEnvironment();
		Biome biome = playerLoc.getWorld().getBiome(playerLoc.getBlockX(), playerLoc.getBlockX());
		
		for (Region region : regionList)
		{
			List<Mob> mobs = region.getMobs(playerLoc.getBlockY(), worldEnv, biome);
			
			player.sendMessage(String.format("%1$sRegion: %2$s%3$s %1$sPri: %2$s%4$d %1$sMobCount: %2$s%5$d", ChatColor.DARK_GREEN, ChatColor.AQUA, region.name, region.priority, mobs.size()));
			
			for (Mob mob : mobs)
			{
				bldr.append("%1$s-C:%2$s").append(mob.spawnChance).append("%1$s,T:%2$s").append(mob.getMobType());
				
				if (mob.getAbilitySet() != null)
					bldr.append("%1$s,A:%2$s").append(mob.getAbilitySet());
				
				if (mob.getRequirements() != null)
					bldr.append("%1$s,R:%2$s").append(mob.getRequirements().toString(true));
				player.sendMessage(String.format(bldr.toString(), ChatColor.DARK_GREEN, ChatColor.AQUA));
				
				bldr.delete(0, bldr.length());
			}
		}
	}

	@Override
	public String getUsage()
	{
		return "%s/%s %s%s";
	}

	@Override
	public String getDescription()
	{
		return "Checks mobs which can spawn in this location";
	}

	@Override
	public String getAliases()
	{
		return "checkmobs";
	}
}
