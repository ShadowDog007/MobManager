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

package com.forgenz.mobmanager;

import org.bukkit.World.Environment;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Golem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WaterMob;

public enum MobType
{
	/**
	 * Represents a monster
	 */
	@SuppressWarnings("unchecked")
	MONSTER("M", "Monsters", 0, 512, 10, getClasses(Monster.class, Ghast.class, Slime.class)),
	
	/**
	 * Represents animals (Includes IronGolems and Snowmen)
	 */
	@SuppressWarnings("unchecked")
	ANIMAL("A", "Animals", 1, 320, 10, getEnvironments(Environment.NORMAL), getClasses(Animals.class, Golem.class)),
	
	/**
	 * Represents a water animal (Just squid at the moment)
	 */
	@SuppressWarnings("unchecked")
	WATER_ANIMAL("W", "Water_Animals", 2, 128, 4, getEnvironments(Environment.NORMAL), getClasses(WaterMob.class)),
	
	/**
	 * Represents an ambient creature (Just bats at the moment)
	 */
	@SuppressWarnings("unchecked")
	AMBIENT("Am", "Ambient", 3, 64, 4, getClasses(Ambient.class)),
	
	/**
	 * Represents a villager
	 */
	@SuppressWarnings("unchecked")
	VILLAGER("V", "Villager", 4, 64, 5, getEnvironments(Environment.NORMAL), getClasses(Villager.class));
	
	public final String shortName;
	public final String cPath;
	public final short index;
	public final Environment[] activeEnvironments;
	public final short default_maximum;
	public final short default_dynMulti;
	
	private final Class<? extends LivingEntity>[] mobTypes;
	
	/**
	 * @param configPath The part of the config path used to get config values for this type of creature
	 * @param index The position in an array this mob type should be stored in
	 * @param maximum the default maximum for this group
	 * @param dynMulti The default dynamic multiplier for this group
	 * @param mobTypes The classes or entities which belong to this group
	 */
	private MobType(String shortName, String cPath, int index, int maximum, int dynMulti, Class<? extends LivingEntity>[] mobTypes)
	{
		this(shortName, cPath, index, maximum, dynMulti, null, mobTypes);
	}
	
	/**
	 * @param environments The environments the mob can spawn in (null for all)
	 */
	private MobType(String shortName, String cPath, int index, int maximum, int dynMulti, Environment[] environments, Class<? extends LivingEntity>[] mobTypes)
	{
		this.shortName = shortName;
		this.cPath = cPath;
		this.index = (short) index;
		this.mobTypes = mobTypes;
		
		this.activeEnvironments = environments;
		this.default_maximum = (short) maximum;
		this.default_dynMulti = (short) dynMulti;
	}
	
	private static Environment[] getEnvironments(Environment ...environments)
	{
		return environments.length != 0 ? environments : null;
	}
	
	private static Class<? extends LivingEntity>[] getClasses(Class<? extends LivingEntity> ...mobTypes)
	{
		return mobTypes;
	}
	
	public short getDefaultMax(Environment environment)
	{
		if (this.activeEnvironments == null)
			return this.default_maximum;
		
		for (Environment e : this.activeEnvironments)
		{
			if (e == environment)
				return this.default_maximum;
		}
		return 0;
	}
	
	public short getDefaultDynMulti(Environment environment)
	{
		if (this.activeEnvironments == null)
			return this.default_dynMulti;
		
		for (Environment e : this.activeEnvironments)
		{
			if (e == environment)
				return this.default_dynMulti;
		}
		return 0;
	}
	
	public String toString()
	{
		return cPath;
	}
	
	public boolean belongs(Entity entity)
	{
		if (entity instanceof LivingEntity)
			return belongs((LivingEntity) entity);
		return false;
	}
	
	public boolean belongs(LivingEntity entity)
	{
		for (Class<? extends LivingEntity> mobType : mobTypes)
		{
			if (mobType.isAssignableFrom(entity.getClass()))
				return true;
		}
		
		return false;
	}
	
	public static MobType valueOf(LivingEntity entity)
	{
		for (Class<? extends LivingEntity> mobType : ANIMAL.mobTypes)
		{
			if (mobType.isAssignableFrom(entity.getClass()))
				return ANIMAL;
		}
		
		for (Class<? extends LivingEntity> mobType : WATER_ANIMAL.mobTypes)
		{
			if (mobType.isAssignableFrom(entity.getClass()))
				return WATER_ANIMAL;
		}
		
		for (Class<? extends LivingEntity> mobType : AMBIENT.mobTypes)
		{
			if (mobType.isAssignableFrom(entity.getClass()))
				return AMBIENT;
		}
		
		for (Class<? extends LivingEntity> mobType : MONSTER.mobTypes)
		{
			if (mobType.isAssignableFrom(entity.getClass()))
				return MONSTER;
		}
		
		for (Class<? extends LivingEntity> mobType : VILLAGER.mobTypes)
		{
			if (mobType.isAssignableFrom(entity.getClass()))
				return VILLAGER;
		}
		
		return null;
	}
	
	public static MobType valueOf(Entity entity)
	{
		if (entity instanceof LivingEntity)
		{
			return valueOf((LivingEntity) entity);
		}
		return null;
	}
}
