package com.forgenz.mobmanager;

import java.util.HashMap;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Squid;

public class MMWorld
{
	private World world;

	private HashMap<MMCoord, MMChunk> chunks;
	private int numChunks = 0;

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
				if (entity instanceof Monster)
					++numMonsters;
				else if (entity instanceof Animals)
					++numAnimals;
				else if (entity instanceof Squid)
					++numSquid;
		}
		
		int maxMonsters = P.cfg.getInt("WorldMaximum." + world.getName() + ".Monsters", Integer.MAX_VALUE);
		int maxAnimals = P.cfg.getInt("WorldMaximum." + world.getName() + ".Animals", Integer.MAX_VALUE);
		int maxSquid = P.cfg.getInt("WorldMaximum." + world.getName() + ".Squid", Integer.MAX_VALUE);
		
		P.p.getLogger().info(String.format("Loaded world '%s' with maximums M:%d, A:%d, S:%d", world.getName(), maxMonsters, maxAnimals, maxSquid));
	}

	public World getWorld()
	{
		return world;
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

	public boolean withinMonsterLimit()
	{
		return (numMonsters < P.cfg.getInt("WorldMaximum." + world.getName() + ".Monsters", Integer.MAX_VALUE)) 
				&& (numMonsters < P.cfg.getInt("ChunkCalculatedMaximum." + world.getName() + ".Monsters", Integer.MAX_VALUE) * numChunks / 256);
	}
	
	public void changeNumMonsters(boolean increase) {
		if (increase)
			++numMonsters;
		else
			--numMonsters;
	}

	public boolean withinAnimalLimit()
	{
		return (numAnimals < P.cfg.getInt("WorldMaximum." + world.getName() + ".Animals", Integer.MAX_VALUE))
				&& (numAnimals < P.cfg.getInt("ChunkCalculatedMaximum." + world.getName() + ".Animals", Integer.MAX_VALUE) * numChunks / 256);
	}
	
	public void changeNumAnimals(boolean increase) {
		if (increase)
			++numAnimals;
		else
			--numAnimals;
	}

	public boolean withinSquidLimit()
	{
		return (numSquid < P.cfg.getInt("WorldMaximum." + world.getName() + ".Squid", Integer.MAX_VALUE))
				&& (numSquid < P.cfg.getInt("ChunkCalculatedMaximum." + world.getName() + ".Squid", Integer.MAX_VALUE) * numChunks / 256);
	}
	
	public void changeNumSquid(boolean increase) {
		if (increase)
			++numSquid;
		else
			--numSquid;
	}
}
