package com.cubefury.vendingmachine.blocks;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.lazy;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;

import java.util.ArrayList;
import java.util.List;

import com.cubefury.vendingmachine.network.handlers.NetAvailableTradeSync;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cubefury.vendingmachine.blocks.gui.MTEVendingMachineGui;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.IAlignmentLimits;
import com.gtnewhorizon.structurelib.alignment.IAlignmentProvider;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;

import gregtech.api.GregTechAPI;
import gregtech.api.covers.CoverRegistry;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ISecondaryDescribable;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;

public class MTEVendingMachine extends MTEMultiBlockBase
    implements ISurvivalConstructable, ISecondaryDescribable, IAlignment {

    public static final int CUSTOM_UI_HEIGHT = 300;

    public static final int INPUT_SLOTS = 7;
    public static final int OUTPUT_SLOTS = 1;

    public static final int MAX_TRADES = 999;

    public static final int STRUCTURE_CHECK_TICKS = 20;

    private static final IStructureDefinition<MTEVendingMachine> STRUCTURE_DEFINITION = IStructureDefinition
        .<MTEVendingMachine>builder()
        .addShape("main", new String[][] { { "cc", "c~", "cc" } })
        .addElement('c', lazy(t -> ofBlock(GregTechAPI.sBlockCasings11, 0)))
        .build();
    private static final ITexture[] FACING_SIDE = {
        TextureFactory.of(Textures.BlockIcons.MACHINE_CASING_ITEM_PIPE_TIN) };
    private static final ITexture[] FACING_FRONT = {
        TextureFactory.of(Textures.BlockIcons.MACHINE_CASING_BRICKEDBLASTFURNACE_INACTIVE) };
    private static final ITexture[] FACING_ACTIVE = {
        TextureFactory.of(Textures.BlockIcons.MACHINE_CASING_BRICKEDBLASTFURNACE_ACTIVE), TextureFactory.builder()
            .addIcon(Textures.BlockIcons.MACHINE_CASING_BRICKEDBLASTFURNACE_ACTIVE_GLOW)
            .glow()
            .build() };

    private MultiblockTooltipBuilder tooltipBuilder;

    public int mUpdate = 0;
    public boolean mMachine = false;

    public ItemStackHandler inputItems = new ItemStackHandler(INPUT_SLOTS);
    public ItemStackHandler outputItems = new ItemStackHandler(OUTPUT_SLOTS);
    public List<ItemStack> outputBuffer = new ArrayList<>();

    public MTEVendingMachine(final int aID, final String aName, final String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    @Override
    public boolean getDefaultHasMaintenanceChecks() {
        return false;
    }

    public MTEVendingMachine(String aName) {
        super(aName);
    }

    @Override
    public String[] getStructureDescription(ItemStack stackSize) {
        return getTooltip().getStructureHint();
    }

    protected MultiblockTooltipBuilder getTooltip() {
        if (tooltipBuilder == null) {
            tooltipBuilder = new MultiblockTooltipBuilder();
            tooltipBuilder.addMachineType("Vending Machine")
                .addInfo("Who even restocks this...")
                .beginStructureBlock(2, 3, 1, false)
                .addController("Middle")
                .addOtherStructurePart("Tin Item Pipe Casings", "Everything except the controller")
                .toolTipFinisher();
        }
        return tooltipBuilder;
    }

    @Override
    protected boolean forceUseMui2() {
        return true;
    }

    @Override
    protected @NotNull MTEVendingMachineGui getGui() {
        NetAvailableTradeSync.requestSync();
        return new MTEVendingMachineGui(this, CUSTOM_UI_HEIGHT);
    }

    @Override
    public int getGUIHeight() {
        return CUSTOM_UI_HEIGHT;
    }

    @Override
    public boolean isTeleporterCompatible() {
        return false;
    }

    @Override
    public boolean isFacingValid(ForgeDirection facing) {
        return (facing.flag & (ForgeDirection.UP.flag | ForgeDirection.DOWN.flag)) == 0;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean active, boolean redstoneLevel) {
        if (side == facing) {
            return active ? FACING_ACTIVE : FACING_FRONT;
        }
        return FACING_SIDE;
    }

    /*
     * Use this to implement the items slowly dispensing? idk
     * temp code copied from bricked blast furnace implementation
     * Make sure to add progresstime and maxprogresstime to save/load nbt
     * @Override
     * public int getProgresstime() {
     * return this.mProgresstime;
     * }
     * @Override
     * public int maxProgresstime() {
     * return this.mMaxProgresstime;
     * }
     * @Override
     * public int increaseProgress(int aProgress) {
     * this.mProgresstime += aProgress;
     * return this.mMaxProgresstime - this.mProgresstime;
     * }
     */
    @Override
    public String[] getDescription() {
        return getCurrentDescription();
    }

    @Override
    public boolean allowCoverOnSide(ForgeDirection side, ItemStack coverItem) {
        return (CoverRegistry.getCoverPlacer(coverItem)
            .allowOnPrimitiveBlock()) && (super.allowCoverOnSide(side, coverItem));
    }

    @Override
    public String[] getPrimaryDescription() {
        return getTooltip().getInformation();
    }

    @Override
    public String[] getSecondaryDescription() {
        return getTooltip().getStructureInformation();
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        if (inputItems != null) {
            aNBT.setTag("inputs", inputItems.serializeNBT());
        }
        if (outputItems != null) {
            aNBT.setTag("outputs", outputItems.serializeNBT());
        }
        NBTTagList pendingOutputs = new NBTTagList();
        for (ItemStack itemStack : outputBuffer) {
            pendingOutputs.appendTag(itemStack.writeToNBT(new NBTTagCompound()));
        }
        aNBT.setTag("outputBuffer", pendingOutputs);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        if (inputItems != null) {
            inputItems.deserializeNBT(aNBT.getCompoundTag("inputs"));
        }
        if (outputItems != null) {
            outputItems.deserializeNBT(aNBT.getCompoundTag("outputs"));
        }
        NBTTagList pendingOutputs = aNBT.getTagList("outputBuffer", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < pendingOutputs.tagCount(); i++) {
            outputBuffer.add(GTUtility.loadItem(pendingOutputs.getCompoundTagAt(i)));
        }
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return false;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return false;
    }

    @Override
    public byte getTileEntityBaseType() {
        return 0;
    }

    @Override
    public ExtendedFacing getExtendedFacing() {
        return ExtendedFacing.of(getBaseMetaTileEntity().getFrontFacing());
    }

    @Override
    public void setExtendedFacing(ExtendedFacing alignment) {
        getBaseMetaTileEntity().setFrontFacing(alignment.getDirection());
    }

    @Override
    public IAlignmentLimits getAlignmentLimits() {
        return (d, r, f) -> (d.flag & (ForgeDirection.UP.flag | ForgeDirection.DOWN.flag)) == 0;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEVendingMachine(this.mName);
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        return STRUCTURE_DEFINITION.check(
            this,
            "main",
            getBaseMetaTileEntity().getWorld(),
            getExtendedFacing(),
            getBaseMetaTileEntity().getXCoord(),
            getBaseMetaTileEntity().getYCoord(),
            getBaseMetaTileEntity().getZCoord(),
            1,
            1,
            0,
            !mMachine);
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
        if ((aBaseMetaTileEntity.isClientSide()) && (aBaseMetaTileEntity.isActive())) {
            // spawn something maybe
        }
        if (aBaseMetaTileEntity.isServerSide()) {
            if (this.mUpdate++ % STRUCTURE_CHECK_TICKS == 0) {
                this.mMachine = checkMachine(aBaseMetaTileEntity, null);
                aBaseMetaTileEntity.setActive(this.mMachine);
            }
            /*
             * if (this.mMachine) {
             * // dispense mechanic goes here
             * }
             */
        }
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
        if (aBaseMetaTileEntity.isClientSide())
            StructureLibAPI.queryAlignment((IAlignmentProvider) aBaseMetaTileEntity);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return STRUCTURE_DEFINITION.survivalBuild(
            this,
            stackSize,
            "main",
            getBaseMetaTileEntity().getWorld(),
            getExtendedFacing(),
            getBaseMetaTileEntity().getXCoord(),
            getBaseMetaTileEntity().getYCoord(),
            getBaseMetaTileEntity().getZCoord(),
            1,
            1,
            0,
            elementBudget,
            env,
            false);
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        STRUCTURE_DEFINITION.buildOrHints(
            this,
            stackSize,
            "main",
            getBaseMetaTileEntity().getWorld(),
            getExtendedFacing(),
            getBaseMetaTileEntity().getXCoord(),
            getBaseMetaTileEntity().getYCoord(),
            getBaseMetaTileEntity().getZCoord(),
            1,
            1,
            0,
            hintsOnly);
    }
}
