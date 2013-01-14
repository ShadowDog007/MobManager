package com.forgenz.mobmanager.listeners.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MMCommandHelp extends MMCommand
{
	private List<MMCommand> commandList;
	MMCommandHelp(List<MMCommand> commandList)
	{
		super(Pattern.compile("help|h|\\?", Pattern.CASE_INSENSITIVE), Pattern.compile("^.*$"), 0, 0);
		
		this.commandList = commandList;
	}

	@Override
	public void run(CommandSender sender, String maincmd, String[] args)
	{
		if (sender instanceof Player)
		{
			if (!sender.hasPermission("mobmanager.butcher") && !sender.hasPermission("mobmanager.count") && !sender.hasPermission("mobmanager.reload") && !sender.hasPermission("mobmanager.debug"))
			{
				sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use /mm help");
				return;
			}
		}
		
		if (!super.validArgs(sender, maincmd, args))
			return;
		
		ArrayList<String> messages = new ArrayList<String>();
		
		for (MMCommand command : commandList)
		{
			messages.add(String.format(command.getUsage() + ChatColor.YELLOW + " " + command.getDescription(),
					ChatColor.AQUA, maincmd, command.getAliases(), ChatColor.DARK_AQUA));
		}
		
		String[] messageArray = messages.toArray(new String[0]);
		
		sender.sendMessage(messageArray);
	}

	@Override
	public String getUsage()
	{
		return "%s/%s %s%s";
	}

	@Override
	public String getDescription()
	{
		return "Shows command usage";
	}

	@Override
	public String getAliases()
	{
		return "help,h,?";
	}

}
