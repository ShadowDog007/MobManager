package com.forgenz.mobmanager.attributes.abilities;

import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.attributes.AbilityTypes;
import com.forgenz.mobmanager.util.ValueChance;

public class DamageAbility extends Ability
{
	public final float multi;

	private DamageAbility(float multi)
	{
		this.multi = multi;
	}
	
	@Override
	public void addAbility(LivingEntity entity)
	{
		if (multi == 1.0F)
			return;
		
		// Multiplies the multiplier with the existing multiplier (If found) >:D
		float multi = this.multi * getMetaValue(entity);
		
		if (multi == 1.0F)
		{
			entity.removeMetadata("MOBMANAGER_DAMAGE_MULTI", P.p);
		}
		else
		{	
			entity.setMetadata("MOBMANAGER_DAMAGE_MULTI", new FixedMetadataValue(P.p, multi));
		}
	}

	@Override
	public void removeAbility(LivingEntity entity)
	{
		entity.removeMetadata("MOBMANAGER_DAMAGE_MULTI", P.p);
	}
	
	public static float getMetaValue(LivingEntity entity)
	{
		List<MetadataValue> meta = entity.getMetadata("MOBMANAGER_DAMAGE_MULTI");
		
		if (meta == null)
			return 1.0F;
		
		for (MetadataValue val : meta)
		{
			if (val.getOwningPlugin() != P.p)
				continue;
			
			return val.asFloat();
		}
		
		return 1.0F;
	}
	
	@Override
	public AbilityTypes getAbilityType()
	{
		return AbilityTypes.DAMAGE_MULTI;
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
				P.p.getLogger().warning("The value " + str + " is invalid for MobAtributes." + mob + "." + AbilityTypes.DAMAGE_MULTI.getConfigPath());
				it.remove();
				continue;
			}
			
			int chance = Integer.valueOf(split[0]);
			float multi = Float.valueOf(split[1]);
			
			if (chance <= 0)
				continue;
			
			if (multi < 0)
			{
				P.p.getLogger().warning("Damage multipliers must be positive!");
				multi = 1.0F;
			}
			
			abilityChances.addChance(chance, new DamageAbility(multi));
		}
	}
	
	public static DamageAbility setup(EntityType mob, String optVal)
	{
		if (!AbilityTypes.DAMAGE_MULTI.valueMatches(optVal))
		{
			P.p.getLogger().warning("The value " + optVal + " is invalid for MobAtributes." + mob + "." + AbilityTypes.DAMAGE_MULTI);
			return null;
		}
		
		return new DamageAbility(Float.valueOf(optVal));
	}
	
}
