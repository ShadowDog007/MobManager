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

package com.forgenz.mobmanager.config;

import java.util.ArrayList;

public class MobIntChance
{
	private class Chance
	{
		final int min;
		final int max;
		
		final int value;
		
		private Chance(int min, int max, int bonus)
		{
			this.min = min;
			this.max = max;
			this.value = bonus;
		}
	}
	
	private int totalChance = 0;
	private ArrayList<Chance> chances = new ArrayList<Chance>();
	
	public void addChance(int chance, int value)
	{
		int min = totalChance;
		
		totalChance += chance;
		
		int max = totalChance;
		
		chances.add(new Chance(min, max, value));
	}
	
	public int getBonus()
	{
		if (chances.size() == 0)
			return 0;
		
		int chance = Config.rand.nextInt(totalChance);
		
		for (Chance c : chances)
		{
			if (c.min <= chance && c.max > chance)
				return c.value;
		}
		
		return 0;
	}
}
