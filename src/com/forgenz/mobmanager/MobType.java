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
	MONSTER("Monsters", 0, 512, 16, getClasses(Monster.class, Ghast.class, Slime.class)),
	
	/**
	 * Represents animals (Includes IronGolems and Snowmen)
	 */
	@SuppressWarnings("unchecked")
	ANIMAL("Animals", 1, 320, 12, getEnvironments(Environment.NORMAL), getClasses(Animals.class, Golem.class)),
	
	/**
	 * Represents a water animal (Just squid at the moment)
	 */
	@SuppressWarnings("unchecked")
	WATER_ANIMAL("Water_Animals", 2, 128, 4, getEnvironments(Environment.NORMAL), getClasses(WaterMob.class)),
	
	/**
	 * Represents an ambient creature (Just bats at the moment)
	 */
	@SuppressWarnings("unchecked")
	AMBIENT("Ambient", 3, 64, 4, getClasses(Ambient.class)),
	
	/**
	 * Represents a villager
	 */
	@SuppressWarnings("unchecked")
	VILLAGER("Villager", 4, 64, 5, getEnvironments(Environment.NORMAL), getClasses(Villager.class));
	
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
	private MobType(String cPath, int index, int maximum, int dynMulti, Class<? extends LivingEntity>[] mobTypes)
	{
		this(cPath, index, maximum, dynMulti, null, mobTypes);
	}
	
	/**
	 * @param environments The environments the mob can spawn in (null for all)
	 */
	private MobType(String cPath, int index, int maximum, int dynMulti, Environment[] environments, Class<? extends LivingEntity>[] mobTypes)
	{
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
