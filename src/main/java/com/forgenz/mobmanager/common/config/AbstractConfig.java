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

package com.forgenz.mobmanager.common.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.forgenz.mobmanager.P;

/**
 * AbstractConfig provides methods to make it easy to manage a file configuration
 * @author Michael McKnight (ShadowDog007)
 *
 */
public abstract class AbstractConfig
{
	public final static String WORLDS_FOLDER = "worlds";
	public final static String LIMITER_CONFIG_NAME = "limiter.yml";
	public final static String ABILITY_CONFIG_NAME = "abilities.yml";
	
	public static FileConfiguration getConfig(String folder, String config)
	{
		return YamlConfiguration.loadConfiguration(new File(P.p().getDataFolder(), (folder.length() != 0 ? folder + File.separator : "") + config));
	}
	
	public static void saveConfig(String folder, String config, FileConfiguration cfg)
	{
		try
		{
			cfg.save(new File(P.p().getDataFolder(), folder + File.separator + config));
		}
		catch (IOException exception)
		{
			P.p().getLogger().severe("Unable to write to config file at \"" + folder + File.separator + config + "\"");
		}
	}
	
	public static String getResourceAsString(String resource)
	{
		InputStream headerStream = P.p().getResource(resource);
		if (headerStream == null)
			return "";
		
		StringBuilder header = new StringBuilder();
		
		int numBytes = 1;
		while (numBytes > 0)
		{
			byte[] bytes = new byte[64];
			
			try
			{
				numBytes = headerStream.read(bytes);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			header.ensureCapacity(numBytes + header.length());
			
			for (int i = 0; i < numBytes; ++i)
			{
				header.append((char) bytes[i]);
			}
		}
		
		return header.toString();
	}
	public static void copyHeader(FileConfiguration cfg, String resource)
	{		
		copyHeader(cfg, resource, "");
	}
	
	public static void copyHeader(FileConfiguration cfg, String resource, String add)
	{
		copyHeader(add + getResourceAsString(resource), cfg);
	}
	
	public static void copyHeader(String header, FileConfiguration cfg)
	{
		cfg.options().header(header);
		cfg.options().copyHeader(true);
	}
	
	public static void set(ConfigurationSection cfg, String path, Object obj)
	{
		cfg.set(path, null);
		cfg.set(path, obj);
	}
}
