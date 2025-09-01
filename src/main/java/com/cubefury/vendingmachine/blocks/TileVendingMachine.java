package com.cubefury.vendingmachine.blocks;

import net.minecraft.tileentity.TileEntity;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cubefury.vendingmachine.blocks.gui.GuiVendingMachine;

public class TileVendingMachine extends TileEntity implements IGuiHolder<PosGuiData> {

    public TileVendingMachine() {
        super();
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return new GuiVendingMachine().getGui(data, syncManager, settings);
    }
}
