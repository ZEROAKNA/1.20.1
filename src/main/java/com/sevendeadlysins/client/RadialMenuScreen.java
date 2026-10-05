package com.sevendeadlysins.client;

import com.sevendeadlysins.data.PlayerSinsData;
import com.sevendeadlysins.network.CastSinPayload;
import com.sevendeadlysins.network.ModNetwork;
import com.sevendeadlysins.network.SelectSinPayload;
import com.sevendeadlysins.registry.ModAttachmentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

/**
 * Радиальное меню выбора Смертного Греха (R) для Minecraft 1.20.1 (Forge 47.3.0).
 * Адаптировано под Cisco's Fantasy Medieval RPG [Dragonfyre].
 */
public class RadialMenuScreen extends Screen {
    private static final String[] LABELS = {
            "Гордыня", "Алчность", "Похоть", "Зависть", "Чревоугодие", "Гнев", "Лень"
    };

    private static final String[] TITLES = {
            "I. ГОРДЫНЯ (PRIDE) — СОЛНЕЧНЫЙ МОНАРХ [DRAGONFYRE]",
            "II. АЛЧНОСТЬ (GREED) — КОРОЛЬ СОКРОВИЩ APOTHEOSIS",
            "III. ПОХОТЬ (LUST) — ВЛАДЫКА РАЗУМА И СТАИ",
            "IV. ЗАВИСТЬ (ENVY) — ПОХИТИТЕЛЬ ДРАКОНОВ И БОССОВ",
            "V. ЧРЕВОУГОДИЕ (GLUTTONY) — ПОЖИРАТЕЛЬ ДУШ DRAGONFYRE",
            "VI. ГНЕВ (WRATH) — ПРОБОЙ БРОНИ И КРИТ APOTHEOSIS",
            "VII. ЛЕНЬ (SLOTH) — ГИБЕРНАЦИЯ И 5x ПРОБУЖДЕНИЕ"
    };

    private static final String[] ACTIVE_DESC = {
            "[V] Режим 1: Остановка Времени (56 бл.) | Режим 2: Гравитация (% Max HP) | Режим 3: Солнце",
            "[V] Режим 1: Копия БЛОКА в инвентарь + Скан Apotheosis | Режим 2: Каталог",
            "[V] Приручение цели и мобов (8 бл.) как верной СОБАКИ: ТП и защита хозяина",
            "[V] Режим 1: Кража (Ice&Fire, Cataclysm, Iron's Spells) | Режим 2: Каст | Режим 3: Арсенал",
            "[V] Воронка Бездны (14 бл.): стягивает врагов, % HP урон, +Мана и сброс КД Iron's Spells",
            "[V] Катаклизм Гнева (12 бл.): мгновенно +35% стаков, взрыв % HP и бафф Crit/Armor Shred",
            "[V] Гибернация Короля: 100% HP, снятие проклятий, 20 золотых сердец и 5.0x урон"
    };

    private static final String[] PASSIVE_DESC = {
            "Пассивно: Остановка времени замораживает всех мобов и снаряды в воздухе",
            "Пассивно: Сохраняет 100% мифических аффиксов Apotheosis, сокетов и самоцветов",
            "Пассивно: Приручённые мобы лечатся и атакуют всех врагов хозяина",
            "Пассивно: Крадёт дыхание драконов Ice & Fire, навыки Cataclysm и снимает баффы L2Hostility",
            "Пассивно: Сферы душ боссов дают +50 макс. маны, +1 Ранг Dragonfyre и сброс КД магии",
            "Пассивно: Увеличивает Spell Power (Iron's Spells), Crit Chance и Armor Shred (Apotheosis)",
            "Пассивно: Стоя на месте +22 маны/сек и +5 HP/сек; после сна 5x урон"
    };

    private static final String[] MANA_COSTS = {
            "Мана: 35 ед.",
            "Мана: 10 / 30 ед.",
            "Мана: 25 ед.",
            "Мана: 20 / 24 ед.",
            "Мана: 20 ед.",
            "Ярость: +35% стаков",
            "Кулдаун: 10с -> 5x Бафф"
    };

    private static final int[] SIN_COLORS = {
            0xFFF59E0B,
            0xFFEAB308,
            0xFFEC4899,
            0xFF10B981,
            0xFF06B6D4,
            0xFFEF4444,
            0xFF38BDF8
    };

    private int hoveredSin = -1;
    private final int currentSin;

    public RadialMenuScreen(int currentSin) {
        super(Component.literal("Выбор Смертного Греха [Dragonfyre 1.20.1]"));
        this.currentSin = currentSin;
        this.hoveredSin = currentSin;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x88050811);

