package crimsonfluff.crimsonxp;

import crimsonfluff.crimsonxp.init.itemsInit;
import crimsonfluff.crimsonxp.util.ConfigBuilder;
import crimsonfluff.crimsonxp.util.Curios;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
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

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIGURATION.COMMON);
        itemsInit.ITEMS.register(MOD_EVENTBUS);

        MinecraftForge.EVENT_BUS.register(this);
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

    @SubscribeEvent
    public void onXpPickupEvent(PlayerXpEvent.PickupXp event) {
        ExperienceOrbEntity xpOrb = event.getOrb();

    // Check ALL inventory + Curios to see if wearing XP_CHARM
    // if TRUE then add xp into Charm
        ItemStack CHARM = ItemStack.EMPTY;
        if (Curios.isModLoaded()) CHARM = Curios.findItem(itemsInit.XP_CHARM_ITEM.get(), event.getPlayer());

        if (CHARM == ItemStack.EMPTY) {
            for(int a=0; a<event.getPlayer().inventory.getSizeInventory(); a++) {
                CHARM = event.getPlayer().inventory.getStackInSlot(a);
                if (CHARM.getItem() == itemsInit.XP_CHARM_ITEM.get()) break;
            }
        }

        if (CHARM.getItem() == itemsInit.XP_CHARM_ITEM.get()) {
            if (CHARM.getOrCreateTag().getBoolean("active")) {
                CHARM.getOrCreateTag().putInt("xp", CHARM.getOrCreateTag().getInt("xp") + xpOrb.getXpValue());
                event.getPlayer().world.playSound(null, event.getPlayer().getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1f, 1f);

                xpOrb.remove();
                event.setCanceled(true);

                //CrimsonXP.LOGGER.info("PICKUP: FOUND CHARM");
            }
        }
    }
}
