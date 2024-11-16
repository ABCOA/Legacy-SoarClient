package me.eldodebug.soar.utils.vector;

import org.lwjgl.util.vector.Vector3f;

public class Vertex {
	
	private final Vector3f pos;
    private final float u;
    private final float v;

    public Vertex(float f, float g, float h, float i, float j) {
        this(new Vector3f(f, g, h), i, j);
    }

    public Vertex remap(float f, float g) {
        return new Vertex(this.pos, f, g);
    }

    public Vertex(Vector3f vector3f, float f, float g) {
        this.pos = vector3f;
        this.u = f;
        this.v = g;
    }

	public Vector3f getPos() {
		return pos;
	}

	public float getU() {
		return u;
	}

	public float getV() {
		return v;
	}
}
