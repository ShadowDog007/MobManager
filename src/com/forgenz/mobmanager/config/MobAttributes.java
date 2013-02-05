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
	
	public final MobIntChance bonusHealth;
	public final MobIntChance bonusDamage;
	
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
		bonusHealth = new MobIntChance();
		
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
			int value = Integer.valueOf(split[1]);
			
			if (chance <= 0 || value == 0)
				continue;
			
			bonusHealth.addChance(chance, value);
		}
		cfg.set("BonusHealth", list);
		
		/* ######## BonusDamage ######## */
		bonusDamage = new MobIntChance();
		
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
			int value = Integer.valueOf(split[1]);
			
			if (chance <= 0 || value == 0)
				continue;
			
			bonusDamage.addChance(chance, value);
		}
		cfg.set("BonusDamage", list);
	}
}
