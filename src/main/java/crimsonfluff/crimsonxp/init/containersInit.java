package crimsonfluff.crimsonxp.init;

import crimsonfluff.crimsonxp.CrimsonXP;
import crimsonfluff.crimsonxp.containers.CharmContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class containersInit {
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, CrimsonXP.MOD_ID);

    public static final RegistryObject<ContainerType<CharmContainer>> GENERIC_CHEST = CONTAINERS.register("generic_chest",
            () -> new ContainerType<>(CharmContainer::new));
}
