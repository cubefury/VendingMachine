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
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
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
import com.cubefury.vendingmachine.gui.WidgetThemes;
import com.cubefury.vendingmachine.trade.TradeCategory;
import com.cubefury.vendingmachine.trade.TradeDatabase;
import com.cubefury.vendingmachine.util.BigItemStack;
import com.cubefury.vendingmachine.util.Translator;

import gregtech.api.metatileentity.implementations.gui.MTEMultiBlockBaseGui;
import gregtech.api.modularui2.GTGuiTextures;
import gregtech.api.modularui2.GTWidgetThemes;

public class MTEVendingMachineGui extends MTEMultiBlockBaseGui {

    private final MTEVendingMachine base;
    private final int height;

    public static boolean forceRefresh = false;

    private boolean ejectItems = false;
    private final Map<TradeCategory, List<TradeItemDisplayWidget>> displayedTrades = new HashMap<>();
    private final List<TradeCategory> tradeCategories = new ArrayList<>();

    private PosGuiData guiData;
    private PagedWidget.Controller tabController;
    private SearchBar searchBar;

    public static final int ITEMS_PER_ROW = 3;
    public static final int ITEM_HEIGHT = 25;
    public static final int ITEM_WIDTH = 47;
    private static final int ROW_SEPARATOR_HEIGHT = 5;

    public MTEVendingMachineGui(MTEVendingMachine base, int height) {
        super(base);
        this.base = base;
        this.height = height;

        this.tradeCategories.add(TradeCategory.ALL);
        this.tradeCategories.addAll(TradeDatabase.INSTANCE.getTradeCategories());

        for (TradeCategory c : this.tradeCategories) {
            displayedTrades.put(c, new ArrayList<>(MTEVendingMachine.MAX_TRADES));
            for (int i = 0; i < MTEVendingMachine.MAX_TRADES; i++) {
                displayedTrades.get(c)
                    .add(new TradeItemDisplayWidget(null));
            }
        }

        this.tabController = VendingMachine.proxy.isClient() ? new PagedWidget.Controller() : null;
        this.searchBar = VendingMachine.proxy.isClient() ? createSearchBar() : null;
    }

    public MTEVendingMachine getBase() {
        return base;
    }

    public static void setForceRefresh() {
        forceRefresh = true;
    }

    @Override
    public ModularPanel build(PosGuiData guiData, PanelSyncManager syncManager, UISettings uiSettings) {
        this.guiData = guiData;

        registerSyncValues(syncManager);
        ModularPanel panel = new TradeMainPanel("MTEMultiBlockBase", this, guiData, syncManager).size(178, height)
            .padding(4);
        panel.child(createCategoryTabs(this.tabController));
        Flow mainColumn = new Column().width(170);
        if (VendingMachine.proxy.isClient()) { // client side filtering
            mainColumn.child(createTitleTextStyle(base.getLocalName()))
                .child(this.searchBar)
                .child(createTradeUI((TradeMainPanel) panel, this.tabController));
        }
        mainColumn.child(createInventoryRow(panel, syncManager));
        panel.child(mainColumn);
        panel.child(
            new Column().size(20)
                .right(5));
        panel.child(createIOColumn(syncManager));
        return panel;
    }

