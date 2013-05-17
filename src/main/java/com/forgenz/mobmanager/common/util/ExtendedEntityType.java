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

package com.forgenz.mobmanager.common.util;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;

import com.forgenz.mobmanager.limiter.util.MobType;

public class ExtendedEntityType
{
	private static ExtendedEntityType UNKNOWN;
	private static HashMap<String, ExtendedEntityType> entityTypes = new HashMap<String, ExtendedEntityType>();
	
	// Adds entities
	static
	{
		// Wither Skeleton
		ExtendedEntityType type = new ExtendedEntityType(EntityType.SKELETON, SkeletonType.WITHER);
		entityTypes.put(type.getTypeData().toUpperCase(), type);
		
		// EntityTypes
		for (EntityType eType : EntityType.values())
		{
			type = new ExtendedEntityType(eType, "");
			if (eType.isAlive())
				entityTypes.put(type.toString().toUpperCase(), type);
		}
		
		// Unknown mobs
		UNKNOWN = new ExtendedEntityType(EntityType.UNKNOWN, "");
		entityTypes.put(UNKNOWN.getTypeData().toUpperCase(), UNKNOWN);
	}
	
	public static ExtendedEntityType[] values()
	{
		return entityTypes.values().toArray(new ExtendedEntityType[entityTypes.size()]);
	}
	
	public static ExtendedEntityType valueOf(EntityType entityType)
	{
		return valueOf(entityType.toString());
	}
	
	public static ExtendedEntityType valueOf(Entity entity)
	{
		return valueOf(getEntityTypeData(entity));
	}
	
	public static ExtendedEntityType valueOf(String string)
	{
		ExtendedEntityType type = entityTypes.get(string.toUpperCase());
		
		return type != null ? type : UNKNOWN;
	}
	
	public static String getEntityTypeData(Entity entity)
	{
		return entity.getType().toString() + getEntityData(entity);
	}
	
	public static String getEntityData(Entity entity)
	{
		// Handle the case for wither skeletons
		if (entity.getType() == EntityType.SKELETON && ((Skeleton) entity).getSkeletonType() != SkeletonType.NORMAL)
			return getDataSeperator() + ((Skeleton) entity).getSkeletonType().toString();
		
		return "";
	}
	
	private final EntityType eType;
	private final Object eData;
	private final MobType mobType;
	
	private ExtendedEntityType(EntityType eType, Object eData)
	{
		this.eType = eType;
		this.eData = eData;
		
		if (eType != null && eType.getEntityClass() != null)
			mobType = MobType.valueOf(eType);
		else
			mobType = null;
	}
	
	public EntityType getBukkitEntityType()
	{
		return eType;
	}
	
	/**
	 * Returns the pre-calculated mob type for this EntityType
	 * @return The EntityTypes MobType
	 */
	public MobType getMobType()
	{
		return mobType;
	}
	
	/**
	 * Returns the MobType of this entity</br>
	 * If the MobType is unknown it is calculated given an Entity</br>
	 * This provides support for mobs which do not exist in Vanilla Minecraft
	 * @param entity The entity for which we want the MobType for (Should match the EntityType)
	 * @return The Entities MobType
	 */
	public MobType getMobType(LivingEntity entity)
	{
		if (mobType == null)
			return MobType.valueOf(entity);
		return mobType;
	}
	
	public String getData()
	{
		return eData.toString();
	}
	
	public static String getDataSeperator()
	{
		return "_";
	}
	
	public String getTypeData()
	{
		String dataString = getData();
		
		return String.format("%s%s%s", eType.toString(), dataString.length() != 0 ? getDataSeperator() : "", dataString);
	}
	
	public static String getExtendedEntityList()
	{
		final int charLimit = 68;
		int currentLoc = 1;
		
		String list = "";
		
		for (ExtendedEntityType type : entityTypes.values())
		{			
			String addition = type.getTypeData();
			
			if (currentLoc + addition.length() + 2 > charLimit)
			{
				currentLoc = 1;
				list += ",\n";
			}
			
			if (currentLoc != 1)
				list += ", ";
			list += addition;
			currentLoc += addition.length();
		}
		
		return list;
	}

	@Override
	public String toString()
	{
		return getTypeData();
	}
	
	public LivingEntity spawnMob(Location loc)
	{
		if (loc == null || loc.getWorld() == null)
			return null;
		
		LivingEntity entity = (LivingEntity) loc.getWorld().spawnEntity(loc, eType);
		
		if (entity == null)
			return null;
		
		if (eData != null)
		{
			if (eData == SkeletonType.WITHER)
				((Skeleton) entity).setSkeletonType(SkeletonType.WITHER);
		}
		
		return entity;
	}
}
