package com.forgenz.mobmanager.config;

import java.util.ArrayList;
import java.util.List;

import com.forgenz.mobmanager.P;

public class EnumSettingContainer
{
	private ArrayList<String> contains = null;
	
	public EnumSettingContainer(Class<?> enumClass, List<?> objectList, String missingEnumError)
	{
		if (objectList == null)
			return;
		
		this.contains = new ArrayList<String>();
		
		Object[] enumValues = enumClass.getEnumConstants();
		
		for (Object obj : objectList)
		{
			if (obj instanceof String == false)
				continue;
			
			String string = (String) obj;
			
			boolean found = false;
			
			for (Object value : enumValues)
			{
				if (value.toString().equalsIgnoreCase(string))
				{
					contains.add(value.toString());
					found = true;
				}
			}
			
			if (!found)
				P.p.getLogger().info(String.format(missingEnumError, string));
		}
	}
	
	public List<String> getList()
	{
		if (contains == null)
			return new ArrayList<String>();
		
		return contains;
	}
	
	public boolean containsValue(String string)
	{
		if (contains == null)
			return false;
		return contains.contains(string);
	}
	
	public void addDefaults(String ...defaults)
	{
		if (contains != null)
			return;
		
		if (defaults.length != 0)
			contains = new ArrayList<String>();
		
		for (String str : defaults)
		{
			contains.add(str);
		}
	}
	
	public String toString()
	{
		if (contains == null)
			return "";
		
		String str = "";
		
		for (String s : contains)
		{
			if (str.length() != 0)
				str += ",";
			str += s;
		}
		return str;
	}
}
