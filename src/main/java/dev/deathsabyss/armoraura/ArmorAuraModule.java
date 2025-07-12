package dev.deathsabyss.armoraura;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.ModuleCategory;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class ArmorAuraModule extends Module {
    public ArmorAuraModule() {
        super(ModuleCategory.RENDER, "armor-aura", "Displays 3D armor and durability above your player.");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player == null) return;

        // Start rendering above the player's head
        double yBase = player.getY() + player.getStandingEyeHeight() + 0.6;
        double x = player.getX();
        double z = player.getZ();

        int slotNum = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack stack = player.getEquippedStack(slot);
                if (!stack.isEmpty()) {
                    double yOffset = yBase + slotNum * 0.35;

                    // Render the armor as a floating item above the player
                    renderArmorItem(stack, x, yOffset, z, event);

                    // Render durability bar below the item
                    renderDurabilityBar(stack, x, yOffset - 0.25, z, event);

                    slotNum++;
                }
            }
        }
    }

    private void renderArmorItem(ItemStack stack, double x, double y, double z, Render3DEvent event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ItemRenderer renderer = mc.getItemRenderer();

        // Push matrix for positioning
        event.matrices.push();
        event.matrices.translate(x - mc.getCameraEntity().getX(), y - mc.getCameraEntity().getY(), z - mc.getCameraEntity().getZ());
        event.matrices.scale(1.0f, 1.0f, 1.0f);
        // Rotate to always face the camera
        event.matrices.multiply(mc.gameRenderer.getCamera().getRotation());

        renderer.renderItem(stack, net.minecraft.client.render.model.json.ModelTransformationMode.GROUND, 15728880, OverlayTexture.DEFAULT_UV, event.matrices, event.vertexConsumers, null, 0);
        event.matrices.pop();
    }

    private void renderDurabilityBar(ItemStack stack, double x, double y, double z, Render3DEvent event) {
        if (!stack.isDamageable()) return;
        int max = stack.getMaxDamage();
        int cur = max - stack.getDamage();
        float percent = max == 0 ? 1.0f : (float) cur / max;

        float width = 0.6f;
        float height = 0.08f;

        float red = 1.0f - percent;
        float green = percent;
        float blue = 0.0f;

        Matrix4f matrix = event.matrices.peek().getPositionMatrix();

        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        // Background (gray)
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, (float) x - width/2, (float) y, (float) z).color(0.2f, 0.2f, 0.2f, 1.0f).next();
        buffer.vertex(matrix, (float) x + width/2, (float) y, (float) z).color(0.2f, 0.2f, 0.2f, 1.0f).next();
        buffer.vertex(matrix, (float) x + width/2, (float) y + height, (float) z).color(0.2f, 0.2f, 0.2f, 1.0f).next();
        buffer.vertex(matrix, (float) x - width/2, (float) y + height, (float) z).color(0.2f, 0.2f, 0.2f, 1.0f).next();
        Tessellator.getInstance().draw();

        // Foreground (colored)
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, (float) x - width/2, (float) y, (float) z).color(red, green, blue, 1.0f).next();
        buffer.vertex(matrix, (float) x - width/2 + width * percent, (float) y, (float) z).color(red, green, blue, 1.0f).next();
        buffer.vertex(matrix, (float) x - width/2 + width * percent, (float) y + height, (float) z).color(red, green, blue, 1.0f).next();
        buffer.vertex(matrix, (float) x - width/2, (float) y + height, (float) z).color(red, green, blue, 1.0f).next();
        Tessellator.getInstance().draw();

        RenderSystem.enableTexture();
    }
}