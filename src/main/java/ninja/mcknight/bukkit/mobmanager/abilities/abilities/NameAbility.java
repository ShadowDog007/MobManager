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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ninja.mcknight.bukkit.mobmanager.abilities.util.ValueChance;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import ninja.mcknight.bukkit.mobmanager.MMComponent.Component;
import ninja.mcknight.bukkit.mobmanager.abilities.AbilityType;
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;
import ninja.mcknight.bukkit.mobmanager.common.util.MiscUtil;

public class NameAbility extends Ability
{
	public final String name;
	public final boolean showOverhead;

	private NameAbility(String name, boolean showOverhead)
	{
		this.name = name;
		this.showOverhead = showOverhead;
	}
	
	@Override
	public void addAbility(LivingEntity entity)
	{
		if (entity.getType() == EntityType.PLAYER)
		{
			return;
		}
		
		if (entity.getCustomName() == null)
		{
			entity.setCustomName(name);
			if (showOverhead)
				entity.setCustomNameVisible(true);
		}
	}

	@Override
	public AbilityType getAbilityType()
	{
		return AbilityType.NAME;
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
			
			NameAbility ability = setup(mob, optMap);
			
			if (ability != null)
				abilityChances.addChance(chance, ability);
		}
	}
	
	public static NameAbility setup(ExtendedEntityType mob, Map<String, Object> optMap)
	{
		String name = MiscUtil.getString(optMap.get("NAME"));
		
		if (name == null)
		{
			Component.ABILITIES.warning("Missing name in NameAbility" + (mob != null ? " for " + mob : ""));
			return null;
		}
		
		name = ChatColor.translateAlternateColorCodes('&', name);
		
		boolean showOverhead = MiscUtil.getBoolean(optMap.get("SHOWOVERHEAD"), true);
		
		return new NameAbility(name, showOverhead);
	}
	
	public static NameAbility setup(ExtendedEntityType mob, Object opt)
	{
		if (opt == null)
			return null;
		
		Map<String, Object> optMap = MiscUtil.getConfigMap(opt);
		
		return setup(mob, optMap);
	}
}
