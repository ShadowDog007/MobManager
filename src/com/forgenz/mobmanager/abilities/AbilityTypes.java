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

package com.forgenz.mobmanager.abilities;

import java.util.List;

import com.forgenz.mobmanager.abilities.abilities.Ability;
import com.forgenz.mobmanager.abilities.abilities.AbilitySet;
import com.forgenz.mobmanager.abilities.abilities.AngryAbility;
import com.forgenz.mobmanager.abilities.abilities.ArmourAbility;
import com.forgenz.mobmanager.abilities.abilities.BabyAbility;
import com.forgenz.mobmanager.abilities.abilities.ChargedCreeperAbility;
import com.forgenz.mobmanager.abilities.abilities.DamageAbility;
import com.forgenz.mobmanager.abilities.abilities.HealthAbility;
import com.forgenz.mobmanager.abilities.abilities.ItemAbility;
import com.forgenz.mobmanager.abilities.abilities.NullAbility;
import com.forgenz.mobmanager.abilities.abilities.PotionAbility;
import com.forgenz.mobmanager.abilities.util.ValueChance;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;

public enum AbilityTypes
{
	NONE(null, false),
	
	POTION("PotionEffects"),
	
	HEALTH_BONUS("HealthBonus"),
	
	DAMAGE_MULTI("DamageMulti"),
	
	ARMOUR("Armour"),
	
	ITEM_HAND("Item_Hand"),
	
	BABY("BabyRate", false),
	
	ANGRY("Angry", false),
	
	CHARGED("Charged", false),
	
	ABILITY_SET("ApplySets");
	
	private final String abilityConfigPath;
	private final boolean valueChanceAbility;
	
	AbilityTypes(String abilityConfigPath)
	{
		this(abilityConfigPath, true);
	}
	
	AbilityTypes(String abilityConfigPath, boolean valueChanceAbility)
	{
		this .abilityConfigPath = abilityConfigPath;
		this.valueChanceAbility = valueChanceAbility;
	}
	
	public String getConfigPath()
	{
		return abilityConfigPath;
	}
	
	public boolean isValueChanceAbility()
	{
		return valueChanceAbility;
	}
	
	public static AbilityTypes getAbilityType(String option)
	{
		for (AbilityTypes ability : values())
		{
			if (ability.toString().equalsIgnoreCase(option))
				return ability;
		}
		
		return null;
	}
	
	public boolean setup(ExtendedEntityType mob, ValueChance<Ability> abilityChances, List<Object> optList)
	{
		switch (this)
		{
		case ABILITY_SET:
			AbilitySet.setup(mob, abilityChances, optList);
			break;
		case ARMOUR:
			ArmourAbility.setup(mob, abilityChances, optList);
			break;
		case DAMAGE_MULTI:
			DamageAbility.setup(mob, abilityChances, optList);
			break;
		case HEALTH_BONUS:
			HealthAbility.setup(mob, abilityChances, optList);
			break;
		case ITEM_HAND:
			ItemAbility.setup(mob, abilityChances, optList);
			break;
		case POTION:
			PotionAbility.setup(mob, abilityChances, optList);
			break;
		case NONE:
		case BABY:
		case ANGRY:
		case CHARGED:
		default:
			break;
		
		}
		
		return abilityChances.getNumChances() > 0;
	}
	
	public Ability setup(ExtendedEntityType mob, Object opt)
	{
		switch (this)
		{
		case ARMOUR:
			return ArmourAbility.setup(mob, opt);
		case BABY:
			return BabyAbility.ability;
		case ANGRY:
			return AngryAbility.ability;
		case CHARGED:
			return ChargedCreeperAbility.ability;
		case DAMAGE_MULTI:
			return DamageAbility.setup(mob, opt);
		case HEALTH_BONUS:
			return HealthAbility.setup(mob, opt);
		case ITEM_HAND:
			return ItemAbility.setup(mob, opt);
		case NONE:
			return NullAbility.ability;
		case POTION:
			return PotionAbility.setup(mob, opt);
		case ABILITY_SET:
		default:
			return null;
		}
	}
}
