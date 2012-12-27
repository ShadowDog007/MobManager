package com.forgenz.mobmanager;

import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import com.forgenz.mobmanager.listeners.MobListener;
import com.forgenz.mobmanager.world.MMCoord;
import com.forgenz.mobmanager.world.MMWorld;


public class MobDespawnTask extends BukkitRunnable
{
	@Override
	public void run()
	{
		if (P.worlds == null)
		{
			cancel();
			return;
		}
		
		for (MMWorld world : P.worlds.values())
		{
			world.updateMobCounts();
			
			for (LivingEntity entity : world.getWorld().getLivingEntities())
			{
				// Don't despawn animals
				// TODO Decide on a way to determine if an animal is part of a players farm
				// NOTE: Perhaps count how many animals are inside of a single chunk?
				if (MobType.ANIMAL.belongs(entity))
					continue;
				
				if (!MobListener.i.playerNear(world, new MMCoord(entity.getLocation().getChunk()), entity.getLocation().getBlockY(), MobListener.i.mobFlys(entity)))
				{
					entity.remove();
					world.decrementMobCount(MobType.valueOf(entity));
				}
			}
		}
	}
}
