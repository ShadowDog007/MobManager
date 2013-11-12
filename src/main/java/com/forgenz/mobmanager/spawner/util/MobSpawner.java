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

package com.forgenz.mobmanager.spawner.util;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.forgenz.mobmanager.MMComponent;
import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.abilities.config.AbilityConfig;
import com.forgenz.mobmanager.abilities.config.MobAbilityConfig;
import com.forgenz.mobmanager.abilities.listeners.AbilitiesMobListener;
import com.forgenz.mobmanager.spawner.config.Mob;
import com.forgenz.mobmanager.spawner.config.Region;

/**
 * Handles spawning of mobs
 */
public class MobSpawner
{
	private final Region region;
	private final Player player;
	private final Location location;
	private final Mob mob;
	
	private final int playerY;
	private final int heightRange;
	
	public MobSpawner(Region region, Player player, Location location, Mob mob, int playerY, int heightRange)
	{
		this.region = region;
		this.player = player;
		this.location = location;
		this.mob = mob;
		
		this.playerY = playerY;
		this.heightRange = heightRange;
	}
	
	public boolean spawn()
	{
		// Don't let the limiter or abilities component mess with us
		P.p().ignoreNextSpawn(true);
		
		// Add the height offset to the mobs spawn location
		if (!mob.addHeightOffset(location, playerY, heightRange))
			return false;
		
		LivingEntity entity = mob.getMobType().spawnMob(location);
		
		if (entity == null)
			return false;
		
		
		MobReference mobRef = new MobReference(entity);
		if (!mob.bypassMobManagerLimit &&
				(!MMComponent.getSpawner().getSpawnFinder().addSpawnedMob(player, mob, mobRef) || !region.addSpawnedMob(mob, mobRef))
				|| !mob.addSpawnedMob(mobRef))
		{
			mobRef.invalidate();
			entity.remove();
			return false;
		}
		
		mob.executeAction(location);
		
		// Apply abilities to the mob
		if (MMComponent.getAbilities().isEnabled())
		{
			MobAbilityConfig mobCfg = null;
			MobAbilityConfig rateMa = null;
			boolean applyNormalAbilities = true;
			
			// Check if there is an ability set
			if (mob.getAbilitySet() != null)
			{
				// Check if we should apply normal abilities
				if (mob.getAbilitySet().applyNormalAbilities())
					// Fetch the mobs config
					mobCfg = AbilityConfig.i().getMobConfig(entity.getWorld().getName(), mob.getMobType(), null);
				else
					applyNormalAbilities = false;
				
				// Add the set abilities to the mob
				mob.getAbilitySet().addAbility(entity);
				// Fetch the rates config
				rateMa = mob.getAbilitySet().getAbilityConfig();
			}
			// If we don't have rates from the ability set fetch the default ones
			else
			{
				mobCfg = rateMa = AbilityConfig.i().getMobConfig(entity.getWorld().getName(), mob.getMobType(), null);
				
				// If there is no config for this mob we are done
				if (mobCfg == null)
					return true;
			}
			
			// Apply rates
			rateMa.applyRates(entity);
			
			// Apply normal abilities
			if (applyNormalAbilities)
				AbilitiesMobListener.applyNormalAbilities(entity, mobCfg);
			
			// Apply set abilities
			if (mob.getAbilitySet() != null)
				mob.getAbilitySet().addAbility(entity);
		}
		
		return true;
	}
}
