package com.forgenz.mobmanager.attributes.abilities;

import java.util.regex.Pattern;

import org.bukkit.entity.LivingEntity;

import com.forgenz.mobmanager.attributes.AbilityTypes;

public abstract class Ability
{
	private static final Pattern chanceSplit = Pattern.compile(":");
	
	/** Adds the ability to the entity */
	public abstract void addAbility(LivingEntity entity);
	
	/** Removes the ability from the entity (For reloads) */
	public abstract void removeAbility(LivingEntity entity);
	
	public abstract AbilityTypes getAbilityType();
	
	protected static String getString(Object obj)
	{
		if (obj instanceof String)
			return (String) obj;
		
		return null;
	}
	
	protected static String[] getChanceSplit(String str)
	{
		if (str == null)
			return null;
		
		String[] split = chanceSplit.split(str);
		
		return split.length == 2 ? split : null;
	}

	@Override
	public int hashCode()
	{
		return getAbilityType() != null ? getAbilityType().hashCode() : 0;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		Ability other = (Ability) obj;
		if (getAbilityType() != other.getAbilityType())
		{
			return false;
		}
		return true;
	}
	
}
