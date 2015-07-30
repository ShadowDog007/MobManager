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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class LocationCache
{
	/** Limits the size of the cache */
	private static final int MAX_CACHE_SIZE = 50;
	
	private static final Queue<CachedLocation> locationCacheQueue = new ConcurrentLinkedQueue<CachedLocation>();
	
	public static Location getCachedLocation()
	{
		Location cacheLoc = locationCacheQueue.poll();
		return cacheLoc != null ? cacheLoc : new CachedLocation();
	}
	
	public static Location getCachedLocation(World world, double x, double y, double z, float yaw, float pitch)
	{
		Location cacheLoc = getCachedLocation();
		
		cacheLoc.setWorld(world);
		cacheLoc.setX(x);
		cacheLoc.setY(y);
		cacheLoc.setZ(z);
		cacheLoc.setYaw(yaw);
		cacheLoc.setPitch(pitch);
		
		return cacheLoc;
	}
	
	public static Location getCachedLocation(World world, double x, double y, double z)
	{
		return getCachedLocation(world, x, y, z, 0.0F, 0.0F);
	}
	
	public static Location getCachedLocation(Location loc)
	{
		return getCachedLocation(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
	}
	
	public static Location getCachedLocation(Entity entity)
	{
		return entity.getLocation(getCachedLocation());
	}
	
	private static void storeLocationCache(CachedLocation cacheLoc)
	{
		locationCacheQueue.add(cacheLoc);
	}
	
	private static class CachedLocation extends Location
	{

		public CachedLocation()
		{
			super(null, 0.0, 0.0, 0.0);
		}
		
		@Override
		public void finalize() throws Exception
		{
			// Make sure we are not wasting too much memory :3
			if (locationCacheQueue.size() < MAX_CACHE_SIZE)
			{
				storeLocationCache(this);
				throw new Exception();
			}
		}
	}
}
