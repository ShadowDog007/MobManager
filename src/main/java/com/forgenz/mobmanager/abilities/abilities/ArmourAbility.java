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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.forgenz.mobmanager.MMComponent;
import com.forgenz.mobmanager.abilities.AbilityType;
import com.forgenz.mobmanager.abilities.util.ValueChance;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;
import com.forgenz.mobmanager.common.util.MiscUtil;
import com.forgenz.mobmanager.common.util.Patterns;

public class ArmourAbility extends Ability
{
	public static final Pattern valuePattern = Pattern.compile("^(DIAMOND|IRON|CHAIN|GOLD|LEATHER|NONE|DEFAULT)$", Pattern.CASE_INSENSITIVE);
	
	public enum ArmourPosition
	{
		HELMET(0), CHESTPLATE(1), LEGGINGS(2), BOOTS(3);
		
		public final byte p;
		
		ArmourPosition(int p)
		{
			this.p = (byte) p;
		}
		
	}
	public enum ArmourMaterials
	{
		DIAMOND(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS),
		IRON(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS),
		CHAIN(Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS),
		GOLD(Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS),
		LEATHER(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS),
		NONE(Material.AIR, Material.AIR, Material.AIR, Material.AIR),
		DEFAULT(null, null, null, null);
		
		private final Material[] materials = new Material[4]; 
		
		ArmourMaterials(Material head, Material chest, Material legs, Material feet)
		{
			materials[ArmourPosition.HELMET.p] = head;
			materials[ArmourPosition.CHESTPLATE.p] = chest;
			materials[ArmourPosition.LEGGINGS.p] = legs;
			materials[ArmourPosition.BOOTS.p] = feet;
		}
		
		public Material getMaterial(ArmourPosition position)
		{
			return materials[position.p];
		}
	}
	
	public static class Armour
	{
		public final ArmourPosition position;
		public final float dropChance;
		private final ItemStack itemTemplate;
		
		public Armour(ArmourPosition position, float dropChance, ArmourMaterials material, ArrayList<Enchantment> enchantments, ArrayList<Integer> enchantLevels)
		{
			this.position = position;
			this.dropChance = dropChance < 0.0F ? 0.15F : dropChance;
			
			// The following generates the template itemstack
			
			// Fetches and validates the material
			Material itemMat = material.getMaterial(position);
			
			if (itemMat == null)
			{
				itemTemplate = null;
				return;
			}
			
			// Create an Item Stack with the new material
			itemTemplate = new ItemStack(itemMat);
			
			// Add enchantments to the item stack
			if (enchantments != null && enchantLevels != null && enchantments.size() == enchantLevels.size())
			{
				// Iterate through each enchantment
				for (int i = 0; i < enchantments.size(); ++i)
				{
					Enchantment e = enchantments.get(i);
					// Check if you can enchant the item
					if (e.canEnchantItem(itemTemplate))
					{
						// Fetch the level
						Integer level = enchantLevels.get(i);
						
						// Validate the level
						if (level != null)
						{
							if (level > e.getMaxLevel())
							{
								level = e.getMaxLevel();
							}
							else if (level < e.getStartLevel())
							{
								level = e.getStartLevel();
							}
						}
						else
						{
							level = e.getStartLevel();
						}
						
						// Add the enchantment to the item stack
						itemTemplate.addEnchantment(e, level);
					}
				}
			}
		}
		
		public void addArmour(LivingEntity entity)
		{
			// If no template exists we do nothing
			if (itemTemplate == null)
				return;
			
			// Create a copy of the item template
			ItemStack item = new ItemStack(itemTemplate);
			
			switch (position)
			{
			case BOOTS:
				entity.getEquipment().setBoots(item);
				entity.getEquipment().setBootsDropChance(dropChance);
				break;
			case CHESTPLATE:
				entity.getEquipment().setChestplate(item);
				entity.getEquipment().setChestplateDropChance(dropChance);
				break;
			case HELMET:
				entity.getEquipment().setHelmet(item);
				entity.getEquipment().setHelmetDropChance(dropChance);
				break;
			case LEGGINGS:
				entity.getEquipment().setLeggings(item);
				entity.getEquipment().setLeggingsDropChance(dropChance);
				break;			
			}
		}
	}
	
	private final ArrayList<Armour> armour;
	
	private ArmourAbility(ArrayList<Armour> armour)
	{
		this.armour = armour;
	}