        int centerX = this.width / 2;
        int centerY = this.height / 2 - 12;
        int radius = 104;

        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > 34 && dist < 165) {
            double angle = Math.atan2(dy, dx) + Math.PI / 2.0D;
            if (angle < 0) angle += Math.PI * 2.0D;
            int sector = (int) Math.round((angle / (Math.PI * 2.0D)) * 7.0D) % 7;
            this.hoveredSin = sector;
        }

        int displaySin = (this.hoveredSin >= 0 && this.hoveredSin < 7) ? this.hoveredSin : this.currentSin;
        int accentColor = SIN_COLORS[displaySin];

        guiGraphics.drawCenteredString(this.font, "§6§l✦ КОЛЕСО СЕМИ СМЕРТНЫХ ГРЕХОВ [DRAGONFYRE 1.20.1] ✦", centerX, 14, 0xFFFBBF24);
        guiGraphics.drawCenteredString(this.font, "Наведите курсор или нажмите [1 - 7] • [ЛКМ / Отпустить R] — выбрать • [ПКМ / X] — сменить режим", centerX, 26, 0xFF94A3B8);

        for (int i = 0; i < 7; i++) {
            double theta = (2.0D * Math.PI * i / 7.0D) - (Math.PI / 2.0D);
            int nodeX = centerX + (int) (Math.cos(theta) * radius);
            int nodeY = centerY + (int) (Math.sin(theta) * radius);

            boolean isHovered = (i == displaySin);
            boolean isEquipped = (i == this.currentSin);
            int nodeColor = SIN_COLORS[i];

            int boxW = isHovered ? 56 : 50;
            int boxH = isHovered ? 16 : 14;

            int borderColor = isHovered ? nodeColor : (isEquipped ? 0xFF64748B : 0xFF1E293B);
            guiGraphics.fill(nodeX - boxW - 2, nodeY - boxH - 2, nodeX + boxW + 2, nodeY + boxH + 2, borderColor);

            int bgColor = isHovered ? 0xFF1E293B : 0xEB0B0F17;
            guiGraphics.fill(nodeX - boxW, nodeY - boxH, nodeX + boxW, nodeY + boxH, bgColor);

            guiGraphics.fill(nodeX - boxW, nodeY - boxH, nodeX - boxW + 4, nodeY + boxH, nodeColor);

            String title = "[" + (i + 1) + "] " + LABELS[i];
            guiGraphics.drawCenteredString(this.font, title, nodeX + 2, nodeY - 4, isHovered ? 0xFFFFFFFF : 0xFFCBD5E1);

            if (isEquipped) {
                guiGraphics.drawCenteredString(this.font, "§a● Активен", nodeX + 2, nodeY + 6, 0xFF4ADE80);
            }
        }

        guiGraphics.fill(centerX - 52, centerY - 22, centerX + 52, centerY + 22, accentColor);
        guiGraphics.fill(centerX - 50, centerY - 20, centerX + 50, centerY + 20, 0xFF0B0F17);
        guiGraphics.drawCenteredString(this.font, LABELS[displaySin].toUpperCase(), centerX, centerY - 9, accentColor);
        guiGraphics.drawCenteredString(this.font, MANA_COSTS[displaySin], centerX, centerY + 3, 0xFFE2E8F0);

        int panelW = Math.min(460, this.width - 24);
        int panelX = centerX - panelW / 2;
        int panelY = this.height - 68;

        guiGraphics.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + 58, accentColor);
        guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + 56, 0xF20B0F17);

        guiGraphics.drawCenteredString(this.font, TITLES[displaySin], centerX, panelY + 6, accentColor);
        guiGraphics.drawCenteredString(this.font, ACTIVE_DESC[displaySin], centerX, panelY + 20, 0xFFF8FAFC);
        guiGraphics.drawCenteredString(this.font, PASSIVE_DESC[displaySin], centerX, panelY + 33, 0xFF94A3B8);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            PlayerSinsData data = ModAttachmentTypes.get(mc.player);
            String statusLine = String.format("Мана: %.0f / %.0f  |  Гнев: %.0f%%  |  Ранг Dragonfyre: %d  |  Подрежим: #%d",
                    data.getCurrentMana(), data.getMaxMana(), data.getWrathStacks(), data.getDragonfyreSoulRank(), data.getActiveSubMode() + 1);
            guiGraphics.drawCenteredString(this.font, statusLine, centerX, panelY + 45, 0xFF38BDF8);
        }
    }

    private void selectAndClose(int sinIndex) {
        if (sinIndex >= 0 && sinIndex < 7) {
            ModNetwork.sendToServer(new SelectSinPayload(sinIndex));
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 0.7F, 1.2F);
            }
        }
        this.onClose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            ModNetwork.sendToServer(new CastSinPayload(this.hoveredSin >= 0 ? this.hoveredSin : this.currentSin, -1));
            return true;
        }
        if (this.hoveredSin >= 0 && this.hoveredSin < 7) {
            selectAndClose(this.hoveredSin);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_7) {
            int chosen = keyCode - GLFW.GLFW_KEY_1;
            selectAndClose(chosen);
            return true;
        }
        if (keyCode == ModKeyBindings.SWITCH_MODE_KEY.getKey().getValue()) {
            ModNetwork.sendToServer(new CastSinPayload(this.hoveredSin >= 0 ? this.hoveredSin : this.currentSin, -1));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == ModKeyBindings.RADIAL_MENU_KEY.getKey().getValue()) {
            selectAndClose(this.hoveredSin);
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
