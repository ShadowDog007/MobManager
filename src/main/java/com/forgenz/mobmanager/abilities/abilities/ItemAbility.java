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

package com.forgenz.mobmanager.abilities.abilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import com.forgenz.mobmanager.MMComponent;
import com.forgenz.mobmanager.abilities.AbilityType;
import com.forgenz.mobmanager.abilities.config.AbilityConfig;
import com.forgenz.mobmanager.abilities.config.MobAbilityConfig;
import com.forgenz.mobmanager.abilities.util.ValueChance;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;
import com.forgenz.mobmanager.common.util.MiscUtil;

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
		
		MobAbilityConfig ma = AbilityConfig.i().getMobConfig(entity.getWorld().getName(), ExtendedEntityType.get(entity), null);
		
		float dropChance = ma != null ? ma.equipmentDropChance : 0.15F; 
		
		entity.getEquipment().setItemInHand(new ItemStack(item));
		entity.getEquipment().setItemInHandDropChance(dropChance);
	}
	
	@Override
	public AbilityType getAbilityType()
	{
		return AbilityType.ITEM_HAND;
	}

	public static void setup(ExtendedEntityType mob, ValueChance<Ability> abilityChances, List<Object> optList)
	{
		Iterator<Object> it = optList.iterator();
		
		while (it.hasNext())
		{
			Map<String, Object> optMap = MiscUtil.getConfigMap(it.next());
			
			if (optMap == null)
				continue;
			
			int chance = MiscUtil.getInteger(optMap.get("CHANCE"));
			
			if (chance <= 0)
				continue;
			
			Object obj = MiscUtil.getMapValue(optMap, "ID", "ITEM_HAND-" + mob, Object.class);
			
			if (obj == null)
				continue;
			
			ItemAbility ability = setup(mob, MiscUtil.getInteger(obj));
			
			if (ability != null)
				abilityChances.addChance(chance, ability);
		}
	}

	public static ItemAbility setup(ExtendedEntityType mob, int optVal)
	{
		
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
	
	public static ItemAbility setup(ExtendedEntityType mob, Object opt)
	{
		if (opt == null)
			return null;
		
		int itemId = MiscUtil.getInteger(opt, Integer.MIN_VALUE);
		
		if (itemId == Integer.MIN_VALUE)
		{
			MMComponent.getAbilities().warning(String.format("Found an error in abilities config for %s-ItemAbility. The value must be a whole number", mob.toString()));
			itemId = 0;
		}
		
		return setup(mob, itemId);
	}

}
