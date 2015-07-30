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

import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

abstract class MMCommand
{
	protected final Pattern aliases;
	protected final Pattern argFormat;
	
	protected final int minArgs;
	protected final int maxArgs;
	
	MMCommand(Pattern aliases, Pattern argFormat, int minArgs, int maxArgs)
	{
		this.aliases = aliases;
		this.argFormat = argFormat;
		
		this.minArgs = minArgs;
		this.maxArgs = maxArgs;
		
		MMCommandListener.registerCommand(this);
	}
	
	public boolean isCommand(String command)
	{
		return aliases.matcher(command).matches();
	}

	public abstract void run(CommandSender sender, String maincmd, String[] args);

	protected boolean validArgs(CommandSender sender, String maincmd, String[] args)
	{
		String[] messages = null;
		if (args.length - 1 < minArgs || args.length - 1 > maxArgs)
		{
			messages = new String[2];

			messages[0] = ChatColor.RED + "Too " + (args.length - 1 < minArgs ? "few" : "many") + " arguments." + ChatColor.YELLOW + " Use like this:";
			messages[1] = String.format(getUsage(), ChatColor.AQUA.toString(), maincmd, args[0], ChatColor.DARK_AQUA.toString());

			sender.sendMessage(messages);
			return false;
		}
		
		String argString = "";
		for (int i = 1; i < args.length; ++i)
		{
			args[i] = args[i].trim();
			if (argString.length() != 0)
				argString += " ";
			argString += args[i];
		}
		
		if (!argFormat.matcher(argString).matches())
		{
			messages = new String[2];

			messages[0] = ChatColor.RED + "Invalid arguments." + ChatColor.YELLOW + " Use like this:";
			messages[1] = String.format(getUsage(), ChatColor.AQUA.toString(), maincmd, args[0], ChatColor.DARK_AQUA.toString());

			sender.sendMessage(messages);
			return false;
		}
		return true;
	}
	
	public abstract String getAliases();
	public abstract String getUsage();
	public abstract String getDescription();
}
