package com.forgenz.mobmanager;

public class MMLayer
{

	int miny;
	int maxy;
	int numPlayers = 0;

	public MMLayer(final int miny, final int maxy, final int numPlayers)
	{
		this.miny = miny;
		this.maxy = maxy;
		if (numPlayers > 0)
			this.numPlayers = numPlayers;
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
		P.p.getLogger().warning(String.format("ENTERED Layer: %d - %d P: %d", miny, maxy, numPlayers + 1));
		return ++numPlayers;
	}

	public int playerLeft()
	{
		if (--numPlayers < 0)
		{
			numPlayers = 0;

			P.p.getLogger().warning("Player left a layer with no players in it?");
			/*try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}*/
		}
		
		P.p.getLogger().warning(String.format("LEFT Layer: %d - %d P: %d", miny, maxy, numPlayers));

		return numPlayers;
	}

	public boolean insideRange(final int y)
	{
		return miny <= y && maxy >= y;
	}
}
