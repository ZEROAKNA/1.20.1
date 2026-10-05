package com.sevendeadlysins.client;

import com.sevendeadlysins.data.PlayerSinsData;
import com.sevendeadlysins.network.CastSinPayload;
import com.sevendeadlysins.network.ModNetwork;
import com.sevendeadlysins.registry.ModAttachmentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Экран Арсенала Зависти (EnvySelectionScreen) для Minecraft 1.20.1 (Forge 47.3.0).
 * Отображает похищенные способности из ваниллы, Ice and Fire: Dragons, L_Ender's Cataclysm,
 * Iron's Spells 'n Spellbooks, Simply Swords и других модов сборки Cisco's RPG [Dragonfyre].
 */
public class EnvySelectionScreen extends Screen {

    public EnvySelectionScreen() {
        super(Component.literal("✦ Арсенал Зависти — Способности [Cisco's Dragonfyre] ✦"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static String formatAbilityName(String rawId) {
        if (rawId == null || rawId.isEmpty()) return "Не выбрано";
        switch (rawId) {
            case "enderman_blink":
                return "Теневой Блинк Эндермена (22 бл. + % HP)";
            case "warden_sonic_boom":
                return "Звуковой Луч Вардена (55 + 8% Max HP)";
            case "creeper_blast_guard":
                return "Катаклизм Крипера (65 урона + Иммунитет)";
            case "dragonfyre:fire_dragon_breath":
                return "[Dragonfyre] Огненное Дыхание Дракона";
            case "dragonfyre:ice_dragon_breath":
                return "[Dragonfyre] Ледяное Дыхание Дракона";
            case "dragonfyre:lightning_dragon_breath":
                return "[Dragonfyre] Грозовое Дыхание Дракона";
            case "dragonfyre:gorgon_petrify_gaze":
                return "[Ice & Fire] Окаменяющий Взгляд Горгоны";
            case "cataclysm:ignis_abyssal_burn":
                return "[Cataclysm] Пепельный Катаклизм Игниса";
            case "cataclysm:maledictus_phantom_halberd":
                return "[Cataclysm] Призрачные Алебарды Маледиктуса";
            case "cataclysm:harbinger_death_laser":
                return "[Cataclysm] Аннигиляционный Луч Предвестника";
            case "cataclysm:leviathan_abyss_blast":
                return "[Cataclysm] Разлом Бездны Левиафана";
            default:
                if (rawId.startsWith("irons_spell:")) {
                    return "[Iron's Spells] Магия: " + rawId.substring("irons_spell:".length());
                } else if (rawId.startsWith("cataclysm:")) {
                    return "[Cataclysm] Босс: " + rawId.substring("cataclysm:".length());
                } else if (rawId.startsWith("dragonfyre:")) {
                    return "[Dragonfyre] Дракон: " + rawId.substring("dragonfyre:".length());
                } else if (rawId.startsWith("tensura_racial:")) {
                    return "[Tensura] Навык: " + rawId.substring("tensura_racial:".length());
                } else if (rawId.startsWith("mod_weapon:")) {
                    return "[Оружие RPG] " + rawId.substring("mod_weapon:".length());
                } else if (rawId.startsWith("mod_skill:")) {
                    return "[Мод RPG] " + rawId.substring("mod_skill:".length());
                }
                return rawId;
        }
    }

    @Override
    protected void init() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        PlayerSinsData data = ModAttachmentTypes.get(mc.player);
        List<String> abilities = data.getStolenAbilities();
        String activeId = data.getStolenAbilityId();

        int startX = this.width / 2 - 175;
        int startY = 58;

        for (int i = 0; i < abilities.size(); i++) {
            final int index = i;
            String id = abilities.get(i);
            boolean isCurrent = id.equals(activeId);
            String label = (isCurrent ? "§a● " : "✦ ") + formatAbilityName(id);
            if (label.length() > 30) {
                label = label.substring(0, 29) + "…";
            }

            int col = i % 2;
            int row = i / 2;
            int btnX = startX + col * 180;
            int btnY = startY + row * 24;

            this.addRenderableWidget(Button.builder(
                    Component.literal(label),
                    btn -> {
                        ModNetwork.sendToServer(new CastSinPayload(3, 200 + index));
                        this.onClose();
                    }
            ).bounds(btnX, btnY, 170, 20).build());
        }

        this.addRenderableWidget(Button.builder(
                Component.literal("Закрыть Арсенал Зависти [ESC]"),
                btn -> this.onClose()
        ).bounds(this.width / 2 - 90, this.height - 32, 180, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xB004120C);

        int panelW = 384;
        int panelX = this.width / 2 - panelW / 2;
        int panelY = 14;
        int panelH = this.height - 28;

        guiGraphics.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + panelH + 2, 0xFF10B981);
        guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xF00B0F17);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 22, 0xFF34D399);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            PlayerSinsData data = ModAttachmentTypes.get(mc.player);
            String activeText = "Активная способность: " + formatAbilityName(data.getStolenAbilityId());
            guiGraphics.drawCenteredString(this.font, activeText, this.width / 2, 36, 0xFFA7F3D0);

            if (data.getStolenAbilities().isEmpty()) {
                guiGraphics.drawCenteredString(
                        this.font,
                        "Арсенал пуст. В Режиме 1 наведите прицел на Дракона, Босса Cataclysm или моба и нажмите [V]!",
                        this.width / 2,
                        this.height / 2,
                        0xFF94A3B8
                );
            }
        }
    }
}
