package com.forgenz.mobmanager.common;

import com.forgenz.mobmanager.MMComponent;
import com.forgenz.mobmanager.common.config.AbstractConfig;

public class CommonComponent extends MMComponent
{
	private boolean enabled = false;

	public CommonComponent(Component c)
	{
		super(c);
	}

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	protected boolean initializeConfig()
	{
		return true;
	}

	@Override
	public void enable(boolean force) throws IllegalStateException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void disable(boolean force) throws IllegalStateException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public AbstractConfig getConfig() throws IllegalStateException
	{
		// TODO Auto-generated method stub
		return null;
	}
}
