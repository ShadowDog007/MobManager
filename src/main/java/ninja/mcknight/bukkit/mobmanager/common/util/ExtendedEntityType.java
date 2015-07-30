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

package ninja.mcknight.bukkit.mobmanager.common.util;

import java.util.LinkedHashMap;

import ninja.mcknight.bukkit.mobmanager.limiter.util.MobType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.inventory.ItemStack;

public class ExtendedEntityType
{
	private static int nextId = 0;
	public static final ExtendedEntityType UNKNOWN;
	private static LinkedHashMap<String, ExtendedEntityType> entityTypes = new LinkedHashMap<String, ExtendedEntityType>();
	
	// Adds entities
	static
	{
		// Wither Skeleton
		new ExtendedEntityType(EntityType.SKELETON, SkeletonType.WITHER);
		
		// EntityTypes
		for (EntityType eType : EntityType.values())
		{
			if (eType.isAlive() && eType != EntityType.PLAYER)
				new ExtendedEntityType(eType, "");
		}
		
		ExtendedEntityType horse = ExtendedEntityType.valueOf(EntityType.HORSE);
		
		for (Variant v : Variant.values())
		{
			if (v == Variant.HORSE)
			{
				for (Style s : Style.values()) {
					if (s == Style.NONE)
						s = null;
					for (Color c : Color.values())
						new ExtendedEntityType(EntityType.HORSE, new Object[] {c, s}, horse);
				}
			}
			else
			{
				new ExtendedEntityType(EntityType.HORSE, v);
			}
		}
		
		// Unknown mobs
		UNKNOWN = new ExtendedEntityType(EntityType.UNKNOWN, "");
	}
	
	public static ExtendedEntityType[] values()
	{
		return entityTypes.values().toArray(new ExtendedEntityType[entityTypes.size()]);
	}
	
	public static ExtendedEntityType valueOf(int id)
	{
		for (ExtendedEntityType type : values())
		{
			if (type.id == id)
				return type;
		}
		
		return null;
	}
	
	public static ExtendedEntityType valueOf(EntityType entityType)
	{
		return valueOf(entityType.toString());
	}
	
	public static ExtendedEntityType valueOf(LivingEntity entity)
	{
		return valueOf(getEntityTypeData(entity));
	}
	
	public static ExtendedEntityType valueOf(String string)
	{
		ExtendedEntityType type = entityTypes.get(string.toUpperCase());
		
		return type != null ? type : UNKNOWN;
	}
	
	public static String getEntityTypeData(LivingEntity entity)
	{
		String entityData = getEntityData(entity);
		
		if (entityData.length() != 0)
			return String.format("%s%s%s", entity.getType().toString(), getDataSeperator(), entityData);
		return entity.getType().toString();
	}
	
	public static String getEntityData(LivingEntity entity)
	{
		// Handle the case for wither skeletons
		if (entity.getType() == EntityType.SKELETON && ((Skeleton) entity).getSkeletonType() != SkeletonType.NORMAL)
			return ((Skeleton) entity).getSkeletonType().toString();
		
		// Handle the case for horses
		if (entity.getType() == EntityType.HORSE)
		{
			Horse horse = (Horse) entity;
			if (horse.getVariant() == Variant.HORSE)
				return horse.getStyle() + getDataSeperator() + horse.getColor();
			return horse.getVariant().toString();
		}
		
		return "";
	}
	
	private final int id;
	private final EntityType eType;
	private final Object eData;
	private final MobType mobType;
	private final ExtendedEntityType parent;
	
	private ExtendedEntityType(EntityType eType, Object eData)
	{
		this(eType, eData, null);
	}
	
