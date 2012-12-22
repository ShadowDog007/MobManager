package com.forgenz.mobmanager.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import com.forgenz.mobmanager.MobType;
import com.forgenz.mobmanager.P;

/**
 * Keeps track of the number of mobs of different types within a world
 * 
 * @author Michael McKnight (ShadowDog007)
 *
 */
public class MMWorld
{
	private class MobSettings
	{
		public short mobCount;
		public short mobMax;
		public short mobDynMulti;
		
		public MobSettings(MobType mob)
		{
			mobCount = 0;
			mobMax = (short) P.cfg.getInt("WorldMaximum." + world.getName() + "." + mob, Short.MAX_VALUE);
			mobDynMulti = (short) P.cfg.getInt("ChunkCalculatedMaximum." + world.getName() + "." + mob, Short.MAX_VALUE);
		}
	}
	
	private static long ticksPerRecount = P.cfg.getLong("TicksPerRecount", 40L);
	/**
	 * Bukkit world this object as affiliated with
	 */
	private World world;

	/**
	 * Loaded chunks in the world
	 */
	private HashMap<MMCoord, MMChunk> chunks;
	/**
	 * Count of loaded chunks in the world
	 */
	private int numChunks = 0;

	/**
	 * Used to check if we should update mob counts
	 */
	private boolean needsUpdate = true;
	
	
	/**
	 * Stores all mob counts
	 */
	private MobSettings[] mobSettings;
	
	
	public MMWorld(final World world)
	{
		this.world = world;
		
		mobSettings = new MobSettings[MobType.getAll().length];
		for (MobType mob : MobType.getAll())
			mobSettings[mob.index] = new MobSettings(mob);

		chunks = new HashMap<MMCoord, MMChunk>();

		// Store already loaded chunks
		for (final Chunk chunk : world.getLoadedChunks())
		{
			final MMChunk mmchunk = new MMChunk(chunk);

			chunks.put(mmchunk.getCoord(), mmchunk);
			++numChunks;

			// Counts the number of living entities
			for (final Entity entity : chunk.getEntities())
			{
				// Fetch the creature type
				MobType mob = MobType.valueOf(entity);
				// If the creature type is null we ignore the entity
				if (mob == null)
					continue;
					
				++getMobSettings(mob).mobCount;
			}
		}

		final int maxMonsters = P.cfg.getInt("WorldMaximum." + world.getName() + "." + MobType.MONSTER, Integer.MAX_VALUE);
		final int maxAnimals = P.cfg.getInt("WorldMaximum." + world.getName() + "." + MobType.ANIMAL, Integer.MAX_VALUE);
		final int maxWater = P.cfg.getInt("WorldMaximum." + world.getName() + "." + MobType.WATER_ANIMAL, Integer.MAX_VALUE);
		final int maxAmbient = P.cfg.getInt("WorldMaximum." + world.getName() + "." + MobType.AMBIENT, Integer.MAX_VALUE);
		final int maxVillagers = P.cfg.getInt("WorldMaximum." + world.getName() + "." + MobType.VILLAGER, Integer.MAX_VALUE);

		P.p.getLogger().info(String.format("[%s] Limits M:%d, A:%d, W:%d, Am:%d, V:%d", world.getName(), maxMonsters, maxAnimals, maxWater, maxAmbient, maxVillagers));
	}
	
	private void resetMobCounts()
	{
		for (MobType mob : MobType.getAll())
		{
			getMobSettings(mob).mobCount = 0;
		}
	}
	
	private MobSettings getMobSettings(MobType mob)
	{
		return mobSettings[mob.index];
	}

	public boolean updateMobCounts()
	{
		if (needsUpdate)
		{
			resetMobCounts();

			// Loop through each loaded chunk in the world
			for (final Chunk chunk : world.getLoadedChunks())
			{
				final MMChunk mmchunk = getChunk(chunk);

				mmchunk.resetNumAnimals();

				// Loop through each entity in the chunk
				for (final Entity entity : chunk.getEntities())
				{
					// Fetch mob type
					MobType mob = MobType.valueOf(entity);
					// If the mob type is null ignore the entity
					if (mob == null)
						continue;
					
					// Increment counter
					++getMobSettings(mob).mobCount;
				}
			}
			// Reset 'updatedThisTick' so updates can be run again later
			P.p.getServer().getScheduler().runTaskLater(P.p,
				new Runnable()
				{
					public void run()
					{
						needsUpdate = true;
					}
				}
				, ticksPerRecount);
			
			needsUpdate = false;
			return true;
		}
		return false;
	}

	public World getWorld()
	{
		return world;
	}

	public Set<Map.Entry<MMCoord, MMChunk>> getChunks()
	{
		return chunks.entrySet();
	}

	public MMChunk getChunk(final Chunk chunk)
	{
		if (!chunk.isLoaded())
			return null;

		return getChunk(new MMCoord(chunk.getX(), chunk.getZ()));
	}

	public MMChunk getChunk(final MMCoord coord)
	{
		return chunks.get(coord);
	}

	public void addChunk(final Chunk chunk)
	{
		final MMChunk mmchunk = new MMChunk(chunk);

		if (chunks.put(mmchunk.getCoord(), mmchunk) != null)
		{
			P.p.getLogger().warning("Newly loaded chunk already existed in chunk map");
			return;
		}

		++numChunks;
	}

	public void removeChunk(final Chunk chunk)
	{
		if (chunks.remove(new MMCoord(chunk.getX(), chunk.getZ())) == null)
		{
			P.p.getLogger().warning("A chunk was unloaded but no object existed for it");
			return;
		}

		--numChunks;
	}

	public int getMobCount(MobType mob)
	{
		if (mob == null)
			return 0;
		
		return getMobSettings(mob).mobCount;
	}
	
	/**
	 * Calculates the maximum number of monsters currently allowed in the world
	 * @return The max number of monsters
	 */
	public short maxMobs(final MobType mob)
	{
		if (mob == null)
			return Short.MAX_VALUE;

		short dynMax = (short) (getMobSettings(mob).mobDynMulti * numChunks >> 8);
			
		return getMobSettings(mob).mobMax < dynMax ? getMobSettings(mob).mobMax : dynMax;
	}

	public boolean withinMobLimit(MobType mob)
	{
		if (mob == null)
			return true;
		
		return maxMobs(mob) > getMobSettings(mob).mobCount;
	}

	public void incrementMobCount(MobType mob)
	{
		if (mob == null)
			return;
		++getMobSettings(mob).mobCount;
	}
	
	public void decrementMobCount(MobType mob)
	{
		if (mob == null)
			return;
		--getMobSettings(mob).mobCount;
	}
}
