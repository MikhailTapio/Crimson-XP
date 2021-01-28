package crimsonfluff.crimsonxp.items;

import crimsonfluff.crimsonxp.CrimsonXP;
import crimsonfluff.crimsonxp.containers.CharmContainer;
import crimsonfluff.crimsonxp.init.itemsInit;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;

public class ItemXPCharm extends Item {
    public ItemXPCharm() { super(new Properties().group(ItemGroup.MISC).maxStackSize(1)); }
    private int isNBTAmount = 0;

    private final IIntArray bitsData = new IIntArray() {
        @Override
        public int get(int index) { return isNBTAmount; }

        @Override
        public void set(int index, int value) { isNBTAmount=value; }

        @Override
        public int size() { return 1; }
    };

    private int tick=0;

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent("tip." + CrimsonXP.MOD_ID + ".xp_charm").mergeStyle(TextFormatting.YELLOW));
        tooltip.add(new StringTextComponent("Range is " + CrimsonXP.CONFIGURATION.CharmRange.get() + " blocks").mergeStyle(TextFormatting.AQUA));

        if (stack.hasTag()) {
            int amount = stack.getTag().getInt("xp");
            if (amount > 0)
                tooltip.add(new StringTextComponent("XP Stored: " + getStringFromInt(amount)).mergeStyle(TextFormatting.GREEN));
        }

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    public String getStringFromInt(int number) { return new DecimalFormat("###,###,###,###,###").format(number); }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);

        if (playerIn.isCrouching()) {
            if(!worldIn.isRemote){
                NetworkHooks.openGui((ServerPlayerEntity)playerIn, new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return stack.hasDisplayName() ? stack.getDisplayName() : new TranslationTextComponent("container."+CrimsonXP.MOD_ID+".charm");
                    }

                    @Nullable
                    @Override
                    public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
                        if (stack.hasTag()) isNBTAmount = stack.getTag().getInt("xp");  // make sure TrackIntArray has latest value

                        return new CharmContainer(windowId, playerIn.inventory, bitsData);
                    }
                }, data -> data.writeInt(playerIn.inventory.currentItem));
            }

        } else {
            boolean active = !stack.getOrCreateTag().getBoolean("active");

            stack.getTag().putBoolean("active", active);
            playerIn.world.playSound(null, playerIn.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, (active) ? 0.9f : 0.1f);
        }

        return ActionResult.resultSuccess(stack);
    }

    @Override
    public boolean hasEffect(ItemStack stack) { return (stack.getOrCreateTag().getBoolean("active")); }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!worldIn.isRemote) {
            tick++;

            if (tick == 10) {
                tick = 0;
                //isNBTAmount = stack.getOrCreateTag().getInt("xp");// + AddAmount;

                if (stack.getOrCreateTag().getBoolean("active")) {
                    double x = entityIn.getPosX();
                    double y = entityIn.getPosY();
                    double z = entityIn.getPosZ();

                    PlayerEntity playerIn = (PlayerEntity) entityIn;
                    PlayerInventory inv = playerIn.inventory;

                    int r = (CrimsonXP.CONFIGURATION.CharmRange.get());
                    AxisAlignedBB area = new AxisAlignedBB(x - r, y - 3, z - r, x + r, y + 3, z + r);

                    int AddAmount = 0;

                // Handle XP_ITEMs
                    List<ItemEntity> items = worldIn.getEntitiesWithinAABB(EntityType.ITEM, area, item -> item.getItem().getItem() == itemsInit.XP_ITEM.get());
                    if (items.size() != 0) {
                        for (ItemEntity itemIE : items) {
                            if (CrimsonXP.CONFIGURATION.CharmShowParticles.get())
                                ((ServerWorld) worldIn).spawnParticle(ParticleTypes.POOF, itemIE.getPosX(), itemIE.getPosY(), itemIE.getPosZ(), 8, 0D, 0D, 0D, 0D);

                            AddAmount = AddAmount + itemIE.getItem().getCount();

                            itemIE.remove();
                        }

                        worldIn.playSound(null, x, y, z, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1.6f);
                    }

                // Handle XP_ORBs
                    List<ExperienceOrbEntity> orbs = worldIn.getEntitiesWithinAABB(ExperienceOrbEntity.class, area);
                    if (orbs.size() != 0) {
                        worldIn.playSound(null, x, y, z, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1f, 1f);

                        for (ExperienceOrbEntity orb : orbs) {
                            if (CrimsonXP.CONFIGURATION.CharmShowParticles.get())
                                ((ServerWorld) worldIn).spawnParticle(ParticleTypes.POOF, orb.getPosX(), orb.getPosY(), orb.getPosZ(), 8, 0D, 0D, 0D, 0D);

                            AddAmount = AddAmount + orb.getXpValue();

                            orb.remove();
                        }
                    }

                // Handle BOTTLE_O_ENCHANTINGs
                    if (CrimsonXP.CONFIGURATION.CharmBottlePickup.get()) {
                        items = worldIn.getEntitiesWithinAABB(EntityType.ITEM, area, item -> item.getItem().getItem() == Items.EXPERIENCE_BOTTLE);
                        if (items.size() != 0) {
                            for (ItemEntity itemIE : items) {
                                if (CrimsonXP.CONFIGURATION.CharmShowParticles.get())
                                    ((ServerWorld) worldIn).spawnParticle(ParticleTypes.POOF, itemIE.getPosX(), itemIE.getPosY(), itemIE.getPosZ(), 8, 0D, 0D, 0D, 0D);

                                // Crack open each Bottle and return 3-11 xp each
                                for (int a=0; a<itemIE.getItem().getCount(); a++) {
                                    int i = 3 + worldIn.rand.nextInt(5) + worldIn.rand.nextInt(5);  // from ExperienceBottle class
                                    AddAmount = AddAmount + i;
                                }

                                itemIE.remove();
                            }

                            worldIn.playSound(null, x, y, z, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1.6f);
                        }
                    }

                    if (AddAmount > 0) {
                        // Update Amulet NBT
                        //isNBTAmount += AddAmount;
                        isNBTAmount = stack.getOrCreateTag().getInt("xp") + AddAmount;

                        stack.getOrCreateTag().putInt("xp", isNBTAmount);
                        stack.setAnimationsToGo(5);
                    }
                }
            }
        }
    }
}
