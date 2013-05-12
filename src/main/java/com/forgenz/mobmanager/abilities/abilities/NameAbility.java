package com.forgenz.mobmanager.abilities.abilities;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import com.forgenz.mobmanager.MMComponent.Component;
import com.forgenz.mobmanager.abilities.AbilityType;
import com.forgenz.mobmanager.abilities.util.ValueChance;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;
import com.forgenz.mobmanager.common.util.MiscUtil;

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
