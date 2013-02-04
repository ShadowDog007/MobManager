package com.forgenz.mobmanager.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import com.forgenz.mobmanager.P;

public class MobAttributes
{	
	private final Pattern chanceSplit = Pattern.compile(":");
	
	public final EntityType mob;
	
	public final float spawnRate;
	
	public final MobBonusIntChance bonusHealth;
	public final MobBonusIntChance bonusDamage;
	
	public MobAttributes(EntityType mob, ConfigurationSection cfg)
	{
		this.mob = mob;
		
		/* ######## SpawnRate ######## */
		float spawnRate = (float) cfg.getDouble("SpawnRate", 1.0F);
		if (spawnRate <= 0)
			spawnRate = 1.0F;
		this.spawnRate = spawnRate;
		cfg.set("SpawnRate", spawnRate);
		
		/* ######## BonusHealth ######## */
		bonusHealth = new MobBonusIntChance();
		
		List<?> list = cfg.getList("BonusHealth", new ArrayList<Object>());
		
		Iterator<?> it = list.iterator();
		
		while (it.hasNext())
		{
			Object obj = it.next();
			
			if (obj instanceof String == false)
			{
				it.remove();
				continue;
			}
			
			String str = (String) obj;
			
			String[] split = chanceSplit.split(str);
			
			if (split.length != 2)
			{
				P.p.getLogger().warning("The value " + str + " is invalid for MobAtributes." + mob + ".BonusHealth");
				it.remove();
				continue;
			}
			
			int chance = Integer.valueOf(split[0]);
			int bonus = Integer.valueOf(split[1]);
			
			if (chance <= 0 || bonus == 0)
				continue;
			
			bonusHealth.addChance(chance, bonus);
		}
		cfg.set("BonusHealth", list);
		
		/* ######## BonusDamage ######## */
		bonusDamage = new MobBonusIntChance();
		
		list = cfg.getList("BonusDamage", new ArrayList<Object>());
		
		it = list.iterator();
		
		while (it.hasNext())
		{
			Object obj = it.next();
			
			if (obj instanceof String == false)
			{
				it.remove();
				continue;
			}
			
			String str = (String) obj;
			
			String[] split = chanceSplit.split(str);
			
			if (split.length != 2)
			{
				P.p.getLogger().warning("The value " + str + " is invalid for MobAtributes." + mob + ".BonusDamage");
				it.remove();
				continue;
			}
			
			int chance = Integer.valueOf(split[0]);
			int bonus = Integer.valueOf(split[1]);
			
			if (chance <= 0 || bonus == 0)
				continue;
			
			bonusDamage.addChance(chance, bonus);
		}
		cfg.set("BonusDamage", list);
	}
}
