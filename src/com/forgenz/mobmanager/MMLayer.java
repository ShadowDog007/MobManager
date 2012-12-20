package com.forgenz.mobmanager;

public class MMLayer
{

	int miny;
	int maxy;
	int numPlayers;

	public MMLayer(final int miny, final int maxy, final int numPlayers)
	{
		this.miny = miny;
		this.maxy = maxy;
		this.numPlayers = numPlayers;
	}

	public boolean isEmpty()
	{
		return numPlayers == 0;
	}

	public int playerEntered()
	{
		return ++numPlayers;
	}

	public int playerLeft()
	{
		return --numPlayers;
	}

	public boolean insideRange(final int y)
	{
		return miny <= y && maxy >= y;
	}
}
