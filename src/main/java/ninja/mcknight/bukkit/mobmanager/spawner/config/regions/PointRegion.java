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

package ninja.mcknight.bukkit.mobmanager.spawner.config.regions;

import ninja.mcknight.bukkit.mobmanager.spawner.config.Region;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public abstract class PointRegion extends Region
{
	private int minY;
	private int maxY;
	
	protected int x;
	protected int z;
	protected int radius;
	
	public PointRegion(ConfigurationSection cfg, RegionType type)
	{
		super(cfg, type);
	}
	
	public void initialise()
	{
		minY = getAndSet("MinY", 0);
		maxY = getAndSet("MaxY", 256);
		
		x = getAndSet("X", 0);
		z = getAndSet("Z", 0);
		
		radius = getAndSet("Radius", 0);
	}
	
	@Override
	public boolean withinRegion(Location loc)
	{
		if (loc.getBlockY() >= minY && loc.getBlockY() <= maxY)
			return withinRadius(loc);
		return false;
	}
	
	protected abstract boolean withinRadius(Location loc);
}
