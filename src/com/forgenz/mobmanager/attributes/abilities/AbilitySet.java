package com.forgenz.mobmanager.attributes.abilities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.attributes.AbilityTypes;
import com.forgenz.mobmanager.util.ValueChance;

public class AbilitySet extends Ability
{
	private final static AbilitySet nullSet = new AbilitySet(new HashSet<Ability>());
	
	private final static Pattern abilitySplit = Pattern.compile(",");
	private final static Pattern valueSplit = Pattern.compile("=");
	
	private final HashSet<Ability> abilities;
	
	private AbilitySet(HashSet<Ability> abilities)
	{
		this.abilities = abilities;
	}

	@Override
	public void addAbility(LivingEntity entity)
	{
		for (Ability ability : abilities)
		{
			ability.addAbility(entity);
		}
	}

	@Override
	public void removeAbility(LivingEntity entity)
	{
		for (Ability ability : abilities)
		{
			ability.removeAbility(entity);
		}
	}

	@Override
	public AbilityTypes getAbilityType()
	{
		return AbilityTypes.ABILITY_SET;
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
				P.p.getLogger().warning("The value " + str + " is invalid for MobAtributes." + mob + "." + AbilityTypes.ABILITY_SET.getConfigPath());
				it.remove();
				continue;
			}
			
			int chance = Integer.valueOf(split[0]);
			
			if (chance <= 0)
				continue;
			
			HashSet<Ability> abilities = new HashSet<Ability>();
			
			String[] abilityStrings = abilitySplit.split(split[1]);
			
			for (String abilityString : abilityStrings)
			{
				split = valueSplit.split(abilityString);
				
				if (split.length != 2)
				{
					if (split.length == 1 && (split[0].equalsIgnoreCase(AbilityTypes.NONE.toString()) || split[0].equalsIgnoreCase(AbilityTypes.BABY.toString())))
					{
						split = new String[] {split[0], ""};
					}
					else
					{
						P.p.getLogger().warning("The ability " + abilityString + " is invalid for the ability set " + str);
						continue;
					}
				}
				AbilityTypes abilityType = null;
				
				for (AbilityTypes type : AbilityTypes.values())
				{
					if (type.toString().equalsIgnoreCase(split[0]))
					{
						abilityType = type;
						break;
					}
				}
				
				if (abilityType == null)
				{
					P.p.getLogger().warning("The ability " + split[0] + " does not exist");
					continue;
				}
				
				Ability ability = abilityType.setup(mob, split[1]);
				
				if (ability != null)
					abilities.add(ability);
			}
			
			if (abilities.size() == 0)
			{
				abilityChances.addChance(chance, nullSet);
			}
			else
			{
				abilityChances.addChance(chance, new AbilitySet(abilities));
			}
		}
	}

}
