package com.forgenz.mobmanager.attributes.abilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.attributes.AbilityTypes;
import com.forgenz.mobmanager.util.ValueChance;

public class ItemAbility extends Ability
{
	private final static HashMap<Material, ItemAbility> itemAbilities = new HashMap<Material, ItemAbility>();
	
	public final Material item;
	
	private ItemAbility(Material item)
	{
		this.item = item;
	}

	@Override
	public void addAbility(LivingEntity entity)
	{
		if (item == null)
			return;
		
		entity.getEquipment().setItemInHand(new ItemStack(item));
	}

	@Override
	public void removeAbility(LivingEntity entity)
	{
		if (item == null)
			return;
		
		if (entity.getEquipment().getItemInHand().getType() == item)
			entity.getEquipment().setItemInHand(new ItemStack(Material.AIR));
	}
	
	@Override
	public AbilityTypes getAbilityType()
	{
		return AbilityTypes.ITEM_HAND;
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
			
			if (split == null || !AbilityTypes.ITEM_HAND.valueMatches(split[1]))
			{
				P.p.getLogger().warning("The value " + str + " is invalid for MobAtributes." + mob + "." + AbilityTypes.DAMAGE_MULTI.getConfigPath());
				it.remove();
				continue;
			}
			
			int chance = Integer.valueOf(split[0]);
			Material item = Material.getMaterial(Integer.valueOf(split[1]));
			
			if (chance <= 0 || item == null)
				continue;
			
			abilityChances.addChance(chance, new ItemAbility(item));
		}
	}

	public static Ability setup(EntityType mob, String optVal)
	{
		if (!AbilityTypes.ITEM_HAND.valueMatches(optVal))
		{
			P.p.getLogger().warning("The value " + optVal + " must be an item id (0 for none) for MobAtributes." + mob + "." + AbilityTypes.ITEM_HAND);
			return null;
		}
		
		Material material = Material.getMaterial(Integer.valueOf(optVal));
		if (material == null)
			material = Material.AIR;
		
		ItemAbility ability = itemAbilities.get(material);
		
		if (ability == null)
		{
			ability = new ItemAbility(material);
			itemAbilities.put(material, ability);
		}
		
		return ability;
	}

}
