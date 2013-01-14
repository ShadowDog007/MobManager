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

package com.forgenz.mobmanager.world;

import com.forgenz.mobmanager.Config;
import com.forgenz.mobmanager.P;

/**
 * Keeps track of players within a given Y coordinate range
 * 
 * @author Michael McKnight (ShadowDog007)
 *
 */
public class MMLayer
{
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

	public int getMaxY()
	{
		return maxy;
	}

	public int getMinY()
	{
		return miny;
	}
	
	public void resetPlayers()
	{
		numPlayers = 0;
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

			if (!Config.disableWarnings)
				P.p.getLogger().warning(String.format("Player left the layer (%d,%d) without any players being in it?", miny, maxy));
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
