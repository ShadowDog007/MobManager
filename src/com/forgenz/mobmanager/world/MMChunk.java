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

package com.forgenz.mobmanager.world;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.forgenz.mobmanager.Config;
import com.forgenz.mobmanager.P;

/**
 * Keeps track of players within a given chunk </br>
 * Also keeps track of animals inside the chunk for limiting the number of animals that can be bread (Is That The Right Word?) in one place
 * 
 * @author Michael McKnight (ShadowDog007)
 *
 */
public class MMChunk
{	
	private MMWorld mmWorld;
	private Chunk chunk;
	private MMCoord coord;
	private ArrayList<MMLayer> layers;

	private int numPlayers = 0;
	private int numAnimals = 0;

	public MMChunk(final Chunk chunk, final MMWorld mmWorld)
	{
		this.chunk = chunk;
		this.mmWorld = mmWorld;
		coord = new MMCoord(chunk.getX(), chunk.getZ());
		
		// Fetches the layers from the config
		final List<String> layerList = P.cfg.getStringList("Layers");

		// Creates an array to store the layers
		layers = new ArrayList<MMLayer>(layerList.size());
		// Creates the layer objects
		for (final String layer : layerList)
		{
			// Checks the layer string is value
			if (!Config.layerPattern.matcher(layer).matches())
				P.p.getLogger().warning(String.format("Found an invalid layer '%s'", layer));
			// Splits the range string for the layer
			final String[] range = Config.layerSplitPattern.split(layer);

			// Converts range into integers
			int miny = Integer.valueOf(range[0]);
			int maxy = Integer.valueOf(range[1]);

			// Makes sure miny is actually the lower value
			if (maxy < miny)
			{
				miny = miny ^ maxy;
				maxy = miny ^ maxy;
				miny = miny ^ maxy;
			}

			layers.add(new MMLayer(miny, maxy, 0));
		}
		
		// Order the layers
		boolean ordered = false;
		while (!ordered)
		{
			int numOrdered = 0;
			for (int i = 0; i < layers.size() - 1; ++i)
			{
				if (layers.get(i).compare(layers.get(i+1)) <= 0)
				{
					++numOrdered;
				}
				else
				{
					MMLayer upperLayer = layers.get(i);
					MMLayer lowerLayer = layers.get(i+1);
					
					layers.add(i, lowerLayer);
					layers.add(i+1, upperLayer);
				}
			}
			ordered = numOrdered == layers.size() - 1;
		}
		
		// Adds any already existing Players or Animals
		for (final Entity entity : chunk.getEntities())
			if (entity instanceof Player)
			{
				playerEntered();
				for (final MMLayer layerIn : getLayersAt(entity.getLocation().getBlockY()))
					layerIn.playerEntered();
			} else if (entity instanceof Animals)
				++numAnimals;
	}
	
	public MMWorld getMMWorld()
	{
		return mmWorld;
	}
	public Chunk getChunk()
	{
		return chunk;
	}

	public MMCoord getCoord()
	{
		return coord;
	}

	public MMLayer[] getLayers()
	{
		return layers.toArray(new MMLayer[0]);
	}

	public List<MMLayer> getLayersAt(final int y)
	{
		final ArrayList<MMLayer> layersAt = new ArrayList<MMLayer>();

		for (final MMLayer layer : layers)
			if (layer.insideRange(y))
				layersAt.add(layer);

		return layersAt;
	}
	
	public List<MMLayer> getLayersAtAndBelow(final int y)
	{
		final ArrayList<MMLayer> atAndBelow = new ArrayList<MMLayer>();
		
		int checkedBelow = -1;
		for (int i = layers.size() - 1; i >= 0; --i)
		{
			if (layers.get(i).insideRange(y))
			{
				atAndBelow.add(layers.get(i));
				checkedBelow = 0;
			}
			else if (checkedBelow != -1)
					if (++checkedBelow <= P.cfg.getInt("FlyingMobAditionalLayerDepth", 2))
						atAndBelow.add(layers.get(i));
					else
						break;
		}

		return atAndBelow;
	}

	public boolean withinBreedingLimits()
	{
		return mmWorld.worldConf.breedingLimit < 0 || numAnimals < mmWorld.worldConf.breedingLimit;
	}
	
	public int getAnimalCount()
	{
		return numAnimals;
	}

	public void resetNumAnimals()
	{
		numAnimals = 0;
	}

	public void changeNumAnimals(final boolean increase)
	{
		if (increase)
			++numAnimals;
		else
			--numAnimals;
	}

	public boolean hasPlayers()
	{
		return numPlayers > 0;
	}

	public int getNumPlayers()
	{
		return numPlayers;
	}

	public void playerEntered()
	{
		++numPlayers;
	}

	public void playerLeft()
	{
		if (--numPlayers < 0)
		{
			numPlayers = 0;

			P.p.getLogger().warning("Player left a chunk with no players in it?");
		}
	}

	@Override
	public int hashCode()
	{
		return chunk.hashCode();
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MMChunk other = (MMChunk) obj;
		if (coord == null)
		{
			if (other.coord != null)
				return false;
		} else if (!coord.equals(other.coord))
			return false;
		return true;
	}

}
