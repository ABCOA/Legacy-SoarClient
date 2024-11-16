package me.eldodebug.soar.utils.vector;

public class Pose {
	
	public final Matrix4f pose;

	public final Matrix3f normal;

	public Pose(Matrix4f matrix4f, Matrix3f matrix3f) {
        this.pose = matrix4f;
        this.normal = matrix3f;
    }

    public Matrix4f pose() {
        return this.pose;
    }

    public Matrix3f normal() {
        return this.normal;
    }
}
