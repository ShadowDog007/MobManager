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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.abilities.AbilityType;
import com.forgenz.mobmanager.abilities.util.MiscUtil;
import com.forgenz.mobmanager.abilities.util.ValueChance;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;

public abstract class AbstractSpawnAbility extends Ability
{
	protected final static Location loc = new Location(null, 0, 0, 0);
	private static boolean spawning = false;
	
	private final ExtendedEntityType type;
	private final int count;
	private final String abilitySet;
	
	protected AbstractSpawnAbility(ExtendedEntityType type, int count, String abilitySet)
	{
		this.type = type;
		this.count = count;
		this.abilitySet = abilitySet;
	}

	@Override
	public void addAbility(LivingEntity entity)
	{
		if (spawning || type == null)
			return;
		// Make sure the bonus mobs don't recursively spawn more bonus mobs
		spawning = true;
		
		AbilitySet abilities = abilitySet != null ? AbilitySet.getAbilitySet(abilitySet) : null;
		
		// Spawn each mob
		for (int i = 0; i < count; ++i)
		{
			// If the mob has an ability set we do not add any abilities
			if (abilities != null)
				P.p.abilitiesIgnoreNextSpawn(true);
			
			if (!P.p.abilityCfg.limitBonusSpawns)
				P.p.limiterIgnoreNextSpawn(true);
			
			// Spawn the entity
			LivingEntity spawnedEntity = type.spawnMob(entity.getLocation(loc));
			
			if (abilities != null)
				abilities.addAbility(spawnedEntity);
		}
		
		// Make sure we can spawn more mobs later :)
		spawning = false;
	}

	@Override
	public void removeAbility(LivingEntity entity)
	{
		// Nothing to see here folks, move along
		return;
	}

	public static void setup(AbilityType type, ExtendedEntityType mob, ValueChance<Ability> abilityChances, List<Object> optList)
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
			
			AbstractSpawnAbility ability = setup(type, mob, optMap);
			
			if (ability != null)
				abilityChances.addChance(chance, ability);
		}
	}

	public static AbstractSpawnAbility setup(AbilityType type, ExtendedEntityType mob, Map<String, Object> optMap)
	{
		String mobTypeString = MiscUtil.getMapValue(optMap, "MOBTYPE", (type == AbilityType.BIRTH_SPAWN ? "Birth" : "Death") + " Spawn", String.class);
		
		if (mobTypeString == null)
			return null;
		
		ExtendedEntityType mobType = mobTypeString.equalsIgnoreCase("NONE") ? null : ExtendedEntityType.get(mobTypeString);
		
		if (!mobTypeString.equalsIgnoreCase("NONE") && mobType == null)
		{
			P.p.getLogger().warning("No EntityType called " + mobTypeString + " for " + (type == AbilityType.BIRTH_SPAWN ? "Birth" : "Death") + " Spawn");
			return null;
		}
		
		int count = MiscUtil.getInteger(optMap.get("COUNT"));
		
		if (count <= 0)
			count = 1;
		
		String abilitySet = MiscUtil.getString(optMap.get("ABILITYSET"));
		
		switch (type)
		{
		case BIRTH_SPAWN:
			return new BirthSpawnAbility(mobType, count, abilitySet);
		case DEATH_SPAWN:
			return new DeathSpawnAbility(mobType, count, abilitySet);
		default:
			return null;
		}
	}

	public static Ability setup(AbilityType type, ExtendedEntityType mob, Object opt)
	{
		Map<String, Object> map = MiscUtil.getConfigMap(opt);
		
		if (map == null)
			return null;
		
		return setup(type, mob, map);
	}

}
