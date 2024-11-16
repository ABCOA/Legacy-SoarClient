package me.eldodebug.soar.management.mods.impl.projectiletrail;

import me.eldodebug.soar.management.language.TranslateText;
import net.minecraft.util.EnumParticleTypes;

public enum ProjectileTrailType {
	
    BLACK_SMOKE(EnumParticleTypes.SMOKE_NORMAL, TranslateText.BLACK_SMOKE, 0.07F, 0.0F, 2), 
    FIRE(EnumParticleTypes.FLAME, TranslateText.FIRE, 0.1F, 0.0F, 1), 
    GREEN_STAR(EnumParticleTypes.VILLAGER_HAPPY, TranslateText.GREEN_STAR, 0.0F, 0.1F, 1), 
    HEARTS(EnumParticleTypes.HEART, TranslateText.HEARTS, 0.0F, 0.2F, 1), 
    MAGIC(EnumParticleTypes.SPELL_WITCH, TranslateText.MAGIC, 1.0F, 0.0F, 2), 
    MUSIC_NOTES(EnumParticleTypes.NOTE, TranslateText.MUSIC_NOTES, 1.0F, 1.0F, 2), 
    SLIME(EnumParticleTypes.SLIME, TranslateText.SLIME, 0.5F, 0.3F, 1), 
    SPARK(EnumParticleTypes.FIREWORKS_SPARK, TranslateText.SPARK, 0.05F, 0.0F, 1), 
    SWIRL(EnumParticleTypes.SPELL_MOB, TranslateText.SWIRL, 1.0F, 0.0F, 1), 
    WHITE_SMOKE(EnumParticleTypes.SNOW_SHOVEL, TranslateText.WHITE_SMOKE, 0.07F, 0.0F, 2);

    public EnumParticleTypes particle;
    private TranslateText nameTranslate;
    public float velocity;
    public float translate;
    public int count;

    private ProjectileTrailType(EnumParticleTypes particle, TranslateText nameTranslate, float velocity, float translate, int count) {
        this.particle = particle;
        this.nameTranslate = nameTranslate;
        this.velocity = velocity;
        this.translate = translate;
        this.count = count;
    }
    
    public TranslateText getNameTranslate() {
		return nameTranslate;
	}

	public static ProjectileTrailType getTypeByKey(String key) {
    	
    	for(ProjectileTrailType t : ProjectileTrailType.values()) {
    		if(t.getNameTranslate().getKey().equals(key)) {
    			return t;
    		}
    	}
    	
    	return ProjectileTrailType.HEARTS;
    }
}