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

public class RandomLocationGen
{
	private static Random rand = new Random();
	private static Location loc = new Location(null, 0.0, 0.0, 0.0);
	
	private RandomLocationGen()
	{
		
	}

	public static Location getLocation(Location center, int range, int minRange, int heightRange)
	{
		Location loc = Bukkit.isPrimaryThread() ? RandomLocationGen.loc : new Location(null, 0.0, 0.0, 0.0);
		return getLocation(center, range, minRange, heightRange, loc);
	}
	
	public static Location getLocation(Location center, int range, int minRange, int heightRange, Location cacheLoc)
	{
		// Make sure the centers world is valid
		if (center.getWorld() == null)
		{
			P.p().getLogger().warning("Null world passed to location generator");
			return center;
		}
		
		if (range < minRange)
		{
			range = range ^ minRange;
			minRange = range ^ minRange;
			range = range ^ minRange;
		}
		
		if (heightRange < 0)
			heightRange = 1;
		
		int rangeDiff = range - minRange;

		// Calculate the total (up/down) range of heightRange
		int heightRange2 = heightRange << 1;

		// Copy the world
		cacheLoc.setWorld(center.getWorld());
		
		// Make 10 attempts to find a safe spawning location
		for (int i = 0; i < 10; ++i)
		{
			// Calculate a random direction for the X/Z values
			double theta = 2 * Math.PI * rand.nextDouble();
			
			// Generate random locations for X/Z
			double trig = Math.cos(theta);
			cacheLoc.setX(Location.locToBlock(rand.nextDouble() * rangeDiff * trig + minRange * trig) + center.getBlockX() + 0.5);
			trig = Math.sin(theta);
			cacheLoc.setZ(Location.locToBlock(rand.nextDouble() * rangeDiff * trig + minRange * trig) + center.getBlockZ() + 0.5);
			
			// Generate coordinates for Y
			cacheLoc.setY(rand.nextInt(heightRange2) - heightRange + center.getBlockY() + 0.5);
			
			// Generate a random Yaw/Pitch
			cacheLoc.setYaw(rand.nextFloat() * 360.0F);
			
			// Check the location is safe
			Block block = cacheLoc.getBlock();
			
			// If the location is not safe we try again			
			if (!(isSafe(block) && isSafe(block.getRelative(BlockFace.UP))))
			{
				continue;
			}
			
			// Calculate the height diff
			int heightDiff = Math.abs(cacheLoc.getBlockY() - center.getBlockY());
			
			boolean onGround = false;
			// Move the position down as close to the ground as we can
			while (heightDiff < heightRange)
			{
				block = block.getRelative(BlockFace.DOWN);
				
				// If the below block is empty we shift the location down
				if (isSafe(block))
				{
					cacheLoc.setY(cacheLoc.getY() - 1.0D);
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
			if (onGround || !isSafe(block.getRelative(BlockFace.DOWN)) || !isSafe(block.getRelative(BlockFace.DOWN, 2)))
			{
				return cacheLoc;
			}
		}
		
		// If no safe location was found in a reasonable timeframe just return the center
		return center;
	}
	
	private static boolean isSafe(Block block)
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
