package crimsonfluff.crimsonxp.containers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import crimsonfluff.crimsonxp.CrimsonXP;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.text.DecimalFormat;

public class CharmScreen extends ContainerScreen<CharmContainer> {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(CrimsonXP.MOD_ID, "textures/gui/amulet.png");

    public CharmScreen(CharmContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.ySize = 136;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        this.font.func_243248_b(matrixStack, this.title, 8, 6.0F, 4210752);

        this.font.func_243248_b(matrixStack, this.playerInventory.getDisplayName(), 8, (float) (this.ySize - 93), 4210752);

        String s="XP Stored: " + getStringFromInt(this.container.array.get(0));
        this.font.func_243248_b(matrixStack, new StringTextComponent(s), 30, 15+9, 6569383);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        blit(matrixStack, x, y, 0, 0, this.xSize, this.ySize, 256, 256);
    }

    public String getStringFromInt(int number) { return new DecimalFormat("###,###,###,###,###").format(number); }
}
