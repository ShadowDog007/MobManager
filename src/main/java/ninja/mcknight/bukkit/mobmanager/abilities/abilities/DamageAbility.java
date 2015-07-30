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
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.P;
import ninja.mcknight.bukkit.mobmanager.abilities.AbilityType;
import ninja.mcknight.bukkit.mobmanager.common.util.MiscUtil;

public class DamageAbility extends Ability
{
	private static final String metadataKey = "MOBMANAGER_DAMAGE_MULTI";
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
			entity.removeMetadata(metadataKey, P.p());
		}
		else
		{	
			entity.setMetadata(metadataKey, new FixedMetadataValue(P.p(), multi));
		}
	}
	
	public static float getMetaValue(LivingEntity entity)
	{
		List<MetadataValue> meta = entity.getMetadata(metadataKey);
		
		if (meta == null)
			return 1.0F;
		
		for (MetadataValue val : meta)
		{
			if (val.getOwningPlugin() != P.p())
				continue;
			
			return val.asFloat();
		}
		
		return 1.0F;
	}
	
	@Override
	public AbilityType getAbilityType()
	{
		return AbilityType.DAMAGE_MULTI;
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
			
			Object obj = MiscUtil.getMapValue(optMap, "MULTI", "DamageMuli-" + mob, Object.class);
			
			if (obj == null)
				continue;
			
			float multi = MiscUtil.getFloat(obj);
			
			if (multi < 0)
			{
				MMComponent.getAbilities().warning("Damage multipliers must be positive!");
				multi = 1.0F;
			}
			
			abilityChances.addChance(chance, new DamageAbility(multi));
		}
	}
	
	public static DamageAbility setup(ExtendedEntityType mob, float multi)
	{
		if (multi < 0.0F)
		{
			MMComponent.getAbilities().warning("Damage multipliers must be positive!");
			multi = 1.0F;
		}
		
		return new DamageAbility(multi);
	}
	
	public static DamageAbility setup(ExtendedEntityType mob, Object obj)
	{
		if (obj == null)
			return null;
		
		float multi = MiscUtil.getFloat(obj, Float.MIN_VALUE);
		
		if (multi == Float.MIN_VALUE)
		{
			MMComponent.getAbilities().warning(String.format("Found an error in abilities config for %s-DamageMulti. The value must be a decimal number", mob.toString()));
			multi = 1.0F;
		}
		
		return setup(mob, MiscUtil.getFloat(obj));
	}
}
