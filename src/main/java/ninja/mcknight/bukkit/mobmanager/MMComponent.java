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

package ninja.mcknight.bukkit.mobmanager;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import ninja.mcknight.bukkit.mobmanager.abilities.AbilitiesComponent;
import ninja.mcknight.bukkit.mobmanager.bounty.BountyComponent;
import ninja.mcknight.bukkit.mobmanager.common.config.AbstractConfig;
import ninja.mcknight.bukkit.mobmanager.common.util.MMLogger;
import ninja.mcknight.bukkit.mobmanager.limiter.LimiterComponent;
import ninja.mcknight.bukkit.mobmanager.spawner.SpawnerComponent;

public abstract class MMComponent
{
	public enum Component
	{
		//COMMON(CommonComponent.class, "MobManager", (Component[]) null),
		LIMITER(LimiterComponent.class, "MM-Limiter", (Component[]) null),
		ABILITIES(AbilitiesComponent.class,"MM-Abilities", (Component[]) null),
		BOUNTY(BountyComponent.class, "MM-Bounty", (Component[]) null),
		SPAWNER(SpawnerComponent.class, "MM-Spawner", Component.LIMITER);
		
		private final boolean canEnable;
		private final MMComponent instance;
		private final Logger log;
		private ArrayList<Component> depends = null;
		private final Component[] dependencies;
		
		Component(Class<? extends MMComponent> clazz, String loggerName, Component... dependencies)
		{
			this(true, clazz, loggerName, dependencies);
		}
		
		Component(boolean canEnable, Class<? extends MMComponent> clazz, String loggerName, Component... dependencies)
		{
			this.canEnable = canEnable;
			this.log = new MMLogger(clazz, loggerName);
			
			MMComponent instance = null;
			
			try
			{
				instance = clazz.getConstructor(Component.class).newInstance(this);
			} catch (Exception e)
			{
				System.out.print("Failure creating component instances for MobManager");
				e.printStackTrace();
			}
			
			this.instance = instance;
			

			this.dependencies = dependencies;
			if (dependencies != null)
			{
				for (Component c : dependencies)
				{
					c.iDependOnYou(this);
				}
			}
		}
		
		public boolean canEnable()
		{
			return canEnable;
		}
		
		/**
		 * Tells the component that the given component depends on it
		 * @param c The component which depends on this
		 */
		private void iDependOnYou(Component c)
		{
			if (depends == null)
			{
				depends = new ArrayList<Component>();
			}
			else if (depends.contains(c))
			{
				throw new IllegalStateException(String.format("%s already depends on %s", c.toString(), toString()));
			}
			
			depends.add(c);
		}
		
		/**
		 * Fetches all the dependencies of this component
		 */
		public Component[] getDependencies()
		{
			return dependencies != null ? dependencies.clone() : new Component[0];
		}
		
		/**
		 * Fetches all the components which depend on this component
		 */
		public Component[] getDepends()
		{
			return depends != null ? depends.toArray(new Component[depends.size()]) : new Component[0];
		}
		
		public MMComponent i()
		{
			return instance;
		}
		
		public void info(String info)
		{
			log.info(info);
		}
		
		public void info(String info, Object ...args)
		{
			info(String.format(info, args));
		}
		
		public void warning(String warning)
		{
			log.warning(warning);
		}

		public void warning(String warning, Object ...args)
		{
			warning(String.format(warning, args));
		}
		
		public void severe(String error)
		{
			log.severe(error);
		}
		
		public void severe(String error, Object ...args)
		{
			severe(String.format(error, args));
		}
		
		public void severe(String error, Throwable e)
		{
			log.log(Level.SEVERE, error, e);
		}
		
		public void severe(String error, Throwable e, Object ...args)
		{
			severe(String.format(error, args), e);
		}
		
		public static void enableComponents()
		{
			Component[] values = values();
			
			for (int i = 0; i < values.length; ++i)
			{
				Component c = values[i];
				if (c.canEnable() && !c.i().isEnabled())
				{
					try
					{
						c.i().enable(false);
					}
					catch (Throwable e)
					{
						c.severe("Error when enabling component " + c.getFancyName());
						e.printStackTrace();
					}
				}
			}
		}
		
		public static void disableComponents()
		{
			Component[] values = values();
			// Disable each component backwards
			for (int i = values.length - 1; i >= 0; --i)
			{
				Component c = values[i];
				if (c.canEnable() && c.i().isEnabled())
				{
					try
					{
						c.i().disable(false);
					}
					catch (Exception e)
					{
						c.severe("Error when disabling component");
						e.printStackTrace();
					}
				}
			}
		}
		
		public String getFancyName()
		{
			String name = toString();
			
			return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
		}
	}
	
	private Component c;
	
	public MMComponent(Component c)
	{
		this.c = c;
	}
	
	public Component getComponent()
	{
		return c;
	}
	
	public void info(String info)
	{
		getComponent().info(info);
	}
	
	public void info(String info, Object ...args)
	{
		info(String.format(info, args));
	}
	
	public void warning(String warning)
	{
		getComponent().warning(warning);
	}

	public void warning(String warning, Object ...args)
	{
		warning(String.format(warning, args));
	}
	
	public void severe(String error)
	{
		getComponent().severe(error);
	}
	
	public void severe(String error, Object ...args)
	{
		severe(String.format(error, args));
	}
	
	public void severe(String error, Throwable e)
	{
		getComponent().severe(error, e);
	}
	
	public void severe(String error, Throwable e, Object ...args)
	{
		severe(String.format(error, args), e);
	}

	/**
	 * Checks if the component is enabled
	 * @return true if enabled
	 */
	public abstract boolean isEnabled();
	
	/**
	 * Initializes the main config file with Enable<component>
	 * @return The value stored in the config for this component
	 */
	protected abstract boolean initializeConfig();
	
	/**
	 * Enables the component
	 * @param force If true config.yml settings are ignored (e.g. if EnableLimiter = false, Limiter will still enable)
	 * @throws IllegalStateException
	 */
	public abstract void enable(boolean force) throws IllegalStateException;
	
	/**
	 * Disables the component
	 * @param force
	 * @throws IllegalStateException
	 */
	public abstract void disable(boolean force) throws IllegalStateException;

	public abstract AbstractConfig getConfig() throws IllegalStateException;
	
	public static LimiterComponent getLimiter()
	{
		return (LimiterComponent) Component.LIMITER.i();
	}
	
	public static AbilitiesComponent getAbilities()
	{
		return (AbilitiesComponent) Component.ABILITIES.i();
	}
	
	public static BountyComponent getBounties()
	{
		return (BountyComponent) Component.BOUNTY.i();
	}
	
	public static SpawnerComponent getSpawner()
	{
		return (SpawnerComponent) Component.SPAWNER.i();
	}
}
