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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ninja.mcknight.bukkit.mobmanager.abilities.util.ValueChance;
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.abilities.AbilityType;
import ninja.mcknight.bukkit.mobmanager.abilities.util.ItemParser;
import ninja.mcknight.bukkit.mobmanager.common.util.MiscUtil;

public class EquipmentAbility extends Ability
{
	public static final Pattern valuePattern = Pattern.compile("^(DIAMOND|IRON|CHAIN|GOLD|LEATHER|NONE|DEFAULT)$", Pattern.CASE_INSENSITIVE);
	
	public enum ArmourPosition
	{
		HELMET(0), CHESTPLATE(1), LEGGINGS(2), BOOTS(3), HAND(4);
		
		public final byte p;
		
		ArmourPosition(int p)
		{
			this.p = (byte) p;
		}
		
	}
	public enum ArmourMaterial
	{
		DIAMOND(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.DIAMOND_SWORD),
		IRON(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.IRON_SWORD),
		CHAIN(Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, Material.IRON_SWORD),
		GOLD(Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS, Material.GOLD_SWORD),
		LEATHER(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.WOOD_SWORD),
		NONE(Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR),
		DEFAULT(null, null, null, null, null);
		
		private final Material[] materials = new Material[5]; 
		
		ArmourMaterial(Material head, Material chest, Material legs, Material feet, Material hand)
		{
			materials[ArmourPosition.HELMET.p] = head;
			materials[ArmourPosition.CHESTPLATE.p] = chest;
			materials[ArmourPosition.LEGGINGS.p] = legs;
			materials[ArmourPosition.BOOTS.p] = feet;
			materials[ArmourPosition.HAND.p] = hand;
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
		private final ItemParser itemTemplate;
		
		public Armour(ArmourPosition position, ItemParser item, float dropChance)
		{
			this.position = position;
			this.dropChance = dropChance < 0.0F ? 0.15F : dropChance;
			
			this.itemTemplate = item;
		}
		
		public void addArmour(LivingEntity entity)
		{
			// If no template exists we do nothing
			if (itemTemplate == null)
				return;
			
			// Create a copy of the item template
			ItemStack item = itemTemplate.getItem();
			EntityEquipment equipment = entity.getEquipment();
			
			switch (position)
			{
			case BOOTS:
				equipment.setBoots(item);
				equipment.setBootsDropChance(dropChance);
				break;
			case CHESTPLATE:
				equipment.setChestplate(item);
				equipment.setChestplateDropChance(dropChance);
				break;
			case HELMET:
				equipment.setHelmet(item);
				equipment.setHelmetDropChance(dropChance);
				break;
			case LEGGINGS:
				equipment.setLeggings(item);
				equipment.setLeggingsDropChance(dropChance);
				break;	
			case HAND:
				equipment.setItemInHand(item);
				equipment.setItemInHandDropChance(dropChance);
				break;
			}
		}
	}
	
	private final ArrayList<Armour> armour;
	
	private EquipmentAbility(ArrayList<Armour> armour)
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
		return AbilityType.EQUIPMENT_SET;
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
				
			EquipmentAbility ability = setup(mob, MiscUtil.getList(optMap.get("PIECES")));
			
			if (ability != null)
				abilityChances.addChance(chance, ability);
		}
	}
	
	public static EquipmentAbility setup(ExtendedEntityType mob, List<Object> optList)
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
		
		return new EquipmentAbility(armourList);
	}
	
	public static Armour getArmour(Map<String, Object> optMap)
	{
		// Check for valid option map
		if (optMap == null)
			return null;
		
		String tmpStr = MiscUtil.getString(optMap.get("POSITION"));
		
		if (tmpStr == null)
		{
			MMComponent.getAbilities().warning("Missing equipment position in abilities.yml");
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
			MMComponent.getAbilities().info("Invalid equipment position given: " + tmpStr);
			return null;
		}
		
		// Fetch the Item
		ItemParser item = null;
		// If the custom item does not exist we check for a material type
		if (optMap.containsKey("CUSTOMITEM"))
		{
			Map<String, Object> itemMap = MiscUtil.getConfigMap(optMap.get("CUSTOMITEM"), false);
			
			if (itemMap != null)
			{
				try
				{
					item = new ItemParser(itemMap);
				}
				catch (Exception e)
				{
					MMComponent.getAbilities().severe("Error in CustomItem for equipment, falling back to material type");
				}
			}
			else
			{
				MMComponent.getAbilities().warning("Invalid CustomItem data for equipment, falling back to material type.");
			}
		}

		if (item == null)
		{
			tmpStr = MiscUtil.getString(optMap.get("MATERIAL"));
			
			if (tmpStr == null || !valuePattern.matcher(tmpStr).matches())
			{
				if (tmpStr != null)
				{
					MMComponent.getAbilities().warning("Invalid armour material given: " + tmpStr);
				}
				return null;
			}
			
			ArmourMaterial armourMaterial = ArmourMaterial.valueOf(tmpStr.toUpperCase());
			
			Material material = armourMaterial.getMaterial(position);
			
			item = material != null ? new ItemParser(new ItemStack(material)) : null;
		}
		
		float dropChance = MiscUtil.getFloat(optMap.get("DROPCHANCE"), 0.15F);
		
		return new Armour(position, item, dropChance);
	}
	
	public static EquipmentAbility setup(ExtendedEntityType mob, Object opt)
	{
		List<Object> optList = MiscUtil.getList(opt);
		if (optList != null)
			return setup(mob, optList);
		
		MMComponent.getAbilities().warning(String.format("Found an error in abilities config for %s-Equipmentset. The value must be a list of equipment peices", mob.toString()));
		return null;
	}
}
