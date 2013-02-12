package com.forgenz.mobmanager.attributes;

import org.bukkit.entity.LivingEntity;

public interface JoinableAttribute<T>
{
	public void joinAttributes(LivingEntity entity, T ...attributes);
}
