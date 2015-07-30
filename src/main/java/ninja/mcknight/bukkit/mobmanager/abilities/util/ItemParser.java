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

package ninja.mcknight.bukkit.mobmanager.abilities.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import ninja.mcknight.bukkit.mobmanager.common.util.MiscUtil;
import ninja.mcknight.bukkit.mobmanager.common.util.RandomUtil;

public class ItemParser
{
	private ItemStack item;
	
	private int amountDiff = 0;
	private int damageDiff = 0;
	
	public ItemParser(ItemStack item)
	{
		this.item = item.clone();
	}
	
	public ItemParser(Map<String, Object> itemMap)
	{
		// Validate item type
		if (itemMap.containsKey("type"))
		{
			String typeName = null;

			// Check if type is an item id
			int val = MiscUtil.getInteger(itemMap.get("type"), -1);
			if (val != -1)
			{
				@SuppressWarnings("deprecation")
				Material material = Material.getMaterial(val);
				if (material != null)
					typeName = material.name();
			}

			// If typeName is null we fetch the object as a string
			if (typeName == null)
			{
				typeName = MiscUtil.getString(itemMap.get("type")).toUpperCase();
			}
		}

		item = ItemStack.deserialize(itemMap);

		// Validate the min/max range
		if (itemMap.containsKey("amount-max"))
		{
			int min = item.getAmount();
			int max = MiscUtil.getInteger(itemMap.get("amount-max"), min);

			// Max sure min is actually the min
			if (min > max)
			{
				max = max ^ min;
				min = max ^ min;
				max = max ^ min;

				item.setAmount(min);
			}
			
			if (max < 1)
			{
				max = 1;
			}

			amountDiff = max - min;
		}
		else if (item.getAmount() < 1)
		{
			item.setAmount(1);
		}
		
		// Validate the damage
		if (itemMap.containsKey("amount-max"))
		{
			int min = item.getDurability();
			int max = MiscUtil.getInteger(itemMap.get("damage-max"), min);

			// Max sure min is actually the min
			if (min > max)
			{
				max = max ^ min;
				min = max ^ min;
				max = max ^ min;

				item.setDurability((short) min);
			}

			damageDiff = max - min;
		}
		
		// Colourise everything!!!
		colouriseMeta(item, true);
	}
	
	private String colouriseString(String str, boolean toColour)
	{
		return toColour ? ChatColor.translateAlternateColorCodes('&', str) : str.replace('\u00A7', '&');
	}
	
	private void colouriseMeta(ItemStack item, boolean toColour)
	{
		// Colourise everything!!!
		if (item.hasItemMeta())
		{
			ItemMeta meta = item.getItemMeta();
			
			// Colour display name
			if (meta.hasDisplayName())
				meta.setDisplayName(colouriseString(meta.getDisplayName(), toColour));
			// Colour lore
			if (meta.hasLore())
			{
				List<String> lore = meta.getLore();
				for (int i = 0; i < lore.size(); ++i)
					lore.set(i, colouriseString(lore.get(i), toColour));
				meta.setLore(lore);
			}
			
			// Check if the item is a book
			if (item.getType() == Material.WRITTEN_BOOK || item.getType() == Material.BOOK_AND_QUILL)
			{
				BookMeta bmeta = (BookMeta) meta;
				
				// Colour author
				if (bmeta.hasAuthor())
					bmeta.setAuthor(colouriseString(bmeta.getAuthor(), toColour));
				// Colour title
				if (bmeta.hasTitle())
					bmeta.setTitle(colouriseString(bmeta.getTitle(), toColour));
				// Colour pages
				if (bmeta.hasPages())
				{
					ArrayList<String> pages = new ArrayList<String>(bmeta.getPages());
					for (int i = 0; i < pages.size(); ++i)
						pages.set(i, colouriseString(pages.get(i), toColour));
					bmeta.setPages(pages);
				}
			}
			
			item.setItemMeta(meta);
		}
	}
	
	public ItemStack getItem()
	{
		// Clone the template
		ItemStack item = this.item.clone();
		
		// Set the stack size
		item.setAmount(item.getType() != Material.AIR ? 1 : 0);
		// Randomise the damage
		int damage = damageDiff > 0 ? RandomUtil.i.nextInt(damageDiff + 1) + item.getDurability() : item.getDurability();
		item.setDurability((short) damage);
		
		return item;
	}
	
	public List<ItemStack> getItems()
	{
		// Drop air? I think not.
		if (item.getType() == Material.AIR)
			return null;
		
		// Calculate the number of items to create
		int count = amountDiff > 0 ? RandomUtil.i.nextInt(amountDiff + 1) + item.getAmount() : item.getAmount();
		int damage = damageDiff > 0 ? RandomUtil.i.nextInt(damageDiff + 1) + item.getDurability() : item.getDurability();
		
		// Make sure count is more than 0
		if (count <= 0)
			return null;
		
		// Create a list of the perfect size :)
		List<ItemStack> items = new ArrayList<ItemStack>(count / item.getType().getMaxStackSize() + 1);
		
		// Create the item stack/stacks
		while (count > 0)
		{
			// Clone our template
			ItemStack clone = item.clone();
			// Set an appropriate amount for the item stack
			clone.setAmount(count > item.getType().getMaxStackSize() ? item.getType().getMaxStackSize() : count);
			
			// Set the damage
			clone.setDurability((short) damage);
			
			items.add(clone);
			count -= item.getType().getMaxStackSize();
		}
		
		// Return the item in question
		return items;
	}
	
	public boolean hasValidCountRange()
	{
		return item.getAmount() + amountDiff > 0;
	}
	
	public Map<String, Object> serialize()
	{
		ItemStack item = this.item.clone();
		
		// De-colourise everything :(
		colouriseMeta(item, false);
		
		Map<String, Object> itemMap = item.serialize();
		
		itemMap.put("amount-max", item.getAmount() + amountDiff);
		itemMap.put("damage-max", item.getDurability() + damageDiff);
		
		return itemMap;
	}
}
