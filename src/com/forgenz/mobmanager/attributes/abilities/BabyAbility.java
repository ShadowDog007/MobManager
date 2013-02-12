package com.forgenz.mobmanager.attributes.abilities;

import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;

import com.forgenz.mobmanager.attributes.AbilityTypes;

public class BabyAbility extends Ability
{
	
	public static final BabyAbility ability = new BabyAbility();
	
	private BabyAbility()
	{
		
	}
	
	@Override
	public void addAbility(LivingEntity entity)
	{
		if (entity instanceof Ageable)
		{
			Ageable baby = (Ageable) entity;
		
			baby.setBaby();
		}
		else if (entity instanceof Zombie)
		{
			((Zombie) entity).setBaby(true);
		}
	}

	@Override
	public void removeAbility(LivingEntity entity)
	{
		if (entity instanceof Ageable)
		{
			Ageable adult = (Ageable) entity;
		
			adult.setAdult();
		}
		else if (entity instanceof Zombie)
		{
			((Zombie) entity).setBaby(false);
		}
	}
	
	public static boolean isValid(EntityType entity)
	{
		if (entity == null)
			return false;
		
		return Ageable.class.isAssignableFrom(entity.getEntityClass());
	}

	@Override
	public AbilityTypes getAbilityType()
	{
		return AbilityTypes.BABY;
	}

}
