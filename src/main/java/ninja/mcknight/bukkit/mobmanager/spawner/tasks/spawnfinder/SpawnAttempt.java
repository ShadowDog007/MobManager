/*
 * Copyright 2013 Michael McKnight. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package ninja.mcknight.bukkit.mobmanager.spawner.tasks.spawnfinder;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.common.util.LocationCache;
import ninja.mcknight.bukkit.mobmanager.common.util.RandomLocationGen;
import ninja.mcknight.bukkit.mobmanager.spawner.config.Region;
import ninja.mcknight.bukkit.mobmanager.spawner.util.MobSpawner;

/**
 * Handles each step of a SpawnAttempt
 */
public class SpawnAttempt implements Runnable
{
	public enum AttemptState
	{
		/** Starts the attempt */
		INITIALISE,
		/** Generates a random location */
		GENERATE(true),
		/** Fetches the region of the generated location */
		GET_REGION,
		/** Fetches the light at that location */
		GET_LOCATION_INFO(true),
		/** Picks a mob and spawns it */
		PREPARE_MOB,
		/** Spawns the mob */
		SPAWN(true),
		/** Done :3 */
		FINISH;
		
		public final boolean sync;
		
		private AttemptState()
		{
			this(false);
		}
		
		private AttemptState(boolean sync)
		{
			this.sync = sync;
		}
		
		public AttemptState getNext()
		{
			int next = ordinal() + 1;
			AttemptState[] states = values();
			return next < states.length ? states[next] : FINISH;
		}
		
		public boolean after(AttemptState state)
		{
			return ordinal() > state.ordinal();
		}
	}
	
	private final SpawnAttemptExecutor saExecutor;
	private final Player player;
	private final int maxRange, minRange, heightRange;
	private boolean outsideSpawnLimits;
	
	private AttemptState currentState = AttemptState.INITIALISE;
	
	private Location spawnLocation;
	boolean wideLoc, tallLoc;
	private Region spawnRegion;
	private int playerY;
	
	private int lightLevel;
	private Biome biome;
	private Material materialBelow;
	private int time;
	private Environment environment;
	
	private MobSpawner spawner;
	
	public SpawnAttempt(SpawnAttemptExecutor saExecutor, Player player, int maxRange, int minRange, int heightRange, boolean outsideSpawnLimits)
	{
		this.saExecutor = saExecutor;
		this.player = player;
		
		this.maxRange = maxRange;
		this.minRange = minRange;
		this.heightRange = heightRange;
		this.outsideSpawnLimits = outsideSpawnLimits;
	}
	
	@Override
	public void run()
	{
		// If the player is invalid there is no need to continue
		if (!player.isValid())
			return;

		try
		{
			// Execute a step depending on the current state
			switch (currentState)
			{
			case INITIALISE:
				break;
			case GENERATE:
				stateMethodGenerate();
				break;
			case GET_REGION:
				stateMethodGetRegion();
				break;
			case GET_LOCATION_INFO:
				stateMethodGetLocationInfo();
				break;
			case PREPARE_MOB:
				stateMethodPrepareMob();
				break;
			case SPAWN:
				stateMethodSpawn();
				break;
			case FINISH:
				return;
			}
		}
		catch (Exception e)
		{
			MMComponent.getSpawner().severe("Error occured when attempting to spawn a mob", e);
			finish(true);
			return;
		}
		
		// Fetch the next state
		AttemptState nextState = currentState.getNext();
		
		// Check if we are not finished
		if (nextState != AttemptState.FINISH)
		{
			// Check if the next state can be executed now
//			boolean runNow = nextState.sync == currentState.sync;
			// Update the current state
			currentState = nextState;
			
			// Execute the task
			// If the next state can be run in the same thread we execute it right away
//			if (runNow)
//				run();
//			else
			saExecutor.addTask(this, currentState.sync);
		}
	}
	
	public AttemptState getState()
	{
		return currentState;
	}
	
	/**
	 * Sets the state to finished if the condition is true
	 * 
	 * @param condition The condition to check
	 * 
	 * @return The condition
	 */
	private boolean finish(boolean condition)
	{
		if (condition)
			currentState = AttemptState.FINISH;
		return condition;
	}
	
	/**
	 * Generates a random location
	 */
	private void stateMethodGenerate()
	{
		SpawnAttemptCache cache = saExecutor.threadCache.get();
		
		player.getLocation(cache.playerLoc);
		playerY = cache.playerLoc.getBlockY();
		
		Location spawnLoc = RandomLocationGen.getLocation(true, true, saExecutor.cfg.spawnGenerationAttempts, cache.playerLoc, maxRange, minRange, heightRange, cache.cacheLoc);

		// If the location is the players location then we don't want to spawn the mob 
		if (!finish(spawnLoc == cache.playerLoc))
		{
			spawnLoc.setWorld(cache.playerLoc.getWorld());
			spawnLocation = LocationCache.getCachedLocation(spawnLoc);
			Block b = spawnLocation.getBlock();
			wideLoc = RandomLocationGen.isWideLocation(b);
			tallLoc = RandomLocationGen.isTallLocation(b);
		}
	}
	
	/**
	 * Fetches a region which location is in and ensures one exists
	 */
	private void stateMethodGetRegion()
	{
		spawnRegion = MMComponent.getSpawner().getConfig().getRegion(spawnLocation);
		
		// Check if we actually got a region
		if (finish(spawnRegion == null))
			return;

		// If we are not outside player limits check if we are outside region limits
		if (!outsideSpawnLimits)
			outsideSpawnLimits = !spawnRegion.withinAliveLimit();
		
		// If we are outside spawn limits and can't ignore them we are finished
		finish(outsideSpawnLimits && !spawnRegion.ignoreMobLimits());
	}
	
	/**
	 * Fetch information about the spawn location to</br>
	 * be used to test requirements
	 */
	private void stateMethodGetLocationInfo()
	{
		Block b = spawnLocation.getBlock();
		
		lightLevel = b.getLightLevel();
		biome = b.getBiome();
		materialBelow = b.getRelative(BlockFace.DOWN).getType();
		
		time = (int) spawnLocation.getWorld().getTime();
		environment = spawnLocation.getWorld().getEnvironment();
	}
	
	/**
	 * Prepare a mob to be spawned
	 */
	private void stateMethodPrepareMob()
	{
		// Fetch the mob spawner
		spawner = spawnRegion.spawnMob(player, playerY, heightRange, spawnLocation, wideLoc, tallLoc, time, lightLevel, biome, materialBelow, environment, outsideSpawnLimits);
		// If we didn't get one we are finished
		finish(spawner == null);
	}
	
	/**
	 * Executes the mob spawner
	 */
	private void stateMethodSpawn()
	{
		spawner.spawn();
	}
	
	@Override
	public int hashCode()
	{
		return currentState.ordinal();
	}
}
