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

public class ExtendedEntityType
{
	private static HashMap<String, ExtendedEntityType> entityTypes = new HashMap<String, ExtendedEntityType>();
	
	// Adds entities
	static
	{
		// Wither Skeleton
		ExtendedEntityType type = new ExtendedEntityType(EntityType.SKELETON, SkeletonType.WITHER);
		entityTypes.put(type.getTypeData(), type);
		
		// EntityTypes
		for (EntityType eType : EntityType.values())
		{
			if (eType.isAlive())
				entityTypes.put(eType.toString(), new ExtendedEntityType(eType, ""));
		}
	}
	
	public static ExtendedEntityType[] values()
	{
		return entityTypes.values().toArray(new ExtendedEntityType[entityTypes.size()]);
	}
	
	public static ExtendedEntityType get(EntityType entityType)
	{
		return entityTypes.get(entityType.toString());
	}
	
	public static ExtendedEntityType get(Entity entity)
	{
		return entityTypes.get(getEntityTypeData(entity));
	}
	
	public static ExtendedEntityType get(String string)
	{
		return entityTypes.get(string.toUpperCase());
	}
	
	public static String getEntityTypeData(Entity entity)
	{
		return entity.getType().toString() + getEntityData(entity);
	}
	
	public static String getEntityData(Entity entity)
	{
		// Handle the case for wither skeletons
		if (entity.getClass() == Skeleton.class && ((Skeleton) entity).getSkeletonType() != SkeletonType.NORMAL)
			return getDataSeperator() + ((Skeleton) entity).getSkeletonType().toString();
		
		return "";
	}
	
	private final EntityType eType;
	private final Object eData;
	private ExtendedEntityType(EntityType eType, Object eData)
	{
		this.eType = eType;
		
		this.eData = eData;
	}
	
	public EntityType getBukkitEntityType()
	{
		return eType;
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
		String dataString = eData.toString();
		
		return eType.toString() + (dataString.length() != 0 ? getDataSeperator() + dataString : "");
	}
	
	private static String getStringForEntityList(EntityType entityType)
	{		
		// Handle special cases
		if (entityType == EntityType.SKELETON)
		{
			String str = entityType.toString();
			
			for (SkeletonType type : SkeletonType.values())
			{
				if (type != SkeletonType.NORMAL)
					str += ", " + entityType.toString() + getDataSeperator() + type.toString();
			}
			
			return str;
		}
		// Handle general cases
		else
		{
			return entityType.toString();
		}
	}
	
	public static String getExtendedEntityList()
	{
		final int charLimit = 68;
		int currentLoc = 1;
		
		String list = "";
		
		for (EntityType type : EntityType.values())
		{
			if (!type.isAlive())
				continue;
			
			String addition = getStringForEntityList(type);
			
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
