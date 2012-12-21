package com.forgenz.mobmanager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class MMWorld
{
	private World world;

	private HashMap<MMCoord, MMChunk> chunks;
	private int numChunks = 0;

	private boolean updatedThisTick = false;

	private int numMonsters = 0;
	private int numAnimals = 0;
	private int numSquid = 0;

	public MMWorld(final World world)
	{
		this.world = world;

		chunks = new HashMap<MMCoord, MMChunk>();

		// Store already loaded chunks
		for (final Chunk chunk : world.getLoadedChunks())
		{
			final MMChunk mmchunk = new MMChunk(chunk);

			chunks.put(mmchunk.getCoord(), mmchunk);
			++numChunks;

			// Counts the number of living entities
			for (final Entity entity : chunk.getEntities())
				if (P.p.isMonster(entity))
					++numMonsters;
				else if (P.p.isAnimal(entity))
					++numAnimals;
				else if (P.p.isSquid(entity))
					++numSquid;
		}

		final int maxMonsters = P.cfg.getInt("WorldMaximum." + world.getName() + ".Monsters", Integer.MAX_VALUE);
		final int maxAnimals = P.cfg.getInt("WorldMaximum." + world.getName() + ".Animals", Integer.MAX_VALUE);
		final int maxSquid = P.cfg.getInt("WorldMaximum." + world.getName() + ".Squid", Integer.MAX_VALUE);

		P.p.getLogger().info(String.format("Loaded world '%s', limits M:%d, A:%d, S:%d", world.getName(), maxMonsters, maxAnimals, maxSquid));
	}

	protected boolean updateNumMobs()
	{
		if (!updatedThisTick)
		{
			numMonsters = 0;
			numAnimals = 0;
			numSquid = 0;

			for (final Chunk chunk : world.getLoadedChunks())
			{
				final MMChunk mmchunk = getChunk(chunk);

				mmchunk.resetNumAnimals();

				for (final Entity entity : chunk.getEntities())
				{
					if (!(entity instanceof LivingEntity))
						continue;

					if (P.p.isMonster(entity))
						++numMonsters;
					else if (P.p.isAnimal(entity))
					{
						++numAnimals;
						mmchunk.changeNumAnimals(true);
					} else if (P.p.isSquid(entity))
						++numSquid;
				}
			}
			// Reset 'updatedThisTick' so updates can be run in the next tick
			P.p.getServer().getScheduler().runTaskLater(P.p,
				new Runnable()
				{
					public void run()
					{
						updatedThisTick = false;
					}
				}
				, 1L);
			
			updatedThisTick = true;
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

	public int getNumMonsters()
	{
		return numMonsters;
	}

	public boolean withinMonsterLimit()
	{
		return (numMonsters < P.cfg.getInt("WorldMaximum." + world.getName() + ".Monsters", Integer.MAX_VALUE)) && (numMonsters < P.cfg.getInt("ChunkCalculatedMaximum." + world.getName() + ".Monsters", Integer.MAX_VALUE) * numChunks >> 8);
	}

	public void changeNumMonsters(final boolean increase)
	{
		if (increase)
			++numMonsters;
		else
			--numMonsters;
	}

	public int getNumAnimals()
	{
		return numAnimals;
	}

	public boolean withinAnimalLimit()
	{
		return (numAnimals < P.cfg.getInt("WorldMaximum." + world.getName() + ".Animals", Integer.MAX_VALUE)) && (numAnimals < P.cfg.getInt("ChunkCalculatedMaximum." + world.getName() + ".Animals", Integer.MAX_VALUE) * numChunks >> 8);
	}

	public void changeNumAnimals(final boolean increase)
	{
		if (increase)
			++numAnimals;
		else
			--numAnimals;
	}

	public int getNumSquid()
	{
		return numSquid;
	}

	public boolean withinSquidLimit()
	{
		return (numSquid < P.cfg.getInt("WorldMaximum." + world.getName() + ".Squid", Integer.MAX_VALUE)) && (numSquid < P.cfg.getInt("ChunkCalculatedMaximum." + world.getName() + ".Squid", Integer.MAX_VALUE) * numChunks >> 8);
	}

	public void changeNumSquid(final boolean increase)
	{
		if (increase)
			++numSquid;
		else
			--numSquid;
	}
}
