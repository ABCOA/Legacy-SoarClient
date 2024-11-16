package me.eldodebug.soar.management.mods.impl.minimap;

public class ChunkTile {

    private final int chunkX;
    private final int chunkZ;

    private final int offset;

    public ChunkTile(int chunkX, int chunkZ, int offset) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.offset = offset;
    }

    public int getChunkX() {
        return this.chunkX;
    }

    public int getChunkZ() {
        return this.chunkZ;
    }

    public int getOffset() {
        return this.offset;
    }
}
