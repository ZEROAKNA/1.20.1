package com.sevendeadlysins.client;

import com.sevendeadlysins.data.PlayerSinsData;
import com.sevendeadlysins.registry.ModAttachmentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/**
 * Клиентский HUD оверлей Семи Смертных Грехов для Minecraft 1.20.1 (Forge IGuiOverlay).
 * Позиционируется в левом нижнем углу так, чтобы не перекрывать полосу маны Iron's Spells
 * и RPG-интерфейс в модпаке Cisco's Fantasy Medieval RPG [Dragonfyre].
 */
public class SinsHudOverlay implements IGuiOverlay {
    private static final String[] SIN_NAMES = {
            "I. Гордыня", "II. Алчность", "III. Похоть",
            "IV. Зависть", "V. Чревоугодие", "VI. Гнев", "VII. Лень"
    };

    private static final String[][] MODE_NAMES = {
            {"Тайм Стоп (20с / Анти-Реген)", "Очищение Эффектов [Взгляд]", "Мгновенная Смерть [Взгляд]"},
            {"Копия Стака в Руке / Блока", "Каталог Алчности [Удал.]"},
            {"Кровавый Контракт (+300% HP)", "Кровавый Контракт (+300% HP)", "Кровавый Контракт (+300% HP)"},
            {"Кража Способности [Моды]", "Каст Украденного Навыка", "Арсенал Зависти [V]"},
            {"Воронка Бездны", "Воронка Бездны", "Воронка Бездны"},
            {"Катаклизм Гнева", "Катаклизм Гнева", "Катаклизм Гнева"},
            {"Гибернация Короля", "Покой Лени", "Гибернация Короля"}
    };

    private static final int[] SIN_COLORS = {
            0xFFF59E0B, 0xFFEAB308, 0xFFEC4899,
            0xFF10B981, 0xFF06B6D4, 0xFFEF4444, 0xFF38BDF8
    };

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        PlayerSinsData data = ModAttachmentTypes.get(mc.player);
        if (!data.isUnlocked()) return;

        int idx = Mth.clamp(data.getActiveSinIndex(), 0, 6);
        int subMode = Mth.clamp(data.getActiveSubMode(), 0, MODE_NAMES[idx].length - 1);
        int sinColor = SIN_COLORS[idx];

        int x = 10;
        int y = screenHeight - 76;
        int panelWidth = 186;
        int panelHeight = 64;

        guiGraphics.fill(x - 2, y - 2, x + panelWidth + 2, y + panelHeight + 2, sinColor);
        guiGraphics.fill(x, y, x + panelWidth, y + panelHeight, 0xE60B0F17);

        String header = SIN_NAMES[idx] + " [Ранг " + data.getDragonfyreSoulRank() + "]";
        guiGraphics.drawString(mc.font, header, x + 6, y + 5, sinColor, true);

        String modeTitle = "Режим: " + MODE_NAMES[idx][subMode];
        guiGraphics.drawString(mc.font, modeTitle, x + 6, y + 16, 0xFFF1F5F9, true);

        int barX = x + 6;
        int barY = y + 28;
        int barW = panelWidth - 12;
        guiGraphics.fill(barX, barY, barX + barW, barY + 9, 0xFF1E293B);
        int manaFilled = (int) ((data.getCurrentMana() / Math.max(1.0F, data.getMaxMana())) * barW);
        guiGraphics.fill(barX, barY, barX + Mth.clamp(manaFilled, 0, barW), barY + 9, 0xFF2563EB);
        String manaText = String.format("Мана: %.0f / %.0f", data.getCurrentMana(), data.getMaxMana());
        guiGraphics.drawCenteredString(mc.font, manaText, barX + barW / 2, barY + 1, 0xFFFFFFFF);

        int wrathY = y + 40;
        guiGraphics.fill(barX, wrathY, barX + barW, wrathY + 6, 0xFF1E293B);
        int wrathFilled = (int) ((data.getWrathStacks() / 100.0F) * barW);
        guiGraphics.fill(barX, wrathY, barX + Mth.clamp(wrathFilled, 0, barW), wrathY + 6, 0xFFDC2626);

        long gameTime = mc.level != null ? mc.level.getGameTime() : 0L;
        StringBuilder status = new StringBuilder(String.format("Гнев: %.0f%%", data.getWrathStacks()));
        if (data.isSinOverdriveActive(gameTime)) {
            status.append(" | §4§lМЕТКА ДЕМОНА x1.5!");
        } else if (gameTime > 0L && gameTime >= data.getSlothLockedUntil() && gameTime < data.getSlothBuffUntil()) {
            status.append(" | §bУдар 5.0x!");
        } else if (!data.getStolenAbilityId().isEmpty()) {
            status.append(" | ").append(data.getStolenAbilityId());
        } else if (!data.getObservedArtifacts().isEmpty()) {
            status.append(" | Каталог: ").append(data.getObservedArtifacts().size()).append("/20");
        }
        String statusStr = status.toString();
        if (statusStr.length() > 30) {
            statusStr = statusStr.substring(0, 29) + "…";
        }
        guiGraphics.drawString(mc.font, statusStr, x + 6, y + 50, 0xFFFCA5A5, true);
    }
}
