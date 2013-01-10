package com.forgenz.mobmanager.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.world.MMWorld;

public class ChunkListener implements Listener
{
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkLoad(final ChunkLoadEvent event)
	{
		final MMWorld world = P.worlds.get(event.getChunk().getWorld().getName());

		// If the world is not found it must be inactive
		if (world == null)
			return;

		// Add the chunk to the world
		world.addChunk(event.getChunk(), true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkUnload(final ChunkUnloadEvent event)
	{
		final MMWorld world = P.worlds.get(event.getChunk().getWorld().getName());

		// If the world is not found it must be inactive
		if (world == null)
			return;

		// Remove the chunk from the world
		world.removeChunk(event.getChunk());
	}
}
