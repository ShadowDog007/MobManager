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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.forgenz.mobmanager.MMComponent;
import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.abilities.AbilityType;
import com.forgenz.mobmanager.abilities.util.ValueChance;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;
import com.forgenz.mobmanager.common.util.MiscUtil;
import com.forgenz.mobmanager.common.util.Patterns;
import com.forgenz.mobmanager.common.util.RandomUtil;

public class DropsAbility extends Ability
{
	public static class DropSet
	{
		private final ItemStack item;

		public final int range;
		public final short durabilityRange;
		
		public DropSet(Material material, byte data, short minDurability, short maxDurability, int min, int max, String title, List<String> lore)
		{
			// Validate min/max counts
			if (min > max)
			{
				min = min ^ max;
				max = min ^ max;
				min = min ^ max;
			}
			// Calculate the difference between min/max counts
			this.range = max - min;
			
			// Validate min/max durability
			if (minDurability > maxDurability)
			{
				minDurability = (short) (minDurability ^ maxDurability);
				maxDurability = (short) (minDurability ^ maxDurability);
				minDurability = (short) (minDurability ^ maxDurability);
			}
			// Calculate the difference between min/max durabilities
			this.durabilityRange = (short) (maxDurability - minDurability);
			
			// Create our ItemStack template
			item = new ItemStack(material, min, minDurability);
			
			// If data is not 0 add the data
			if (data != 0)
			{
				item.setData(item.getType().getNewData(data));
			}
			
			// Setup Title/Lore
			if (title != null || lore != null)
			{
				// Fetch an ItemMeta object
				ItemMeta meta = item.getItemMeta();
				
				// Add the title to ItemMeta
				if (title != null)
				{
					meta.setDisplayName(title);
				}
				
				// Add the lore to ItemMeta
				if (lore != null)
				{
					meta.setLore(lore);
				}
				
				item.setItemMeta(meta);
			}
		}
		
		public boolean hasValidCountRange()
		{
			return item.getAmount() + range > 0;
		}
		
		public boolean addEnchantment(Enchantment e, int level)
		{
			if (e == null)
				return false;
			
			if (level < 0)
				level = 0;
			
			if (item.containsEnchantment(e))
				return false;
			
			item.addUnsafeEnchantment(e, level);
			return true;
		}
		
		public List<ItemStack> getItem()
		{
			// Drop air? I think not.
			if (item.getType() == Material.AIR)
				return null;
			
			// Calculate the number of items to create
			int count = range > 0 ? RandomUtil.i.nextInt(range + 1) + item.getAmount() : item.getAmount();
			
			// Make sure count is more than 0
			if (count <= 0)
				return null;
			
			// Create a list of the perfect size :)
			List<ItemStack> itemz = new ArrayList<ItemStack>(count / item.getType().getMaxStackSize() + 1);
			
			// Create the item stack/stacks
			while (count > 0)
			{
				// Clone our template
				ItemStack clone = item.clone();
				// Set an appropriate amount for the item stack
				clone.setAmount(count > item.getType().getMaxStackSize() ? item.getType().getMaxStackSize() : count);
				
				itemz.add(clone);
				count -= item.getType().getMaxStackSize();
			}
			
			// Return the item in question
			return itemz;
		}
	}
	
	public static final String metaStorageKey = "MOBMANAGER_DROPS";
	public static final DropsAbility emptyDrops = new DropsAbility(null, false);
	
	private final ArrayList<DropSet> drops;
	private final boolean replaceDrops;
	
	private DropsAbility(ArrayList<DropSet> drops, boolean replaceDrops)
	{
		this.drops = drops;
		this.replaceDrops = replaceDrops;
	}
	
	@Override
	public void addAbility(LivingEntity entity)
	{
		if (drops != null)
		{
			entity.setMetadata(metaStorageKey, new FixedMetadataValue(P.p(), this));
		}
	}

	@Override
	public AbilityType getAbilityType()
	{
		return AbilityType.DROPS;
	}
	
	public boolean replaceDrops()
	{
		return replaceDrops;
	}
	
	public List<ItemStack> getItemList()
	{
		// Creates a list of items
		List<ItemStack> items = new ArrayList<ItemStack>();
		
		// Make sure there are drops
		if (drops != null)
		{
			// Iterate through each dropset
			for (DropSet drop : drops)
			{
				// Get a list of itemstacks from the dropset
				List<ItemStack> itemz = drop.getItem();
				
				// If the dropset provided a list add them to the main list
				if (itemz != null)
				{
					for (ItemStack item : itemz)
					{
						items.add(item);
					}
				}
			}
		}
		
		return items;
	}
	
	public static DropsAbility getAbility(LivingEntity entity)
	{
		List<MetadataValue> metaList = entity.getMetadata(metaStorageKey);
		
		if (metaList == null)
			return null;
		
		for (MetadataValue meta : metaList)
		{
			if (meta.getOwningPlugin() != P.p())
				continue;
			
			if (meta.value() instanceof DropsAbility)
				return (DropsAbility) meta.value();
		}
		
		return null;
	}

	public static void setup(ExtendedEntityType mob, ValueChance<Ability> abilityChances, List<Object> optList)
	{
		Iterator<?> it = optList.iterator();
		
		// Iterate through each object
		while (it.hasNext())
		{
			// Fetch the map object
			Map<String, Object> optMap = MiscUtil.getConfigMap(it.next());
			
			// Continue if no map was found
			if (optMap == null)
			{
				MMComponent.getAbilities().warning("Invalid options given to " + AbilityType.DROPS + " ability");
				continue;
			}
			
			// Fetch the chance for the object
			int chance = MiscUtil.getInteger(optMap.get("CHANCE"));
			
			// Validate the chance
			if (chance <= 0)
				continue;
			
			// Fetch the ability
			DropsAbility ability = setup(mob, optMap);
			
			// If the ability is valid add it to the abilityChances
			if (ability != null)
				abilityChances.addChance(chance, ability);
		}
	}
	
