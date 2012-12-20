package com.forgenz.mobmanager;

import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Squid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class P extends JavaPlugin implements Listener
{

	public static P p = null;
	public static MemoryConfiguration cfg = null;

	private HashMap<String, MMWorld> worlds = new HashMap<String, MMWorld>();

	public P()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onLoad()
	{
		P.p = this;

		saveDefaultConfig();
		cfg = getConfig();
		
		// Makes sure the setting 'SpawnChunkDistance' is valid
		int searchDist = P.cfg.getInt("SpawnChunkDistance", 6);
		
		if (searchDist < 0)
			P.cfg.set("SpawnChunkDistance", Math.abs(searchDist));
		else if (searchDist == 0)
			P.cfg.set("SpawnChunkDistance", 1);
		
	}

	@Override
	public void onEnable()
	{
		P.p = this;

		final long time = System.currentTimeMillis();

		// Load all the worlds required
		for (final String worldName : cfg.getStringList("EnabledWorlds"))
		{
			final World world = getServer().getWorld(worldName);

			// Check the world exists
			if (world == null)
			{
				getLogger().warning(String.format("Could not find world '%s'", worldName));
				continue;
			}

		}

		// We are done :D
		getLogger().info(String.format("Finished loading %s %s (%.3fs)", getDescription().getName(), getDescription().getVersion(), (System.currentTimeMillis() - time) / 1000.0D));
	}

	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		p = null;
		cfg = null;
	}

	// Events Listener Methods

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkLoad(final ChunkLoadEvent event)
	{
		final MMWorld world = worlds.get(event.getChunk().getWorld().getName());

		// If the world is not found it must be inactive
		if (world == null)
			return;

		// Add the chunk to the world
		world.addChunk(event.getChunk());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkUnload(final ChunkUnloadEvent event)
	{
		if (event.isCancelled())
			return;

		final MMWorld world = worlds.get(event.getChunk().getWorld().getName());

		// If the world is not found it must be inactive
		if (world == null)
			return;

		// Remove the chunk from the world
		world.removeChunk(event.getChunk());
	}
	
	/**
	 * Scans nearby chunks for players on layers which cross a given height
	 * @param world The World the chunk is in
	 * @param chunk The coordinate of the center chunk
	 * @param y The height to search for players at
	 * @return True if there is a player within range of the center chunk and in a layer which overlaps the height 'y'
	 */
	private boolean playerNear(MMWorld world, MMCoord center, int y)
	{		
		Spiral spiral = new Spiral(center, P.cfg.getInt("SpawnChunkDistance", 6));
		
		while (!spiral.isFinished())
		{
			MMChunk chunk = world.getChunk(spiral.run());
			
			if (chunk == null)
				continue;
			
			if (!chunk.hasPlayers())
				continue;
			
			for (MMLayer layer : chunk.getLayersAt(y))
			{
				if (!layer.isEmpty())
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the entity needs to be ignored
	 * @param entity The entity in question
	 * @return True if the entity should be ignored
	 */
	private boolean ignoreCreature(LivingEntity entity)
	{
		return !(entity instanceof Monster || entity instanceof Animals || entity instanceof Squid);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onCreatureSpawn(final CreatureSpawnEvent event)
	{
		// This spawn reason is ignored as it mobs spawned with this reason are counted on chunk load
		if (event.getSpawnReason() == SpawnReason.CHUNK_GEN)
			return;
		
		// Checks if we can ignore the creature spawn
		if (ignoreCreature(event.getEntity()))
			return;
		
		MMWorld world = worlds.get(event.getLocation().getWorld().getName());
		// If the world is not found we ignore the spawn
		if (world == null)
			return;
		
		MMChunk chunk = world.getChunk(event.getLocation().getChunk());
		if (chunk == null)
		{
			P.p.getLogger().warning("Creature spawned on an unknown chunk");
			return;
		}
		
		switch (event.getSpawnReason())
		{
		// These Spawn Reasons will require additional checks
		case BREEDING:
		case EGG:			
			// Not sure if this is required, but meh
			if (event.getEntity() instanceof Animals)
			{
				// Check if we are within breeding limits
				if (!chunk.withinBreedingLimits())
				{
					event.setCancelled(true);
					return;
				}
			}
			
			
		// These Spawn Reasons will be only be allowed if limits have not been met
		case NATURAL:
		case SPAWNER:
		case VILLAGE_DEFENSE:
		case VILLAGE_INVASION:
			// Check if we are within spawn limits
			if (event.getEntity() instanceof Monster)
			{
				if (!world.withinMonsterLimit())
				{
					event.setCancelled(true);
					return;
				}
			}
			else if (event.getEntity() instanceof Animals)
			{
				if (!world.withinAnimalLimit())
				{
					event.setCancelled(true);
					return;
				}
			}
			else if (event.getEntity() instanceof Squid)
			{
				if (!world.withinSquidLimit())
				{
					event.setCancelled(true);
					return;
				}
			}
			// Checks that there is a player within range of the creature spawn
			if (!playerNear(world, chunk.getCoord(), event.getLocation().getBlockY()))
			{
				event.setCancelled(true);
				return;
			}
			
			
		// These Spawn Reasons are ignored and will be allowed regardless of limits
		case SLIME_SPLIT:
		case SPAWNER_EGG:
		case CUSTOM:
			// Meh Might as well.
		case BUILD_IRONGOLEM:
		case BUILD_SNOWMAN:
		case BUILD_WITHER:
			// Not sure what to do with these ones
		case JOCKEY:
		case LIGHTNING:
		default:
			// Counts the mob			
			if (event.getEntity() instanceof Monster)
			{
				world.changeNumMonsters(true);
			}
			else if (event.getEntity() instanceof Animals)
			{
				world.changeNumAnimals(true);
				chunk.changeNumAnimals(true);
			}
			else if (event.getEntity() instanceof Squid)
			{
				world.changeNumSquid(true);
			}
			break;
		}
	}

}
