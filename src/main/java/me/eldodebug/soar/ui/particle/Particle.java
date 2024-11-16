package me.eldodebug.soar.ui.particle;

import java.util.Random;

import me.eldodebug.soar.utils.TimerUtils;
import me.eldodebug.soar.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class Particle {

	private Minecraft mc = Minecraft.getMinecraft();
	private TimerUtils timer = new TimerUtils();
	
	private float x;
    private float y;
    private final float size;
    private final float ySpeed = new Random().nextInt(5);
    private final float xSpeed = new Random().nextInt(5);
    private int height;
    private int width;

    public Particle(int x, int y) {
        this.x = x;
        this.y = y;
        this.size = genRandom() + 0.4F;
    }

    private float lint1(float f) {
        return ((float) 1.02 * (1.0f - f)) + ((float) 1.0 * f);
    }

    private float lint2(float f) {
        return (float) 1.02 + f * ((float) 1.0 - (float) 1.02);
    }

    public void connect(float x, float y) {
        RenderUtils.connectPoints(getX(), getY(), x, y);
    }

    public void interpolation() {
    	
        for(int n = 0; n <= 64; ++n) {
            final float f = n / 64.0f;
            final float p1 = lint1(f);
            final float p2 = lint2(f);

            if(p1 != p2) {
                y -= f;
                x -= f;
            }
        }
    }

    public void fall() {
    	
		ScaledResolution sr = new ScaledResolution(mc);
		
        y = (y + ySpeed);
        x = (x + xSpeed);

        if(y > mc.displayHeight) {
            y = 1;
        }

        if(x > mc.displayWidth) {
            x = 1;
        }

        if(x < 1) {
            x = sr.getScaledWidth();
        }

        if(y < 1) {
            y = sr.getScaledHeight();
        }
    }

    private float genRandom() {
        return (float) (0.3f + Math.random() * (0.6f - 0.3f + 1.0F));
    }

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getSize() {
		return size;
	}

	public float getySpeed() {
		return ySpeed;
	}

	public float getxSpeed() {
		return xSpeed;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public TimerUtils getTimer() {
		return timer;
	}
}

