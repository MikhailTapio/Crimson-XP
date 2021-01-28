package crimsonfluff.crimsonxp;

import crimsonfluff.crimsonxp.containers.CharmScreen;
import crimsonfluff.crimsonxp.init.containersInit;
import crimsonfluff.crimsonxp.init.itemsInit;
import crimsonfluff.crimsonxp.util.ConfigBuilder;
import crimsonfluff.crimsonxp.util.Curios;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

@Mod(CrimsonXP.MOD_ID)
public class CrimsonXP {
    public static final String MOD_ID = "crimsonxp";
    public static final Logger LOGGER = LogManager.getLogger(CrimsonXP.MOD_ID);
    public static final ConfigBuilder CONFIGURATION = new ConfigBuilder();

    final IEventBus MOD_EVENTBUS = FMLJavaModLoadingContext.get().getModEventBus();

    public CrimsonXP() {
        MOD_EVENTBUS.addListener(this::enqueueIMC);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> MOD_EVENTBUS.addListener(this::doClientStuff));

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIGURATION.COMMON);
        itemsInit.ITEMS.register(MOD_EVENTBUS);
        containersInit.CONTAINERS.register(MOD_EVENTBUS);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @OnlyIn(Dist.CLIENT)
    public void doClientStuff(final FMLClientSetupEvent event) {
        ScreenManager.registerFactory(containersInit.GENERIC_CHEST.get(), CharmScreen::new);
    }

    @Mod.EventBusSubscriber(modid = CrimsonXP.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientProxy {
        @SubscribeEvent
        public static void stitchTextures(TextureStitchEvent.Pre event) {
            if (event.getMap().getTextureLocation().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE))
                event.addSprite(new ResourceLocation(CrimsonXP.MOD_ID, "item/empty_charm_slot"));
        }
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        if (CrimsonXP.CONFIGURATION.CharmCurios.get()) {
            if (Curios.isModLoaded()) {
                if (!SlotTypePreset.findPreset("charm2").isPresent()) {
                    InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE,
                        () -> new SlotTypeMessage
                            .Builder("charm2")
                            .icon(new ResourceLocation(CrimsonXP.MOD_ID, "item/empty_charm_slot"))
                            .size(1)
                            .build());
                }
            }
        }
    }
}