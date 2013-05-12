package com.forgenz.mobmanager;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.forgenz.mobmanager.abilities.AbilitiesComponent;
import com.forgenz.mobmanager.common.config.AbstractConfig;
import com.forgenz.mobmanager.common.util.MMLogger;
import com.forgenz.mobmanager.limiter.LimiterComponent;
import com.forgenz.mobmanager.spawner.SpawnerComponent;

public abstract class MMComponent
{
	public enum Component
	{
		//COMMON(CommonComponent.class, "MobManager", (Component[]) null),
		LIMITER(LimiterComponent.class, "MobManager-Limiter", (Component[]) null),
		ABILITIES(AbilitiesComponent.class,"MobManager-Abilities", (Component[]) null),
		SPAWNER(SpawnerComponent.class, "MobManager-Spawner", Component.LIMITER);
		
		private final MMComponent instance;
		private final Logger log;
		private ArrayList<Component> depends = null;
		private final Component[] dependencies;
		
		Component(Class<? extends MMComponent> clazz, String loggerName, Component... dependencies)
		{
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
		
		public void warning(String warning)
		{
			log.warning(warning);
		}
		
		public void severe(String error)
		{
			log.severe(error);
		}
		
		public static void enableComponents()
		{
			Component[] values = values();
			
			for (int i = 0; i < values.length; ++i)
			{
				if (!values[i].i().isEnabled())
					values[i].i().enable(false);
			}
		}
		
		public static void disableComponents()
		{
			Component[] values = values();
			// Disable each component backwards
			for (int i = values.length - 1; i >= 0; --i)
			{
				if (values[i].i().isEnabled())
					values[i].i().disable(false);
			}
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
	
	public void warning(String warning)
	{
		getComponent().warning(warning);
	}
	
	public void severe(String error)
	{
		getComponent().severe(error);
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
	
	public static SpawnerComponent getSpawner()
	{
		return (SpawnerComponent) Component.SPAWNER.i();
	}
}
