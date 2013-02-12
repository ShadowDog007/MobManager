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

package com.forgenz.mobmanager.attributes.abilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.attributes.AbilityTypes;
import com.forgenz.mobmanager.util.ValueChance;

public class ItemAbility extends Ability
{
	private final static HashMap<Material, ItemAbility> itemAbilities = new HashMap<Material, ItemAbility>();
	private final static ItemAbility nullItem = new ItemAbility(null);
	
	public final Material item;
	
	private ItemAbility(Material item)
	{
		this.item = item;
	}

	@Override
	public void addAbility(LivingEntity entity)
	{
		if (item == null)
			return;
		
		entity.getEquipment().setItemInHand(new ItemStack(item));
	}

	@Override
	public void removeAbility(LivingEntity entity)
	{
		if (item == null)
			return;
		
		if (entity.getEquipment().getItemInHand().getType() == item)
			entity.getEquipment().setItemInHand(new ItemStack(Material.AIR));
	}
	
	@Override
	public AbilityTypes getAbilityType()
	{
		return AbilityTypes.ITEM_HAND;
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
			
			if (split == null || !AbilityTypes.ITEM_HAND.valueMatches(split[1]))
			{
				P.p.getLogger().warning("The value " + str + " is invalid for MobAtributes." + mob + "." + AbilityTypes.DAMAGE_MULTI.getConfigPath());
				it.remove();
				continue;
			}
			
			int chance = Integer.valueOf(split[0]);
			Material item = Material.getMaterial(Integer.valueOf(split[1]));
			
			if (chance <= 0 || item == null)
				continue;
			
			abilityChances.addChance(chance, new ItemAbility(item));
		}
	}

	public static Ability setup(EntityType mob, String optVal)
	{
		if (!AbilityTypes.ITEM_HAND.valueMatches(optVal))
		{
			P.p.getLogger().warning("The value " + optVal + " must be an item id (0 for none) for MobAtributes." + mob + "." + AbilityTypes.ITEM_HAND);
			return null;
		}
		
		int itemId = Integer.valueOf(optVal);
		
		if (itemId < 0)
			return nullItem;
			
		Material material = Material.getMaterial(Integer.valueOf(optVal));
		if (material == null)
			material = Material.AIR;
		
		ItemAbility ability = itemAbilities.get(material);
		
		if (ability == null)
		{
			ability = new ItemAbility(material);
			itemAbilities.put(material, ability);
		}
		
		return ability;
	}

}
