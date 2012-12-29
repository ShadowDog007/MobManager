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

package com.forgenz.mobmanager.util;

import com.forgenz.mobmanager.world.MMCoord;

/**
 * Spirals out from a center chunk fetching chunks within a radius
 * 
 * @author Michael McKnight (ShadowDog007)
 *
 */
public class Spiral
{

	/*
	 * reference diagram, task should move in this pattern out from chunk 0 in the center.
	 *    [6][>][>][>][>][>][6]
	 *    [^][3][>][>][>][4][v]
	 *    [^][^][1][>][2][v][v]
	 *    [^][^][1][0][v][v][v]
	 *    [^][3][<][<][2][v][v]
	 *    [5][<][<][<][<][5][v]
	 * etc[<][<][<][<][<][<][7]
	 */

	enum Qaudrant
	{
		TOP_LEFT, TOP_RIGHT, BOT_LEFT, BOT_RIGHT;

		public static Qaudrant calculate(final int x, final int z)
		{
			if (z > 0)
				if (x > 0)
					return TOP_RIGHT;
				else
					return TOP_LEFT;

			// Use >= here to make sure BOT_RIGHT is calculated from (0,0)
			if (x >= 0)
				return BOT_RIGHT;
			else
				return BOT_LEFT;
		}
	}

	private int radius;
	private int radiusSqrd;

	private MMCoord center;
	private boolean beenOutside = false;
	private boolean finished = false;

	private int x = 1;
	private int z = 0;
	private int limit = 0;

	Qaudrant qaud = Qaudrant.BOT_RIGHT;

	public Spiral(final MMCoord center, final int radius)
	{
		this.radius = radius;
		radiusSqrd = radius * radius;
		this.center = center;

		if (radius < 6)
			beenOutside = true;
	}

	public MMCoord getCenter()
	{
		return center;
	}

	public int getRadius()
	{
		return radius;
	}

	public MMCoord run()
	{
		if (finished)
			return null;

		while (!step())
			if (finished)
				return null;
		return new MMCoord(x + center.getX(), z + center.getZ());
	}

	private boolean withinRadius()
	{
		return radiusSqrd >= (x * x + z * z);
	}

	private boolean atLimit()
	{
		switch (qaud)
		{
		case BOT_LEFT:
		case TOP_RIGHT:
			return limit == Math.abs(z);
		case BOT_RIGHT:
		case TOP_LEFT:
			return limit == Math.abs(x);
		default:
			return false;
		}
	}

	private boolean step()
	{
		switch (qaud)
		{
		case BOT_LEFT:
			++z;
			break;
		case BOT_RIGHT:
			--x;
			break;
		case TOP_LEFT:
			++x;
			break;
		case TOP_RIGHT:
			--z;
			break;
		default:
		}

		if (atLimit())
			if (!turn())
			{
				finished = true;
				return false;
			}
		return beenOutside ? withinRadius() : true;
	}

	private boolean turn()
	{
		qaud = Qaudrant.calculate(x, z);
		switch (qaud)
		{
		case BOT_RIGHT:
			// When we move into the next layer of the square we check if the
			// limit is valid
			// We also need to check if we are within the circle because after
			// we leave for the first time we must check it on every pass
			// P.p.logQueue.add(new Pair<Level,String>(Level.INFO,
			// String.format("Limit is now %d", limit + 1)));
			beenOutside = !withinRadius();
			return ++limit <= radius;
		case BOT_LEFT:
		case TOP_LEFT:
		case TOP_RIGHT:
		default:
			return true;
		}
	}

	public boolean isFinished()
	{
		return finished;
	}
}
