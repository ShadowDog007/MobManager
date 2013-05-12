package com.forgenz.mobmanager.metrics;

import com.forgenz.mobmanager.MMComponent;
import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.metrics.Metrics.Plotter;

public class Plotters
{
	public final static Metrics.Plotter limiterEnabled = new Metrics.Plotter("Limiter")
	{

		@Override
		public int getValue()
		{
			return MMComponent.getLimiter().isEnabled() ? 1 : 0;
		}
		
	};
	
	public final static Metrics.Plotter abilitiesEnabled = new Metrics.Plotter("Abilities")
	{

		@Override
		public int getValue()
		{
			return MMComponent.getAbilities().isEnabled() ? 1 : 0;
		}
		
	};

	public final static Plotter version = new Metrics.Plotter(P.p().getDescription().getVersion())
	{
		
		@Override
		public int getValue()
		{
			return 1;
		}
	};
}
