package com.forgenz.mobmanager.attributes.abilities;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.attributes.AbilityTypes;
import com.forgenz.mobmanager.util.ValueChance;

public class ArmourAbility extends Ability
{
	
	public enum ArmourMaterials
	{
		DIAMOND(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS),
		IRON(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS),
		CHAIN(Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS),
		GOLD(Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS),
		LEATHER(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS),
		NONE(Material.AIR, Material.AIR, Material.AIR, Material.AIR);
		
		private ArmourAbility ability = null;
		private final Material head;
		private final Material chest;
		private final Material legs;
		private final Material feet;
		
		ArmourMaterials(Material head, Material chest, Material legs, Material feet)
		{
			this.head = head;
			this.chest = chest;
			this.legs = legs;
			this.feet = feet;
		}
		
		public void setHead(LivingEntity entity)
		{
			entity.getEquipment().setHelmet(new ItemStack(head));
		}
		
		public void setChest(LivingEntity entity)
		{
			entity.getEquipment().setChestplate(new ItemStack(chest));
		}
		
		public void setLegs(LivingEntity entity)
		{
			entity.getEquipment().setLeggings(new ItemStack(legs));
		}
		
		public void setFeet(LivingEntity entity)
		{
			entity.getEquipment().setBoots(new ItemStack(feet));
		}
		
		public ArmourAbility getAbility()
		{
			if (ability == null)
				ability = new  ArmourAbility(this);
			return ability;
		}
	}
	
	private final ArmourMaterials material;
	
	private ArmourAbility(ArmourMaterials material)
	{
		this.material = material;
	}

	@Override
	public void addAbility(LivingEntity entity)
	{
		if (entity == null || entity instanceof Player)
			return;
		
		material.setHead(entity);
		material.setChest(entity);
		material.setLegs(entity);
		material.setFeet(entity);
	}

	@Override
	public void removeAbility(LivingEntity entity)
	{
		
	}
	
	@Override
	public AbilityTypes getAbilityType()
	{
		return AbilityTypes.ARMOUR;
	}

	public static void setup(EntityType mob, ValueChance<Ability> abilityChances, List<?> optList)
	{
		Iterator<?> it = optList.iterator();
		
		while (it.hasNext())
		{
			String str = getString(it.next());
			
			if (str == null)
				continue;
			
			String[] split = getChanceSplit(str);
			
			if (split == null)
			{
				P.p.getLogger().warning("The value " + str + " is invalid for MobAtributes." + mob + "." + AbilityTypes.ARMOUR.getConfigPath());
				it.remove();
				continue;
			}
			
			int chance = Integer.valueOf(split[0]);
			
			if (chance == 0)
				continue;
			
			ArmourAbility ability = setup(mob, split[1]);
			
			if (ability != null)
				abilityChances.addChance(chance, ability);
		}
	}
	
	public static ArmourAbility setup(EntityType mob, String optVal)
	{
		if (!AbilityTypes.ARMOUR.valueMatches(optVal))
		{
			P.p.getLogger().warning("The armour type " + optVal + " is invalid for MobAtributes." + mob + "." + AbilityTypes.ARMOUR);
			return null;
		}
		
		ArmourMaterials material = null;
		
		for (ArmourMaterials mat : ArmourMaterials.values())
		{
			if (mat.toString().equalsIgnoreCase(optVal))
			{
				material = mat;
				break;
			}
		}
		
		if (material == null)
		{
			P.p.getLogger().warning("The armour type " + optVal + " is invalid for MobAtributes." + mob + "." + AbilityTypes.ARMOUR);
			return ArmourMaterials.NONE.getAbility();
		}
		
		return material.getAbility();
	}
	
}
