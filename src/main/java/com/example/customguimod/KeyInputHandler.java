package com.example.customguimod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyInputHandler {

    public static KeyBinding keyOpenCards;

    private boolean wasPressed = false;

    public static void register() {
        keyOpenCards = new KeyBinding("key.customguimod.cards", Keyboard.KEY_C, "key.categories.customguimod");
        ClientRegistry.registerKeyBinding(keyOpenCards);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        boolean isPressed = keyOpenCards.isPressed();

        if (isPressed && !wasPressed) {
            Minecraft.getMinecraft().displayGuiScreen(new CardsGuiScreen());
            CustomGuiMod.logger.info("Открываю CardsGuiScreen");
        }

        wasPressed = isPressed;
    }
}