	@Override
	public void addAbility(LivingEntity entity)
	{
		if (entity == null || entity instanceof Player || armour == null)
			return;
		
		for (Armour a : armour)
		{
			a.addArmour(entity);
		}
	}
	
	@Override
	public AbilityType getAbilityType()
	{
		return AbilityType.ARMOURSET;
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
				
			ArmourAbility ability = setup(mob, MiscUtil.getList(optMap.get("PIECES")));
			
			if (ability != null)
				abilityChances.addChance(chance, ability);
		}
	}
	
	public static ArmourAbility setup(ExtendedEntityType mob, List<Object> optList)
	{
		ArrayList<Armour> armourList = null;
		
		if (optList != null)
		{
			for (Object obj : optList)
			{
				Armour armour = getArmour(MiscUtil.getConfigMap(obj));
				
				if (armour != null)
				{
					if (armourList == null)
						armourList = new ArrayList<Armour>(1);
					
					armourList.add(armour);
				}
			}
		}
		
		return new ArmourAbility(armourList);
	}
	
	public static Armour getArmour(Map<String, Object> optMap)
	{
		// Check for valid option map
		if (optMap == null)
			return null;
		
		String tmpStr = MiscUtil.getString(optMap.get("POSITION"));
		
		if (tmpStr == null)
		{
			MMComponent.getAbilities().warning("Missing armour position in abilities.yml");
			return null;
		}
		
		ArmourPosition position = null;
		// Find the armour position
		for (ArmourPosition pos : ArmourPosition.values())
		{
			if (tmpStr.equalsIgnoreCase(pos.toString()))
			{
				position = pos;
				break;
			}
		}
		
		if (position == null)
		{
			MMComponent.getAbilities().info("Invalid armour position given: " + tmpStr);
			return null;
		}
		
		// Fetch the material
		tmpStr = MiscUtil.getString(optMap.get("MATERIAL"));
		
		if (tmpStr == null || !valuePattern.matcher(tmpStr).matches())
		{
			if (tmpStr != null)
			{
				MMComponent.getAbilities().warning("Invalid armour material given: " + tmpStr);
			}
			return null;
		}
		
		ArmourMaterials armourMaterial = ArmourMaterials.valueOf(tmpStr.toUpperCase());
		
		float dropChance = MiscUtil.getFloat(optMap.get("DROPCHANCE"), 0.15F);
		
		List<Object> enchantmentList = MiscUtil.getList(optMap.get("ENCHANTMENTS"));
		
		ArrayList<Enchantment> enchantments = null;
		ArrayList<Integer> enchantLevels = null;
		
		if (enchantmentList != null)
		{
			enchantments = new ArrayList<Enchantment>();
			enchantLevels = new ArrayList<Integer>();
			
			for (Object obj : enchantmentList)
			{
				String enchantString = MiscUtil.getString(obj);
				
				if (enchantString == null)
					continue;
				
				// Split the string <Enchantment>:<Level>
				String[] split = Patterns.colonSplit.split(enchantString);
				
				if (split.length == 0)
					continue;
				
				// Fetch the enchantment
				Enchantment enchantment = Enchantment.getByName(split[0].toUpperCase());
				
				if (enchantment == null)
				{
					MMComponent.getAbilities().warning("Invalid Enchantment given to ArmourSet: " + split[0]);
					continue;
				}
				
				// Fetch the level for the enchantment
				int level = split.length == 2 ? MiscUtil.getInteger(split[1], enchantment.getStartLevel()) : enchantment.getStartLevel();
				// Validate the level
				if (level > enchantment.getMaxLevel())
					level = enchantment.getMaxLevel();
				else if (level < enchantment.getStartLevel())
					level = enchantment.getStartLevel();
				
				// Add the enchantment and its level to the lists
				enchantments.add(enchantment);
				enchantLevels.add(level);
			}
			
			if (enchantments.size() == 0)
			{
				enchantments = null;
				enchantLevels = null;
			}
		}
		
		return new Armour(position, dropChance, armourMaterial, enchantments, enchantLevels);
	}
	
	public static ArmourAbility setup(ExtendedEntityType mob, Object opt)
	{
		List<Object> optList = MiscUtil.getList(opt);
		if (optList != null)
			return setup(mob, optList);
		
		MMComponent.getAbilities().warning(String.format("Found an error in abilities config for %s-ArmourSset. The value must be a list of armour peices", mob.toString()));
		return null;
	}
}
