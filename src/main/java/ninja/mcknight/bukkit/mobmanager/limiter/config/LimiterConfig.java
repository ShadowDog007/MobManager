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

package ninja.mcknight.bukkit.mobmanager.limiter.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.P;
import ninja.mcknight.bukkit.mobmanager.common.config.AbstractConfig;
import ninja.mcknight.bukkit.mobmanager.common.config.EnumSettingContainer;
import ninja.mcknight.bukkit.mobmanager.common.config.TSettingContainer;
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;
import ninja.mcknight.bukkit.mobmanager.limiter.util.MobType;
import ninja.mcknight.bukkit.mobmanager.limiter.world.MMWorld;

public class LimiterConfig extends AbstractConfig
{
	public final static Pattern layerPattern = Pattern.compile("^\\d+/{1}\\d+$");
	public final static Pattern layerSplitPattern = Pattern.compile("/{1}");
	
	public static boolean disableWarnings;
	public static boolean ignoreCreativePlayers;
	public static boolean useAsyncDespawnScanner;
	
	public static boolean removeTamedAnimals;
	public static boolean countTamedAnimals;
	
	public static boolean enableAnimalDespawning;
	public static boolean enableAnimalTracking;
	public static double daysTillFarmAnimalCleanup;
	public static int protectedFarmAnimalSaveInterval;
	
	public static short despawnSearchDistance, despawnSearchDistanceSquared;
	public static short despawnSearchHeight;
	public static short flyingMobAditionalBlockDepth;
	public static int ticksPerRecount;
	public static int ticksPerDespawnScan;
	public static int minTicksLivedForDespawn;
	
	public static TSettingContainer<ExtendedEntityType> ignoredMobs;
	public static TSettingContainer<ExtendedEntityType> disabledMobs;
	
	private static boolean[] mobOfTypeBeingIgnored;
	
	public static EnumSettingContainer enabledSpawnReasons;
	
	public static Set<String> enabledWorlds = new HashSet<String>();
	
	public static HashMap<String, WorldConfig> worldConfigs;
	
