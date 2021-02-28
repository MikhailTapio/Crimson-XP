package crimsonfluff.crimsonxp.items;

import crimsonfluff.crimsonxp.CrimsonXP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.InputMappings;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ItemXPCharm extends Item {
    public ItemXPCharm() { super(new Properties().group(ItemGroup.MISC).maxStackSize(1)); }
    private int tick=0;

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        long WINDOW = Minecraft.getInstance().getMainWindow().getHandle();

        if (InputMappings.isKeyDown(WINDOW, GLFW.GLFW_KEY_LEFT_SHIFT) || InputMappings.isKeyDown(WINDOW, GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            tooltip.add(new TranslationTextComponent("tip1." + CrimsonXP.MOD_ID + ".xp_charm").mergeStyle(TextFormatting.YELLOW));
            tooltip.add(new TranslationTextComponent("tip2." + CrimsonXP.MOD_ID + ".xp_charm").mergeStyle(TextFormatting.YELLOW));

        } else {
            tooltip.add(new TranslationTextComponent("tip." + CrimsonXP.MOD_ID + ".xp_charm").mergeStyle(TextFormatting.YELLOW));
            tooltip.add(new TranslationTextComponent("shift." + CrimsonXP.MOD_ID + ".xp_charm").mergeStyle(TextFormatting.YELLOW));
        }

        if (stack.hasTag()) {
            int amount = stack.getTag().getInt("xp");
            if (amount > 0) {
                if (InputMappings.isKeyDown(WINDOW, GLFW.GLFW_KEY_LEFT_SHIFT) || InputMappings.isKeyDown(WINDOW, GLFW.GLFW_KEY_RIGHT_SHIFT))
                    tooltip.add(new StringTextComponent("XP Stored: " + new DecimalFormat("###,###,###,###").format(amount)).mergeStyle(TextFormatting.GREEN));
                else
                    tooltip.add(new StringTextComponent("XP Stored: " + getStringFromInt(amount)).mergeStyle(TextFormatting.GREEN));
            }
        }

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    public String getStringFromInt(int number) {
        DecimalFormat decimalFormat = new DecimalFormat("0.#");

        if (number >= 1000000000) return decimalFormat.format(number / 1000000000f) + "b";
        if (number >= 1000000) return decimalFormat.format(number / 1000000f) + "m";
        if (number >= 100000) return decimalFormat.format(number / 1000f) + "k";

        return new DecimalFormat("###,###").format(number);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);

        if (!worldIn.isRemote) {
            if (playerIn.isCrouching()) {
                int isAmount = stack.getOrCreateTag().getInt("xp");

//                CrimsonXP.LOGGER.info("playerIn.experience = " + playerIn.experience);
//                CrimsonXP.LOGGER.info("playerIn.experienceTotal = " + playerIn.experienceTotal);
//                CrimsonXP.LOGGER.info("playerIn.experienceLevel = " + playerIn.experienceLevel);

                // NOTE: Don't cheat just levels because experienceTotal will be 0
                // so charm wont pull any xp from player
                if (isAmount == 0) {
                    stack.getOrCreateTag().putInt("xp", playerIn.experienceTotal);
                    playerIn.experience=0;
                    playerIn.experienceTotal=0;
                    playerIn.experienceLevel=0;

                } else {
                    int isTake = Integer.min(isAmount, 64);
                    worldIn.playSound(null, playerIn.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1f, 1f);
                    stack.getOrCreateTag().putInt("xp", isAmount - isTake);

                    ArrayList<ItemStack> MendingItems = new ArrayList<>();
                    ItemStack stacks;

                // NOTE: Borrowed from Magnet XP Collection re-write
                // getRandomEquippedWithEnchantment only works with offhand, main hand, armor slots
                // so make a list of valid items then randomly choose one to repair
                // Also vanilla way would call onXpPickupEvent thing, which we DON'T want because we handle that too !!!
                    for (int a=36; a<41; a++) {
                        stacks = playerIn.inventory.getStackInSlot(a);
                        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stacks) > 0)
                            if (stacks.isDamaged()) MendingItems.add(stacks);
                    }

                // Choose random item from MendingItems list
                    if (MendingItems.size() > 0) {
                        int r = worldIn.rand.nextInt(MendingItems.size());
                        stacks = MendingItems.get(r);

                        int i = Math.min((int)(isTake * stacks.getXpRepairRatio()), stacks.getDamage());
                        isTake -= i/2;     //orb.durabilityToXp(i);
                        stacks.setDamage(stacks.getDamage() - i);

                        if (stacks.getDamage() == 0) MendingItems.remove(r);
                    }

                    if (isTake > 0) playerIn.giveExperiencePoints(isTake);
                }

            } else {
                boolean active = !stack.getOrCreateTag().getBoolean("active");
                stack.getTag().putBoolean("active", active);

                playerIn.world.playSound(null, playerIn.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, (active) ? 0.9f : 0.1f);
                playerIn.sendStatusMessage(new StringTextComponent("\u00A7bXP Charm is now " + (active ? "\u00A72Active" : "\u00A74Inactive")), true);
            }
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

                if (stack.getOrCreateTag().getBoolean("active")) {
                    double x = entityIn.getPosX();
                    double y = entityIn.getPosY();
                    double z = entityIn.getPosZ();
                    int AddAmount = 0;

                    int r = (CrimsonXP.CONFIGURATION.CharmRange.get());
                    AxisAlignedBB area = new AxisAlignedBB(x - r, y - 3, z - r, x + r, y + 3, z + r);

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
                        List<ItemEntity>  items = worldIn.getEntitiesWithinAABB(EntityType.ITEM, area, item -> item.getItem().getItem() == Items.EXPERIENCE_BOTTLE);
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
                        stack.getOrCreateTag().putInt("xp", stack.getOrCreateTag().getInt("xp") + AddAmount);
                        stack.setAnimationsToGo(5);
                    }
                }
            }
        }
    }
}
