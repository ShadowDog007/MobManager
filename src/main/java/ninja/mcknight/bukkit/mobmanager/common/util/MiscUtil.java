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

package ninja.mcknight.bukkit.mobmanager.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ninja.mcknight.bukkit.mobmanager.P;

public class MiscUtil
{	
	public static Map<String, Object> getConfigMap(Object obj)
	{
		return getConfigMap(obj, true);
	}
	
	public static Map<String, Object> getConfigMap(Object obj, boolean uppercase)
	{
		if (obj == null || obj instanceof Map == false)
			return null;
		
		Map<?, ?> map = (Map<?, ?>) obj;
		
		Map<String, Object> stringMap = new LinkedHashMap<String, Object>();
		
		for (Entry<?, ?> e : map.entrySet())
		{
			String key = e.getKey().toString();
			key = uppercase ? key.toUpperCase() : key.toLowerCase();
			
			stringMap.put(key, e.getValue());
		}
		
		return stringMap;
	}
	
	public static Map<String, Object> copyConfigMap(Object obj)
	{
		if (obj == null || obj instanceof Map == false)
			return null;
		
		Map<?, ?> map = (Map<?, ?>) obj;
		
		Map<String, Object> stringMap = new LinkedHashMap<String, Object>();
		
		for (Entry<?, ?> e : map.entrySet())
		{
			stringMap.put(e.getKey().toString(), e.getValue());
		}
		
		return stringMap;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Object> getList(Object obj)
	{
		if (obj == null)
			return null;
		
		if (obj instanceof List)
			return (List<Object>) obj;
		
		return null;
	}
	
	public static List<String> getStringList(Object obj)
	{
		List<Object> list = getList(obj);
		
		if (list != null && list.size() > 0)
		{
			List<String> strList = new ArrayList<String>(list.size());
			
			for (Object o : list)
				if (o != null)
					strList.add(o.toString());
			
			return strList;
		}
		return Collections.emptyList();
	}
	
	@SuppressWarnings("unchecked")
	public static Entry<String, Object> getEntry(Object obj)
	{
		if (obj instanceof Entry)
		{
			Entry<?,?> e = (Entry<?,?>) obj;
			
			if (e.getKey() instanceof String)
				return (Entry<String, Object>) e;
		}
		
		return null;
	}
	
	public static <T> T getMapValue(Map<String, Object> map, String key, String error, Class<T> classOfT)
	{
		return getMapValue(map, key, error, classOfT, null);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getMapValue(Map<String, Object> map, String key, String error, Class<T> classOfT, T def)
	{
		Object obj = map.get(key);
		
		if (obj == null)
		{
			if (error != null)
				P.p().getLogger().warning(String.format("Missing key %s for %s", key, error));
			return def;
		}
		
		if (classOfT.isAssignableFrom(obj.getClass()))
			return (T) obj;
		
		if (error != null)
			P.p().getLogger().warning(String.format("Invalid Value for key %s for %s", key, error));
		return def;
	}
	
	public static String getString(Object obj)
	{
		if (obj == null)
			return null;
		
		if (obj instanceof String)
			return (String) obj;
		
		return null;
	}
	
	public static int getInteger(Object obj)
	{
		return getInteger(obj, 0);
	}
	
	public static int getInteger(Object obj, int def)
	{
		if (obj == null)
			return def;
		
		if (obj instanceof Number)
			return ((Number) obj).intValue();
		
		try
		{
			if (obj instanceof String)
				return Integer.valueOf((String) obj);
		}
		catch (Exception e)
		{
			return def;
		}
		
		return def;
	}
	
	public static float getFloat(Object obj)
	{
		return getFloat(obj, 1.0F);
	}
	
	public static float getFloat(Object obj, float def)
	{
		if (obj == null)
			return def;
		
		if (obj instanceof Number)
			return ((Number) obj).floatValue();
			
		try
		{
			if (obj instanceof String)
				return Float.valueOf((String) obj);
		}
		catch (Exception e)
		{
			return def;
		}
		
		return def;
	}

	public static boolean getBoolean(Object obj, boolean def)
	{
		if (obj == null)
		{
			return def;
		}
		
		if (obj instanceof Boolean == false)
		{
			return def;
		}
		
		return ((Boolean) obj).booleanValue();
	}

	public static double getDouble(Object obj)
	{
		return getDouble(obj, 0.0);
	}
	
	public static double getDouble(Object obj, double def)
	{
		if (obj == null)
			return def;
		
		if (obj instanceof Number)
			return ((Number) obj).doubleValue();
			
		try
		{
			if (obj instanceof String)
				return Double.valueOf((String) obj);
		}
		catch (Exception e)
		{
			return def;
		}
		
		return def;
	}
}
