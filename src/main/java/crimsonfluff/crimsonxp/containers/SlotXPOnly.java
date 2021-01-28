package crimsonfluff.crimsonxp.containers;

import crimsonfluff.crimsonxp.CrimsonXP;
import crimsonfluff.crimsonxp.init.itemsInit;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class SlotXPOnly extends Slot {
    public SlotXPOnly(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    public boolean isItemValid(ItemStack stack) {
        if (CrimsonXP.CONFIGURATION.CharmBottlePickup.get() && stack.getItem() == Items.EXPERIENCE_BOTTLE) return true;

        return stack.getItem() == itemsInit.XP_ITEM.get();
    }
}
