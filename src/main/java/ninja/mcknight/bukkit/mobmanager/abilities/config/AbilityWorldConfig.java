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

package ninja.mcknight.bukkit.mobmanager.abilities.config;

import java.util.HashMap;

import ninja.mcknight.bukkit.mobmanager.common.config.EnumSettingContainer;
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import ninja.mcknight.bukkit.mobmanager.MMComponent;
import ninja.mcknight.bukkit.mobmanager.common.config.AbstractConfig;

public class AbilityWorldConfig extends AbstractConfig
{	
	private boolean useWorldSettings = true;
	public final EnumSettingContainer enabledSpawnReasons;
	
	public final HashMap<ExtendedEntityType, MobAbilityConfig> mobs = new HashMap<ExtendedEntityType, MobAbilityConfig>();
	
	protected AbilityWorldConfig(FileConfiguration cfg, String folder)
	{		
		/* ################ UseWorldSettings ################ */
		if (folder.length() != 0)
		{
			useWorldSettings = cfg.getBoolean("UseWorldSettings", false);
			set(cfg, "UseWorldSettings", useWorldSettings);
		}
		
		/* ################ EnabledSpawnReasons ################ */
		enabledSpawnReasons = new EnumSettingContainer(SpawnReason.class, cfg.getList("EnabledSpawnReasons", null), "The Spawn Reason '%s' is invalid");
		enabledSpawnReasons.addDefaults((Object[]) SpawnReason.values());
		set(cfg, "EnabledSpawnReasons", enabledSpawnReasons.getList());
		
		/* ################ MobAbilities ################ */
		ConfigurationSection sect = cfg.getConfigurationSection("MobAbilities");
		if (sect == null)
			sect = cfg.createSection("MobAbilities");
		set(cfg, "MobAbilities", sect);
		
		if (!useWorldSettings || enabledSpawnReasons.getList().size() == 0)
			return;
		
		for (String key : sect.getKeys(false))
		{
			ExtendedEntityType mobType = ExtendedEntityType.valueOf(key);
			
			if (mobType == null)
			{
				MMComponent.getAbilities().warning(String.format("No such mob named '%s' in %s", key, AbilityConfig.ABILITY_CONFIG_NAME));
				continue;
			}
			
			ConfigurationSection mobSect = sect.getConfigurationSection(key);
			if (mobSect == null)
				mobSect = sect.createSection(key);
			
			mobs.put(mobType, new MobAbilityConfig(mobType, mobSect));
			set(sect, key, mobSect);
		}
	}
	
	public boolean worldSettingsEnabled()
	{
		return useWorldSettings;
	}
}