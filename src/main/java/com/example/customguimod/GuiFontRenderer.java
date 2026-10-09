package com.example.customguimod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/** Screen-local smooth type; Minecraft's HUD and resource-pack fonts are untouched. */
final class GuiFontRenderer extends FontRenderer {
    private static final int CELL = 48;
    private static final int ATLAS = 768;
    private static final float SCALE = 1.0F / 3.0F;
    private static GuiFontRenderer instance;
    private final int[] glyphs = new int[65536];
    private final float[] advances = new float[65536];
    private final ResourceLocation texture;

    static FontRenderer get(Minecraft mc) {
        if (instance == null) instance = new GuiFontRenderer(mc);
        return instance;
    }

    private GuiFontRenderer(Minecraft mc) {
        super(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.getTextureManager(), false);
        FONT_HEIGHT = 12;
        Arrays.fill(glyphs, -1);
        BufferedImage image = new BufferedImage(ATLAS, ATLAS, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
        graphics.setColor(java.awt.Color.WHITE);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        FontMetrics metrics = graphics.getFontMetrics();
        StringBuilder characters = new StringBuilder();
        for (char c = 32; c <= 126; c++) characters.append(c);
        for (char c = '\u0400'; c <= '\u045f'; c++) characters.append(c);
        characters.append("–—№•");
        for (int i = 0; i < characters.length(); i++) {
            char c = characters.charAt(i);
            glyphs[c] = i;
            advances[c] = (float) metrics.getStringBounds(String.valueOf(c), graphics).getWidth() * SCALE;
            graphics.drawString(String.valueOf(c), i % 16 * CELL + 2, i / 16 * CELL + metrics.getAscent());
        }
        graphics.dispose();
        texture = mc.getTextureManager().getDynamicTextureLocation("equipment_gui_font", new DynamicTexture(image));
    }

    private float advance(char c) {
        return advances[glyphs[c] < 0 ? '?' : c];
    }

    @Override
    public int getCharWidth(char c) {
        return c == '\u00a7' ? -1 : Math.round(advance(c));
    }

    @Override
    public int getStringWidth(String text) {
        if (text == null) return 0;
        float width = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\u00a7' && i + 1 < text.length()) { i++; continue; }
            width += advance(text.charAt(i));
        }
        return Math.round(width);
    }

    @Override
    public String trimStringToWidth(String text, int width, boolean reverse) {
        if (reverse) {
            int start = text.length();
            while (start > 0 && getStringWidth(text.substring(start - 1)) <= width) start--;
            return text.substring(start);
        }
        float used = 0;
        int end = 0;
        while (end < text.length()) {
            if (text.charAt(end) == '\u00a7' && end + 1 < text.length()) { end += 2; continue; }
            float next = advance(text.charAt(end));
            if (used + next > width) break;
            used += next;
            end++;
        }
        return text.substring(0, end);
    }

    @Override
    public int drawStringWithShadow(String text, float x, float y, int color) {
        return drawString(text, x, y, color, true);
    }

    @Override
    public int drawString(String text, int x, int y, int color) {
        return drawString(text, (float) x, (float) y, color, false);
    }

    @Override
    public int drawString(String text, float x, float y, int color, boolean shadow) {
        if (text == null || text.isEmpty()) return (int) x;
        // Keep the reference's clean, light strokes instead of a heavy pixel shadow.
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1, 1, 1, 1);
        int alpha = color >>> 24;
        if (alpha == 0) alpha = 255;
        int rgb = color;
        float cursor = x;
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\u00a7' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(++i));
                if ("0123456789abcdef".indexOf(code) >= 0) rgb = getColorCode(code);
                else if (code == 'r') rgb = color;
                continue;
            }
            int glyph = glyphs[c] < 0 ? glyphs['?'] : glyphs[c];
            float u = glyph % 16 * CELL / (float) ATLAS;
            float v = glyph / 16 * CELL / (float) ATLAS;
            float uv = CELL / (float) ATLAS;
            float size = CELL * SCALE;
            int r = rgb >> 16 & 255, g = rgb >> 8 & 255, b = rgb & 255;
            buffer.pos(cursor, y + size, 0).tex(u, v + uv).color(r, g, b, alpha).endVertex();
            buffer.pos(cursor + size, y + size, 0).tex(u + uv, v + uv).color(r, g, b, alpha).endVertex();
            buffer.pos(cursor + size, y, 0).tex(u + uv, v).color(r, g, b, alpha).endVertex();
            buffer.pos(cursor, y, 0).tex(u, v).color(r, g, b, alpha).endVertex();
            cursor += advance(c);
        }
        Tessellator.getInstance().draw();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
        return Math.round(cursor);
    }
}
