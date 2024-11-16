package me.eldodebug.soar.ui.particle;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.eldodebug.soar.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;

public class ParticleEngine {

	private Minecraft mc = Minecraft.getMinecraft();
	
    private final List<Particle> particles = new ArrayList<>();
    private int amount;

    private int prevWidth;
    private int prevHeight;

    public void draw(int mouseX, int mouseY) {
    	
        if(particles.isEmpty() || prevWidth != mc.displayWidth || prevHeight != mc.displayHeight) {
            particles.clear();
            amount = (mc.displayWidth + mc.displayHeight) / 8;
            create();
        }

        prevWidth = mc.displayWidth;
        prevHeight = mc.displayHeight;

        for(final Particle particle : particles) {
        	
        	if(particle.getTimer().delay(1000 / 60)) {
        		
                particle.fall();
                particle.interpolation();
                
        		particle.getTimer().reset();
        	}

            int range = 50;
            final boolean mouseOver = (mouseX >= particle.getX() - range) && 
            		(mouseY >= particle.getY() - range) && 
            		(mouseX <= particle.getX() + range) && 
            		(mouseY <= particle.getY() + range);

            if(mouseOver) {
                particles.stream()
                        .filter(part -> (part.getX() > particle.getX() && part.getX() - particle.getX() < range
                                && particle.getX() - part.getX() < range)
                                && (part.getY() > particle.getY() && part.getY() - particle.getY() < range
                                || particle.getY() > part.getY() && particle.getY() - part.getY() < range))
                        .forEach(connectable -> particle.connect(connectable.getX(), connectable.getY()));
            }

            RenderUtils.drawRect(particle.getX(), particle.getY(), particle.getSize(), particle.getSize(), Color.WHITE);
        }
    }

    private void create() {
    	
        Random random = new Random();

        for(int i = 0; i < amount; i++) {
            particles.add(new Particle(random.nextInt(mc.displayWidth), random.nextInt(mc.displayHeight)));
        }
    }
}