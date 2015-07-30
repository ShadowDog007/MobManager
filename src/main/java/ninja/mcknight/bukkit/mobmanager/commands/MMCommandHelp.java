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

package ninja.mcknight.bukkit.mobmanager.commands;

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
		super(Pattern.compile("help|h|\\?", Pattern.CASE_INSENSITIVE), Pattern.compile("^[ ]{0}|\\d{1,2}$"), 0, 1);
		
		this.commandList = commandList;
	}

	@Override
	public void run(CommandSender sender, String maincmd, String[] args)
	{
		if (sender instanceof Player)
		{
			if (!sender.hasPermission("mobmanager.admin")
					&& !sender.hasPermission("mobmanager.butcher")
					&& !sender.hasPermission("mobmanager.count")
					&& !sender.hasPermission("mobmanager.reload")
					&& !sender.hasPermission("mobmanager.spawn")
					&& !sender.hasPermission("mobmanager.pspawn")
					&& !sender.hasPermission("mobmanager.abilitysetlist")
					&& !sender.hasPermission("mobmanager.mobtypes")
					&& !sender.hasPermission("mobmanager.version")
					&& !sender.hasPermission("mobmanager.debug"))
			{
				sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use /mm help");
				return;
			}
		}
		
		if (!super.validArgs(sender, maincmd, args))
			return;
		
		ArrayList<String> messages = new ArrayList<String>();
		
		final int commandsPerPage = 4;
		int page = args.length == 2 ? Integer.valueOf(args[1]) - 1 : 0;
		
		while(page * commandsPerPage > commandList.size())
			--page;
		
		int count = page * commandsPerPage;
		
		messages.add(ChatColor.GOLD + "------------.:"
				+ ChatColor.DARK_GREEN + "MobManager Help Page "
				+ (page + 1) + "/" + (commandList.size() / commandsPerPage + (commandList.size() % commandsPerPage > 0 ? 1 : 0))
				+ ChatColor.GOLD + ":.------------");
		
		for (;count < (page + 1) * commandsPerPage && count < commandList.size(); ++count)
		{
			MMCommand command = commandList.get(count);
			
			messages.add(String.format(command.getUsage() + ChatColor.YELLOW + " " + command.getDescription(),
					ChatColor.AQUA, maincmd, command.getAliases(), ChatColor.DARK_AQUA));
		}
		
		String[] messageArray = messages.toArray(new String[0]);
		
		sender.sendMessage(messageArray);
	}

	@Override
	public String getUsage()
	{
		return "%s/%s %s %s[Page]";
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
