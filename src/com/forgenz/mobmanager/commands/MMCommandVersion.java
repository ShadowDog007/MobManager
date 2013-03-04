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

package com.forgenz.mobmanager.commands;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.forgenz.mobmanager.P;

public class MMCommandVersion extends MMCommand
{

	MMCommandVersion()
	{
		super(Pattern.compile("^version$", Pattern.CASE_INSENSITIVE), Pattern.compile("^.*$"), 0, 0);
	}
	
	private final Pattern versionStringSplit = Pattern.compile("\\|");
	private final Pattern dotSplit = Pattern.compile("\\.");

	@Override
	public void run(final CommandSender sender, String maincmd, String[] args)
	{
		if (sender instanceof Player && !sender.hasPermission("mobmanager.version"))
		{
			sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use /mm version");
			return;
		}
		
		if (!super.validArgs(sender, maincmd, args))
			return;
		
		final String versionString = P.p().getDescription().getVersion();
		
		sender.sendMessage(ChatColor.DARK_GREEN + P.p().getDescription().getName() + " version " + versionString);
		
		if (!P.p().isVersionCheckEnabled())
		{
			sender.sendMessage(ChatColor.DARK_GREEN + "Turn on EnableVersionCheck to allow checking for a new version");
			return;
		}
		
		P.p().getServer().getScheduler().runTaskAsynchronously(P.p(), new Runnable()
		{

			@Override
			public void run()
			{
				URL url;
				InputStream is = null;
				DataInputStream dis;
				byte[] chars = new byte[32];
				int numChars;
				
				String update = "";

				try
				{
				    url = new URL("http://forgenz.com/MobManager_Version.txt");
				    is = url.openStream();
				    dis = new DataInputStream(new BufferedInputStream(is));

				    numChars = dis.read(chars);
				    
				    for (int i = 0; i < numChars; ++i)
				    	update += (char) chars[i];
				}
				catch (Exception e)
				{
				}
				finally
				{
					try
					{
						is.close();
					}
					catch (IOException e)
					{
				    }
				}
				
				if (update.length() != 0)
				{
					String[] split = versionStringSplit.split(update);
					
					if (split.length != 2)
						return;
					
					String[] versionL = dotSplit.split(split[0]);
					String[] versionC = dotSplit.split(versionString);
					
					int majorL = Integer.valueOf(versionL[0]);
					int majorC = Integer.valueOf(versionC[0]);
					
					int minorL = Integer.valueOf(versionL[1].replaceAll("[_A-Za-z]", ""));
					int minorC = Integer.valueOf(versionC[1].replaceAll("[_A-Za-z]", ""));
					//if (latestVersion.length != 2 || currentVersion.length != 2)
					//	return;
					
					// Check if the major version is old
					if (majorL > majorC
							// Check if the minor version is old
							|| (majorL == majorC && minorL > minorC)
							//  Check if the sub version is old
							|| (majorL == majorC && minorL == minorC && compareLetters(versionL[1], versionC[1])))
						sender.sendMessage(ChatColor.DARK_GREEN + "There is a new version of MobManager: v" + split[0] + " for " + split[1]);
					else
						sender.sendMessage(ChatColor.DARK_GREEN + "MobManager is up to date");
				}
				else
					sender.sendMessage(ChatColor.DARK_GREEN + "Failed to check for updates");
			}
			
		});
	}
	
	private boolean compareLetters(String latest, String current)
	{
		latest = latest.replaceAll("[0-9]", "");
		current = current.replaceAll("[0-9]|(_dev)", "");
		
		if (latest.length() == 0)
			return false;
		
		if (latest.length() > 0 && current.length() == 0)
			return true;
		
		char l = latest.charAt(0);
		char c = current.charAt(0);
		
		return l > c;
	}

	@Override
	public String getAliases()
	{
		return "version";
	}

	@Override
	public String getUsage()
	{
		return "%s/%s %s%s";
	}

	@Override
	public String getDescription()
	{
		return "Fetches version information, also checks for updates for MobManager";
	}

}
