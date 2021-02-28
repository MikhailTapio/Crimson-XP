package crimsonfluff.crimsonxp.init;

import crimsonfluff.crimsonxp.CrimsonXP;
import crimsonfluff.crimsonxp.items.ItemXPCharm;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class itemsInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CrimsonXP.MOD_ID);

    public static final RegistryObject<Item> XP_CHARM_ITEM = ITEMS.register("xp_charm", ItemXPCharm::new);
}
