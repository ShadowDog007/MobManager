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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.abilities.AbilityType;
import com.forgenz.mobmanager.abilities.util.MiscUtil;
import com.forgenz.mobmanager.abilities.util.ValueChance;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;

public class AbilitySet extends Ability
{
	/** Stores all the ability sets */
	private final static HashMap<String, AbilitySet> abilitySets = new HashMap<String, AbilitySet>();
	public static void resetAbilitySets()
	{
		abilitySets.clear();
		// Add default none ability
		abilitySets.put("none", new AbilitySet(null, null, null, false));
	}
	
	private final HashSet<Ability> abilities;
	protected final ExtendedEntityType type;
	private final String name;
	private final boolean showOverheadName;
	
	public static AbilitySet getAbilitySet(String name)
	{
		return abilitySets.get(name.toLowerCase());
	}
	
	public static String[] getAbilitySetNames()
	{
		return abilitySets.keySet().toArray(new String[abilitySets.size()]);
	}
	
	private AbilitySet(HashSet<Ability> abilities, ExtendedEntityType type, String name, boolean showOverheadName)
	{
		this.abilities = abilities;
		this.type = type;
		this.name = name;
		this.showOverheadName = showOverheadName;
	}

	@Override
	public void addAbility(LivingEntity entity)
	{
		if (abilities == null)
			return;
		
		// Add each ability to the entity
		for (Ability ability : abilities)
		{
			ability.addAbility(entity);
		}
		
		// If the entity has a name show it
		if (name != null)
		{
			entity.setCustomName(name);
			if (showOverheadName)
				entity.setCustomNameVisible(true);
		}
	}

	@Override
	public AbilityType getAbilityType()
	{
		return AbilityType.ABILITY_SET;
	}
	
	public int getNumAbilities()
	{
		if (abilities == null)
			return 0;
		return abilities.size();
	}
	
	public ExtendedEntityType getAbilitySetsEntityType()
	{
		return type;
	}
	
	public static void createAbilitySet(Map<String, Object> optMap)
	{
		// If there are no options return
		if (optMap == null)
			return;
		
		// Fetch the Sets name from the map
		String name = MiscUtil.getMapValue(optMap, "NAME", "AbilitySets", String.class);
		
		// If no name is given return
		if (name == null)
			return;
		
		// If the name already exists return
		if (abilitySets.containsKey(name.toLowerCase()))
		{
			P.p().getLogger().warning("AbilitySet with name " + name + " already exists");
			return;
		}
		
		// Fetch the default mob type if it exists
		ExtendedEntityType entityType = null;
		if (optMap.containsKey("MOBTYPE"))
		{
			String key = MiscUtil.getString(optMap.get("MOBTYPE"));
			if (key != null)
				entityType = ExtendedEntityType.get(key);
			
			if (entityType == null)
			{
				P.p().getLogger().warning("Invalid EntityType " + key + " in AbilitySets");
			}
		}
		
		// Get the list of abilities for the set
		List<?> optList = MiscUtil.getMapValue(optMap, "OPTIONS", "AbilitySets", List.class);
		
		// If the list is missing return
		if (optList == null)
			return;
		
		// Create the HashSet which will store the abilities
		HashSet<Ability> abilities = new HashSet<Ability>();
		// Iterate through each object in the list
		for (Object obj : optList)
		{
			// If the object is not a map its invalid
			if (obj instanceof Map == false)
				continue;
			
			// Cast the object to a map
			Map<?, ?> map = (Map<?, ?>) obj;
			
			// If there are no elements in the map it is invalid
			if (map.size() == 0)
				continue;
			
			// Fetch what should be the only key
			Object str = map.keySet().toArray(new Object[1])[0];
			
			// If the key is not a string the map is invalid
			if (str instanceof String == false)
				continue;
			
			// Determine which ability type the key is for
			AbilityType abilityType = AbilityType.getAbilityType((String) str);
			
			// If no ability type was found produce an error and continue
			if (abilityType == null)
			{
				P.p().getLogger().warning("The ability " + str + " does not exist");
				continue;
			}
			
			// Setup the ability
			Ability ability = abilityType.setup(ExtendedEntityType.get(EntityType.UNKNOWN), map.get(str));
			
			// If the ability was successfully created add it to the ability set
			if (ability != null)
				abilities.add(ability);
		}
		
		Boolean showName = MiscUtil.getMapValue(optMap, "SHOWNAME", null, Boolean.class);
		if (showName == null)
			showName = false;
		
		Boolean showOverheadName = MiscUtil.getMapValue(optMap, "SHOWOVERHEADNAME", null, Boolean.class);
		if (showOverheadName == null)
			showOverheadName = false;
		
		// If at least one ability was created we save the ability set
		if (abilities.size() > 0)
			abilitySets.put(name.toLowerCase(), new AbilitySet(abilities, entityType, showName || showOverheadName ? name : null, showOverheadName));
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
			
			String setName = MiscUtil.getMapValue(optMap, "SETNAME", mob.toString(), String.class);
			
			if (setName == null)
				continue;
			
			AbilitySet set = getAbilitySet(setName);
			
			if (set == null)
			{
				P.p().getLogger().warning("Missing SetName " + setName + " for " + mob);
				continue;
			}
			
			abilityChances.addChance(chance, set);
		}
	}

}
