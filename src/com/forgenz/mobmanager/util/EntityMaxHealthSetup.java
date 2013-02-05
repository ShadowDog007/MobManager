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

package com.forgenz.mobmanager.util;

import org.bukkit.entity.Damageable;

import com.forgenz.mobmanager.config.MobAttributes;

public class EntityMaxHealthSetup
{
	public static void setMaxHealth(Damageable entity, MobAttributes mobAttributes)
	{
		if (mobAttributes == null || entity == null)
			return;
		
		// Add the players bonus health
		int bonusHealth = mobAttributes.bonusHealth.getBonus();
		
		// Fetch the entities old max/actual health (Used for HP scaling)
		int oldHp = entity.getHealth();
		int oldMax = entity.getMaxHealth();

		// Make sure we do not add too much HP to the entity
		entity.resetMaxHealth();

		// Calculate the new maximum value
		int newMax = bonusHealth + entity.getMaxHealth();

		// Validate the new maximum
		if (newMax <= 0)
			newMax = 1;

		// Set the new maximum
		entity.setMaxHealth(newMax);

		// Scale the entities HP relative to its old/new maximum
		entity.setHealth(oldHp * newMax / oldMax);
	}
}
