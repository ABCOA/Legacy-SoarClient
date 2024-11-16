package me.eldodebug.soar.management.mods.impl.minimap;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.IntStream;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import me.eldodebug.soar.injection.interfaces.IMixinWorld;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ChunkAtlas implements Iterable<ChunkTile> {

    private final ChunkCoordIntPair[] chunkCoords;
    private final BitSet reusableChunks;
    private final int radius;
    private final int chunkSpanL2;
    private final double chunkWidth, chunkHeight;
    private final IntBuffer pixels;
    private final int texture;
    
    public ChunkAtlas(int maxChunkRadius) {
    	
        int maxChunks = maxChunkRadius * maxChunkRadius << 2;

        int texWidth = Integer.highestOneBit(maxChunks - 1) << 5;
        int texHeight = 16;

        int texLimit = Minecraft.getGLMaximumTextureSize();
        while (texWidth > texLimit) {
            texWidth >>= 1;
            texHeight <<= 1;
        }

        while (texHeight > texLimit) {
            texHeight >>= 1;
        }

        int chunkCapacity = texWidth * texHeight >> 8;

        if (maxChunks > chunkCapacity) {
            maxChunks = chunkCapacity;
        }

        this.radius = (int) Math.sqrt(maxChunks >> 2);
        this.chunkSpanL2 = Integer.numberOfTrailingZeros(texWidth >> 4);

        this.chunkWidth = 16.0 / texWidth;
        this.chunkHeight = 16.0 / texHeight;

        this.chunkCoords = new ChunkCoordIntPair[maxChunks];
        this.reusableChunks = new BitSet(maxChunks);

        this.texture = GL11.glGenTextures();
        GlStateManager.bindTexture(this.texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D, 0,
                GL11.GL_RGBA,
                texWidth, texHeight, 0,
                GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
                (IntBuffer) null
        );
        
        this.pixels = GLAllocation.createDirectIntBuffer(256);
    }
    
    public int getChunkRadius() {
        return this.radius;
    }

    public int getTextureHandle() {
        return this.texture;
    }

    public double getSpriteX(int offset) {
        return (offset & ((1 << this.chunkSpanL2) - 1)) * this.chunkWidth;
    }

    public double getSpriteY(int offset) {
        return (offset >> this.chunkSpanL2) * this.chunkHeight;
    }

    public double getSpriteWidth() {
        return this.chunkWidth;
    }

    public double getSpriteHeight() {
        return this.chunkHeight;
    }

    public void clear() {
        Arrays.fill(this.chunkCoords, null);
    }

    public void loadChunks(int chunkX, int chunkZ) {
        World w = Minecraft.getMinecraft().theWorld;
        if (w == null) {
            return;
        }

        this.reusableChunks.clear();

        for (int offs = 0; offs < this.chunkCoords.length; offs++) {
            ChunkCoordIntPair coords = this.chunkCoords[offs];
            if (coords == null) {
                continue;
            }

            int offsX = coords.chunkXPos - chunkX;
            int offsZ = coords.chunkZPos - chunkZ;

            if (offsX < -this.radius || this.radius <= offsX) {
                this.chunkCoords[offs] = null;
                continue;
            }

            if (offsZ < -this.radius || this.radius <= offsZ) {
                this.chunkCoords[offs] = null;
                continue;
            }

            offsX += this.radius;
            offsZ += this.radius;

            this.reusableChunks.set(offsX + offsZ * this.radius * 2);
        }

        for (int relZ = this.radius * 2 - 1; relZ >= 0; relZ--) {
            for (int relX = this.radius * 2 - 1; relX >= 0; relX--) {
                int checkIdx = relX + relZ * this.radius * 2;

                if (this.reusableChunks.get(checkIdx)) {
                    continue;
                }

                int x = chunkX + relX - this.radius;
                int z = chunkZ + relZ - this.radius;

                Chunk c = this.getLoadedChunk(x, z);
                if (c == null) {
                    continue;
                }

                this.reserveOffset(c);

                this.recolorChunk(x, z + 1);
            }
        }
    }

    public void refreshChunk(int x, int z) {
        this.recolorChunk(x, z);
        this.recolorChunk(x, z + 1);
    }

    @Override
    public Iterator<ChunkTile> iterator() {
        return IntStream.range(0, this.chunkCoords.length)
                .filter(offs -> this.chunkCoords[offs] != null)
                .mapToObj(offs -> {
                    ChunkCoordIntPair coords = this.chunkCoords[offs];
                    return new ChunkTile(coords.chunkXPos, coords.chunkZPos, offs);
                }).iterator();
    }

    private void reserveOffset(Chunk c) {
    	
        int offs = this.searchChunkAtlas(null);
        
        if (offs == -1) {
            throw new IllegalStateException("Chunk coordinate array full.");
        }

        this.chunkCoords[offs] = c.getChunkCoordIntPair();

        this.updateColorData(c, offs);
    }

    private void recolorChunk(int x, int z) {
    	
        Chunk c = this.getLoadedChunk(x, z);
        
        if (c == null) {
            return;
        }

        int offs = this.searchChunkAtlas(new ChunkCoordIntPair(x, z));
        if (offs == -1) {
            return;
        }

        this.updateColorData(c, offs);
    }

    private void updateColorData(Chunk src, int offs) {
    	
        this.computeColors(src);

        int x = offs & ((1 << this.chunkSpanL2) - 1);
        int y = offs >> this.chunkSpanL2;

        x <<= 4;
        y <<= 4;

        GlStateManager.bindTexture(this.texture);
        GL11.glTexSubImage2D(
                GL11.GL_TEXTURE_2D, 0,
                x, y, 16, 16,
                GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
                this.pixels
        );
    }

    private void computeColors(Chunk src) {
    	
        int[] northHeights = new int[16];
        
        Arrays.fill(northHeights, -1);

        Chunk north = this.getLoadedChunk(src.xPosition, src.zPosition - 1);
        if (north != null) {
            for (int x = 0; x < 16; x++) {
                northHeights[x] = this.getTopColoredBlockState(north, x, 15).getY();
            }
        }

        for (int x = 0; x < 16; x++) {
            int northHeight = northHeights[x];

            for (int z = 0; z < 16; z++) {
                BlockPos pos = this.getTopColoredBlockState(src, x, z);
                IBlockState state = src.getBlockState(pos);
                MapColor color = state.getBlock().getMapColor(state);

                int height = pos.getY();
                int shade = 1;

                if (northHeight > height) {
                    shade = 0;
                } else if (northHeight >= 0 && northHeight < height) {
                    shade = 2;
                }

                int depth = 0;
                while (pos.getY() >= 0 && !state.getBlock().getMaterial().isSolid()) {
                    pos = pos.add(0, -1, 0);
                    state = src.getBlockState(pos);
                    depth++;
                }

                if (depth > 0) {
                    int dither = depth + (((x ^ z) & 1) << 1);

                    if (dither < 5) {
                        shade = 2;
                    } else if (dither > 9) {
                        shade = 0;
                    }
                }

                int rgb;

                if (height > 0) {
                    rgb = color.getMapColor(shade);
                } else if (((x ^ z) & 3) == 0) {
                    rgb = 0x2d2d5a;
                } else {
                    rgb = 0x1e1e3c;
                }

                northHeight = height;

                this.pixels.put(x | z << 4, rgb);
            }
        }
    }

    private BlockPos getTopColoredBlockState(Chunk src, int x, int z) {
    	
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = src.getTopFilledSegment() + 15; y >= 0; y--) {
            IBlockState state = src.getBlockState(pos.set(x, y, z));

            if (state.getBlock().getMapColor(state) != MapColor.airColor) {
                break;
            }
        }

        return pos;
    }

    private int searchChunkAtlas(ChunkCoordIntPair c) {
    	
        for (int offs = 0; offs < this.chunkCoords.length; offs++) {
            if (Objects.equals(c, this.chunkCoords[offs])) {
                return offs;
            }
        }

        return -1;
    }

    private Chunk getLoadedChunk(int x, int z) {
    	
        World world = Minecraft.getMinecraft().theWorld;
        
        if (world == null) {
            return null;
        }

        if (!((IMixinWorld)world).isLoaded(x, z, true)) {
            return null;
        }

        Chunk c = world.getChunkFromChunkCoords(x, z);
        if (c.isEmpty()) {
            return null;
        }

        return c;
    }
}
