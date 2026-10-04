package com.example.customguimod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class HealthBarRenderer {

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Post event) {
        EntityLivingBase entity = event.getEntity();

        if (!(entity instanceof EntityLiving)) return;
        if (entity == Minecraft.getMinecraft().player) return;
        if (!entity.hasCustomName()) return;

        // Только зомби и скелеты
        if (!(entity instanceof EntityZombie) && !(entity instanceof EntitySkeleton)) return;

        String name = entity.getCustomNameTag();

        // Имя должно начинаться с §c
        if (!name.startsWith("§c")) return;

        // Имя не должно содержать скобки
        if (name.contains("[")) return;

        // Не рисуем далёких мобов
        if (entity.getDistance(Minecraft.getMinecraft().player) > 32.0F) return;

        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        if (maxHealth <= 0) return;
        if (health <= 0) return;

        float ratio = health / maxHealth;

        double x = event.getX();
        double y = event.getY() + entity.height + 0.5;
        double z = event.getZ();

        // === СОХРАНЯЕМ ВСЁ СОСТОЯНИЕ ===
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);

        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int barWidth = 60;
        int barHeight = 6;
        int halfWidth = barWidth / 2;

        drawRect(-halfWidth - 1, -1, halfWidth + 1, barHeight + 1, 0xFF000000);
        drawRect(-halfWidth, 0, halfWidth, barHeight, 0xFF333333);

        int fillWidth = (int) (barWidth * ratio);
        int color;
        if (ratio > 0.6f) color = 0xFF00FF00;
        else if (ratio > 0.3f) color = 0xFFFFFF00;
        else color = 0xFFFF0000;
        drawRect(-halfWidth, 0, -halfWidth + fillWidth, barHeight, color);

        // Восстанавливаем до отрисовки текста
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();

        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        String text = (int) health + " / " + (int) maxHealth;
        int textWidth = font.getStringWidth(text);

        GlStateManager.pushMatrix();
        float scale = 0.6f;
        GlStateManager.scale(scale, scale, scale);
        font.drawStringWithShadow(text, -(textWidth / 2), 1, 0xFFFFFF);
        GlStateManager.popMatrix();

        // === ВОССТАНАВЛИВАЕМ ВСЁ ===
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.color(r, g, b, a);
        buffer.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION);
        buffer.pos(left, bottom, 0.0D).endVertex();
        buffer.pos(right, bottom, 0.0D).endVertex();
        buffer.pos(right, top, 0.0D).endVertex();
        buffer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
    }
}