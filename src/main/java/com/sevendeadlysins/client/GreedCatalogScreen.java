package com.sevendeadlysins.client;

import com.sevendeadlysins.data.PlayerSinsData;
import com.sevendeadlysins.network.CastSinPayload;
import com.sevendeadlysins.network.ModNetwork;
import com.sevendeadlysins.registry.ModAttachmentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Меню Сокровищницы Алчности (GreedCatalogScreen) для Minecraft 1.20.1 (Forge 47.3.0).
 *
 * ОБНОВЛЕНИЯ ПО ЗАПРОСУ:
 * - Если предмет был скопирован со стаком (например, 13 штук) — при нажатии «Создать» выдаётся ровно 13 штук!
 * - Напротив каждого сохранённого предмета добавлена кнопка «[✕]» для мгновенного удаления предмета из Каталога,
 *   а также кнопка «Очистить весь каталог» внизу экрана.
 */
public class GreedCatalogScreen extends Screen {
    private final List<ItemStack> parsedItems = new ArrayList<>();
    private final List<Integer> iconPositionsX = new ArrayList<>();
    private final List<Integer> iconPositionsY = new ArrayList<>();

    public GreedCatalogScreen() {
        super(Component.literal("✦ Каталог Алчности — Создание Полных Стаков и Управление ✦"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.parsedItems.clear();
        this.iconPositionsX.clear();
        this.iconPositionsY.clear();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        PlayerSinsData data = ModAttachmentTypes.get(mc.player);
        List<CompoundTag> artifacts = data.getObservedArtifacts();

        int startX = this.width / 2 - 178;
        int startY = 58;

        for (int i = 0; i < artifacts.size(); i++) {
            final int slotIndex = i;
            ItemStack parsed = ItemStack.of(artifacts.get(i));
            this.parsedItems.add(parsed);

            int stackCount = Math.max(1, parsed.getCount());
            String rawName = parsed.isEmpty() ? "Артефакт #" + (i + 1) : parsed.getHoverName().getString();
            if (rawName.length() > 13) {
                rawName = rawName.substring(0, 12) + "…";
            }

            int col = i % 2;
            int row = i / 2;
            int cellX = startX + col * 182;
            int cellY = startY + row * 24;

            this.iconPositionsX.add(cellX + 3);
            this.iconPositionsY.add(cellY + 2);

            // Кнопка выдачи полного стака (например, x13 шт.)
            this.addRenderableWidget(Button.builder(
                    Component.literal("Взять x" + stackCount + ": " + rawName),
                    btn -> {
                        ModNetwork.sendToServer(new CastSinPayload(1, 100 + slotIndex));
                        this.onClose();
                    }
            ).bounds(cellX + 22, cellY, 128, 20).build());

            // Кнопка удаления сохранённого предмета из Каталога Алчности
            this.addRenderableWidget(Button.builder(
                    Component.literal("§c✕"),
                    btn -> {
                        data.removeObservedArtifact(slotIndex);
                        ModNetwork.sendToServer(new CastSinPayload(1, 300 + slotIndex));
                        this.rebuildWidgets();
                    }
            ).bounds(cellX + 152, cellY, 22, 20).build());
        }

        if (!artifacts.isEmpty()) {
            this.addRenderableWidget(Button.builder(
                    Component.literal("§cОчистить весь каталог"),
                    btn -> {
                        data.clearObservedArtifacts();
                        ModNetwork.sendToServer(new CastSinPayload(1, 399));
                        this.rebuildWidgets();
                    }
            ).bounds(this.width / 2 - 166, this.height - 32, 156, 20).build());
        }

        this.addRenderableWidget(Button.builder(
                Component.literal("Закрыть [ESC]"),
                btn -> this.onClose()
        ).bounds(this.width / 2 + 10, this.height - 32, 156, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xB0070B12);

        int panelW = 384;
        int panelX = this.width / 2 - panelW / 2;
        int panelY = 14;
        int panelH = this.height - 28;

        guiGraphics.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + panelH + 2, 0xFFEAB308);
        guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xF00B0F17);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 24, 0xFFFACC15);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            PlayerSinsData data = ModAttachmentTypes.get(mc.player);
            String subTitle = String.format("Выдаёт весь стак (напр. 13 шт.) за 30 Маны | [✕] — удалить предмет | Мана: %.0f / %.0f",
                    data.getCurrentMana(), data.getMaxMana());
            guiGraphics.drawCenteredString(this.font, subTitle, this.width / 2, 38, 0xFF38BDF8);
        }

        ItemStack hoveredStack = ItemStack.EMPTY;
        for (int i = 0; i < this.parsedItems.size(); i++) {
            ItemStack stack = this.parsedItems.get(i);
            int ix = this.iconPositionsX.get(i);
            int iy = this.iconPositionsY.get(i);

            guiGraphics.fill(ix - 2, iy - 2, ix + 18, iy + 18, 0xFF1E293B);
            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, ix, iy);
                guiGraphics.renderItemDecorations(this.font, stack, ix, iy);

                if (mouseX >= ix - 2 && mouseX <= ix + 148 && mouseY >= iy - 2 && mouseY <= iy + 18) {
                    hoveredStack = stack;
                }
            }
        }

        if (!hoveredStack.isEmpty()) {
            guiGraphics.renderTooltip(this.font, hoveredStack, mouseX, mouseY);
        }
    }
}
