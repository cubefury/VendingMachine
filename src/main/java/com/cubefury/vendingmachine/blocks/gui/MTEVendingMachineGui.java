package com.cubefury.vendingmachine.blocks.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.SingleChildWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.blocks.MTEVendingMachine;
import com.cubefury.vendingmachine.gui.GuiTextures;
import com.cubefury.vendingmachine.trade.TradeCategory;
import com.cubefury.vendingmachine.trade.TradeDatabase;

import gregtech.api.metatileentity.implementations.gui.MTEMultiBlockBaseGui;
import gregtech.api.modularui2.GTGuiTextures;
import gregtech.api.modularui2.GTWidgetThemes;

public class MTEVendingMachineGui extends MTEMultiBlockBaseGui {

    private final MTEVendingMachine base;
    private final int height;

    public boolean forceRefresh = false;

    private boolean ejectItems = false;
    private final Map<TradeCategory, IItemHandlerModifiable> tradeItemHandlers = new HashMap<>();
    private final Map<TradeCategory, List<TradeItemDisplay>> displayedTrades = new HashMap<>();
    private final List<TradeCategory> tradeCategories = new ArrayList<>();

    private PosGuiData guiData;
    private PagedWidget.Controller tabController;

    public static final int ITEMS_PER_ROW = 3;
    private static final int ITEM_HEIGHT = 18;
    private static final int ROW_SEPARATOR_HEIGHT = 5;

    public MTEVendingMachineGui(MTEVendingMachine base, int height) {
        super(base);
        this.base = base;
        this.height = height;

        this.tradeCategories.add(TradeCategory.ALL);
        this.tradeCategories.addAll(TradeDatabase.INSTANCE.getTradeCategories());

        for (TradeCategory c : this.tradeCategories) {
            tradeItemHandlers.put(c, new ItemStackHandler(MTEVendingMachine.MAX_TRADES));
        }

        this.tabController = new PagedWidget.Controller();
    }

    public MTEVendingMachine getBase() {
        return base;
    }

    @Override
    public ModularPanel build(PosGuiData guiData, PanelSyncManager syncManager, UISettings uiSettings) {
        this.guiData = guiData;

        registerSyncValues(syncManager);
        ModularPanel panel = new TradeMainPanel("MTEMultiBlockBase", this, guiData, syncManager, this.tabController)
            .size(198, height)
            .padding(4);
        panel = panel.child(createCategoryTabs(this.tabController));
        panel = panel.child(
            new Column().width(170)
                .child(createTitleTextStyle(base.getLocalName()))
                .child(createInputRow(syncManager))
                .child(createTradeUI((TradeMainPanel) panel, this.tabController))
                .child(createInventoryRow(panel, syncManager)));
        panel = panel.child(
            new Column().size(20)
                .right(5)
                .child(createOutputSlot()));
        return panel;
    }

    public IWidget createCategoryTabs(PagedWidget.Controller tabController) {
        Flow tabColumn = new Column().width(40)
            .height(100)
            .left(-30)
            .top(50)
            .coverChildren();

        for (int i = 0; i < this.tradeCategories.size(); i++) {
            tabColumn = tabColumn.child(
                new PageButton(i, tabController).tab(com.cleanroommc.modularui.drawable.GuiTextures.TAB_LEFT, -1)
                    .overlay(
                        this.tradeCategories.get(i)
                            .getTexture()
                            .asIcon()
                            .margin(4)
                            .center()));
        }
        return tabColumn;
    }

    // why is the original method private lmao
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
        if (!this.guiData.isClient()) {
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
                        // TODO: There's still a race condition here where the stack size changes between these
                        // two null checks. Fix
                        ItemStack extracted = base.inputItems.extractItem(i, stack.stackSize, false);
                        if (extracted == null) { // if somehow it got pulled out already
                            continue;
                        }
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
                .key('I', index -> {
                    return new ItemSlot().slot(
                        new ModularSlot(base.inputItems, index).slotGroup("inputSlotGroup")
                            .changeListener((newItem, onlyAmountChanged, client, init) -> {
                                if (guiData.isClient()) {
                                    forceRefresh = true;
                                }
                            }));
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
                ModularSlot ms = new ModularSlot(base.outputItems, index).accessibility(false, true)
                    .slotGroup("outputSlotGroup");
                ms.changeListener((newItem, onlyAmountChanged, client, init) -> {
                    VendingMachine.LOG.info("Changed output slot");
                    VendingMachine.LOG.info(base.outputItems.getStackInSlot(0));
                });
                return new ItemSlot().slot(ms);
            })
            .build();
    }

