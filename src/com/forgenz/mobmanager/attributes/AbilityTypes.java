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
