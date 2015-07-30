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

import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import ninja.mcknight.bukkit.mobmanager.P;
import ninja.mcknight.bukkit.mobmanager.abilities.AbilityType;
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;

public class DeathSpawnAbility extends AbstractSpawnAbility
{
	private final static String metadataKey = "MOBMANAGER_SPAWN_DEATH";
	protected DeathSpawnAbility(ExtendedEntityType type, int count, String abilitySet, int range, int heightRange)
	{
		super(type, count, abilitySet, range, heightRange);
	}

	@Override
	public void addAbility(LivingEntity entity)
	{
		// If the entity is dead we spawn entities on it
		if (entity.isDead())
		{
			super.addAbility(entity);
		}
		// Else we store the ability as metadata for use when the enitity dies
		else
		{
			entity.setMetadata(metadataKey, new FixedMetadataValue(P.p(), this));
		}
	}

	@Override
	public AbilityType getAbilityType()
	{
		return AbilityType.DEATH_SPAWN;
	}
	
	public static DeathSpawnAbility getDeathSpawnAbility(LivingEntity entity)
	{
		List<MetadataValue> metadata = entity.getMetadata(metadataKey);
		
		if (metadata == null)
			return null;
		
		for (MetadataValue metaValue : metadata)
		{
			if (metaValue.getOwningPlugin() != P.p())
				continue;
			
			if (metaValue.value() instanceof DeathSpawnAbility)
				return (DeathSpawnAbility) metaValue.value();
		}
		
		return null;
	}

}
