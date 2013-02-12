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
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;

import com.forgenz.mobmanager.attributes.AbilityTypes;
import com.forgenz.mobmanager.attributes.abilities.Ability;
import com.forgenz.mobmanager.util.ValueChance;

public class MobAttributes
{	
	public final EntityType mob;
	
	public final float spawnRate;
	public final float babyRate;
	
	public final HashMap<AbilityTypes, ValueChance<Ability>> attributes;
	
	public MobAttributes(EntityType mob, ConfigurationSection cfg)
	{
		attributes = new HashMap<AbilityTypes, ValueChance<Ability>>();
		
		this.mob = mob;
		
		/* ######## SpawnRate ######## */
		float spawnRate = (float) cfg.getDouble("SpawnRate", 1.0F);
		if (spawnRate <= 0)
			spawnRate = 1.0F;
		this.spawnRate = spawnRate;
		cfg.set("SpawnRate", spawnRate);
		
		/* ######## BabyRate ######## */
		if (Ageable.class.isAssignableFrom(mob.getEntityClass()) || mob == EntityType.ZOMBIE)
		{
			float babyRate = (float) cfg.getDouble("BabyRate", 0.0F);
			if (babyRate <= 0)
				babyRate = 0.0F;
			this.babyRate = babyRate;
			cfg.set("BabyRate", babyRate);
		}
		else
		{
			babyRate = 0.0F;
		}
		
		/* ######## ValueChance Abilities ######## */
		for (AbilityTypes ability : AbilityTypes.values())
		{
			// Ignore abilities which don't work as ValueChance + Stand alone
			if (!ability.isValueChanceAbility())
				continue;
			
			ValueChance<Ability> abilityChances = new ValueChance<Ability>();
			// Fetch String list from config
			List<?> optList = cfg.getList(ability.getConfigPath());
			if (optList == null)
				optList = new ArrayList<String>();
			// Store the ability chances if there is at least one
			if (ability.setup(mob, abilityChances, optList))
				attributes.put(ability, abilityChances);
			
			// Update String list (ability.setup can removes invalid settings)
			cfg.set(ability.getConfigPath(), optList);
		}
			
	}
}
