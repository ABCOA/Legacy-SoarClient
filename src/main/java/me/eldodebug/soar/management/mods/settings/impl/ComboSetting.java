package me.eldodebug.soar.management.mods.settings.impl;

import java.util.ArrayList;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.settings.Setting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;

public class ComboSetting extends Setting {

	private ArrayList<Option> options;
	
	private Option defaultOption, option;
	
	public ComboSetting(TranslateText text, Mod parent, TranslateText defaultOption, ArrayList<Option> options) {
		super(text, parent);
		
		this.options = options;
		this.option = getOptionByNameKey(defaultOption.getKey());
		this.defaultOption = getOptionByNameKey(defaultOption.getKey());
		
		Soar.getInstance().getModManager().addSettings(this);
	}

	@Override
	public void reset() {
        Object before = getOption();

		this.option = defaultOption;
	
        if (!java.util.Objects.equals(before, getOption())) changed();
    }
	
	public Option getOption() {
		return option;
	}

	public void setOption(Option option) {
        Object before = getOption();

		this.option = option;
	
        if (!java.util.Objects.equals(before, getOption())) changed();
    }

	public ArrayList<Option> getOptions() {
		return options;
	}
	
	public Option getOptionByNameKey(String nameKey) {
		
		for(Option op : options) {
			if(op.getNameKey().equals(nameKey)) {
				return op;
			}
		}
		
		return option;
	}

	public Option getDefaultOption() {
		return defaultOption;
	}
}
