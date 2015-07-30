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

package ninja.mcknight.bukkit.mobmanager.spawner.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ninja.mcknight.bukkit.mobmanager.common.util.PlayerFinder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import ninja.mcknight.bukkit.mobmanager.common.config.AbstractConfig;
import ninja.mcknight.bukkit.mobmanager.common.util.MiscUtil;
import ninja.mcknight.bukkit.mobmanager.common.util.RandomUtil;

public class Action extends AbstractConfig
{
	private static final Pattern playerPattern = Pattern.compile("{player}", Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
	private static boolean first = false;
	
	private final boolean randomMessage;
	private final ArrayList<String> messages;
	private final ArrayList<String> commands;
	
	private final PlayerFinder.FinderModeConfig messageFinder;
	private final PlayerFinder.FinderModeConfig commandFinder;
	
	public Action(ConfigurationSection cfg)
	{
		this(cfg.getValues(true), false);
		cfg.getParent().set(cfg.getName(), super.getMapCfg());
		
		super.clearCfg();
	}
	
	public Action(Map<String, Object> cfg)
	{
		this(cfg, true);
	}
	
	private Action(Map<String, Object> cfg, boolean clearCfg)
	{
		super.setMapCfg(cfg);
	
		PlayerFinder.FinderModeConfig tmp;
		
		// Setup messages
		tmp = new PlayerFinder.FinderModeConfig(getAndSet("MessageSettings", new LinkedHashMap<String, Object>()));
		
		randomMessage = getAndSet("RandomMessage", false);
		
		List<String> tmplList = MiscUtil.getStringList(getAndSet("Messages", new ArrayList<String>()));
		
		if (tmplList.isEmpty())
		{
			messageFinder = null;
			messages = null;
		}
		else if (tmp.validRange())
		{
			messageFinder = tmp;
			messages = new ArrayList<String>(tmplList);
			for (int i = 0; i < this.messages.size(); ++i)
				messages.set(i, ChatColor.translateAlternateColorCodes('&', messages.get(i)));
		}
		else
		{
			messageFinder = null;
			messages = null;
		}
		
		// Setup commands
		tmp = new PlayerFinder.FinderModeConfig(getAndSet("CommandSettings", new LinkedHashMap<String, Object>()));
		
		tmplList = MiscUtil.getStringList(getAndSet("Commands", new ArrayList<String>()));
		
		if (tmplList.isEmpty())
		{
			commandFinder = null;
			commands = null;
		}
		else if (tmp.validRange())
		{
			commandFinder = tmp.equals(messageFinder) ? messageFinder : tmp;
			commands = new ArrayList<String>(tmplList);
			for (int i = 0; i < this.messages.size(); ++i)
				messages.set(i, ChatColor.translateAlternateColorCodes('&', messages.get(i)));
		}
		else
		{
			commandFinder = null;
			commands = null;
		}
		
		if (clearCfg)
			super.clearCfg();
		
		if (!first)
		{
			if (!required())
				cfg.clear();
		}
		else
			first = false;
	}
	
	public boolean required()
	{
		return messages != null || commands != null;
	}
	
	public void execute(Location location)
	{
		if (messages == null && commands == null)
			return;
		
		ArrayList<Player> nearbyPlayers = new ArrayList<Player>();
		
		if (messages != null)
		{
			PlayerFinder.findNearbyPlayers(location, messageFinder, nearbyPlayers);
			if (randomMessage)
			{
				String message = messages.get(RandomUtil.i.nextInt(messages.size()));
				
				Matcher matcher = playerPattern.matcher(message);
				
				// Check if the player replace string is in the message
				boolean found = matcher.find();
				
				// Send message
				for (Player player : nearbyPlayers)
					player.sendMessage(found ? matcher.replaceAll(player.getName()) : message);
			}
			else
			{
				for (String message : messages)
				{
					Matcher matcher = playerPattern.matcher(message);
					
					// Check if the player replace string is in the message
					boolean found = matcher.find();
					
					// Send message
					for (Player player : nearbyPlayers)
						player.sendMessage(found ? matcher.replaceAll(player.getName()) : message);
				}
			}
		}
		
		if (commands != null)
		{
			if (!commandFinder.equals(messageFinder))
				PlayerFinder.findNearbyPlayers(location, commandFinder, nearbyPlayers);
			
			for (String command : commands)
			{
				Matcher matcher = playerPattern.matcher(command);
				
				// Check if the player replace string is in the message
				if (matcher.find())
				{
					// Execute customised command
					for (Player player : nearbyPlayers)
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), matcher.replaceAll(player.getName()));
				}
				else
				{
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
				}
			}
		}
	}
	
	public static void resetConfigFlag()
	{
		first = true;
	}
}
