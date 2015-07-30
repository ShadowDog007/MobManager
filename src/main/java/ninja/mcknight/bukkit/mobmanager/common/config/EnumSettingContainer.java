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

package ninja.mcknight.bukkit.mobmanager.common.config;

import java.util.ArrayList;
import java.util.List;

import ninja.mcknight.bukkit.mobmanager.P;

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
				P.p().getLogger().info(String.format(missingEnumError, string));
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
	
	public void addDefaults(Object ...defaults)
	{
		if (contains != null)
			return;
		
		if (defaults.length != 0)
			contains = new ArrayList<String>();
		
		for (Object obj : defaults)
		{
			contains.add(obj.toString());
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
