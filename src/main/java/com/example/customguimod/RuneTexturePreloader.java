package com.example.customguimod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.Set;

@SideOnly(Side.CLIENT)
final class RuneTexturePreloader implements IResourceManagerReloadListener {

    static void register() {
        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        if (resourceManager instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager) resourceManager).registerReloadListener(new RuneTexturePreloader());
        } else {
            new RuneTexturePreloader().onResourceManagerReload(resourceManager);
        }
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        Minecraft mc = Minecraft.getMinecraft();
        Set<String> loaded = new HashSet<>();

        for (int i = 0; i < EarthRuneCatalog.size(); i++) {
            EarthRuneCatalog.Definition definition = EarthRuneCatalog.at(i);
            if (definition == null || !loaded.add(definition.iconTexture)) continue;

            mc.getTextureManager().bindTexture(new ResourceLocation(definition.iconTexture));
        }

        CustomGuiMod.logger.info("Preloaded " + loaded.size() + " rune textures");
    }
}
