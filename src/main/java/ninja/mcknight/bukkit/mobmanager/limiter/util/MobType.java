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

package ninja.mcknight.bukkit.mobmanager.limiter.util;

import org.bukkit.World.Environment;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
	MONSTER("M", "Monsters", 512, 20, Monster.class, Ghast.class, Slime.class),
	
	/**
	 * Represents animals (Includes IronGolems and Snowmen)
	 */
	@SuppressWarnings("unchecked")
	ANIMAL("A", "Animals", 320, 15,new Environment[] {Environment.NORMAL}, Animals.class, Golem.class),
	
	/**
	 * Represents a water animal (Just squid at the moment)
	 */
	@SuppressWarnings("unchecked")
	WATER_ANIMAL("W", "Water_Animals", 128, 6, new Environment[] {Environment.NORMAL}, WaterMob.class),
	
	/**
	 * Represents an ambient creature (Just bats at the moment)
	 */
	@SuppressWarnings("unchecked")
	AMBIENT("Am", "Ambient", 64, 6, Ambient.class),
	
	/**
	 * Represents a villager
	 */
	@SuppressWarnings("unchecked")
	VILLAGER("V", "Villager", 128, 10, new Environment[] {Environment.NORMAL}, Villager.class);
	
	public final String shortName;
	public final String cPath;
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
	private MobType(String shortName, String cPath, int maximum, int dynMulti, Class<? extends LivingEntity> ...mobTypes)
	{
		this(shortName, cPath, maximum, dynMulti, null, mobTypes);
	}
	
	/**
	 * @param environments The environments the mob can spawn in (null for all)
	 */
	private MobType(String shortName, String cPath, int maximum, int dynMulti, Environment[] environments, Class<? extends LivingEntity> ...mobTypes)
	{
		this.shortName = shortName;
		this.cPath = cPath;
		this.mobTypes = mobTypes;
		
		this.activeEnvironments = environments;
		this.default_maximum = (short) maximum;
		this.default_dynMulti = (short) dynMulti;
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
		if (entity == null)
			return null;
		return valueOf(entity.getClass());
	}
	
	public static MobType valueOf(Entity entity)
	{
		if (entity instanceof LivingEntity)
		{
			return valueOf((LivingEntity) entity);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static MobType valueOf(EntityType eType)
	{
		if (eType == null)
			return null;
		
		if (LivingEntity.class.isAssignableFrom(eType.getEntityClass()))
		{
			return valueOf((Class<? extends LivingEntity>) eType.getEntityClass());
		}
		return null;
	}
	
	public static MobType valueOf(Class<? extends LivingEntity> clazz)
	{
		if (clazz == null)
			return null;
		
		for (MobType type : values())
		{
			for (Class<? extends LivingEntity> typeClazz : type.mobTypes)
			{
				if (typeClazz.isAssignableFrom(clazz))
					return type;
			}
		}
		
		return null;
	}
}