	public LimiterConfig()
	{
		FileConfiguration cfg = getConfig("", LIMITER_CONFIG_NAME);
		
		/* ################ ActiveWorlds ################ */
		List<String> activeWorlds = cfg.getStringList("EnabledWorlds");
		
		if (activeWorlds == null || activeWorlds.size() == 0)
		{
			activeWorlds = new ArrayList<String>();
			for (World world : P.p().getServer().getWorlds())
			{
				activeWorlds.add(world.getName());
			}
		}
		for (String world : activeWorlds)
			enabledWorlds.add(world);
		set(cfg, "EnabledWorlds", activeWorlds);
		
		/* ################ DisableWarnings ################ */
		disableWarnings = cfg.getBoolean("DisableWarnings", true);
		set(cfg, "DisableWarnings", disableWarnings);
		
		/* ################ UseAsyncDespawnScanner ################ */
		useAsyncDespawnScanner = cfg.getBoolean("UseAsyncDespawnScanner", false);
		set(cfg, "UseAsyncDespawnScanner", useAsyncDespawnScanner);
		// Disable async despawner as it it unsafe
		useAsyncDespawnScanner = false;
		
		/* ################ IgnoreCreativePlayers ################ */
		ignoreCreativePlayers = cfg.getBoolean("IgnoreCreativePlayers", false);
		set(cfg, "IgnoreCreativePlayers", ignoreCreativePlayers);
		
		/* ################ TamedAnimals ################ */
		removeTamedAnimals = cfg.getBoolean("RemoveTamedAnimals", false);
		set(cfg, "RemoveTamedAnimals", removeTamedAnimals);
		
		countTamedAnimals = cfg.getBoolean("CountTamedAnimals", true);
		set(cfg, "CountTamedAnimals", countTamedAnimals);
		
		/* ################ Animal Despawning Stuff ################ */
		enableAnimalDespawning = cfg.getBoolean("EnableAnimalDespawning", false);
		set(cfg, "EnableAnimalDespawning", enableAnimalDespawning);
		
		enableAnimalTracking = cfg.getBoolean("EnableAnimalTracking", true);
		set(cfg, "EnableAnimalTracking", enableAnimalTracking);
		
		daysTillFarmAnimalCleanup = cfg.getDouble("DaysTillFarmAnimalCleanup", 30.0D);
		set(cfg, "DaysTillFarmAnimalCleanup", daysTillFarmAnimalCleanup);
		
		protectedFarmAnimalSaveInterval = cfg.getInt("ProtectedFarmAnimalSaveInterval", 6000);
		set(cfg, "ProtectedFarmAnimalSaveInterval", protectedFarmAnimalSaveInterval);
		
		/* ################ TicksPerRecount ################ */
		ticksPerRecount = cfg.getInt("TicksPerRecount", 600);
		set(cfg, "TicksPerRecount", ticksPerRecount);
		
		/* ################ TicksPerDespawnScan ################ */
		ticksPerDespawnScan = cfg.getInt("TicksPerDespawnScan", 300);
		set(cfg, "TicksPerDespawnScan", ticksPerDespawnScan);
		
		/* ################ MinTicksLivedForDespawn ################ */
		minTicksLivedForDespawn = cfg.getInt("MinTicksLivedForDespawn", 100);
		set(cfg, "MinTicksLivedForDespawn", minTicksLivedForDespawn);
		
		/* ################ IgnoredMobs ################ */
		ignoredMobs =new TSettingContainer<ExtendedEntityType>(ExtendedEntityType.values(), cfg.getList("IgnoredMobs"), "IgnoredMobs");
		ignoredMobs.addDefaults(ExtendedEntityType.valueOf(EntityType.WITHER), ExtendedEntityType.valueOf(EntityType.VILLAGER));
		List<String> ignoredList = ignoredMobs.getList();
		set(cfg, "IgnoredMobs", ignoredList);
		String strList = ignoredMobs.toString();
		if (strList.length() != 0)
			MMComponent.getLimiter().info("IgnoredMobs: " + strList);
		
		/* ################ DisabledMobs ################ */
		disabledMobs = new TSettingContainer<ExtendedEntityType>(ExtendedEntityType.values(), cfg.getList("DisabledMobs"), "DisabledMobs");
		List<String> disabledList = disabledMobs.getList();
		set(cfg, "DisabledMobs", disabledList);
		strList = disabledMobs.toString();
		if (strList.length() != 0)
			MMComponent.getLimiter().info("DisabledMobs: " + strList);
		
		/* ################ EnabledSpawnReasons ################ */
		enabledSpawnReasons = new EnumSettingContainer(SpawnReason.class, cfg.getList("EnabledSpawnReasons", null), "The Spawn Reason '%s' is invalid");
		enabledSpawnReasons.addDefaults(SpawnReason.DEFAULT,
				SpawnReason.NATURAL,
				SpawnReason.SPAWNER,
				SpawnReason.CHUNK_GEN,
				SpawnReason.VILLAGE_DEFENSE,
				SpawnReason.VILLAGE_INVASION,
				SpawnReason.BUILD_IRONGOLEM,
				SpawnReason.BUILD_SNOWMAN,
				SpawnReason.BREEDING,
				SpawnReason.EGG);
		List<String> srList = enabledSpawnReasons.getList();
		set(cfg, "EnabledSpawnReasons", srList);
		strList = enabledSpawnReasons.toString();
		if (strList.length() != 0)
			MMComponent.getLimiter().info("EnabledSpawnReasons: " + strList);
		
		/* ################ DespawnSearchDistance ################ */
		short despawnSearchDistance = (short) Math.abs(cfg.getInt("DespawnSearchDistance", 72));
		LimiterConfig.despawnSearchDistance = despawnSearchDistance;
		LimiterConfig.despawnSearchDistanceSquared = despawnSearchDistance <= 0 ? 1 : (short) (despawnSearchDistance * despawnSearchDistance);
		set(cfg, "DespawnSearchDistance", despawnSearchDistance);
		
		/* ################ DespawnSearchHeight ################ */
		short despawnSearchHeight = (short) cfg.getInt("DespawnSearchHeight", 24);
		LimiterConfig.despawnSearchHeight = despawnSearchHeight <= 0 ? 24 : despawnSearchHeight;
		set(cfg, "DespawnSearchHeight", despawnSearchHeight);
		
		/* ################ FlyingMobAditionalBlockDepth ################ */
		flyingMobAditionalBlockDepth = (short) cfg.getInt("FlyingMobAditionalBlockDepth", 15);
		if (flyingMobAditionalBlockDepth < 0)
			flyingMobAditionalBlockDepth = 0;
		set(cfg, "FlyingMobAditionalBlockDepth", flyingMobAditionalBlockDepth);
		
		/* ################ Old Settings ################ */
		cfg.set("SpawnChunkSearchDistance", null);
		cfg.set("Layers", null);
		cfg.set("FlyingMobAditionalLayerDepth", null);
		
		// Copy the header to the file
		copyHeader(cfg, "Limiter_ConfigHeader.txt", "Limiter Global Config\n");
		saveConfig("", LIMITER_CONFIG_NAME, cfg);
	}
	
	public int setupWorlds()
	{
		int numWorlds = 0;
		worldConfigs = new HashMap<String, WorldConfig>();
		
		for (String worldName : enabledWorlds)
		{
			World world = P.p().getServer().getWorld(worldName);
			
			if (world == null)
				continue;
			
			WorldConfig wc = new WorldConfig(world);
			
			worldConfigs.put(world.getName(), wc);
			MMComponent.getLimiter().addWorld(new MMWorld(world, wc));
			
			++numWorlds;
		}
		
		return numWorlds;
	}
	
	public static boolean isIgnoringMobType(MobType type)
	{
		if (type == null)
			return true;
		
		return LimiterConfig.mobOfTypeBeingIgnored[type.ordinal()];
	}
}
