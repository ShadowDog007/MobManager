package com.forgenz.mobmanager.world;

import com.forgenz.mobmanager.P;

/**
 * Keeps track of players within a given Y coordinate range
 * 
 * @author Michael McKnight (ShadowDog007)
 *
 */
public class MMLayer
{
	/** Indicates the last time a mob successfully spawned in this layer */
	private long lastSpawn = 0;
	private int miny;
	private int maxy;
	private int numPlayers = 0;

	public MMLayer(final int miny, final int maxy, final int numPlayers)
	{
		this.miny = miny;
		this.maxy = maxy;
		if (numPlayers > 0)
			this.numPlayers = numPlayers;
	}
	
	public boolean hadRecentSpawn()
	{
		return System.currentTimeMillis() - lastSpawn <= 50;
	}
	
	public void updateLastSpawn()
	{
		lastSpawn = System.currentTimeMillis();
	}

	public int getMaxY()
	{
		return maxy;
	}

	public int getMinY()
	{
		return miny;
	}

	public boolean isEmpty()
	{
		return numPlayers == 0;
	}

	public int getNumPlayers()
	{
		return numPlayers;
	}

	public int playerEntered()
	{
		return ++numPlayers;
	}

	public int playerLeft()
	{
		if (--numPlayers < 0)
		{
			numPlayers = 0;

			P.p.getLogger().warning("Player left a layer with no players in it?");
		}

		return numPlayers;
	}

	public boolean insideRange(final int y)
	{
		return miny <= y && maxy >= y;
	}
	
	public int compare(MMLayer layer)
	{
		return this.miny + this.maxy - layer.miny - layer.maxy;
	}
}
