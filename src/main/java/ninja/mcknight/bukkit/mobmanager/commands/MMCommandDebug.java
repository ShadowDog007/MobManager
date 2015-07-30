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

package ninja.mcknight.bukkit.mobmanager.commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import ninja.mcknight.bukkit.mobmanager.abilities.abilities.PotionAbility;
import ninja.mcknight.bukkit.mobmanager.abilities.config.AbilityConfig;
import ninja.mcknight.bukkit.mobmanager.abilities.config.MobAbilityConfig;
import ninja.mcknight.bukkit.mobmanager.abilities.util.ValueChance;
import ninja.mcknight.bukkit.mobmanager.common.util.ExtendedEntityType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ninja.mcknight.bukkit.mobmanager.abilities.abilities.Ability;

public class MMCommandDebug extends MMCommand
{

	MMCommandDebug()
	{
		super(Pattern.compile("debug", Pattern.CASE_INSENSITIVE), Pattern.compile("^.*$"), 0, 1);
	}

	@Override
	public void run(CommandSender sender, String maincmd, String[] args)
	{
		if (sender instanceof Player && !sender.hasPermission("mobmanager.debug"))
		{
			sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use /mm debug");
			return;
		}
		
		if (!super.validArgs(sender, maincmd, args))
			return;
		
		if (args.length >= 2 && args[1].equalsIgnoreCase("entitypotions"))
		{
			if (sender instanceof Player == false)
				return;
			
			Player player = (Player) sender;
			
			HashMap<PotionEffectType, Integer> effectCounts = new HashMap<PotionEffectType, Integer>();
			
			for (LivingEntity entity : player.getWorld().getLivingEntities())
			{
				for (PotionEffect effect : entity.getActivePotionEffects())
				{
					Integer count = effectCounts.get(effect.getType());
					
					if (count == null)
						count = 0;
					
					effectCounts.put(effect.getType(), count + 1);
				}
			}
			
			Iterator<Entry<PotionEffectType, Integer>> it = effectCounts.entrySet().iterator();
			
			while (it.hasNext())
			{
				Entry<PotionEffectType, Integer> e = it.next();
				
				player.sendMessage(String.format("Effect: %s, Count: %d", e.getKey().getName(), e.getValue()));
			}
			return;
		}
		
		if (args.length >= 2 && args[1].equalsIgnoreCase("potionlist"))
		{
			sender.sendMessage("PotionList: " + PotionAbility.getPotionEffectList());
			return;
		}
		
		if (args.length >= 2 && args[1].equalsIgnoreCase("zombieabilities"))
		{
			if (sender instanceof Player == false)
				return;
			
			Player player = (Player) sender;
			
			MobAbilityConfig ma = AbilityConfig.i().getMobConfig(player.getWorld().getName(), ExtendedEntityType.valueOf(EntityType.ZOMBIE), null);
			
			if (ma == null)
			{
				player.sendMessage("No config found");
				return;
			}
			
			player.sendMessage(ma.attributes.size() + " attributes");
			int count = 0;
			
			for (ValueChance<Ability> vc : ma.attributes.values())
				count += vc.getNumChances();
			
			player.sendMessage(count + " individual chances");
			return;
		}
		
		sender.sendMessage("This does nothing without arguments");
	}

	@Override
	public String getUsage()
	{
		return "%s/%s %s%s";
	}

	@Override
	public String getDescription()
	{
		return "Used to debug MobManager (May cause errors without both components)";
	}

	@Override
	public String getAliases()
	{
		return "debug";
	}
}
