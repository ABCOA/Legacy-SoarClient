package me.eldodebug.soar.management.color.palette;

public enum ColorType {
	DARK(0), NORMAL(1);
	
	private int index;
	
	private ColorType(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}
