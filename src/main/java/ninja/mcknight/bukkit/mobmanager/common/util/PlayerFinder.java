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

package ninja.mcknight.bukkit.mobmanager.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import ninja.mcknight.bukkit.mobmanager.limiter.config.LimiterConfig;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Flying;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import ninja.mcknight.bukkit.mobmanager.P;
import ninja.mcknight.bukkit.mobmanager.common.config.AbstractConfig;
import ninja.mcknight.bukkit.mobmanager.limiter.world.MMWorld;

public class PlayerFinder
{
	private PlayerFinder() {}

	public enum FinderMode
	{
		/** Checks distance between two locations */
		SPHERE
		{
			@Override
			public boolean withinRange(Location l1, Location l2, int radiusSquared, int height)
			{
				return l1.distanceSquared(l2) < radiusSquared;
			}
		},
		
		/** Checks the horizontal and vertical distance between two locations */
		CYLINDER
		{
			@Override
			public boolean withinRange(Location l1, Location l2, int radiusSquared, int height)
			{
				// Check height is within range
				if (Math.abs(l1.getBlockY() - l2.getBlockY()) <= height)
				{
					// Check horizontal distance is within range
					final double first = Math.pow(l1.getX() - l2.getX(), 2);
					
					// If the first one is greater the two combined won't be less...
					if (first > radiusSquared)
						return false;
					
					return first + Math.pow(l1.getZ() - l2.getZ(), 2) <= radiusSquared;
				}
				return false;
			}
		};
		
		public abstract boolean withinRange(Location l1, Location l2, int radiusSquared, int height);
		
		public static FinderMode getMode(String string, FinderMode def)
		{
			for (FinderMode mode : values())
			{
				if (mode.toString().equalsIgnoreCase(string))
					return mode;
			}
			return def;
		}
	}
	
	/**
	 * Fetches all nearby players and adds them to the player collection
	 * @param loc The center of the search
	 * @param mode The mode in which to search for players
	 * @param radiusSquared The radius squared in which we are checking for players
	 * @param height The height difference between the center and the player (Only for CYLINDER FinderMode)
	 * @param players A collection which will have players added to it
	 * 
	 * @return The players argument
	 */
	public static <T extends Collection<Player>> T findNearbyPlayers(Location loc, FinderMode mode, int radiusSquared, int height, T players)
	{
		// Fetch a location object for ploc
		Location pLoc = LocationCache.getCachedLocation();
		
		// Iterate through each player to check if there is a player nearby
		for (Player player : P.p().getOnlinePlayers().values())
		{
			// Skip the player if they are in creative mode (And we should be skipping them)
			if (LimiterConfig.ignoreCreativePlayers && player.getGameMode() == GameMode.CREATIVE)
				continue;
			
			// If the worlds differ the player is not nearby
			if (player.getWorld() != loc.getWorld())
				continue;
			
			// Copy the players location into pLoc
			player.getLocation(pLoc);
			
			// Check the location and the player is within range
			if (mode.withinRange(loc, pLoc, radiusSquared, height))
			{
				players.add(player);
			}
		}
		return players;
	}
	
	/**
	 * Fetches all nearby players
	 * 
	 * @See {@link #findNearbyPlayers(Location, FinderMode, int, int, Collection<Player>)}
	 * 
	 * @return A list of all nearby players
	 */
	public static ArrayList<Player> findNearbyPlayers(Location loc, FinderMode mode, int radiusSquared, int height)
	{
		return findNearbyPlayers(loc, mode, radiusSquared, height, new ArrayList<Player>());
	}
	
	/**
	 * Fetches all nearby players
	 * 
	 * @See {@link #findNearbyPlayers(Location, FinderMode, int, int, Collection<Player>)}
	 * 
	 * @param cfg The configuration to use to find the players
	 * @param players A collection which will have players added to it
	 * 
	 * @return The players argument
	 */
	public static <T extends Collection<Player>> T findNearbyPlayers(Location loc, FinderModeConfig cfg, T players)
	{
		return findNearbyPlayers(loc, cfg.mode, cfg.radiusSquared, cfg.height, players);
	}
	
