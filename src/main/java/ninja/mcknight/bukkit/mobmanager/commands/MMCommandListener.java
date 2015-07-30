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

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MMCommandListener implements CommandExecutor
{
	private static ArrayList<MMCommand> commands = null;
	
	static void registerCommand(MMCommand command)
	{
		commands.add(command);
	}
	
	public MMCommandListener()
	{
		commands = new ArrayList<MMCommand>();
		
		// Create Command objects
		new MMCommandHelp(commands);
		new MMCommandCount();
		new MMCommandReload();
		new MMCommandButcher();
		new MMCommandSpawn();
		new MMCommandPSpawn();
		new MMCommandAbilitySetList();
		new MMCommandMobTypes();
		new MMCommandSaveItem();
		new MMCommandSpawnCheck();
		new MMCommandVersion();
		new MMCommandDebug();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (args.length >= 1)
		{
			for (MMCommand mmcommand : commands)
			{
				if (mmcommand.isCommand(args[0].trim()))
				{
					mmcommand.run(sender, label, args);
					return true;
				}
			}
			
			sender.sendMessage(ChatColor.RED + "Sub-Command does not exist");
		}
		else
		{
			sender.sendMessage(ChatColor.DARK_GREEN + "Run /mm help for sub-commands");
		}
		return true;
	}

}
