package com.forgenz.mobmanager;

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
	MONSTER("Monsters", 0, Monster.class, Ghast.class, Slime.class),
	
	/**
	 * Represents animals (Includes IronGolems and Snowmen)
	 */
	@SuppressWarnings("unchecked")
	ANIMAL("Animals", 1, Animals.class, Golem.class),
	
	/**
	 * Represents a water animal (Just squid at the moment)
	 */
	@SuppressWarnings("unchecked")
	WATER_ANIMAL("Water_Animals", 2, WaterMob.class),
	
	/**
	 * Represents an ambient creature (Just bats at the moment)
	 */
	@SuppressWarnings("unchecked")
	AMBIENT("Ambient", 3, Ambient.class),
	
	/**
	 * Represents a villager
	 */
	@SuppressWarnings("unchecked")
	VILLAGER("Villager", 4, Villager.class);
	
	public final String cPath;
	public final int index;
	private final Class<? extends LivingEntity>[] mobTypes;
	
	/**
	 * @param configPath The part of the config path used to get config values for this type of creature
	 * @param index The position in an array this mob type should be stored in
	 * @param mobTypes The classes or entities which belong to this group
	 */
	private MobType(String cPath, int index, Class<? extends LivingEntity> ... mobTypes)
	{
		this.cPath = cPath;
		this.index = index;
		this.mobTypes = mobTypes;
	}
	
	public String toString()
	{
		return cPath;
	}
	
	public static MobType[] getAll()
	{
		MobType[] cTypeArray = new MobType[5];
		
		cTypeArray[0] = MONSTER;
		cTypeArray[1] = ANIMAL;
		cTypeArray[2] = WATER_ANIMAL;
		cTypeArray[3] = AMBIENT;
		cTypeArray[4] = VILLAGER;
		
		return cTypeArray;
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
