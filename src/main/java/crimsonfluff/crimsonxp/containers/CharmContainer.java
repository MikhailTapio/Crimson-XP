package crimsonfluff.crimsonxp.containers;

import crimsonfluff.crimsonxp.init.containersInit;
import crimsonfluff.crimsonxp.init.itemsInit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;

public class CharmContainer extends Container {
    public final IInventory inventory;
    public final IIntArray array;

    public CharmContainer(int windowId, PlayerInventory playerInventory) {
        this(windowId, playerInventory, new IntArray(1));
    }

    public CharmContainer(int windowId, PlayerInventory playerInventory, IIntArray array) {
        super(containersInit.GENERIC_CHEST.get(), windowId);

        this.inventory = new Inventory(1);
        this.array = array;

        // TwitchBox Chest Inventory
        this.addSlot(new SlotXPOnly(this.inventory, 0, 8 , 20));


// TODO: Unless using a KeyBind the Item will ONLY ever be on the HotBar
//       So no need for SlotLocked in PlayerInventory
        // Player Inventory
        for (int chestRows = 0; chestRows < 3; chestRows++) {
            for (int chestCols = 0; chestCols < 9; chestCols++) {
                if (9 + (chestRows * 9) + chestCols == playerInventory.currentItem)
                    this.addSlot(new SlotLocked(playerInventory, chestCols, 8 + chestCols * 18, 112));
                else
                    this.addSlot(new Slot(playerInventory, 9 + (chestRows * 9) + chestCols, 8 + chestCols * 18, 54 + chestRows * 18));
            }
        }

        // Hotbar
        for (int chestCols = 0; chestCols < 9; chestCols++) {
            if (chestCols == playerInventory.currentItem)
                this.addSlot(new SlotLocked(playerInventory, chestCols, 8 + chestCols * 18, 112));
            else
                this.addSlot(new Slot(playerInventory, chestCols, 8 + chestCols * 18, 112));
        }

        this.trackIntArray(array);
        updateCoinage(this.array.get(0));
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) { return true; }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
//        if (clickTypeIn==ClickType.QUICK_MOVE) return ItemStack.EMPTY;  // NO else Shift Click will stop working !!!

        ItemStack itemstack1 = super.slotClick(slotId, dragType, clickTypeIn, player);

        if (clickTypeIn == ClickType.PICKUP) {
            if (slotId == 0) {
                int amount=0;

//                CrimsonXP.LOGGER.info("slotClick-> SLOT: " + slotId + ", Click: " + clickTypeIn + ", '"
//                        + itemstack1.toString() + "', '" + this.inventory.getStackInSlot(slotId).toString() + "'");

                if (itemstack1.isEmpty()) {
                    amount=this.inventory.getStackInSlot(0).getCount();
                    this.array.set(0, this.array.get(0) + amount);

                } else {
                    amount=this.inventory.getStackInSlot(0).getCount() - itemstack1.getCount();
                    this.array.set(0, this.array.get(0) + amount);
                }

//                CrimsonXP.LOGGER.info("slotClick-> " + amount);

                updateCoinage(this.array.get(0));
            }
        }

        return itemstack1;
    }

    private void updateCoinage(int bits) {
        this.inventory.removeStackFromSlot(0);

        if (bits>0)
            this.inventory.setInventorySlotContents(0, new ItemStack(itemsInit.XP_ITEM.get(), bits));
    }
}
