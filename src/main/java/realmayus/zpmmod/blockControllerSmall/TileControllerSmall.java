package realmayus.zpmmod.blockControllerSmall;


import realmayus.zpmmod.ZPMConfig;
import realmayus.zpmmod.itemZPM.ItemZPM;
import realmayus.zpmmod.util.IGuiTile;
import realmayus.zpmmod.util.MyEnergyStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nonnull;

public class TileControllerSmall extends TileEntity implements IGuiTile, ITickable {

    /**
     * 0 = Ignore Redstone
     * 1 = Active on Redstone
     * 2 = Not active on Redstone
     */
    public int redstoneBehaviour = 0;
    public boolean isEnabled = true;

    @Override
    public void update() {
        if(isEnabled) {
            if(isActiveBasedOnSettings()) {
                if(doesContainZPM()) {
                    ItemZPM zpm = (ItemZPM) this.inputHandler.getStackInSlot(0).getItem();
                    sendEnergy(zpm.getEnergyStorage(this.inputHandler.getStackInSlot(0)));
                }
            }
        }
    }

    private boolean isActiveBasedOnSettings() {
        if(redstoneBehaviour == 0) {
            return true;
        } else if(redstoneBehaviour == 1) {
            if(this.world.isBlockPowered(this.getPos())) {
                return true;
            } else {
                return false;
            }
        } else {
            if(this.world.isBlockPowered(this.getPos())) {
                return false;
            } else {
                return true;
            }
        }
    }

    public boolean doesContainZPM() {
        return !this.inputHandler.getStackInSlot(0).isEmpty();
    }

    public int getEnergyOfZPM() {
        if(this.inputHandler.getStackInSlot(0).getCapability(CapabilityEnergy.ENERGY, null) != null) {
            return this.inputHandler.getStackInSlot(0).getCapability(CapabilityEnergy.ENERGY, null).getEnergyStored();
        } else {
            return -1;
        }

    }

    private void sendEnergy(IEnergyStorage energyStorage) {
        if (energyStorage.getEnergyStored() > 0) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
                if (tileEntity != null && tileEntity.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite())) {
                    IEnergyStorage handler = tileEntity.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite());
                    if (handler != null && handler.canReceive()) {
                        int outputPower = Math.min(energyStorage.getEnergyStored(), ZPMConfig.EMIT_ENERGY_MAXIMUM);  // cap power output at 1 mil RF/t
                        int accepted = handler.receiveEnergy(outputPower, false);
                        energyStorage.extractEnergy(accepted, false);
                        if (energyStorage.getEnergyStored() <= 0) {
                            break;
                        }
                    }
                }
            }
            markDirty();
        }
    }

    /**
     * If we are too far away from this tile entity you cannot use it
     */
    public boolean canInteractWith(EntityPlayer playerIn) {
        return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }

        if(capability == CapabilityEnergy.ENERGY) {
            return true;
        }

        return super.hasCapability(capability, facing);

    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(combinedHandler);
        }

        if(capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(myEnergyStorage);

        }

        return super.getCapability(capability, facing);
    }

    //int has to be 0 as we don't want to receive energy
    private MyEnergyStorage myEnergyStorage = new MyEnergyStorage(ZPMConfig.MAX_POWER_CONTROLLER_LARGE, 0);

    /**
     * Handler for the Input Slots
     */
    public ItemStackHandler inputHandler = new ItemStackHandler(1) {

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() instanceof ItemZPM;
        }

        @Override
        protected void onContentsChanged(int slot) {
            TileControllerSmall.this.markDirty();
        }
    };

    /**
     * Handler for the Output Slots
     */
    private ItemStackHandler outputHandler = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            TileControllerSmall.this.markDirty();
        }
    };

    private CombinedInvWrapper combinedHandler = new CombinedInvWrapper(inputHandler, outputHandler);



//    @Override
//    public void readFromNBT(NBTTagCompound compound) {
//        super.readFromNBT(compound);
//        readRestorableFromNBT(compound);
//    }
//
//    public void readRestorableFromNBT(NBTTagCompound compound) {
//    }
//
//    @Override
//    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
//        super.writeToNBT(compound);
//        writeRestorableToNBT(compound);
//        return compound;
//    }
//
//    public void writeRestorableToNBT(NBTTagCompound compound) {
//    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("enabled")) {
            isEnabled = compound.getBoolean("enabled");
        }
        if (compound.hasKey("redstone")) {
            redstoneBehaviour = compound.getInteger("redstone");
        }
        if (compound.hasKey("itemsIN"))  {
            inputHandler.deserializeNBT((NBTTagCompound) compound.getTag("itemsIN"));
        }
        if (compound.hasKey("itemsOUT"))  {
            outputHandler.deserializeNBT((NBTTagCompound) compound.getTag("itemsOUT"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("enabled", isEnabled);
        compound.setInteger("redstone", redstoneBehaviour);
        if (inputHandler != null) {
            if (inputHandler.serializeNBT() != null) {
                compound.setTag("itemsIN", inputHandler.serializeNBT());
            }
        }
        if (outputHandler != null) {
            if (outputHandler.serializeNBT() != null) {
                compound.setTag("itemsOUT", outputHandler.serializeNBT());
            }
        }
        return compound;
    }



}
