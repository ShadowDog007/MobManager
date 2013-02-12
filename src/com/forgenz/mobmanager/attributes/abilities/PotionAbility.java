package com.forgenz.mobmanager.attributes.abilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.attributes.AbilityTypes;
import com.forgenz.mobmanager.util.ValueChance;

public class PotionAbility extends Ability
{
	private static final HashMap<PotionEffectType, PotionAbility> potionAbilities = new HashMap<PotionEffectType, PotionAbility>();
	private static final PotionAbility nullPotion = new PotionAbility(null);
	
	public static final AbilityTypes effect = AbilityTypes.POTION;
	
	public final PotionEffectType potionEffect;
	
	private PotionAbility(PotionEffectType potionEffect)
	{		
		this.potionEffect = potionEffect;
	}

	@Override
	public void addAbility(LivingEntity entity)
	{
		if (potionEffect == null)
			return;
		
		PotionEffect effect = new PotionEffect(potionEffect, Integer.MAX_VALUE, 1);
		
		entity.addPotionEffect(effect);
	}
	
	@Override
	public void removeAbility(LivingEntity entity)
	{
		if (potionEffect == null)
			return;
		
		for (PotionEffect effect : entity.getActivePotionEffects())
		{
			if (effect.getType() != potionEffect)
				continue;
			
			if (effect.getDuration() > Integer.MAX_VALUE >> 1)
				entity.removePotionEffect(potionEffect);
		}
	}
	
	@Override
	public AbilityTypes getAbilityType()
	{
		return AbilityTypes.POTION;
	}
	
	public static String getPotionEffectList()
	{
		String list = "";
		
		for (PotionEffectType effect : PotionEffectType.values())
		{
			if (effect == null)
				continue;
			
			if (list.length() != 0)
				list += "|";
			
			list += effect.getName();
		}
		
		list += "|NONE";
		
		return list;
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
				P.p.getLogger().warning("The value " + str + " is invalid for MobAtributes." + mob + "." + AbilityTypes.POTION.getConfigPath());
				it.remove();
				continue;
			}
			
			int chance = Integer.valueOf(split[0]);
			if (chance <= 0)
				continue;
			
			PotionAbility ability = setup(mob, split[1]);
			
			if (ability != null)
				abilityChances.addChance(chance, ability);
		}
	}
	
	public static PotionAbility setup(EntityType mob, String optVal)
	{
		if (!AbilityTypes.POTION.valueMatches(optVal))
		{
			P.p.getLogger().warning("The potion '" + optVal + "' does not exist for MobAtributes." + mob + "." + AbilityTypes.POTION);
			return null;
		}
		
		PotionEffectType potionEffect = null;
		
		for (PotionEffectType effect : PotionEffectType.values())
		{
			if (effect == null)
				continue;
			
			if (effect.getName().equalsIgnoreCase(optVal))
			{
				potionEffect = effect;
				break;
			}
		}
		
		if (potionEffect == null)
		{
			P.p.getLogger().warning("No potion effect found named " + optVal);
			return nullPotion;
		}
		
		PotionAbility ability = potionAbilities.get(potionEffect);
		
		if (ability == null)
		{
			ability = new PotionAbility(potionEffect);
			potionAbilities.put(potionEffect, ability);
		}
		
		return ability;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((potionEffect == null) ? 0 : potionEffect.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!super.equals(obj))
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		PotionAbility other = (PotionAbility) obj;
		if (potionEffect == null)
		{
			if (other.potionEffect != null)
			{
				return false;
			}
		} else if (!potionEffect.equals(other.potionEffect))
		{
			return false;
		}
		return true;
	}

}