	private ExtendedEntityType(EntityType eType, Object eData, ExtendedEntityType parent)
	{
		id = nextId++;
		this.eType = eType;
		this.eData = eData;
		this.parent = parent;
		
		if (eType != null && eType.getEntityClass() != null)
			mobType = MobType.valueOf(eType);
		else
			mobType = null;
		
		entityTypes.put(getTypeData().toUpperCase(), this);
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
	
	public ExtendedEntityType getParent()
	{
		return parent;
	}
	
	public boolean hasParent()
	{
		return parent != null;
	}
	
	public String getData()
	{
		if (eData instanceof Object[])
		{
			Object[] a = (Object[]) eData;
			String data = "";
			for (int i = 0; i < a.length; ++i)
			{
				if (a[i] == null)
					continue;
				
				if (i > 0)
					data += getDataSeperator();
				data += a[i];
			}
			return data;
		}
		return eData.toString();
	}
	
	public static String getDataSeperator()
	{
		return "_";
	}
	
	public String getTypeData()
	{
		String typeData = eType.toString();
		String dataString = getData();
		
		if (dataString.length() != 0)
			typeData += getDataSeperator() + dataString;
		
		return typeData;
	}
	
	public static String getExtendedEntityList(boolean subtypes)
	{
		final int charLimit = 68;
		int currentLoc = 1;
		
		StringBuilder list = new StringBuilder();
		
		for (ExtendedEntityType type : entityTypes.values())
		{
			if (subtypes && !type.hasParent() || !subtypes && type.hasParent())
				continue;
			
			String addition = type.getTypeData();
			
			if (currentLoc + addition.length() + 2 > charLimit)
			{
				currentLoc = 1;
				list.append(",\n");
			}
			
			if (currentLoc != 1)
				list.append(", ");
			list.append(addition);
			currentLoc += addition.length();
		}
		
		return list.toString();
	}

	@Override
	public String toString()
	{
		return getTypeData();
	}
	
	public LivingEntity spawnMob(Location loc)
	{
		if (eType.getEntityClass() == null || loc == null || loc.getWorld() == null)
			return null;
		
		LivingEntity entity = (LivingEntity) loc.getWorld().spawnEntity(loc, eType);
		
		if (entity == null)
			return null;
		
		if (eType == EntityType.HORSE)
		{
			Horse horse = (Horse) entity;
			Variant v = Variant.HORSE;
			Style s = null;
			Color c = null;
			if (eData.toString().length() == 0)
			{
				s = RandomUtil.getRandomElement(Style.values());
				c = RandomUtil.getRandomElement(Color.values());
			}
			else if (eData instanceof Object[])
			{
				Object[] dataArr = (Object[]) eData;
				if (dataArr[1] != null)
					horse.setStyle((Style) dataArr[1]);
				horse.setColor((Color) dataArr[0]);
			}
			else
			{
				v = (Variant) eData;
			}
			
			horse.setVariant(v);
			
			if (v == Variant.HORSE)
			{
				if (s != null) horse.setStyle(s);
				if (c != null) horse.setColor(c);
			}
			
			return horse;
		}
		
		if (eData != null)
		{
			if (eData == SkeletonType.WITHER)
				((Skeleton) entity).setSkeletonType(SkeletonType.WITHER);
		}
		
		switch (eType)
		{
		case SKELETON:
			Material heldItem = eData == null || eData != SkeletonType.WITHER ? Material.BOW : Material.STONE_SWORD;
			entity.getEquipment().setItemInHand(new ItemStack(heldItem));
			break;
		case PIG_ZOMBIE:
			entity.getEquipment().setItemInHand(new ItemStack(Material.GOLD_SWORD));
			break;
		default:
		}
		
		return entity;
	}

	public int ordinal()
	{
		return id;
	}

	public boolean isWide()
	{
		switch (eType)
		{
		case CAVE_SPIDER:
		case ENDER_DRAGON:
		case GHAST:
		case GIANT:
		case HORSE:
		case IRON_GOLEM:
		case MAGMA_CUBE:
		case SLIME:
		case SPIDER:
		case WITHER:
			return true;
		default:
			return false;
		}
	}

	public boolean isTall()
	{
		switch (eType)
		{
		case ENDERMAN:
		case ENDER_DRAGON:
		case GHAST:
		case GIANT:
		case HORSE:
		case IRON_GOLEM:
		case MAGMA_CUBE:
		case SLIME:
		case WITHER:
			return true;
		default:
			return false;
		}
	}
}
