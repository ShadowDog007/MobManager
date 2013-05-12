package com.forgenz.mobmanager.common.util;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.forgenz.mobmanager.P;

public class MMLogger extends Logger
{
	private final String name;
	public MMLogger(Class<?> clazz, String name)
	{
		super(clazz.getCanonicalName(), null);
		this.name = String.format("[%s] ", name);
		setParent(P.p().getLogger());
		this.setLevel(Level.ALL);
	}
	
	@Override
	public void log(LogRecord logRecord)
	{
		logRecord.setMessage(name + logRecord.getMessage());
		super.log(logRecord);
	}
}
