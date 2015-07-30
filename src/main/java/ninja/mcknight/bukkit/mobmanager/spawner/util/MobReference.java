package ninja.mcknight.bukkit.mobmanager.spawner.util;

import java.lang.ref.WeakReference;

import org.bukkit.entity.LivingEntity;

/**
 * Keeps a reference to the a mob and its spawn time.
 */
public class MobReference
{
	private boolean refSet;
	private boolean valid;
	private WeakReference<LivingEntity> e;
	private final long spawnTime;
	
	public MobReference()
	{
		refSet = false;
		valid = true;
		spawnTime = System.currentTimeMillis();
	}
	
	/**
	 * Fetches the entity if it is still valid
	 * 
	 * @return The entity object if it is still valid
	 */
	public LivingEntity getEntity()
	{
		// If the weak reference is null the entity is invalid
		if (e == null)
			return null;
		
		// Fetch the entity
		LivingEntity entity = e.get();
		// If the entity is null it has been removed by GB
		// If the entity is invalid it is gone
		if (entity == null || !entity.isValid())
		{
			// Remove the weak reference and return null
			e = null;
			return null;
		}
		
		// Fetch the entity and return
		return e.get();
	}
	
	/**
	 * Checks if the spawn cooldown has expired
	 * 
	 * @return Returns true if the spawn cooldown has expired
	 */
	public boolean cooldownExpired(int mobCooldown)
	{
		return mobCooldown > 0 && (System.currentTimeMillis() - spawnTime) > mobCooldown;
	}
	
	public void setReference(LivingEntity entity)
	{
		if (entity != null)
			e = new WeakReference<LivingEntity>(entity);
		else
			valid = false;
		refSet = true;
	}
	
	public boolean isValid()
	{
		return valid;
	}
	
	public boolean isSet()
	{
		if (refSet)
			return true;
		
		if (System.currentTimeMillis() - spawnTime > 50)
		{
			valid = false;
			return true;
		}
		
		return false;
	}
	
	public void invalidate()
	{
		valid = false;
	}
}
