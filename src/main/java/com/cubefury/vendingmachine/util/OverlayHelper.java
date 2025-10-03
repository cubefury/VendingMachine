package com.cubefury.vendingmachine.util;

import java.util.List;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;

import gregtech.api.interfaces.IIconContainer;
import gregtech.api.render.RenderOverlay;
import gregtech.api.render.TextureFactory;

public class OverlayHelper {

    private static final int[] vmX = new int[] { -1, 0, -1, -1, 0 };
    private static final int[] vmY = new int[] { -1, -1, 0, 1, 1 };

    public static void clearVMOverlay(List<RenderOverlay.OverlayTicket> overlayTickets) {
        overlayTickets.forEach(RenderOverlay.OverlayTicket::remove);
        overlayTickets.clear();
    }

    /**
     * Texture Display Front View
     * -----
     * 0 | 1
     * 2 | ~
     * 3 | 4
     * -----
     * Only 0-3 are animated
     * 4 is a static texture without glow
     */
    public static void setVMOverlay(World world, int x, short y, int z, ExtendedFacing facing,
        IIconContainer[] vmTextures, List<RenderOverlay.OverlayTicket> overlayTickets) {
        clearVMOverlay(overlayTickets);

        int[] tXYZOffset = new int[3];
        final ForgeDirection tDirection = facing.getDirection();
        facing = ExtendedFacing.of(tDirection);

        RenderOverlay overlay = RenderOverlay.getOrCreate(world);

        for (int i = 0; i < 5; i++) {
            int[] tABCCoord = new int[] { vmX[i], vmY[i], 0 };

            facing.getWorldOffset(tABCCoord, tXYZOffset);
            int tX = tXYZOffset[0] + x;
            int tY = tXYZOffset[1] + y;
            int tZ = tXYZOffset[2] + z;

            if (i == 4) { // bottom right
                overlayTickets.add(overlay.set(x, y, z, tX, tY, tZ, tDirection, TextureFactory.of(vmTextures[i]), 0));
            } else {
                overlayTickets.add(
                    overlay.set(
                        x,
                        y,
                        z,
                        tX,
                        tY,
                        tZ,
                        tDirection,
                        TextureFactory.builder()
                            .addIcon(vmTextures[i])
                            .glow()
                            .build(),
                        0));
            }
        }
    }
}
