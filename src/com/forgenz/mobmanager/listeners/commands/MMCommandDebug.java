package com.forgenz.mobmanager.listeners.commands;

import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.world.MMChunk;
import com.forgenz.mobmanager.world.MMCoord;
import com.forgenz.mobmanager.world.MMLayer;
import com.forgenz.mobmanager.world.MMWorld;

public class MMCommandDebug extends MMCommand
{

	MMCommandDebug()
	{
		super(Pattern.compile("debug", Pattern.CASE_INSENSITIVE), Pattern.compile("^.*$"), 0, 0);
	}

	@Override
	public void run(CommandSender sender, String maincmd, String[] args)
	{
		if (sender instanceof Player && !sender.hasPermission("mobmanager.debug"))
		{
			sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use /mm debug");
			return;
		}
		
		if (!super.validArgs(sender, maincmd, args))
			return;
		
		int numPlayers = 0;
		int numPlayersLayers = 0;
		
		for (MMWorld world : P.worlds.values())
		{			
			for (Entry<MMCoord, MMChunk> e : world.getChunks())
			{
				MMChunk chunk = e.getValue();
				
				numPlayers += chunk.getNumPlayers();
				
				if (chunk.getNumPlayers() > 0)
					sender.sendMessage(String.format("%d players found in chunk (%d,%d)", chunk.getNumPlayers(), chunk.getCoord().getX(), chunk.getCoord().getZ()));
				
				for (MMLayer layer : chunk.getLayers())
				{
					numPlayersLayers += layer.getNumPlayers();
					if (layer.getNumPlayers() > 0)
					{
						if (chunk.getNumPlayers() <= 0)
							sender.sendMessage("ERROR, Players found in layer, but not its chunk");
						sender.sendMessage(String.format("%d players found in layer (%d-%d):(%d,%d)", layer.getNumPlayers(), layer.getMinY(), layer.getMaxY(), chunk.getCoord().getX(), chunk.getCoord().getZ()));
					}
				}
			}
		}
		
		sender.sendMessage(String.format("Found %d players, and %d players in layers", numPlayers, numPlayersLayers));
	}

	@Override
	public String getUsage()
	{
		return "%s/%s %s%s";
	}

	@Override
	public String getDescription()
	{
		return "Used to debug MobManager";
	}

	@Override
	public String getAliases()
	{
		return "debug";
	}
}
