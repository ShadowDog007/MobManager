package com.forgenz.mobmanager;

public class MMCoord
{
	private int x;
	private int z;

	public MMCoord(final int x, final int z)
	{
		this.x = x;
		this.z = z;
	}
	
	public int getX()
	{
		return x;
	}
	public int getZ()
	{
		return z;
	}

	@Override
	public int hashCode()
	{
		return x << 16 + z;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MMCoord other = (MMCoord) obj;
		if (x != other.x)
			return false;
		if (z != other.z)
			return false;
		return true;
	}

}
