package me.eldodebug.soar.ui.comp;

public class Comp {

	private float x, y;
	
	public Comp(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public void draw(int mouseX, int mouseY, float partialTicks) {}
	
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {}
	
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}
	
	public void keyTyped(char typedChar, int keyCode) {}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
}
