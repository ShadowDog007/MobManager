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
import java.util.HashSet;
import java.util.List;

import ninja.mcknight.bukkit.mobmanager.P;

public class TSettingContainer<T>
{
	private HashSet<T> container;
	
	public TSettingContainer(T[] values, List<?> list, String error)
	{
		if (list == null)
			return;
		
		container = new HashSet<T>();
		
		for (Object obj : list)
		{
			if (obj == null || obj instanceof String == false)
				continue;
			
			boolean valid = false;
			
			for (T val : values)
			{
				if (val.toString().equalsIgnoreCase((String) obj))
				{
					if (!container.contains(val))
						container.add(val);
					valid = true;
					break;
				}
			}
			
			if (!valid)
				P.p().getLogger().warning("Invalid " + error + " " + obj);
		}
	}
	
	public void addDefaults(T ...defaults)
	{
		if (container != null)
			return;
		
		container = new HashSet<T>();
		
		for (T t : defaults)
		{
			container.add(t);
		}
	}

	public List<String> getList()
	{
		List<String> list = new ArrayList<String>();
		
		if (container == null)
			return list;
		
		for (T t : container)
			list.add(t.toString());
		
		return list;
	}
	
	public boolean contains(T val)
	{
		if (container == null)
			return false;
		return container.contains(val);
	}

	@Override
	public String toString()
	{
		if (container == null)
			return "";
		
		StringBuilder str = new StringBuilder();
		
		for (T t : container)
		{
			if (str.length() != 0)
				str.append(',');
			str.append(t.toString());
		}
		
		return str.toString();
	}
}
