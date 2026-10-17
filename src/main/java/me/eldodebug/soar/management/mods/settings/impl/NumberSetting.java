package me.eldodebug.soar.management.mods.settings.impl;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.settings.Setting;

public class NumberSetting extends Setting {

	private double defaultValue, value, minValue, maxValue;
	private boolean integer;
	
	public NumberSetting(TranslateText text, Mod parent, double defaultValue, double minValue, double maxValue, boolean integer) {
		super(text, parent);
		
		this.value = defaultValue;
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.integer = integer;
		
		Soar.getInstance().getModManager().addSettings(this);
	}
	
	@Override
	public void reset() {
        Object before = getValue();

		this.value = defaultValue;
	
        if (!java.util.Objects.equals(before, getValue())) changed();
    }
	
	public double getValue() {
		
		if(integer) {
			this.value = (int) value;
		}
		
		return value;
	}
	
	public int getValueInt() {
		
		if(integer) {
			this.value = (int) value;
		}
		
		return (int) value;
	}
	
	public float getValueFloat() {
		
		if(integer) {
			this.value = (int) value;
		}
		
		return (float) value;
	}
	
	public long getValueLong() {
		
		if(integer) {
			this.value = (int) value;
		}
		
		return (long) value;
	}
	
	public void setValue(double value) {
        Object before = getValue();

		this.value = value;
	
        if (!java.util.Objects.equals(before, getValue())) changed();
    }

	public double getMinValue() {
		return minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public double getDefaultValue() {
		return defaultValue;
	}
}