    public IWidget createCategoryTabs(PagedWidget.Controller tabController) {
        Flow tabColumn = new Column().width(40)
            .height(100)
            .left(-29)
            .top(40)
            .coverChildren();

        for (int i = 0; i < this.tradeCategories.size(); i++) {
            int index = i;
            tabColumn.child(
                new PageButton(i, tabController).tab(GuiTextures.TAB_LEFT, -1)
                    .overlay(
                        this.tradeCategories.get(i)
                            .getTexture()
                            .asIcon()
                            .margin(6)
                            .center())
                    .tooltipBuilder(builder -> {
                        builder.clearText();
                        builder.addLine(
                            Translator.translate(
                                this.tradeCategories.get(index)
                                    .getUnlocalized_name()));
                    }));
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

    private SearchBar createSearchBar() {
        return new SearchBar(this).width(162)
            .left(3)
            .top(5)
            .height(10);
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

    private IWidget createIOColumn(PanelSyncManager syncManager) {
        return new ParentWidget<>().excludeAreaInNEI()
            .width(50)
            .height(160)
            .right(-48)
            .top(40)
            .widgetTheme(WidgetThemes.BACKGROUND_SIDEPANEL)
            .child(
                new Column().child(
                    GuiTextures.INPUT_SPRITE.asWidget()
                        .leftRel(0.5f)
                        .top(8)
                        .width(30)
                        .height(20))
                    .child(
                        new Row().child(createInputRow(syncManager).center())
                            .top(20)
                            .height(18 * 3))
                    .child(
                        new Row().child(
                            new ToggleButton().overlay(GTGuiTextures.OVERLAY_BUTTON_CYCLIC)
                                .tooltipBuilder(t -> t.addLine(IKey.lang("vendingmachine.gui.item_eject")))
                                .syncHandler("ejectItems")
                                .center())
                            .top(80)
                            .height(18))
                    .child(
                        GuiTextures.OUTPUT_SPRITE.asWidget()
                            .leftRel(0.5f)
                            .bottom(34)
                            .width(30)
                            .height(20))
                    .child(
                        new Row().child(createOutputSlots().center())
                            .bottom(6)
                            .height(18 * 2))
                    .right(1));
    }

    private SlotGroupWidget createInputRow(PanelSyncManager syncManager) {
        return SlotGroupWidget.builder()
            .matrix("II", "II", "II")
            .key('I', index -> {
                InterceptingSlot slot = new InterceptingSlot(base.inputItems, index);
                return new ItemSlot().slot(
                    slot.slotGroup("inputSlotGroup")
                        .changeListener((newItem, onlyAmountChanged, client, init) -> {
                            if (
                                slot.intercept(
                                    newItem,
                                    client,
                                    this.getBase()
                                        .getCurrentUser())
                            ) {
                                return;
                            }
                            if (guiData.isClient()) {
                                forceRefresh = true;
                            }
                        }));
            })
            .build();
    }

    private SlotGroupWidget createOutputSlots() {
        // we use slot group widget in case we want to increase the number of output slots in the future
        return SlotGroupWidget.builder()
            .matrix("II", "II")
            .key('I', index -> {
                ModularSlot ms = new ModularSlot(base.outputItems, index).accessibility(false, true)
                    .slotGroup("outputSlotGroup");
                ms.changeListener((newItem, onlyAmountChanged, client, init) -> {});
                return new ItemSlot().slot(ms);
            })
            .build();
    }

    // spotless:off
    private IWidget createTradeUI(TradeMainPanel rootPanel, PagedWidget.Controller tabController) {
        PagedWidget<?> paged = new PagedWidget<>()
            .width(162)
            .debugName("paged")
            .controller(tabController)
            .background(GuiTextures.TEXT_FIELD_BACKGROUND)
            .heightRel(0.5f);
        for (TradeCategory category : this.tradeCategories) {
            ListWidget<IWidget, ?> tradeList = new ListWidget<>().debugName("items").heightRel(1.0f)
                .width(156)
                .margin(1)
                .collapseDisabledChild(true);

            // Higher first row top margin
            Flow row = new TradeRow().height(ITEM_HEIGHT).margin(1).marginTop(4);

            for (int i = 0; i < MTEVendingMachine.MAX_TRADES; i++) {
                int index = i;
                displayedTrades.get(category).get(i).setRootPanel(rootPanel);
                row.child(displayedTrades.get(category).get(i)
                    .tooltipDynamic(builder -> {
                        builder.clearText();
                        synchronized (displayedTrades) {
                            if (index < displayedTrades.get(category).size()) {
                                TradeItemDisplay cur = displayedTrades.get(category).get(index).getDisplay();
                                if (cur != null) {
                                    for (BigItemStack toItem : cur.toItems) {
                                        builder.addLine(IKey.str(toItem.stackSize + " " + toItem.getBaseStack().getDisplayName()).style(IKey.AQUA));
                                        // builder.add(new ItemDrawable(toItem.getBaseStack()));
                                    }
                                    builder.emptyLine();
                                    builder.addLine(IKey.str(Translator.translate("vendingmachine.gui.required_inputs")).style(IKey.DARK_GREEN, IKey.ITALIC));
                                    for (BigItemStack fromItem : cur.fromItems) {
                                        builder.addLine(IKey.str(fromItem.stackSize + " " + fromItem.getBaseStack().getDisplayName()).style(IKey.DARK_GREEN));
                                    }

                                    builder.emptyLine();
                                    builder.addLine(IKey.str(cur.label).style(IKey.GRAY));
                                }
                            }
                        }
                    })
                    .tooltipAutoUpdate(true)
                    .setEnabledIf(slot -> ((TradeItemDisplayWidget) slot).getDisplay() != null)
                    .margin(2));
                if (i % ITEMS_PER_ROW == ITEMS_PER_ROW - 1) {
                    tradeList.child(row);

                    row = new TradeRow().height(ITEM_HEIGHT).margin(1);
                }
            }
            if (row.hasChildren()) {
                tradeList.child(row);
            }
            tradeList.child(new Row().height(2)); // bottom padding for last row
            paged.addPage(tradeList);
        }

        return new Row().child(paged.top(0))
            .left(3)
            .top(24);
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

    public void attemptPurchase(TradeItemDisplay display) {
        submitTradesToServer(display);
        forceRefresh = true;
    }

    private void submitTradesToServer(TradeItemDisplay trade) {
        if (!trade.tradeableNow || !trade.enabled) {
            return;
        }
        base.sendTradeRequest(trade);
    }

    public static void resetForceRefresh() {
        forceRefresh = false;
    }

    public void updateSlots(Map<TradeCategory, List<TradeItemDisplay>> trades) {
        synchronized (displayedTrades) {
            for (Map.Entry<TradeCategory, List<TradeItemDisplayWidget>> entry : displayedTrades.entrySet()) {
                int displayedSize = trades.get(entry.getKey()) == null ? 0
                    : trades.get(entry.getKey())
                        .size();
                for (int i = 0; i < MTEVendingMachine.MAX_TRADES; i++) {
                    if (i < displayedSize) {
                        displayedTrades.get(entry.getKey())
                            .get(i)
                            .setDisplay(
                                trades.get(entry.getKey())
                                    .get(i));
                    } else {
                        displayedTrades.get(entry.getKey())
                            .get(i)
                            .setDisplay(null);
                    }
                }
            }
        }
    }

    public String getSearchBarText() {
        return this.searchBar.getText();
    }

}
