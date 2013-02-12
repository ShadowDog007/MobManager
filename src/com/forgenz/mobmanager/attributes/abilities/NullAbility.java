package com.forgenz.mobmanager.attributes.abilities;

import org.bukkit.entity.LivingEntity;

import com.forgenz.mobmanager.attributes.AbilityTypes;

public class NullAbility extends Ability
{
	
	public static final NullAbility ability = new NullAbility();
	
	private NullAbility()
	{
		
	}

	@Override
	public void addAbility(LivingEntity entity)
	{
	}

	@Override
	public void removeAbility(LivingEntity entity)
	{
	}

	@Override
	public AbilityTypes getAbilityType()
	{
		return AbilityTypes.NONE;
	}

}
