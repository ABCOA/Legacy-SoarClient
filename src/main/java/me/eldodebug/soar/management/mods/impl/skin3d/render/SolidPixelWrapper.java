package me.eldodebug.soar.management.mods.impl.skin3d.render;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.eldodebug.soar.management.mods.impl.skin3d.SkinDirection;
import me.eldodebug.soar.management.mods.impl.skin3d.opengl.NativeImage;

public class SolidPixelWrapper {

    public static CustomizableModelPart wrapBox(NativeImage natImage, int width, int height, int depth, int textureU, int textureV, boolean topPivot, float rotationOffset) {
    	
        List<CustomizableCube> cubes = new ArrayList<>();
        float pixelSize = 1f;
        float staticXOffset = -width / 2f;
        float staticYOffset = topPivot ? +rotationOffset : -height + rotationOffset;
        float staticZOffset = -depth / 2f;
        
        for (int u = 0; u < width; u++) {
            for (int v = 0; v < height; v++) {
                addPixel(natImage, cubes, pixelSize, u == 0 || v == 0 || u == width - 1 || v == height - 1,
                        textureU + depth + u, textureV + depth + v, staticXOffset + u, staticYOffset + v, staticZOffset,
                        SkinDirection.SOUTH);
                
                addPixel(natImage, cubes, pixelSize, u == 0 || v == 0 || u == width - 1 || v == height - 1,
                        textureU + 2 * depth + width + u, textureV + depth + v, staticXOffset + width - 1 - u,
                        staticYOffset + v, staticZOffset + depth - 1, SkinDirection.NORTH);
            }
        }

        for (int u = 0; u < depth; u++) {
            for (int v = 0; v < height; v++) {
            	
                addPixel(natImage, cubes, pixelSize, u == 0 || v == 0 || u == depth - 1 || v == height - 1,
                        textureU - 1 + depth - u, textureV + depth + v, staticXOffset, staticYOffset + v,
                        staticZOffset + u, SkinDirection.EAST);
                
                addPixel(natImage, cubes, pixelSize, u == 0 || v == 0 || u == depth - 1 || v == height - 1,
                        textureU + depth + width + u, textureV + depth + v, staticXOffset + width - 1f,
                        staticYOffset + v, staticZOffset + u, SkinDirection.WEST);

            }
        }
        
        for (int u = 0; u < width; u++) {
            for (int v = 0; v < depth; v++) {
            	
                addPixel(natImage, cubes, pixelSize, u == 0 || v == 0 || u == width - 1 || v == depth - 1,
                        textureU + depth + u, textureV + depth - 1 - v, staticXOffset + u, staticYOffset,
                        staticZOffset + v, SkinDirection.UP);
                
                addPixel(natImage, cubes, pixelSize, u == 0 || v == 0 || u == width - 1 || v == depth - 1,
                        textureU + depth + width + u, textureV + depth - 1 - v, staticXOffset + u,
                        staticYOffset + height - 1f, staticZOffset + v, SkinDirection.DOWN);
            }
        }

        return new CustomizableModelPart(cubes);
    }

    private static int[][] offsets = new int[][] { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
    private static SkinDirection[] hiddenDirN = new SkinDirection[] { SkinDirection.WEST, SkinDirection.EAST, SkinDirection.UP,
            SkinDirection.DOWN };
    private static SkinDirection[] hiddenDirS = new SkinDirection[] { SkinDirection.EAST, SkinDirection.WEST, SkinDirection.UP,
            SkinDirection.DOWN };
    private static SkinDirection[] hiddenDirW = new SkinDirection[] { SkinDirection.SOUTH, SkinDirection.NORTH, SkinDirection.UP,
            SkinDirection.DOWN };
    private static SkinDirection[] hiddenDirE = new SkinDirection[] { SkinDirection.NORTH, SkinDirection.SOUTH, SkinDirection.UP,
            SkinDirection.DOWN };
    private static SkinDirection[] hiddenDirUD = new SkinDirection[] { SkinDirection.EAST, SkinDirection.WEST, SkinDirection.NORTH,
            SkinDirection.SOUTH };

    private static void addPixel(NativeImage natImage, List<CustomizableCube> cubes, float pixelSize, boolean onBorder, int u, int v, float x, float y, float z, SkinDirection dir) {
    	
        if (natImage.getLuminanceOrAlpha(u, v) != 0) {
            Set<SkinDirection> hide = new HashSet<>();
            if (!onBorder) {
                for (int i = 0; i < offsets.length; i++) {
                    int tU = u + offsets[i][1];
                    int tV = v + offsets[i][0];
                    if (tU >= 0 && tU < 64 && tV >= 0 && tV < 64 && natImage.getLuminanceOrAlpha(tU, tV) != 0) {
                    	
                        if (dir == SkinDirection.NORTH) {
                            hide.add(hiddenDirN[i]);
                        }

                        if (dir == SkinDirection.SOUTH) {
                            hide.add(hiddenDirS[i]);
                        }

                        if (dir == SkinDirection.EAST) {
                            hide.add(hiddenDirE[i]);
                        }

                        if (dir == SkinDirection.WEST) {
                            hide.add(hiddenDirW[i]);
                        }

                        if (dir == SkinDirection.UP || dir == SkinDirection.DOWN) {
                            hide.add(hiddenDirUD[i]);
                        }
                    }
                }
                hide.add(dir);
            }
            
            cubes.addAll(CustomizableCubeListBuilder.create().texOffs(u - 2, v - 1).addBox(x, y, z, pixelSize, hide.toArray(new SkinDirection[hide.size()])).getCubes());
        }
    }

}