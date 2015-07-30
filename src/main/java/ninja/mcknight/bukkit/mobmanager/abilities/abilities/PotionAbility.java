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

package ninja.mcknight.bukkit.mobmanager.abilities.abilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.abilities.util.ValueChance;
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;
import ninja.mcknight.bukkit.mobmanager.common.util.MiscUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ninja.mcknight.bukkit.mobmanager.abilities.AbilityType;

public class PotionAbility extends Ability
{
	public static final Pattern potionMatcher = Pattern.compile("^(" + PotionAbility.getPotionEffectList() + ")(:[0-3])?$", Pattern.CASE_INSENSITIVE);
	
	private static final PotionAbility nullPotion = new PotionAbility(null);
	
	public static final AbilityType effect = AbilityType.POTION;
	
	
	public final HashMap<PotionEffectType, Integer> potionEffects;
	
	private PotionAbility(HashMap<PotionEffectType, Integer> potionEffects)
	{		
		this.potionEffects = potionEffects;
	}

	@Override
	public void addAbility(LivingEntity entity)
	{
		if (potionEffects == null)
			return;
		
		for (Entry<PotionEffectType, Integer> e : potionEffects.entrySet())
		{
			PotionEffectType potionEffect = e.getKey();
		
			PotionEffect effect = new PotionEffect(potionEffect, Integer.MAX_VALUE, e.getValue());
		
			entity.addPotionEffect(effect, true);
		}
	}
	
	@Override
	public AbilityType getAbilityType()
	{
		return AbilityType.POTION;
	}
	
	public static String getPotionEffectList()
	{
		String list = "";
		
		for (PotionEffectType effect : PotionEffectType.values())
		{
			if (effect == null)
				continue;
			
			if (effect.isInstant())
				continue;
			
			if (list.length() != 0)
				list += "|";
			
			list += effect.getName();
		}
		
		list += "|NONE";
		
		return list;
	}

	public static void setup(ExtendedEntityType mob, ValueChance<Ability> abilityChances, List<?> optList)
	{
		Iterator<?> it = optList.iterator();
		
		while (it.hasNext())
		{
			Map<String, Object> optMap = MiscUtil.getConfigMap(it.next());
			
			if (optMap == null)
				continue;
			
			int chance = MiscUtil.getInteger(optMap.get("CHANCE"));
			
			if (chance <= 0)
				continue;
			
			@SuppressWarnings("unchecked")
			PotionAbility ability = setup(mob, MiscUtil.getMapValue(optMap, "EFFECTS", mob.toString(), List.class));
			
			if (ability != null)
				abilityChances.addChance(chance, ability);
		}
	}
	
	public static PotionAbility setup(ExtendedEntityType mob, List<Object> optList)
	{
		if (optList == null)
			return null;
		
		HashMap<PotionEffectType, Integer> potionEffects = null;
		
		for (Object obj : optList)
		{
			if (obj instanceof String == false)
				continue;
			
			String optVal = (String) obj;
			
			if (!potionMatcher.matcher(optVal).matches())
			{
				MMComponent.getAbilities().warning("The potion '" + optVal + "' does not exist for MobAtributes." + mob + "." + AbilityType.POTION);
				return null;
			}
			
			if (optVal.equalsIgnoreCase("NONE"))
				continue;
			
			PotionEffectType potionEffect = null;
			
			String[] potionSplit = optVal.split(":");
			
			if (potionSplit.length != 2 && potionSplit.length != 1)
				continue;
			
			for (PotionEffectType effect : PotionEffectType.values())
			{
				if (effect == null)
					continue;
				
				if (effect.getName().equalsIgnoreCase(potionSplit[0]))
				{
					potionEffect = effect;
					break;
				}
			}
			
			if (potionEffect == null)
			{
				MMComponent.getAbilities().warning("No potion effect found named " + optVal);
				continue;
			}
			
			Integer potency = potionSplit.length == 2 ? Integer.valueOf(potionSplit[1]) : 0;
			
			if (potionEffects == null)
				potionEffects = new HashMap<PotionEffectType, Integer>();
			potionEffects.put(potionEffect, potency);
		}
		
		if (potionEffects == null || potionEffects.size() == 0)
			return nullPotion;
		
		PotionAbility ability = new PotionAbility(potionEffects);
		
		return ability;
	}
	
	@SuppressWarnings("unchecked")
	public static PotionAbility setup(ExtendedEntityType mob, Object opt)
	{
		if (opt instanceof List)
			return setup(mob, (List<Object>) opt);
		
		MMComponent.getAbilities().warning(String.format("Found an error in abilities config for %s-PotionAbility", mob.toString()));
		return null;
	}
}
