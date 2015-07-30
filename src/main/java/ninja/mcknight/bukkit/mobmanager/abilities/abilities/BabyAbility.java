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

import ninja.mcknight.bukkit.mobmanager.abilities.AbilityType;
import ninja.mcknight.bukkit.mobmanager.abilities.config.MobAbilityConfig;
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;
import ninja.mcknight.bukkit.mobmanager.common.util.RandomUtil;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;

public class BabyAbility extends Ability
{
	
	public static final BabyAbility ability = new BabyAbility();
	
	private BabyAbility()
	{
		
	}
	
	@Override
	public void addAbility(LivingEntity entity)
	{
		if (entity instanceof Ageable)
		{
			Ageable baby = (Ageable) entity;
		
			baby.setBaby();
		}
		else if (entity instanceof Zombie)
		{
			((Zombie) entity).setBaby(true);
		}
	}
	
	public static void addByChance(LivingEntity entity, MobAbilityConfig ma)
	{
		if (entity == null || ma == null)
			return;
		
		if (isValid(entity) && ma.babyRate <= 1.0F && ma.babyRate != 0.0F)
		{
			// If the random number is higher than the baby chance we don't turn the mob into a baby
			if (ma.babyRate == 1.0F || RandomUtil.i.nextFloat() < ma.babyRate)
			{
				ability.addAbility(entity);
			}
		}
	}
	
	public static boolean isValid(ExtendedEntityType entity)
	{
		if (entity == null)
			return false;
		
		return isValid(entity.getBukkitEntityType());
	}
	
	public static boolean isValid(EntityType entity)
	{
		if (entity == null || entity.getEntityClass() == null)
			return false;
		
		return Ageable.class.isAssignableFrom(entity.getEntityClass()) || Zombie.class.isAssignableFrom(entity.getEntityClass());
	}
	
	public static boolean isValid(LivingEntity entity)
	{
		return entity instanceof Ageable || entity instanceof Zombie;
	}

	@Override
	public AbilityType getAbilityType()
	{
		return AbilityType.BABY;
	}

}
