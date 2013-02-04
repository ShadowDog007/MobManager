package com.forgenz.mobmanager.config;

import java.util.ArrayList;

public class MobBonusIntChance
{
	private class Chance
	{
		final int min;
		final int max;
		
		final int bonus;
		
		private Chance(int min, int max, int bonus)
		{
			this.min = min;
			this.max = max;
			this.bonus = bonus;
		}
	}
	
	private int totalChance = 0;
	private ArrayList<Chance> chances = new ArrayList<Chance>();
	
	public void addChance(int chance, int bonus)
	{
		int min = totalChance;
		
		totalChance += chance;
		
		int max = totalChance;
		
		chances.add(new Chance(min, max, bonus));
	}
	
	public int getBonus()
	{
		if (chances.size() == 0)
			return 0;
		
		int chance = Config.rand.nextInt(totalChance);
		
		for (Chance c : chances)
		{
			if (c.min <= chance && c.max > chance)
				return c.bonus;
		}
		
		return 0;
	}
}
