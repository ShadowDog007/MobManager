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

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.P;
import ninja.mcknight.bukkit.mobmanager.abilities.AbilityType;
import ninja.mcknight.bukkit.mobmanager.abilities.util.ItemParser;
import ninja.mcknight.bukkit.mobmanager.abilities.util.ValueChance;
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;
import ninja.mcknight.bukkit.mobmanager.common.util.MiscUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class DropsAbility extends Ability
{	
	public static final String metaStorageKey = "MOBMANAGER_DROPS";
	public static final DropsAbility emptyDrops = new DropsAbility(null, false);
	
	private final ArrayList<ItemParser> drops;
	private final boolean replaceDrops;
	
	private DropsAbility(ArrayList<ItemParser> drops, boolean replaceDrops)
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
			for (ItemParser drop : drops)
			{
				// Get a list of itemstacks from the dropset
				List<ItemStack> itemz = drop.getItems();
				
				// If the dropset provided a list add them to the main list
				if (itemz != null)
					items.addAll(itemz);
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
		ArrayList<ItemParser> dropSets = null;
		
		// If drops is provided we fetch each set of drops
		if (drops != null)
		{
			// Create a list for the drops to be stored
			dropSets = new ArrayList<ItemParser>();
			// Iterate through each object and look for drops
			for (Object obj : drops)
			{
				// Fetch the map containing drop settings
				Map<String, Object> dropMap = MiscUtil.getConfigMap(obj, false);
				
				// If no map was found continue
				if (dropMap == null)
					continue;
				
				// Create a new Drop and store it in the list
				ItemParser drop;
				try
				{
					 drop = new ItemParser(dropMap);
				}
				catch (Exception e)
				{
					MMComponent.getAbilities().warning("Invalid item data in DropSet for " + mob);
					continue;
				}
				
				if (!drop.hasValidCountRange())
				{
					ItemStack template = drop.getItem();
					MMComponent.getAbilities().warning("Drop made from " + template.getType() + " for " + mob + " will never drop any items.");
					continue;
				}
				
				dropSets.add(drop);
			}
		}
		
		boolean replaceDrops = MiscUtil.getMapValue(optMap, "REPLACE", null, Boolean.class, false).booleanValue();
		
		// Finally create the drops ability and return it
		return dropSets != null && !dropSets.isEmpty() ? new DropsAbility(dropSets, replaceDrops) : DropsAbility.emptyDrops;
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