    // spotless:off
    private IWidget createTradeUI(TradeMainPanel rootPanel, PagedWidget.Controller tabController) {
        PagedWidget<?> paged = new PagedWidget<>().expanded().debugName("paged").controller(tabController).heightRel(0.5f);
        for (TradeCategory category : this.tradeCategories) {
            ListWidget<IWidget, ?> tradeList = new ListWidget<>().debugName("items").heightRel(1.0f)
                .widthRel(1.0f).childSeparator(new Rectangle().setColor(0x0).asIcon().size(ROW_SEPARATOR_HEIGHT))
                .collapseDisabledChild(true);

            Flow row = new Row().height(ITEM_HEIGHT).collapseDisabledChild(true).setEnabledIf(r ->
                r.getChildren().stream().anyMatch(IWidget::isEnabled));

            for (int i = 0; i < MTEVendingMachine.MAX_TRADES; i++) {
                int index = i;
                int x = i % ITEMS_PER_ROW;
                int y = i / ITEMS_PER_ROW;
                row.child(new TradeSlot(category, i, rootPanel)
                    .slot(new ModularSlot(tradeItemHandlers.get(category), i))
                    .tooltipDynamic(builder -> {
                        // builder.clearText();
                        synchronized (displayedTrades) {
                            if (index < displayedTrades.get(category).size()) {
                                TradeItemDisplay cur = displayedTrades.get(category).get(index);
                                if (cur != null && cur.display != null) {
                                    builder.add("yes trade");
                                }
                            }
                        }
                    })
                    .tooltipAutoUpdate(true)    // if it starts lagging, we'll need to index all
                                                // the tradeslots and then call updateTooltip() every
                                                // refresh
                    .background(new DynamicDrawable(() -> {
                        if (index < displayedTrades.get(category).size() && displayedTrades.get(category).get(index).tradeableNow) {
                            return GuiTextures.TRADE_AVAILABLE_BACKGROUND;
                        }
                        return GTGuiTextures.SLOT_ITEM_STANDARD;
                    }))
                    .overlay(
                        new DynamicDrawable(() -> {
                            if (
                                index < displayedTrades.get(category).size()
                                    && (displayedTrades.get(category).get(index).hasCooldown || !displayedTrades.get(category).get(index).enabled)
                            ) {
                                return GuiTextures.OVERLAY_TRADE_DISABLED;
                            }
                            if (index < displayedTrades.get(category).size() && displayedTrades.get(category).get(index).tradeableNow) {
                                return GuiTextures.OVERLAY_TRADE_AVAILABLE_HIGHLIGHT;
                            }
                            return null; }),
                        new DynamicDrawable(() -> {
                            if (index < displayedTrades.get(category).size() && displayedTrades.get(category).get(index).hasCooldown) {
                                return IKey.str(displayedTrades.get(category).get(index).cooldownText);
                            }
                            return null; }))
                    .setEnabledIf(slot -> tradeItemHandlers.get(category).getStackInSlot(index) != null));
                if (i % ITEMS_PER_ROW == ITEMS_PER_ROW - 1) {
                    tradeList.child(row);

                    row = new Row().height(ITEM_HEIGHT).collapseDisabledChild(true).setEnabledIf(r ->
                        r.getChildren().stream().anyMatch(IWidget::isEnabled));
                }
            }
            if (row.hasChildren()) {
                tradeList.child(row);
            }
            paged.addPage(tradeList);
        }

        return new Row().child(paged.top(0))
            .left(4)
            .top(38);
    }
    // spotless:on

    // why is the original method private lmao
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

    public void attemptPurchase(TradeCategory category, int index) {
        TradeItemDisplay trade = null;

        synchronized (displayedTrades) {
            if (
                displayedTrades.get(category)
                    .size() <= index
            ) {
                return;
            }
            trade = displayedTrades.get(category)
                .get(index);
            if (trade == null) {
                return;
            }
            submitTradesToServer(
                displayedTrades.get(category)
                    .get(index));
        }
        this.forceRefresh = true;
    }

    private void submitTradesToServer(TradeItemDisplay trade) {
        if (!trade.tradeableNow || !trade.enabled) {
            return;
        }
        base.sendTradeRequest(trade);
    }

    public void resetForceRefresh() {
        this.forceRefresh = false;
    }

    public void updateSlots(Map<TradeCategory, List<TradeItemDisplay>> trades) {
        synchronized (displayedTrades) {
            displayedTrades.clear();
            displayedTrades.putAll(trades);
            for (Map.Entry<TradeCategory, List<TradeItemDisplay>> entry : displayedTrades.entrySet()) {
                int displayedSize = displayedTrades.get(entry.getKey())
                    .size();
                for (int i = 0; i < MTEVendingMachine.MAX_TRADES; i++) {
                    if (i < displayedSize) {
                        tradeItemHandlers.get(entry.getKey())
                            .setStackInSlot(
                                i,
                                displayedTrades.get(entry.getKey())
                                    .get(i) != null
                                        ? displayedTrades.get(entry.getKey())
                                            .get(i).display
                                        : null);
                    } else {
                        tradeItemHandlers.get(entry.getKey())
                            .setStackInSlot(i, null);
                    }
                }
            }
        }
    }

}
