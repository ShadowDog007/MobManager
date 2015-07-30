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

package ninja.mcknight.bukkit.mobmanager.common.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import ninja.mcknight.bukkit.mobmanager.P;

/**
 * AbstractConfig provides methods to make it easy to manage a file configuration
 * @author Michael McKnight (ShadowDog007)
 *
 */
public abstract class AbstractConfig
{
	private ConfigurationSection cfg;
	private Map<String, Object> mapCfg;
	
	protected void setCfg(ConfigurationSection cfg)
	{
		this.cfg = cfg;
	}
	
	protected void setMapCfg(Map<String, Object> cfg)
	{
		this.mapCfg = cfg;
	}
	
	protected ConfigurationSection getCfg()
	{
		return cfg;
	}
	
	protected Map<String, Object> getMapCfg()
	{
		return mapCfg;
	}
	
	protected void clearCfg()
	{
		cfg = null;
		mapCfg = null;
	}
	
	public ConfigurationSection getConfigurationSection(String path)
	{
		return getConfigurationSection(cfg, path);
	}
	
	public void set(String path, Object obj)
	{
		if (cfg != null)
			set(cfg, path, obj);
		else
			set(mapCfg, path, obj);
	}
	
	public <T> T getAndSet(String path, T def)
	{
		return cfg != null ? getAndSet(cfg, path, def) : getAndSet(mapCfg, path, def);
	}
	
	public final static String WORLDS_FOLDER = "worlds";
	public final static String LIMITER_CONFIG_NAME = "limiter.yml";
	
	public static FileConfiguration getConfig(String folder, String config)
	{
		return getConfig(folder, config, null);
	}
	
	public static FileConfiguration getConfig(String folder, String config, String defaultConfig)
	{
		YamlConfiguration yaml = new YamlConfiguration();
		
		File configFile = new File(P.p().getDataFolder(), (folder.length() != 0 ? folder + File.separator : "") + config);
		
		if (configFile.exists())
		{
			try
			{
				yaml.load(configFile);
			}
			catch (FileNotFoundException e) {}
			catch (Exception e)
			{
				File backupFile = new File(P.p().getDataFolder(), (folder.length() != 0 ? folder + File.separator : "") + ("Backup-" + System.currentTimeMillis() / 1000L + "-") + config);
				P.p().getLogger().log(Level.SEVERE, "Failed to load config: " + configFile.getPath());
				P.p().getLogger().log(Level.SEVERE, "Creating backup: '" + backupFile.getPath(), e);
				
				configFile.renameTo(backupFile);
			}
		}
		else if (defaultConfig != null)
		{
			try
			{
				yaml.load(P.p().getResource(defaultConfig));
				P.p().getLogger().info("Copied Default Configuration - " + configFile.getPath());
			}
			catch (Exception e) {}
		}
		
		return yaml;
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
		cfg.options().header(P.p().getHeaderString() + header);
		cfg.options().copyHeader(true);
	}
	
	public static ConfigurationSection getConfigurationSection(ConfigurationSection cfg, String path)
	{
		ConfigurationSection sect = cfg.getConfigurationSection(path);
		if (sect == null)
			sect = cfg.createSection(path);
		set(cfg, path, sect);
		return sect;
	}
	
	public static void set(ConfigurationSection cfg, String path, Object obj)
	{
		cfg.set(path, null);
		cfg.set(path, obj);
	}
	
	public static <T> T getAndSet(ConfigurationSection cfg, String path, T def)
	{
		Object o = cfg.get(path);
		
		@SuppressWarnings("unchecked")
		T ret = (T) (o != null && def.getClass().isAssignableFrom(o.getClass()) ? o : def);
		
		set(cfg, path, ret);
		return ret;
	}

	public static Map<String, Object> getConfigurationSection(Map<String, Object> cfg, String path)
	{
		return getAndSet(cfg, path, new LinkedHashMap<String, Object>());
	}
	
	public static void set(Map<String, Object> cfg, String path, Object obj)
	{
		cfg.remove(path);
		cfg.put(path, obj);
	}
	
	public static <T> T getAndSet(Map<String, Object> cfg, String path, T def)
	{
		Object o = cfg.get(path);
		
		@SuppressWarnings("unchecked")
		T ret = (T) (o != null && def.getClass().isAssignableFrom(o.getClass()) ? o : def);
		
		set(cfg, path, ret);
		return ret;
	}
}
