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

package com.forgenz.mobmanager.spawner.tasks;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;

import com.forgenz.mobmanager.MMComponent;
import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.spawner.util.MobSpawner;

/**
 * Handles queuing and execution of {@link MobSpawner} objects
 */
public class SpawnerTask implements Runnable
{
	private final AtomicBoolean isRunning = new AtomicBoolean(false);

	private final Queue<MobSpawner> spawners = new LinkedList<MobSpawner>();
	
	public synchronized void addSpawner(MobSpawner spawner)
	{
		spawners.add(spawner);
		schedule();
	}
	
	private synchronized MobSpawner getSpawner()
	{
		return spawners.poll();
	}
	
	private synchronized boolean isEmpty()
	{
		return spawners.isEmpty();
	}
	
	private void schedule()
	{
		if (MMComponent.getAbilities().isEnabled() && isRunning.compareAndSet(false, true))
			Bukkit.getScheduler().runTask(P.p(), this);
	}

	@Override
	public void run()
	{
		MobSpawner spawner;
		// Spawn all the mobs!
		while ((spawner = getSpawner()) != null)
			spawner.spawn();
		
		// Allow the spawner task to run again
		isRunning.set(false);
		
		// Make sure we don't have any spawns left to do
		if (!isEmpty())
			schedule();
	}
}
