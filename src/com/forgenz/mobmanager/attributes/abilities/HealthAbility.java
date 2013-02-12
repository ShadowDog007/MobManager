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
