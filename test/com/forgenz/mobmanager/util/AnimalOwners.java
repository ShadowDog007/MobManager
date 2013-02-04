package com.forgenz.mobmanager.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.config.Config;

public class AnimalOwners extends BukkitRunnable
{
	private File animalOwnersFile;
	private File ownersAnimalsFile;
	private ConcurrentHashMap<UUID, String> animalOwners;
	private ConcurrentHashMap<String, HashSet<UUID>> ownersAnimals;
	
	private int numAttempts = 0;
	private long cleanupPeriod;
	
	private AtomicBoolean cleanupRunning = new AtomicBoolean(false);
	
	@SuppressWarnings("unchecked")
	public AnimalOwners()
	{
		animalOwnersFile = new File(P.p.getDataFolder(), "AnimalOwners.dat");
		ownersAnimalsFile = new File(P.p.getDataFolder(), "OwnersAnimals.dat");
		
		cleanupPeriod = (long) Config.daysTillFarmAnimalCleanup * 24 * 3600 * 1000;

		if (animalOwnersFile.exists())
		{
			try
			{
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(animalOwnersFile));
				animalOwners = (ConcurrentHashMap<UUID, String>) ois.readObject();
				ois.close();
			}
			catch (Exception e)
			{
				P.p.getLogger().severe("Failed to load current protected animals");
				animalOwners = new ConcurrentHashMap<UUID, String>(0, 0.75F, 2);
				
				return;
			}
			
			Iterator<Entry<UUID, String>> it = animalOwners.entrySet().iterator();
			
			// Check the player associated with the animal is still active
			while (it.hasNext())
			{
				Entry<UUID, String> e = it.next();
				
				if (!P.p.animalProtection.checkUUID((e.getKey())))
					it.remove();
			}
		}
		else
		{
			animalOwners = new ConcurrentHashMap<UUID, String>(0, 0.75F, 2);
		}
		
		
	}
	
	public void addUUID(UUID uuid, String username)
	{
		animalOwners.put(uuid, username);
	}

	/**
	 * Periodically saves the protected animals to a file
	 */
	@Override
	public void run()
	{
		if (!cleanupRunning.compareAndSet(false, true))
			return;
		
		for (UUID uuid : animalOwners.keySet().toArray(new UUID[0]))
		{
			removeIfInactive(uuid);
		}
		
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(animalOwnersFile));
			oos.writeObject(animalOwners);
			oos.close();
			numAttempts = 0;
		} catch (IOException e)
		{
			P.p.getLogger().severe("Error writing protected animals list to file");
			
			if (++numAttempts >= 5)
			{
				P.p.getLogger().severe("Max attempts to write file exceeded, no more attempts will be made");
				cancel();
			}
			e.printStackTrace();
		}
		
		cleanupRunning.set(false);
	}
	
	protected void removeIfInactive(UUID uuid)
	{
		Long lastActive = animalOwners.get(uuid);
		
		if (!isActive(lastActive))
			animalOwners.remove(uuid);
	}

	/**
	 * Adds the animal to the list of protected animals if the player is trying to breed the animal
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		// Check if the enitiy can breed
		if (event.getRightClicked() instanceof Animals == false)
			return;
		
		Ageable entity = (Ageable) event.getRightClicked();
		
		if (!entity.canBreed())
			return;
		
		// Check if the entity is being bred
		switch (event.getPlayer().getItemInHand().getType())
		{
		case SEEDS:
		case PUMPKIN_SEEDS:
		case MELON_SEEDS:
			if (event.getRightClicked() instanceof Chicken == false)
				return;
			break;
		case WHEAT:
			if (event.getRightClicked() instanceof Cow == false && event.getRightClicked() instanceof Sheep == false)
				return;
			break;
		case CARROT_ITEM:
			if (event.getRightClicked() instanceof Pig == false)
				return;
		default:
			return;
		}
		
		P.p.animalProtection.addUUID(event.getRightClicked().getUniqueId());
	}
	
	/**
	 * This will add newly bred animals to the list of protected animals</br>
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		if (event.getSpawnReason() != SpawnReason.BREEDING && event.getSpawnReason() != SpawnReason.EGG)
			return;
		
		Animals animal = (Animals) event.getEntity();
		
		addUUID(animal.getUniqueId());
	}
	
	/**
	 * Attempts to remove the entity from protected animals when it dies
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCreatureDeath(EntityDeathEvent event)
	{
		if (event.getEntity() instanceof Animals == false)
			return;
		
		animalOwners.remove(event.getEntity().getUniqueId());
	}
}
