package me.eldodebug.soar.management.mods.impl.skin3d.render;

import me.eldodebug.soar.management.mods.impl.skin3d.SkinDirection;
import me.eldodebug.soar.utils.vector.Polygon;
import me.eldodebug.soar.utils.vector.Vertex;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class CustomizableCube {

    private final SkinDirection[] hidden;
    private final Polygon[] polygons;
    private int polygonCount = 0;
    public final float minX;
    public final float minY;
    public final float minZ;
    public final float maxX;
    public final float maxY;
    public final float maxZ;
    
    public CustomizableCube(int u, int v, float x, float y, float z, float sizeX, float sizeY, float sizeZ, float extraX, float extraY, float extraZ, boolean mirror, float textureWidth, float textureHeight, SkinDirection[] hide) {
        this.hidden = hide;
        this.minX = x;
        this.minY = y;
        this.minZ = z;
        this.maxX = x + sizeX;
        this.maxY = y + sizeY;
        this.maxZ = z + sizeZ;
        this.polygons = new Polygon[6];
        
        float pX = x + sizeX;
        float pY = y + sizeY;
        float pZ = z + sizeZ;
        
        x -= extraX;
        y -= extraY;
        z -= extraZ;
        pX += extraX;
        pY += extraY;
        pZ += extraZ;
        
        if (mirror) {
            float i = pX;
            pX = x;
            x = i;
        }
        
        Vertex vertex = new Vertex(x, y, z, 0.0F, 0.0F);
        Vertex vertex2 = new Vertex(pX, y, z, 0.0F, 8.0F);
        Vertex vertex3 = new Vertex(pX, pY, z, 8.0F, 8.0F);
        Vertex vertex4 = new Vertex(x, pY, z, 8.0F, 0.0F);
        Vertex vertex5 = new Vertex(x, y, pZ, 0.0F, 0.0F);
        Vertex vertex6 = new Vertex(pX, y, pZ, 0.0F, 8.0F);
        Vertex vertex7 = new Vertex(pX, pY, pZ, 8.0F, 8.0F);
        Vertex vertex8 = new Vertex(x, pY, pZ, 8.0F, 0.0F);

        float l = u + sizeZ + sizeX;
        float n = u + sizeZ + sizeX + sizeZ;

        float q = v + sizeZ;
        float r = v + sizeZ + sizeY;
        
        if(visibleFace(SkinDirection.DOWN)) {
            this.polygons[polygonCount++] = new Polygon(new Vertex[]{vertex6, vertex5, vertex, vertex2}, l, q, n, r, textureWidth, textureHeight, mirror, SkinDirection.DOWN);
        }

        if(visibleFace(SkinDirection.UP)) {
            this.polygons[polygonCount++] = new Polygon(new Vertex[]{vertex3, vertex4, vertex8, vertex7}, l, q, n, r, textureWidth, textureHeight, mirror, SkinDirection.UP);
        }

        if(visibleFace(SkinDirection.WEST)) {
            this.polygons[polygonCount++] = new Polygon(new Vertex[]{vertex, vertex5, vertex8, vertex4}, l, q, n, r, textureWidth, textureHeight, mirror, SkinDirection.WEST);
        }

        if(visibleFace(SkinDirection.NORTH)) {
            this.polygons[polygonCount++] = new Polygon(new Vertex[]{vertex2, vertex, vertex4, vertex3}, l, q, n, r, textureWidth, textureHeight, mirror, SkinDirection.NORTH);
        }

        if(visibleFace(SkinDirection.EAST)) {
            this.polygons[polygonCount++] = new Polygon(new Vertex[]{vertex6, vertex2, vertex3, vertex7}, l, q, n, r, textureWidth, textureHeight, mirror, SkinDirection.EAST);
        }

        if(visibleFace(SkinDirection.SOUTH)) {
            this.polygons[polygonCount++] = new Polygon(new Vertex[]{vertex5, vertex6, vertex7, vertex8}, l, q, n, r, textureWidth, textureHeight, mirror, SkinDirection.SOUTH);
        }
    }
    
    private boolean visibleFace(SkinDirection face) {
    	
        for(SkinDirection dir : hidden) {
            if(dir == face) {
            	return false;
            }
        }
        
        return true;
    }

    public void render(WorldRenderer worldRenderer, boolean redTint) {
    	
        redTint = false;
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        Polygon polygon;
        
        for (int id = 0; id < polygonCount; id++) {
            polygon = polygons[id];
            
            for (int i = 0; i < 4; i++) {
                Vertex vertex = polygon.getVertices()[i];
                worldRenderer.pos(vertex.getPos().x, vertex.getPos().y, vertex.getPos().z).tex(vertex.getU(), vertex.getV()).color(255, redTint ? 127 : 255, redTint ? 127 : 255, 255).normal(polygon.getNormal().x, polygon.getNormal().y, polygon.getNormal().z).endVertex();
            }
        }
        
        Tessellator.getInstance().draw();
    }
}