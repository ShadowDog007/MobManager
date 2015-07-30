package ninja.mcknight.bukkit.mobmanager.spawner.util;

import java.lang.ref.WeakReference;

import ninja.mcknight.bukkit.mobmanager.common.util.PlayerFinder;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.common.util.LocationCache;

public class PlayerMobCounter extends MobCounter
{
	private final WeakReference<Player> player;
	
	public PlayerMobCounter(Player player)
	{
		super(0, 0, false);
		
		this.player = new WeakReference<Player>(player);
	}
	
	public boolean withinLimit(int maxAliveMobs, int mobCooldown)
	{
		super.maxAliveMobs = maxAliveMobs;
		super.mobCooldown = mobCooldown;
		
		return super.withinLimit();
	}
	
	@Override
	public boolean remove(LivingEntity entity)
	{
		Player player = this.player.get();
		
		// If the player no longer exists we can free the mob
		if (player == null)
		{
			return true;
		}
		
		return playerOutOfRange(player, entity);
	}
	
	
	/**
	 * Checks if the player is out of range of the given entity
	 * 
	 * @param player The player
	 * @param entity The entity
	 * 
	 * @return True if the player and entity are too far away from each other to count towards the player mob limit
	 */
	private boolean playerOutOfRange(Player player, LivingEntity entity)
	{
		// If the entity and player are in different worlds they are too far away
		if (player.getWorld() != entity.getWorld())
			return true;
		
		// Fetch player and mob locations
		Location playerLoc = LocationCache.getCachedLocation(player);
		Location mobLoc = LocationCache.getCachedLocation(entity);
		
		// Check if the locations are out of range
		return !PlayerFinder.FinderMode.CYLINDER.withinRange(playerLoc, mobLoc, MMComponent.getSpawner().getConfig().mobDistanceForLimitRemoval, 32);
	}
}
