package com.forgenz.mobmanager.abilities.abilities;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.abilities.AbilityType;
import com.forgenz.mobmanager.abilities.config.MobAbilityConfig;
import com.forgenz.mobmanager.limiter.config.LimiterConfig;

public class SunProofAbility extends Ability
{
	
	private static final String METADATA_KEY = "MobManager_SunProofAbility";
	public static Ability ability = new SunProofAbility();
	
	private SunProofAbility()
	{
		
	}
	
	@Override
	public void addAbility(LivingEntity entity)
	{
		entity.setMetadata(METADATA_KEY, new FixedMetadataValue(P.p(), ability));
	}
	
	public static void addByChance(LivingEntity entity, MobAbilityConfig ma)
	{
		if (ma == null || !isValid(entity))
			return;
		
		if (ma.sunProofRate <= 1.0 && ma.sunProofRate != 0.0)
		{
			// If the random number is lower than the sunproof rate we make the entity sun proof :D
			if (ma.sunProofRate == 1.0F || LimiterConfig.rand.nextFloat() < ma.sunProofRate)
			{
				ability.addAbility(entity);
			}
		}
	}
	
	public static boolean isSunProof(LivingEntity entity)
	{
		for (MetadataValue meta : entity.getMetadata(METADATA_KEY))
		{
			if (meta.getOwningPlugin() == P.p())
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isValid(LivingEntity entity)
	{
		return isValid(entity.getType());
	}
	
	public static boolean isValid(EntityType entity)
	{
		switch (entity)
		{
		case ZOMBIE:
		case SKELETON:
			return true;
		default:
			return false;
		}
	}

	@Override
	public AbilityType getAbilityType()
	{
		return AbilityType.SUNPROOF;
	}

}