	public static DropsAbility setup(ExtendedEntityType mob, Map<String, Object> optMap)
	{
		List<Object> drops = MiscUtil.getList(optMap.get("DROPS"));
		ArrayList<DropSet> dropSets = null;
		
		// If drops is provided we fetch each set of drops
		if (drops != null)
		{
			// Create a list for the drops to be stored
			dropSets = new ArrayList<DropSet>();
			// Iterate through each object and look for drops
			for (Object obj : drops)
			{
				// Fetch the map containing drop settings
				Map<String, Object> dropMap = MiscUtil.getConfigMap(obj);
				
				// If no map was found continue
				if (dropMap == null)
					continue;
				
				// Fetch the ID  for the drop
				int id = MiscUtil.getInteger(dropMap.get("ID"), -1);
				
				// Fetch the material for the drop
				Material material = Material.getMaterial(id);
				
				// If the material is invalid, check next drop
				if (material == null)
				{
					MMComponent.getAbilities().warning("No such Item ID " + id + " for " + mob);
					continue;
				}
				
				// Fetch the data for the drop
				byte data = (byte) MiscUtil.getInteger(dropMap.get("DATA"), 0);
				
				// Fetch the min and max counts for the drop
				int minCount = MiscUtil.getInteger(dropMap.get("MINCOUNT"), 1);
				int maxCount = MiscUtil.getInteger(dropMap.get("MAXCOUNT"), minCount);
				
				// Fetch the min and max durabilities
				short minDurability = (short) MiscUtil.getInteger(dropMap.get("MINDURABILITY"), 0);
				short maxDurability = (short) MiscUtil.getInteger(dropMap.get("MAXDURABILITY"), minDurability);
				
				// Fetch the title of the drop
				String title = MiscUtil.getString(dropMap.get("TITLE"));
				if (title != null)
					title = ChatColor.translateAlternateColorCodes('&', title);
				
				// Fetch and validate the lore of the drop
				List<Object> lore = MiscUtil.getList(dropMap.get("LORE"));
				List<String> colouredLore = null;
				if (lore != null)
				{
					colouredLore = new ArrayList<String>();
					// Colour the lore and add it to the list
					for (Object loreLine : lore)
					{
						if (loreLine instanceof String)
							colouredLore.add(ChatColor.translateAlternateColorCodes('&', (String) loreLine));
					}
					// If there is no lore? What are we doing?
					if (colouredLore.isEmpty())
						colouredLore = null;
				}
				
				// Create a new DropSet and store it in the list
				DropSet drop = new DropSet(material, data, minDurability, maxDurability, minCount, maxCount, title, colouredLore);
				
				if (!drop.hasValidCountRange())
				{
					MMComponent.getAbilities().warning("DropSet made from " + id + "-" + material + " for " + mob + " will never drop any items.");
					continue;
				}
				
				dropSets.add(drop);
				
				// Fetch enchantments and add them to the DropSet
				List<Object> enchantments = MiscUtil.getList(dropMap.get("ENCHANTMENTS"));
				
				// Ignore enchantments if key does not match
				if (enchantments == null)
					continue;
				
				// Find enchantments from the list
				for (Object enchObj : enchantments)
				{
					// Fetch the string representing the enchantment
					String ench = MiscUtil.getString(enchObj);
					
					// If no string was found check next object
					if (ench == null)
						continue;
					
					// Split the enchantment and the level
					String[] split = Patterns.colonSplit.split(ench);
					
					// If WTF ignore the enchantment
					if (split.length < 1)
						continue;
					
					// Fetch the enchantment object
					Enchantment enchantment = Enchantment.getByName(split[0].toUpperCase());
					
					if (enchantment == null)
					{
						MMComponent.getAbilities().warning("Invalid enchantment given: " + split[0].toUpperCase());
						continue;
					}
					
					// Make sure you can enchant the given material with the found enchantment
					if (!enchantment.canEnchantItem(new ItemStack(material)))
					{
						MMComponent.getAbilities().warning("Can not enchant " + material.toString() + " with the enchantment: " + enchantment.toString());
						continue;
					}
					
					// Get the level of the enchantment
					int level = split.length == 2 && Patterns.numberCheck.matcher(split[1]).matches() ? Integer.valueOf(split[1]) : enchantment.getStartLevel();
					
					// Validate the enchantment level
					if (level > enchantment.getMaxLevel())
						level = enchantment.getMaxLevel();
					
					if (level < enchantment.getStartLevel())
						level = enchantment.getStartLevel();
					
					// Add the enchantment to the DropSet
					drop.addEnchantment(enchantment, level);
				}
			}
		}
		
		Boolean replaceDrops = MiscUtil.getMapValue(optMap, "REPLACE", null, Boolean.class);
		
		// Finally create the drops ability and return it
		return drops.size() > 0 ? new DropsAbility(dropSets, replaceDrops != null ? replaceDrops : false) : DropsAbility.emptyDrops;
	}

	public static DropsAbility setup(ExtendedEntityType mob, Object opt)
	{
		Map<String, Object> optMap = MiscUtil.getConfigMap(opt);
		
		if (optMap == null)
		{
			MMComponent.getAbilities().warning(String.format("Found an error in abilities config for %s-Drops. The value must be a map", mob.toString()));
		}
		
		return optMap == null ? null : setup(mob, optMap);
	}

}
