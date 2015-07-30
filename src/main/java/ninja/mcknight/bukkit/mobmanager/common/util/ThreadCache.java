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

import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class ThreadCache<T>
{
	private final WeakHashMap<Thread, T> map;
	private final ReentrantReadWriteLock rwLock;
	private Class<T> clazz;
	
	public ThreadCache()
	{
		this(null);
	}
	
	public ThreadCache(Class<T> clazz)
	{
		this.map = new WeakHashMap<Thread, T>();
		rwLock = new ReentrantReadWriteLock();
		this.clazz = clazz;
	}
	
	public T get()
	{
		ReadLock lock = rwLock.readLock();
		lock.lock();
		
		T t = null;
		try
		{
			t = map.get(Thread.currentThread());
		}
		finally
		{
			lock.unlock();
		}
		
		if (t == null && clazz != null)
		{
			try
			{
				t = clazz.getConstructor().newInstance();
				set(t);
			}
			catch (Exception e)
			{
				clazz = null;
			}
		}
		return t;
	}
	
	public void set(T t)
	{
		WriteLock lock = rwLock.writeLock();
		lock.lock();
		try
		{
			map.put(Thread.currentThread(), t);
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public void clear()
	{
		WriteLock lock = rwLock.writeLock();
		lock.lock();
		try
		{
			map.clear();
		}
		finally
		{
			lock.unlock();
		}
	}
}
