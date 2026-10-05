package com.sevendeadlysins.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sevendeadlysins.SevenDeadlySinsMod;
import com.sevendeadlysins.data.PlayerSinsData;
import com.sevendeadlysins.network.CastSinPayload;
import com.sevendeadlysins.network.ModNetwork;
import com.sevendeadlysins.registry.ModAttachmentTypes;
import com.sevendeadlysins.registry.ModEntities;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Регистрация клавиш управления (R, V, X), клиентского рендерера SoulOrbRenderer и HUD-оверлея
 * для Minecraft 1.20.1 (Forge 47.3.0).
 *
 * ОПТИМИЗАЦИЯ ДЛЯ CISCO'S RPG [DRAGONFYRE]:
 * Все клавиши используют KeyConflictContext.IN_GAME, чтобы не конфликтовать с поиском в JEI,
 * деревом пассивных навыков (Passive Skill Tree) и столом заклинаний Iron's Spells.
 */
public class ModKeyBindings {
    public static final String CATEGORY = "key.categories.sevendeadlysins";

    public static final KeyMapping RADIAL_MENU_KEY = new KeyMapping(
            "key.sevendeadlysins.radial_menu",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );

    public static final KeyMapping CAST_SIN_KEY = new KeyMapping(
            "key.sevendeadlysins.cast_sin",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );

    public static final KeyMapping SWITCH_MODE_KEY = new KeyMapping(
            "key.sevendeadlysins.switch_mode",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            CATEGORY
    );

    @Mod.EventBusSubscriber(modid = SevenDeadlySinsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(RADIAL_MENU_KEY);
            event.register(CAST_SIN_KEY);
            event.register(SWITCH_MODE_KEY);
        }

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.SOUL_ORB.get(), SoulOrbRenderer::new);
        }

        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAbove(
                    VanillaGuiOverlay.HOTBAR.id(),
                    "sins_hud",
                    new SinsHudOverlay()
            );
        }
    }

    @Mod.EventBusSubscriber(modid = SevenDeadlySinsMod.MODID, value = Dist.CLIENT)
    public static class ClientForgeBusEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            PlayerSinsData data = ModAttachmentTypes.get(mc.player);
            if (!data.isUnlocked()) return;

            while (RADIAL_MENU_KEY.consumeClick()) {
                if (mc.screen == null) {
                    mc.setScreen(new RadialMenuScreen(data.getActiveSinIndex()));
                }
            }

            while (SWITCH_MODE_KEY.consumeClick()) {
                ModNetwork.sendToServer(new CastSinPayload(data.getActiveSinIndex(), -1));
            }

            while (CAST_SIN_KEY.consumeClick()) {
                if (mc.player.isShiftKeyDown()) {
                    ModNetwork.sendToServer(new CastSinPayload(data.getActiveSinIndex(), -2));
                } else if (data.getActiveSinIndex() == 1 && data.getActiveSubMode() == 1) {
                    mc.setScreen(new GreedCatalogScreen());
                } else if (data.getActiveSinIndex() == 3 && data.getActiveSubMode() == 2) {
                    mc.setScreen(new EnvySelectionScreen());
                } else {
                    ModNetwork.sendToServer(new CastSinPayload(data.getActiveSinIndex(), data.getActiveSubMode()));
                }
            }
        }
    }
}
