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

package com.forgenz.mobmanager.attributes.abilities;

import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;

import com.forgenz.mobmanager.attributes.AbilityTypes;

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

	@Override
	public void removeAbility(LivingEntity entity)
	{
		if (entity instanceof Ageable)
		{
			Ageable adult = (Ageable) entity;
		
			adult.setAdult();
		}
		else if (entity instanceof Zombie)
		{
			((Zombie) entity).setBaby(false);
		}
	}
	
	public static boolean isValid(EntityType entity)
	{
		if (entity == null)
			return false;
		
		return Ageable.class.isAssignableFrom(entity.getEntityClass());
	}

	@Override
	public AbilityTypes getAbilityType()
	{
		return AbilityTypes.BABY;
	}

}
