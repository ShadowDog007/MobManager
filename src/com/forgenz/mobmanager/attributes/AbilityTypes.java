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

package com.forgenz.mobmanager.attributes;

import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.entity.EntityType;

import com.forgenz.mobmanager.attributes.abilities.Ability;
import com.forgenz.mobmanager.attributes.abilities.AbilitySet;
import com.forgenz.mobmanager.attributes.abilities.ArmourAbility;
import com.forgenz.mobmanager.attributes.abilities.BabyAbility;
import com.forgenz.mobmanager.attributes.abilities.DamageAbility;
import com.forgenz.mobmanager.attributes.abilities.HealthAbility;
import com.forgenz.mobmanager.attributes.abilities.ItemAbility;
import com.forgenz.mobmanager.attributes.abilities.NullAbility;
import com.forgenz.mobmanager.attributes.abilities.PotionAbility;
import com.forgenz.mobmanager.util.ValueChance;

public enum AbilityTypes
{
	NONE(null, false, null, Pattern.compile("^$")),
	
	POTION("PotionEffects", PotionAbility.class, Pattern.compile("^" + PotionAbility.getPotionEffectList() + "$", Pattern.CASE_INSENSITIVE)),
	
	HEALTH_BONUS("HealthBonus", HealthAbility.class, Pattern.compile("^-?\\d+$")),
	
	DAMAGE_MULTI("DamageMulti", DamageAbility.class, Pattern.compile("^[0-9]+\\.[0-9]+$")),
	
	ARMOUR("Armour", ArmourAbility.class, Pattern.compile("^(DIAMOND|IRON|CHAIN|GOLD|LEATHER|NONE|DEFAULT)$", Pattern.CASE_INSENSITIVE)),
	
	ITEM_HAND("StartingItem", ItemAbility.class, Pattern.compile("^-1|\\d+$")),
	
	BABY("BabyRate", false, BabyAbility.class, Pattern.compile("^$")),
	
	ABILITY_SET("AbilitySets", AbilitySet.class, Pattern.compile("$(.+,{1})*(.+){1}$"));
	
	private final String abilityConfigPath;
	private final Class<? extends Ability> clazz;
	private final Pattern valuePattern;
	private final boolean valueChanceAbility;
	
	AbilityTypes(String abilityConfigPath, Class<? extends Ability> clazz, Pattern valuePattern)
	{
		this(abilityConfigPath, true, clazz, valuePattern);
	}
	
	AbilityTypes(String abilityConfigPath, boolean valueChanceAbility, Class<? extends Ability> clazz, Pattern valuePattern)
	{
		this .abilityConfigPath = abilityConfigPath;
		this.clazz = clazz;
		this.valuePattern = valuePattern;
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
	
	public Class<?> getAbilityClass()
	{
		return clazz;
	}
	
	public boolean valueMatches(String setting)
	{
		return valuePattern.matcher(setting).matches();
	}
	
	public static AbilityTypes getAbilityType(String option)
	{
		for (AbilityTypes ability : AbilityTypes.values())
		{
			if (ability.valueMatches(option))
				return ability;
		}
		
		return null;
	}
	
	public boolean setup(EntityType mob, ValueChance<Ability> abilityChances, List<?> optList)
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
		default:
			break;
		
		}
		
		return abilityChances.getNumChances() > 0;
	}
	
	public Ability setup(EntityType mob, String optVal)
	{
		switch (this)
		{
		case ARMOUR:
			return ArmourAbility.setup(mob, optVal);
		case BABY:
			return BabyAbility.ability;
		case DAMAGE_MULTI:
			return DamageAbility.setup(mob, optVal);
		case HEALTH_BONUS:
			return HealthAbility.setup(mob, optVal);
		case ITEM_HAND:
			return ItemAbility.setup(mob, optVal);
		case NONE:
			return NullAbility.ability;
		case POTION:
			return PotionAbility.setup(mob, optVal);
		case ABILITY_SET:
		default:
			return null;
		}
	}
}
