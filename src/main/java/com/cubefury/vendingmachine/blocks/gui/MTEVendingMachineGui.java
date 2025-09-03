package com.cubefury.vendingmachine.blocks.gui;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.SingleChildWidget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.blocks.MTEVendingMachine;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import gregtech.api.metatileentity.implementations.gui.MTEMultiBlockBaseGui;
import gregtech.api.modularui2.GTGuiTextures;
import gregtech.api.modularui2.GTWidgetThemes;

public class MTEVendingMachineGui extends MTEMultiBlockBaseGui {

    private final MTEVendingMachine base;
    private final int height;

    private boolean ejectItems = false;
    private IItemHandlerModifiable tradeItemHandler = new ItemStackHandler(MTEVendingMachine.MAX_TRADES);

    public MTEVendingMachineGui(MTEVendingMachine base, int height) {
        super(base);
        this.base = base;
        this.height = height;
    }

    @Override
    public ModularPanel build(PosGuiData guiData, PanelSyncManager syncManager, UISettings uiSettings) {
        registerSyncValues(syncManager);
        ModularPanel panel = new ModularPanel("MTEMultiBlockBase").size(198, height)
            .padding(4);
        panel = panel.child(
            new Column().width(170)
                .child(createTitleTextStyle(base.getLocalName()))
                .child(createInputRow(syncManager))
                .child(createTradeUI())
                .child(createInventoryRow(panel, syncManager)));
        panel = panel.child(
            new Column().size(20)
                .right(5)
                .child(createOutputSlot()));
        return panel;
    }

    // why is this method private lmao
    private IWidget createTitleTextStyle(String title) {
        return new SingleChildWidget<>().coverChildren()
            .topRel(0, -4, 1)
            .leftRel(0, -4, 0)
            .widgetTheme(GTWidgetThemes.BACKGROUND_TITLE)
            .child(
                IKey.str(title)
                    .asWidget()
                    .alignment(Alignment.Center)
                    .widgetTheme(GTWidgetThemes.TEXT_TITLE)
                    .marginLeft(5)
                    .marginRight(5)
                    .marginTop(5)
                    .marginBottom(1));
    }

    private void ejectItems() {
        Side side = FMLCommonHandler.instance()
            .getEffectiveSide();
        if (side.isServer()) {
            if (base.getBaseMetaTileEntity() == null) {
                VendingMachine.LOG.info("Unable to eject items as the base MTE for the Vending Machine was null.");
            } else {
                World world = base.getBaseMetaTileEntity()
                    .getWorld();
                int posX = base.getBaseMetaTileEntity()
                    .getXCoord();
                int posY = base.getBaseMetaTileEntity()
                    .getYCoord();
                int posZ = base.getBaseMetaTileEntity()
                    .getZCoord();
                int offsetX = base.getExtendedFacing()
                    .getDirection().offsetX;
                int offsetY = base.getExtendedFacing()
                    .getDirection().offsetY;
                int offsetZ = base.getExtendedFacing()
                    .getDirection().offsetZ;
                for (int i = 0; i < MTEVendingMachine.INPUT_SLOTS; i++) {
                    ItemStack stack = base.inputItems.getStackInSlot(i);
                    if (stack != null) {
                        ItemStack extracted = base.inputItems.extractItem(i, stack.stackSize, false);
                        final EntityItem itemEntity = new EntityItem(
                            world,
                            posX + offsetX * 0.5,
                            posY + offsetY * 0.5,
                            posZ + offsetZ * 0.5,
                            new ItemStack(extracted.getItem(), extracted.stackSize, extracted.getItemDamage()));
                        if (extracted.hasTagCompound()) {
                            itemEntity.getEntityItem()
                                .setTagCompound(
                                    (NBTTagCompound) extracted.getTagCompound()
                                        .copy());
                        }
                        itemEntity.delayBeforeCanPickup = 0;
                        itemEntity.motionX = 0.05f * offsetX;
                        itemEntity.motionY = 0.05f * offsetY;
                        itemEntity.motionZ = 0.05f * offsetZ;
                        world.spawnEntityInWorld(itemEntity);
                    }
                }
            }
        }
        ejectItems = false;
    }

    private IWidget createInputRow(PanelSyncManager syncManager) {
        Row row = new Row();
        row.child(
            SlotGroupWidget.builder()
                .matrix("IIIIIII")
                .key(
                    'I',
                    index -> {
                        return new ItemSlot().slot(new ModularSlot(base.inputItems, index).slotGroup("inputSlotGroup"));
                    })
                .build());
        row.child(
            new ToggleButton().overlay(GTGuiTextures.OVERLAY_BUTTON_CYCLIC)
                .tooltipBuilder(t -> t.addLine(IKey.lang("vendingmachine.gui.item_eject")))
                .left(144)
                .syncHandler("ejectItems"));
        return row.height(18)
            .width(162)
            .top(0)
            .left(4);
    }

    private IWidget createOutputSlot() {
        // we use slot group widget in case we want to increase the number of output slots in the future
        return SlotGroupWidget.builder()
            .matrix("I")
            .key('I', index -> {
                return new ItemSlot().slot(
                    new ModularSlot(base.outputItems, index).accessibility(false, true)
                        .slotGroup("outputSlotGroup"));
            })
            .build();
    }

    private IWidget createTradeUI() {
        ScrollWidget<?> sw = new ScrollWidget<>(new VerticalScrollData()).size(9 * 18)
            .margin(0);
        sw.getScrollArea()
            .getScrollY()
            .setScrollSize(18 * (MTEVendingMachine.MAX_TRADES) / 9);
        for (int i = 0; i < MTEVendingMachine.MAX_TRADES; i++) {
            int x = i % 9;
            int y = i / 9;
            sw.child(
                new TradeSlot(x, y).pos(x * 18, y * 18)
                    .slot(new ModularSlot(tradeItemHandler, i)));
        }

        return new Row().child(sw.top(0))
            .left(4)
            .top(38);
    }

    // why is this method private lmao
    private IWidget createInventoryRow(ModularPanel panel, PanelSyncManager syncManager) {
        return new Row().widthRel(1)
            .height(76)
            .alignX(0)
            .bottom(5)
            .childIf(
                base.doesBindPlayerInventory(),
                SlotGroupWidget.playerInventory(false)
                    .marginLeft(4));
    }

    @Override
    protected void registerSyncValues(PanelSyncManager syncManager) {
        super.registerSyncValues(syncManager);
        syncManager.registerSlotGroup("inputSlotGroup", 7, true);
        syncManager.registerSlotGroup("outputSlotGroup", 1, false);

        BooleanSyncValue ejectItemsSyncer = new BooleanSyncValue(() -> this.ejectItems, val -> {
            this.ejectItems = val;
            if (this.ejectItems) {
                ejectItems();
            }
        });
        syncManager.syncValue("ejectItems", ejectItemsSyncer);

    }

}
