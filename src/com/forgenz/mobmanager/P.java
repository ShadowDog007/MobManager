package com.forgenz.mobmanager;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Squid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * <b>MobManager</b> </br>
 * MobManager aims to reduce the number of unnecessary mob spawns </br>
 * 
 * @author Michael McKnight (ShadowDog007)
 *
 */
public class P extends JavaPlugin implements Listener, CommandExecutor
{

	public static P p = null;
	public static MemoryConfiguration cfg = null;

	private HashMap<String, MMWorld> worlds = null;

	@Override
	public void onLoad()
	{
	}

	@Override
	public void onEnable()
	{
		p = this;

		saveDefaultConfig();
		cfg = getConfig();

		// Makes sure the setting 'SpawnChunkDistance' is valid
		final int searchDist = P.cfg.getInt("SpawnChunkDistance", 6);

		if (searchDist < 0)
			P.cfg.set("SpawnChunkDistance", Math.abs(searchDist));
		else if (searchDist == 0)
			P.cfg.set("SpawnChunkDistance", 1);

		worlds = new HashMap<String, MMWorld>();

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

			worlds.put(worldName, new MMWorld(world));
		}

		getServer().getPluginManager().registerEvents(this, this);
		getCommand("mm").setExecutor(this);
		// And we are done :D
	}

	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		p = null;
		cfg = null;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args)
	{
		if (args.length == 1)
			if (args[0].equalsIgnoreCase("test"))
			{
				int numChunks = 0;
				int numPlayers = 0;
				int numPlayers2 = 0;
				int numMonsters = 0;
				int numAnimals = 0;
				int numSquid = 0;

				for (final Entry<String, MMWorld> world : worlds.entrySet())
				{
					for (final Entry<MMCoord, MMChunk> chunk : world.getValue().getChunks())
					{
						for (final MMLayer layer : chunk.getValue().getLayers())
							if (layer.getNumPlayers() > 0)
							{
								numPlayers2 += layer.getNumPlayers();
								getLogger().info(String.format("Found %d players | X:%d Z:%d MinY:%d MaxY:%d ", layer.getNumPlayers(), chunk.getValue().getCoord().getX(), chunk.getValue().getCoord().getZ(), layer.getMinY(), layer.getMaxY()));
							}
						numPlayers += chunk.getValue().getNumPlayers();
						++numChunks;
					}
					numMonsters += world.getValue().getNumMonsters();
					numAnimals += world.getValue().getNumAnimals();
					numSquid += world.getValue().getNumSquid();
				}

				sender.sendMessage(String.format("Worlds: %d, Chunks: %d", worlds.size(), numChunks));
				sender.sendMessage(String.format("Players: %d, Monsters: %d, Animals: %d, Squid: %d", numPlayers, numMonsters, numAnimals, numSquid));
				sender.sendMessage(String.format("Players2: %d", numPlayers2));

				numMonsters = 0;
				numAnimals = 0;
				numSquid = 0;

				for (final World world : getServer().getWorlds())
					for (final Entity entity : world.getEntities())
						if (isMonster(entity))
							++numMonsters;
						else if (isAnimal(entity))
							++numAnimals;
						else if (isSquid(entity))
							++numSquid;
				sender.sendMessage(String.format("Monsters: %d, Animals: %d, Squid: %d", numMonsters, numAnimals, numSquid));
			}
		return false;
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

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(final PlayerLoginEvent event)
	{
		// Fetch the world the player logged into
		final MMWorld world = worlds.get(event.getPlayer().getLocation().getWorld().getName());

		// If the world is inactive do nothing
		if (world == null)
			return;

		// Fetch the chunk the player logged into
		final MMChunk chunk = world.getChunk(event.getPlayer().getLocation().getChunk());

		// ERROR D:
		if (chunk == null)
		{
			getLogger().warning("Player logged into an unknown chunk");
			return;
		}

		// Update the chunk and each layers player count
		chunk.playerEntered();
		for (final MMLayer layer : chunk.getLayersAt(event.getPlayer().getLocation().getBlockY()))
			layer.playerEntered();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(final PlayerQuitEvent event)
	{
		// Fetch the world the player quit from
		final MMWorld world = worlds.get(event.getPlayer().getLocation().getWorld().getName());

		// If the world is inactive do nothing
		if (world == null)
			return;

		// Fetch the chunk the player quit from
		final MMChunk chunk = world.getChunk(event.getPlayer().getLocation().getChunk());

		// ERROR D:
		if (chunk == null)
		{
			getLogger().warning("Player quit from an unknown chunk");
			return;
		}

		// Update the chunk and each layers player count
		chunk.playerLeft();
		for (final MMLayer layer : chunk.getLayersAt(event.getPlayer().getLocation().getBlockY()))
			layer.playerLeft();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerMove(final PlayerMoveEvent event)
	{
		// First we make sure there is actually any point in doing anything

		// Check if the player is moving between chunks
		if (event.getTo().getBlockX() >> 3 != event.getFrom().getBlockX() >> 3 || event.getTo().getBlockZ() >> 3 != event.getFrom().getBlockZ() >> 3)
		{
			// Fetch the world the player is moving in
			final MMWorld world = worlds.get(event.getTo().getWorld().getName());

			// If the world is not active ignore the movement
			if (world == null)
				return;

			// Fetch the chunks the player is moving between
			final MMChunk fromChunk = world.getChunk(event.getFrom().getChunk());
			final MMChunk toChunk = world.getChunk(event.getTo().getChunk());

			// Update each chunks player count
			fromChunk.playerLeft();
			toChunk.playerEntered();

			// Update each layers player count
			for (final MMLayer layer : fromChunk.getLayersAt(event.getFrom().getBlockY()))
				layer.playerLeft();

			for (final MMLayer layer : toChunk.getLayersAt(event.getTo().getBlockY()))
				layer.playerEntered();
		}
		// Check if the player is only moving up or down
		else if (event.getTo().getBlockY() != event.getFrom().getBlockY())
		{
			// Fetch the world the player is moving in
			final MMWorld world = worlds.get(event.getTo().getWorld().getName());

			// If the world is not active ignore the movement
			if (world == null)
				return;

			// Fetch the chunk the player is moving in
			final MMChunk chunk = world.getChunk(event.getTo().getChunk());

			// Update each layers player count
			// TODO Find a better way to tell if a player is moving between
			// layers (NOTE: Layers can overlap)
			for (final MMLayer layer : chunk.getLayersAt(event.getFrom().getBlockY()))
				layer.playerLeft();

			for (final MMLayer layer : chunk.getLayersAt(event.getTo().getBlockY()))
				layer.playerEntered();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTeleport(final PlayerTeleportEvent event)
	{
		// Fetch the world the player is teleporting from
		final MMWorld fromWorld = worlds.get(event.getFrom().getWorld().getName());
		MMWorld toWorld = null;

		// Check if the world is active
		if (fromWorld != null)
		{
			// If the from world and to world are the same worlds copy the
			// reference
			if (event.getTo().getWorld().getName().equals(fromWorld.getWorld().getName()))
				toWorld = fromWorld;

			// Fetch the chunk the player teleported from
			final MMChunk fromChunk = fromWorld.getChunk(event.getFrom().getChunk());

			// Update the chunks player count
			fromChunk.playerLeft();

			// Update each layers player count
			for (final MMLayer layer : fromChunk.getLayersAt(event.getFrom().getBlockY()))
				layer.playerLeft();
		}

		// If the toWorld has not been set fetch it (NOTE: Could be because
		// fromWorld was inactive)
		if (toWorld == null)
			toWorld = worlds.get(event.getTo().getWorld().getName());

		// Check if the world is active
		if (toWorld != null)
		{
			// Fetch the chunk the player teleported into
			final MMChunk toChunk = toWorld.getChunk(event.getTo().getChunk());

			// Update the chunks player count
			toChunk.playerEntered();

			// Update each layers player count
			for (final MMLayer layer : toChunk.getLayersAt(event.getTo().getBlockY()))
				layer.playerEntered();
		} else
			// TODO REMOVE THIS SHIT
			getLogger().warning("Missing to world on Teleport");
	}

	/**
	 * Scans nearby chunks for players on layers which cross a given height
	 * 
	 * @param world
	 *            The World the chunk is in
	 * @param chunk
	 *            The coordinate of the center chunk
	 * @param y
	 *            The height to search for players at
	 * @return True if there is a player within range of the center chunk and in
	 *         a layer which overlaps the height 'y'
	 */
	private boolean playerNear(final MMWorld world, final MMCoord center, final int y)
	{
		// Creates a spiral generator
		final Spiral spiral = new Spiral(center, P.cfg.getInt("SpawnChunkDistance", 6));

		// Loop through until the entire circle has been generated
		while (!spiral.isFinished())
		{
			// Fetch the given chunk
			final MMChunk chunk = world.getChunk(spiral.run());

			// If the chunk is not loaded continue
			if (chunk == null)
				continue;

			// If the chunk has no players in it continue
			if (!chunk.hasPlayers())
				continue;

			// Loop through each layer which overlaps the height 'y'
			for (final MMLayer layer : chunk.getLayersAt(y))
				// If the layer has a player in it then there is a player close
				// to the center near Y = 'y'
				if (!layer.isEmpty())
					return true;
		}
		return false;
	}

	/**
	 * Checks if the entity needs to be ignored
	 * 
	 * @param entity
	 *            The entity in question
	 * @return True if the entity should be ignored
	 */
	private boolean ignoreCreature(final Entity entity)
	{
		return !(isMonster(entity) || isAnimal(entity) || isSquid(entity));
	}

	protected boolean isMonster(final Entity entity)
	{
		return entity instanceof Monster || entity instanceof Slime || entity instanceof Ghast;
	}

	protected boolean isAnimal(final Entity entity)
	{
		return entity instanceof Animals;
	}

	protected boolean isSquid(final Entity entity)
	{
		return entity instanceof Squid;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onCreatureSpawn(final CreatureSpawnEvent event)
	{
		// TODO REMOVE THIS SHIT
		if (event.getSpawnReason() == SpawnReason.SPAWNER)
			getLogger().info(event.getEntity().toString() + " SPAWNER");

		// Checks if we can ignore the creature spawn
		if (ignoreCreature(event.getEntity()))
			return;

		final MMWorld world = worlds.get(event.getLocation().getWorld().getName());
		// If the world is not found we ignore the spawn
		if (world == null)
			return;

		// Fetch the chunk the spawn is occurring on
		final MMChunk chunk = world.getChunk(event.getLocation().getChunk());
		// ERROR D:
		if (chunk == null)
		{
			getLogger().warning("Creature spawned on an unknown chunk");
			return;
		}

		switch (event.getSpawnReason())
		{
		// These Spawn Reasons will require additional checks
		case BREEDING:
		case EGG:
			// Not sure if this is required, but meh
			if (isAnimal(event.getEntity()))
			{
				// Try to update number of mobs in this world
				world.updateNumMobs();
				// Check if we are within breeding limits
				if (!chunk.withinBreedingLimits())
				{
					event.setCancelled(true);
					return;
				}
			}

			// These Spawn Reasons will be only be allowed if limits have not
			// been met
		case DEFAULT:
		case NATURAL:
		case SPAWNER:
		case CHUNK_GEN:
		case VILLAGE_DEFENSE:
		case VILLAGE_INVASION:
			// Try to update the number of mobs in this world
			world.updateNumMobs();
			// Check if we are within spawn limits
			if (isMonster(event.getEntity()))
			{
				if (!world.withinMonsterLimit())
				{
					// TODO REMOVE THIS SHIT
					if (event.getSpawnReason() == SpawnReason.SPAWNER)
						getLogger().info(event.getEntity().toString() + " SPAWNER HIT LIMIT");
					event.setCancelled(true);
					return;
				}
			} else if (isAnimal(event.getEntity()))
			{
				if (!world.withinAnimalLimit())
				{
					event.setCancelled(true);
					return;
				}
			} else if (isSquid(event.getEntity()))
				if (!world.withinSquidLimit())
				{
					event.setCancelled(true);
					return;
				}
			// Checks that there is a player within range of the creature spawn
			if (!playerNear(world, chunk.getCoord(), event.getLocation().getBlockY()))
			{
				// TODO REMOVE THIS SHIT
				if (event.getSpawnReason() == SpawnReason.SPAWNER)
				{

					getLogger().info(event.getEntity().toString() + " SPAWNER NO1 NEARBY");
					getLogger().info(String.format("Chunk: %d, %d Y: %d", event.getEntity().getLocation().getChunk().getX(), event.getEntity().getLocation().getChunk().getZ(), event.getEntity().getLocation().getBlockY()));
				}
				event.setCancelled(true);
				return;
			}

			// These Spawn Reasons are ignored and will be allowed regardless of
			// limits
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
			if (isMonster(event.getEntity()))
				world.changeNumMonsters(true);
			else if (isAnimal(event.getEntity()))
			{
				world.changeNumAnimals(true);
				chunk.changeNumAnimals(true);
			} else if (isSquid(event.getEntity()))
				world.changeNumSquid(true);
		}
		// TODO REMOVE THIS SHIT
		getLogger().info(String.format("Entity: %s, Reason: %s", event.getEntity().toString(), event.getSpawnReason().toString()));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDeath(final EntityDeathEvent event)
	{
		if (ignoreCreature(event.getEntity()))
			return;

		final MMWorld world = worlds.get(event.getEntity().getLocation().getWorld().getName());

		if (world == null)
			return;

		if (isMonster(event.getEntity()))
			world.changeNumMonsters(false);
		else if (isAnimal(event.getEntity()))
		{
			world.changeNumAnimals(false);

			final MMChunk chunk = world.getChunk(event.getEntity().getLocation().getChunk());

			if (chunk == null)
			{
				getLogger().warning("Creature died in an unknown chunk");
				return;
			}

			chunk.changeNumAnimals(false);
		} else if (isSquid(event.getEntity()))
			world.changeNumSquid(false);
	}

}
