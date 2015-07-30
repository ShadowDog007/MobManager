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

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import ninja.mcknight.bukkit.mobmanager.P;
import ninja.mcknight.bukkit.mobmanager.abilities.AbilityType;
import ninja.mcknight.bukkit.mobmanager.abilities.config.MobAbilityConfig;
import ninja.mcknight.bukkit.mobmanager.common.util.RandomUtil;

public class SunProofAbility extends Ability
{
	
	private static final String METADATA_KEY = "MobManager_SunProofAbility";
	public static Ability ability = new SunProofAbility();
	
	private SunProofAbility()
	{
		
	}
	
	@Override
	public void addAbility(LivingEntity entity)
	{
		entity.setMetadata(METADATA_KEY, new FixedMetadataValue(P.p(), ability));
	}
	
	public static void addByChance(LivingEntity entity, MobAbilityConfig ma)
	{
		if (ma == null || !isValid(entity))
			return;
		
		if (ma.sunProofRate <= 1.0 && ma.sunProofRate != 0.0)
		{
			// If the random number is lower than the sunproof rate we make the entity sun proof :D
			if (ma.sunProofRate == 1.0F || RandomUtil.i.nextFloat() < ma.sunProofRate)
			{
				ability.addAbility(entity);
			}
		}
	}
	
	public static boolean isSunProof(LivingEntity entity)
	{
		for (MetadataValue meta : entity.getMetadata(METADATA_KEY))
		{
			if (meta.getOwningPlugin() == P.p())
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isValid(LivingEntity entity)
	{
		return isValid(entity.getType());
	}
	
	public static boolean isValid(EntityType entity)
	{
		switch (entity)
		{
		case ZOMBIE:
		case SKELETON:
			return true;
		default:
			return false;
		}
	}

	@Override
	public AbilityType getAbilityType()
	{
		return AbilityType.SUNPROOF;
	}

}
