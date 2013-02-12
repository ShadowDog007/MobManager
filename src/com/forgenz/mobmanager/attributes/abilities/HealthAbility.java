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

package com.forgenz.mobmanager.attributes.abilities;

import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.attributes.AbilityTypes;
import com.forgenz.mobmanager.attributes.JoinableAttribute;
import com.forgenz.mobmanager.util.ValueChance;

public class HealthAbility extends Ability implements JoinableAttribute<HealthAbility>
{
	
	public final int bonus;

	private HealthAbility(int bonus)
	{
		this.bonus = bonus;
	}
	
	@Override
	public void addAbility(LivingEntity entity)
	{		
		setMaxHealth(entity, bonus);
	}
	
	private static void setMaxHealth(Damageable entity, int bonus)
	{
		if (entity == null || bonus == 0)
			return;

		
		// Fetch the entities old max/actual health (Used for HP scaling)
		int oldHp = entity.getHealth();
		int oldMax = entity.getMaxHealth();

		// Make sure we do not add too much HP to the entity
		entity.resetMaxHealth();

		// Calculate the new maximum value
		int newMax = bonus + entity.getMaxHealth();
		
		// Validate the new maximum
		if (newMax <= 0)
			newMax = 1;

		// Set the new maximum
		entity.setMaxHealth(newMax);

		// Scale the entities HP relative to its old/new maximum
		entity.setHealth(oldHp * newMax / oldMax);
	}
	
	@Override
	public void joinAttributes(LivingEntity entity, HealthAbility ...attributes)
	{
		int bonus = this.bonus;
		
		for (HealthAbility ability : attributes)
		{
			if (ability == null)
				continue;
			bonus += ability.bonus;
		}
		
		setMaxHealth(entity, bonus);
	}

	@Override
	public void removeAbility(LivingEntity entity)
	{
		entity.resetMaxHealth();
	}

	@Override
	public AbilityTypes getAbilityType()
	{
		return AbilityTypes.HEALTH_BONUS;
	}
	
	public static void setup(EntityType mob, ValueChance<Ability> abilityChances, List<?> optList)
	{
		Iterator<?> it = optList.iterator();
		
		while (it.hasNext())
		{
			String str = getString(it.next());
			
			if (str == null)
				continue;
			
			String[] split = getChanceSplit(str);
			
			if (split == null)
			{
				P.p.getLogger().warning("The value " + str + " is invalid for MobAtributes." + mob + "." + AbilityTypes.HEALTH_BONUS.getConfigPath());
				it.remove();
				continue;
			}
			
			int chance = Integer.valueOf(split[0]);
			HealthAbility ability = setup(mob, split[1]);
			
			if (ability != null)
				abilityChances.addChance(chance, ability);
		}
	}
	
	public static HealthAbility setup(EntityType mob, String optVal)
	{
		if (!AbilityTypes.HEALTH_BONUS.valueMatches(optVal))
		{
			P.p.getLogger().warning("The value " + optVal + " is invalid for MobAtributes." + mob + "." + AbilityTypes.HEALTH_BONUS);
			return null;
		}
		
		return new HealthAbility(Integer.valueOf(optVal));
	}
	
}
