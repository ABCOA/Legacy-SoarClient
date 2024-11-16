package me.eldodebug.soar.utils.vector;

import org.lwjgl.util.vector.Vector3f;

import me.eldodebug.soar.management.mods.impl.skin3d.SkinDirection;

public class Polygon {
	
    private final Vertex[] vertices;
    private final Vector3f normal;

    public Polygon(Vertex[] vertexs, float f, float g, float h, float i, float j, float k, boolean bl, SkinDirection dir) {
        this.vertices = vertexs;
        float l = 0.0F / j;
        float m = 0.0F / k;
        vertexs[0] = vertexs[0].remap(h / j - l, g / k + m);
        vertexs[1] = vertexs[1].remap(f / j + l, g / k + m);
        vertexs[2] = vertexs[2].remap(f / j + l, i / k - m);
        vertexs[3] = vertexs[3].remap(h / j - l, i / k - m);
        
        if (bl) {
            int n = vertexs.length;
            for (int o = 0; o < n / 2; o++) {
                Vertex vertex = vertexs[o];
                vertexs[o] = vertexs[n - 1 - o];
                vertexs[n - 1 - o] = vertex;
            }
        }
        
        this.normal = dir.step();
        
        if (bl) {
            this.normal.setX(this.normal.getX()*-1);
        }
    }

	public Vertex[] getVertices() {
		return vertices;
	}

	public Vector3f getNormal() {
		return normal;
	}
}
