package com.forgenz.mobmanager.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.forgenz.mobmanager.P;

public abstract class AbstractConfig
{
	public final static Pattern layerPattern = Pattern.compile("^\\d+:{1}\\d+$");
	public final static Pattern layerSplitPattern = Pattern.compile(":{1}");
	
	public FileConfiguration getConfig(String folder, String config)
	{
		return YamlConfiguration.loadConfiguration(new File(P.p.getDataFolder(), folder + File.separator + config));
	}
	
	public void saveConfig(String folder, String config, FileConfiguration cfg)
	{
		try
		{
			cfg.save(new File(P.p.getDataFolder(), folder + File.separator + config));
		}
		catch (IOException exception)
		{
			P.p.getLogger().severe("Unable to write to config file at \"" + folder + File.separator + config + "\"");
		}
	}
	
	public String getResourceAsString(String resource)
	{
		InputStream headerStream = P.p.getResource(resource);
		if (headerStream == null)
			return "";
		
		String header = "";
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
			
			for (int i = 0; i < numBytes; ++i)
			{
				header += (char) bytes[i];
			}
		}
		
		return header;
	}
	public void copyHeader(FileConfiguration cfg, String resource)
	{		
		copyHeader(cfg, resource, "");
	}
	
	public void copyHeader(FileConfiguration cfg, String resource, String add)
	{
		cfg.options().header(add + getResourceAsString(resource));
		cfg.options().copyHeader(true);
	}
	
	/* Settings */
}
