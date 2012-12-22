package com.forgenz.mobmanager.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.world.MMChunk;
import com.forgenz.mobmanager.world.MMLayer;
import com.forgenz.mobmanager.world.MMWorld;

public class PlayerListener implements Listener
{

	public PlayerListener()
	{
	}
	
	/**
	 * Updates the player counts within each chunk </br>
	 * @param toChunk The chunk the player is moving into
	 * @param toY The height of the player inside the to chunk
	 * @param fromChunk The chunk the player moved from
	 * @param fromY The height the player was at when they moved
	 */
	public void updateChunkPlayerCount(final MMChunk toChunk, final int toY, final MMChunk fromChunk, final int fromY)
	{		
		if (fromChunk != null)
		{
			fromChunk.playerLeft();
			
			for (MMLayer layer : fromChunk.getLayersAt(fromY))
				layer.playerLeft();
		}
		
		if (toChunk != null)
		{
			toChunk.playerEntered();
			
			for (MMLayer layer : toChunk.getLayersAt(toY))
				layer.playerEntered();
		}
	}
	
	// Listener methods
	
	/**
	 * Updates player counts when a player joins the server
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event)
	{
		// Fetch the world the player logged into
		final MMWorld world = P.worlds.get(event.getPlayer().getLocation().getWorld().getName());

		// If the world is inactive do nothing
		if (world == null)
			return;

		// Fetch the chunk the player logged into
		final MMChunk chunk = world.getChunk(event.getPlayer().getLocation().getChunk());

		updateChunkPlayerCount(chunk, event.getPlayer().getLocation().getBlockY(), null, 0);
	}
	
	/**
	 * Updates player counts when a player quits the server
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(final PlayerQuitEvent event)
	{
		// Fetch the world the player quit from
		final MMWorld world = P.worlds.get(event.getPlayer().getLocation().getWorld().getName());

		// If the world is inactive do nothing
		if (world == null)
			return;

		// Fetch the chunk the player quit from
		final MMChunk chunk = world.getChunk(event.getPlayer().getLocation().getChunk());

		updateChunkPlayerCount(null, 0, chunk, event.getPlayer().getLocation().getBlockY());
	}

	/**
	 * Updates player counts when players move between chunks or move up or down
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent event)
	{
		// First we make sure there is actually any point in doing anything

		// Check if the player is moving between chunks
		if (event.getTo().getBlockX() >> 3 != event.getFrom().getBlockX() >> 3 || event.getTo().getBlockZ() >> 3 != event.getFrom().getBlockZ() >> 3)
		{
			// Fetch the world the player is moving in
			final MMWorld world = P.worlds.get(event.getTo().getWorld().getName());

			// If the world is not active ignore the movement
			if (world == null)
			{
				return;
			}

			// Fetch the chunks the player is moving between
			final MMChunk fromChunk = world.getChunk(event.getFrom().getChunk());
			final MMChunk toChunk = world.getChunk(event.getTo().getChunk());
			
			// Update chunks player count
			updateChunkPlayerCount(toChunk, event.getTo().getBlockY(), fromChunk, event.getFrom().getBlockY());
		}
		// Check if the player is only moving up or down
		else if (event.getTo().getBlockY() != event.getFrom().getBlockY())
		{
			// Fetch the world the player is moving in
			final MMWorld world = P.worlds.get(event.getTo().getWorld().getName());

			// If the world is not active ignore the movement
			if (world == null)
			{
				return;
			}

			// Fetch the chunk the player is moving in
			final MMChunk chunk = world.getChunk(event.getTo().getChunk());

			updateChunkPlayerCount(chunk, event.getTo().getBlockY(), chunk, event.getFrom().getBlockY());
		}
	}
	
	/**
	 * Updates player counts when players teleport between worlds </br>
	 * If a player teleports within a world it passes the event to <i>onPlayerMove</i>
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(final PlayerTeleportEvent event)
	{
		// Check if the worlds differ
		if (!event.getFrom().getWorld().getName().equals(event.getTo().getWorld().getName()))
		{
			// Fetch each world
			MMWorld toWorld = P.worlds.get(event.getTo().getWorld().getName());
			MMWorld fromWorld = P.worlds.get(event.getFrom().getWorld().getName());
			
			// Fetch each chunk
			MMChunk toChunk = toWorld != null ? toWorld.getChunk(event.getTo().getChunk()) : null;
			MMChunk fromChunk = fromWorld != null ? fromWorld.getChunk(event.getFrom().getChunk()) : null;
			
			// Update player counts
			updateChunkPlayerCount(toChunk, event.getTo().getBlockY(), fromChunk, event.getFrom().getBlockY());
		}
		else
		{
			onPlayerMove(event);
		}
	}
	
	/**
	 * Updates player counts when players teleport between worlds </br>
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPortalEvent(final PlayerPortalEvent event)
	{
		onPlayerTeleport(event);
	}
}
