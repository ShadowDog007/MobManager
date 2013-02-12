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

import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.attributes.AbilityTypes;
import com.forgenz.mobmanager.util.ValueChance;

public class ArmourAbility extends Ability
{
	
	public enum ArmourMaterials
	{
		DIAMOND(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS),
		IRON(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS),
		CHAIN(Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS),
		GOLD(Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS),
		LEATHER(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS),
		NONE(Material.AIR, Material.AIR, Material.AIR, Material.AIR),
		DEFAULT(null, null, null, null);
		
		private ArmourAbility ability = null;
		private final Material head;
		private final Material chest;
		private final Material legs;
		private final Material feet;
		
		ArmourMaterials(Material head, Material chest, Material legs, Material feet)
		{
			this.head = head;
			this.chest = chest;
			this.legs = legs;
			this.feet = feet;
		}
		
		public void setHead(LivingEntity entity)
		{
			if (head != null)
				entity.getEquipment().setHelmet(new ItemStack(head));
		}
		
		public void setChest(LivingEntity entity)
		{
			if (head != null)
				entity.getEquipment().setChestplate(new ItemStack(chest));
		}
		
		public void setLegs(LivingEntity entity)
		{
			if (head != null)
				entity.getEquipment().setLeggings(new ItemStack(legs));
		}
		
		public void setFeet(LivingEntity entity)
		{
			if (head != null)
				entity.getEquipment().setBoots(new ItemStack(feet));
		}
		
		public ArmourAbility getAbility()
		{
			if (ability == null)
				ability = new ArmourAbility(this);
			return ability;
		}
	}
	
	private final ArmourMaterials material;
	
	private ArmourAbility(ArmourMaterials material)
	{
		this.material = material;
	}

	@Override
	public void addAbility(LivingEntity entity)
	{
		if (entity == null || entity instanceof Player)
			return;
		
		material.setHead(entity);
		material.setChest(entity);
		material.setLegs(entity);
		material.setFeet(entity);
	}

	@Override
	public void removeAbility(LivingEntity entity)
	{
		for (ItemStack mat : entity.getEquipment().getArmorContents())
		{
			if (mat.getType() != material.head && mat.getType() != material.chest && mat.getType() != material.legs && mat.getType() != material.feet)
				return;
		}
		
		ArmourMaterials.NONE.setHead(entity);
		ArmourMaterials.NONE.setChest(entity);
		ArmourMaterials.NONE.setLegs(entity);
		ArmourMaterials.NONE.setFeet(entity);
		
	}
	
	@Override
	public AbilityTypes getAbilityType()
	{
		return AbilityTypes.ARMOUR;
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
				P.p.getLogger().warning("The value " + str + " is invalid for MobAtributes." + mob + "." + AbilityTypes.ARMOUR.getConfigPath());
				it.remove();
				continue;
			}
			
			int chance = Integer.valueOf(split[0]);
			
			if (chance == 0)
				continue;
			
			ArmourAbility ability = setup(mob, split[1]);
			
			if (ability != null)
				abilityChances.addChance(chance, ability);
		}
	}
	
	public static ArmourAbility setup(EntityType mob, String optVal)
	{
		if (!AbilityTypes.ARMOUR.valueMatches(optVal))
		{
			P.p.getLogger().warning("The armour type " + optVal + " is invalid for MobAtributes." + mob + "." + AbilityTypes.ARMOUR);
			return null;
		}
		
		ArmourMaterials material = null;
		
		for (ArmourMaterials mat : ArmourMaterials.values())
		{
			if (mat.toString().equalsIgnoreCase(optVal))
			{
				material = mat;
				break;
			}
		}
		
		if (material == null)
		{
			P.p.getLogger().warning("The armour type " + optVal + " is invalid for MobAtributes." + mob + "." + AbilityTypes.ARMOUR);
			return ArmourMaterials.NONE.getAbility();
		}
		
		return material.getAbility();
	}
	
}
