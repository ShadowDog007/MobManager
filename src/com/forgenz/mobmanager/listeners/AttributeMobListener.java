package com.forgenz.mobmanager.listeners;

import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.attributes.AbilityTypes;
import com.forgenz.mobmanager.attributes.abilities.Ability;
import com.forgenz.mobmanager.attributes.abilities.DamageAbility;
import com.forgenz.mobmanager.config.Config;
import com.forgenz.mobmanager.config.MobAttributes;
import com.forgenz.mobmanager.util.ValueChance;
import com.forgenz.mobmanager.world.MMWorld;

public class AttributeMobListener implements Listener
{
	/**
	 * Handles random chance rates
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void rates(CreatureSpawnEvent event)
	{
		final MMWorld world = P.worlds.get(event.getLocation().getWorld().getName());
		
		MobAttributes ma = Config.getMobAttributes(world, event.getEntityType());
		
		if (ma == null)
			return;
		
		if (ma.spawnRate < 1.0)
		{
			if (ma.spawnRate == 0.0)
			{
				event.setCancelled(true);
				return;
			}
			// If the random number is higher than the spawn chance we disallow the spawn
			if (Config.rand.nextFloat() >= ma.spawnRate)
			{
				event.setCancelled(true);
				return;
			}
		}
		
		if ((event.getEntity() instanceof Ageable || event.getEntity() instanceof Zombie) && ma.babyRate <= 1.0F && ma.babyRate != 0.0F)
		{
			// If the random number is higher than the baby chance we don't turn the mob into a baby
			if ( ma.babyRate == 1.0F || Config.rand.nextFloat() < ma.babyRate)
			{
				if (event.getEntity() instanceof Ageable)
					((Ageable) event.getEntity()).setBaby();
				else
					((Zombie) event.getEntity()).setBaby(true);
			}
		}
	}
	
	/**
	 * Adds abilities to the entity
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		addAbilities(event.getEntity());
	}
	
	public static void addAbilities(LivingEntity entity)
	{
		final MMWorld world = P.worlds.get(entity.getWorld().getName());
		
		MobAttributes ma = Config.getMobAttributes(world, entity.getType());
		
		if (ma == null)
			return;
		
		AbilityTypes[] types = AbilityTypes.values();
		for (int i = 0; i < types.length; ++i)
		{
			if (!types[i].isValueChanceAbility())
				continue;
			
			ValueChance<Ability> abilityChance = ma.attributes.get(types[i]);
			
			if (abilityChance == null)
				continue;
			
			Ability ability = abilityChance.getBonus();
			
			if (ability == null)
				continue;
			
			ability.addAbility(entity);
		}
	}
	
	public static void removeAbilities(LivingEntity entity)
	{
		final MMWorld world = P.worlds.get(entity.getWorld().getName());
		
		MobAttributes ma = Config.getMobAttributes(world, entity.getType());
		
		if (ma == null)
			return;
		
		AbilityTypes[] types = AbilityTypes.values();
		for (int i = types.length - 1; i >= 0; --i)
		{
			if (!types[i].isValueChanceAbility())
				continue;
			
			ValueChance<Ability> abilityChance = ma.attributes.get(types[i]);
			
			if (abilityChance == null)
				continue;
			
			Ability ability = abilityChance.getBonus();
			
			if (ability == null)
				continue;
			
			ability.addAbility(entity);
		}
	}
	
	/**
	 * Multiplies damage for entities
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void setDamageMulti(EntityDamageByEntityEvent event)
	{
		// We are only interested in these three damage types
		if (event.getCause() != DamageCause.ENTITY_ATTACK && event.getCause() != DamageCause.PROJECTILE && event.getCause() != DamageCause.ENTITY_EXPLOSION)
			return;
		
		// Get the entity which dealt the damage
		LivingEntity damager = null;
		
		if (event.getDamager() instanceof Projectile)
		{
			Projectile entity = (Projectile) event.getDamager();
			
			damager = entity.getShooter();
		}
		else if (event.getDamager() instanceof LivingEntity)
		{
			damager = (LivingEntity) event.getDamager();
		}
		
		if (damager == null)
			return;
		
		// Fetch the multiplier for damage caused by the mob
		float multi = DamageAbility.getMetaValue(damager);
		
		// If the multiplier is 1.0F we don't do anything
		if (multi != 1.0F)
		{
			// Calculate the new damage
			int newDamage = (int) (event.getDamage() * multi);
			
			// Set the new damage
			event.setDamage(newDamage);
		}
	}
}
