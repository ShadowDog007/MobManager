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
import org.bukkit.entity.Player;

import ninja.mcknight.bukkit.mobmanager.P;

public class MMCommandPSpawn extends MMCommand
{

	MMCommandPSpawn()
	{
		super(Pattern.compile("pspawn|pspawnset", Pattern.CASE_INSENSITIVE), Pattern.compile("^[a-zA-Z_]+ \\d{1,2} [A-Za-z0-9_]{1,16}$"),
				3, 3);
	}

	@Override
	public void run(CommandSender sender, String maincmd, String[] args)
	{
		if (sender instanceof Player && !sender.hasPermission("mobmanager.pspawn"))
		{
			sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use /mm spawn");
			return;
		}
		
		if (!super.validArgs(sender, maincmd, args))
			return;
		
		Player player = P.p().getServer().getPlayer(args[3]);
		
		if (player == null || !player.isOnline())
		{
			sender.sendMessage(ChatColor.RED + "~No player named " + args[3] + " online");
			return;
		}
		
		int count = Integer.valueOf(args[2]);
		
		if (args[0].equalsIgnoreCase("pspawn"))
			MMCommandSpawn.spawn(sender, args[1], player.getLocation(), count, true);
		else
			MMCommandSpawn.spawnset(sender, args[1], player.getLocation(), count, true);
	}

	@Override
	public String getUsage()
	{
		return "%s/%s %s %s<MobType|SetName> <count> <PlayerName>";
	}

	@Override
	public String getDescription()
	{
		return "Spawns a mob or mob with an abilityset at a given player";
	}

	@Override
	public String getAliases()
	{
		return "pspawn,pspawnset";
	}

}
