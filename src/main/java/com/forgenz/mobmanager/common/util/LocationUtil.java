package com.forgenz.mobmanager.common.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public abstract class LocationUtil
{
	
	public static Location getLocationFromString(String location)
	{
		if (location == null)
		{
			return null;
		}
		
		String[] split = Patterns.commaSplit.split(location);
		
		if (split.length != 4)
		{
			return null;
		}
		
		World world = Bukkit.getWorld(split[0]);
		
		if (world == null)
		{
			return null;
		}
		
		for (int i = 1; i < split.length; ++i)
		{
			// Validate each number
			if (!Patterns.doubleCheck.matcher(split[i]).matches())
			{
				return null;
			}
		}
		
		double x = Double.valueOf(split[1]);
		double y = Double.valueOf(split[2]);
		double z = Double.valueOf(split[3]);
		
		return new Location(world, x, y, z);
	}
	
}