	/**
	 * Fetches all nearby players
	 * 
	 * @See {@link #findNearbyPlayers(Location, FinderMode, int, int, Collection<Player>)}
	 * 
	 * @param cfg The configuration to use to find the players
	 * 
	 * @return A list of all nearby players
	 */
	public static ArrayList<Player> findNearbyPlayers(Location loc, FinderModeConfig cfg)
	{
		return findNearbyPlayers(loc, cfg.mode, cfg.radiusSquared, cfg.height);
	}
	
	public static boolean mobFlys(Entity entity)
	{
		if (entity instanceof Flying || entity instanceof Bat)
			return true;
		return false;
	}
	
	/**
	 * Scans nearby chunks for players on layers which cross a given height
	 * 
	 * @param world The World the chunk is in
	 * @param entity The entity to check for players around
	 * @param flying If true layers further down will be checked for player to allow for more flying mobs
	 * 
	 * @return True if there is a player within range of the center chunk and in
	 *         a layer which overlaps the height 'y'
	 */
	public static boolean playerNear(MMWorld world, LivingEntity entity, boolean flying)
	{		
		// Fetch the entities location and a location object for ploc
		Location eLoc = entity.getLocation(LocationCache.getCachedLocation());
		
		// Fetch the worlds search distance at the given entities height
		int searchDist = world.getSearchDistanceSquared((short) eLoc.getBlockY());
		// Fetch the worlds search height
		int searchY = world.getSearchHeight() + (flying ? LimiterConfig.flyingMobAditionalBlockDepth : 0);
		
		// Find nearby players
		return playerNear(eLoc, searchDist, searchY);
	}
	
	/**
	 * Scans nearby chunks for players on layers which cross a given height
	 * 
	 * @param location The location to check for players around
	 * @param searchDist The distance around the location to look for players
	 * @param searchY The vertical distance to look for players
	 * 
	 * @return True if there is a player within range location and in
	 *         a layer which overlaps the height 'y'
	 */
	public static boolean playerNear(Location location, int searchDist, int searchY)
	{		
		// Fetch the entities location and a location object for ploc
		Location pLoc = LocationCache.getCachedLocation();

		// Iterate through each player to check if there is a player nearby
		for (Player player : P.p().getOnlinePlayers().values())
		{
			// Skip the player if they are in creative mode (And we should be skipping them)
			if (LimiterConfig.ignoreCreativePlayers && player.getGameMode() == GameMode.CREATIVE)
				continue;
			
			// If the worlds differ the player is not nearby
			if (player.getWorld() != location.getWorld())
				continue;
			
			// Copy the players location into pLoc
			player.getLocation(pLoc);
			
			// Check the if the distance between the entity and the player is less than the search distance
			// Then check the if the height difference is small enough
			// Return true as soon as we find a player which matches these requirements
			if (FinderMode.CYLINDER.withinRange(location, pLoc, searchDist, searchY))
				return true;
		}
		
		// Return false if no nearby player was found
		return false;
	}
	
	public static class FinderModeConfig extends AbstractConfig
	{
		public final FinderMode mode;
		public final int radiusSquared;
		public final int height;
		
		public FinderModeConfig(ConfigurationSection cfg)
		{
			this(cfg.getValues(true));
		}
		
		public FinderModeConfig(Map<String, Object> cfg)
		{
			super.setMapCfg(cfg);
			
			mode = FinderMode.getMode(getAndSet("FinderMode", FinderMode.CYLINDER.toString()), FinderMode.CYLINDER);
			radiusSquared = (int) Math.pow(getAndSet("Radius", 0), 2);
			height = Math.abs(getAndSet("Height", 0));
			
			super.clearCfg();
		}
		
		public boolean validRange()
		{
			switch (mode)
			{
			case CYLINDER:
				if (height == 0)
					return false;					
			case SPHERE:
				return radiusSquared > 0;
			default:
				return false;
			}
		}

		@Override
		public int hashCode()
		{
			int hash = mode == null ? 0 : mode == FinderMode.CYLINDER ? 8 : 1024;
			
			switch (mode)
			{
			case CYLINDER:
				hash ^= height;
			case SPHERE:
				hash ^= radiusSquared;
			default:
				return hash;
			}
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FinderModeConfig other = (FinderModeConfig) obj;
			if (mode != other.mode)
				return false;
			if (radiusSquared != other.radiusSquared)
				return false;
			if (mode == FinderMode.CYLINDER && height != other.height)
				return false;
			return true;
		}
	}
}
