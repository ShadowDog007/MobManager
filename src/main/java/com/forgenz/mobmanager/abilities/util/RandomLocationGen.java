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

package com.forgenz.mobmanager.abilities.util;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.abilities.config.AbilityConfig;

public class RandomLocationGen
{
	private static Random rand = new Random();
	private static Location loc = new Location(null, 0.0, 0.0, 0.0);
	
	/**
	 * Stop instances of the class from being created
	 */
	private RandomLocationGen()
	{
		
	}

	/**
	 * Generates a random location around the center location
	 */
	public static Location getLocation(Location center, int range, int minRange, int heightRange)
	{
		Location loc = Bukkit.isPrimaryThread() ? RandomLocationGen.loc : new Location(null, 0.0, 0.0, 0.0);
		return getLocation(center, range, minRange, heightRange, loc);
	}
	
	/**
	 * Generates a random location around the center location
	 */
	public static Location getLocation(Location center, int range, int minRange, int heightRange, Location cacheLoc)
	{
		// Make sure the centers world is valid
		if (center.getWorld() == null)
		{
			P.p().getLogger().warning("Null world passed to location generator");
			return center;
		}
		
		// Make sure range is larger than minRange
		if (range < minRange)
		{
			range = range ^ minRange;
			minRange = range ^ minRange;
			range = range ^ minRange;
		}
		
		// Height range must be at least 1
		if (heightRange < 0)
			heightRange = 1;
		
		// Make sure range is bigger than minRange
		if (range == minRange)
			++range;

		// Calculate the total (up/down) range of heightRange
		int heightRange2 = heightRange << 1;
		
		// Copy the world
		cacheLoc.setWorld(center.getWorld());
		
		// Make 10 attempts to find a safe spawning location
		for (int i = 0; i < 10; ++i)
		{
			// Generate the appropriate type of location
			if (AbilityConfig.i().useCircleLocationGeneration)
			{
				getCircularLocation(center, range, minRange, cacheLoc);
			}
			else
			{
				getSquareLocation(center, range, minRange, cacheLoc);
			}
			
			// Generate coordinates for Y
			cacheLoc.setY(rand.nextInt(heightRange2) - heightRange + center.getBlockY() + 0.5);
				
			// If the location is safe we can return the location
			if (isLocationSafe(cacheLoc, center.getBlockY(), heightRange))
			{
				// Generate a random Yaw/Pitch
				cacheLoc.setYaw(rand.nextFloat() * 360.0F);
				return cacheLoc;
			}
		}
		
		// If no safe location was found in a reasonable timeframe just return the center
		return center;
	}
	
	/**
	 * Makes sure the given location is safe to spawn something there
	 * @return true if the location is safe
	 */
	private static boolean isLocationSafe(Location location, int centerY, int heightRange)
	{
		// Check the location is safe
		Block block = location.getBlock();
		
		// If the location is not safe we try again			
		if (!(isSafeBlock(block) && isSafeBlock(block.getRelative(BlockFace.UP))))
		{
			return false;
		}
		
		// Calculate the height diff
		int heightDiff = Math.abs(location.getBlockY() - centerY);
		
		boolean onGround = false;
		// Move the position down as close to the ground as we can
		while (heightDiff < heightRange)
		{
			block = block.getRelative(BlockFace.DOWN);
			
			// If the below block is empty we shift the location down
			if (isSafeBlock(block))
			{
				location.setY(location.getY() - 1.0D);
				++heightDiff;
			}
			// If it isn't the mob is on the ground
			else
			{
				onGround = true;
				break;
			}
		}

		// If the location is on or near the ground the location is good
		if (onGround || !isSafeBlock(block.getRelative(BlockFace.DOWN)) || !isSafeBlock(block.getRelative(BlockFace.DOWN, 2)))
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Generates a random location which is circular around the center
	 */
	private static Location getCircularLocation(Location center, int range, int minRange, Location cacheLoc)
	{
		// Calculate the difference between the max and min range
		int rangeDiff = range - minRange;
		// Calculate a random direction for the X/Z values
		double theta = 2 * Math.PI * rand.nextDouble();
		
		// Generate a random radius
		double radius = rand.nextDouble() * rangeDiff + minRange;
		
		// Set the X/Z coordinates
		double trig = Math.cos(theta);
		cacheLoc.setX(Location.locToBlock(radius * trig) + center.getBlockX() + 0.5);
		trig = Math.sin(theta);
		cacheLoc.setZ(Location.locToBlock(radius * trig) + center.getBlockZ() + 0.5);
		
		return cacheLoc;
	}
	
	/**
	 * Generates a random location which is square around the center
	 */
	private static Location getSquareLocation(Location center, int range, int minRange, Location cacheLoc)
	{
		// Calculate the sum of all the block deviations from the center between minRange and range
		int totalBlockCount = (range * (++range) - minRange * (minRange + 1)) >> 1;
		// Fetch a random number of blocks
		int blockCount = totalBlockCount - rand.nextInt(totalBlockCount);
		
		// While the block deviation from the center for the given range is
		// less than the number of blocks left we remove a layer of blocks
		while (range < blockCount)
			blockCount -= --range;
		
		// Pick a random location on the range line
		int lineLoc = rand.nextInt(range << 1);
		// Choose a line (North/East/West/South lines)
		// Then set the X/Z coordinates
		switch (rand.nextInt(4))
		{
		// East Line going North
		case 0:
			cacheLoc.setX(center.getBlockX() + range + 0.5D);
			cacheLoc.setZ(center.getBlockZ() + range - lineLoc + 0.5D);
			break;
		// South Line going East
		case 1:
			cacheLoc.setX(center.getBlockX() - range + lineLoc + 0.5D);
			cacheLoc.setZ(center.getBlockZ() + range + 0.5D);
			break;
		// West Line going South
		case 2:
			cacheLoc.setX(center.getBlockX() - range + 0.5D);
			cacheLoc.setZ(center.getBlockZ() - range + lineLoc + 0.5D);
			break;
		// North Line going west
		case 3:
		default:
			cacheLoc.setX(center.getBlockX() + range - lineLoc + 0.5D);
			cacheLoc.setZ(center.getBlockZ() - range + 0.5D);
		}
		
		return cacheLoc;
	}
	
	/**
	 * Checks if the block is of a type which is safe for spawning inside of
	 * @return true if the block type is safe
	 */
	private static boolean isSafeBlock(Block block)
	{
		Material mat = block.getType();
		
		switch (mat)
		{
		case AIR:
		case WEB:
		case VINE:
		case SNOW:
		case LONG_GRASS:
		case DEAD_BUSH:
		case SAPLING:
			return true;
		default:
			return false;
		}
	}
}
