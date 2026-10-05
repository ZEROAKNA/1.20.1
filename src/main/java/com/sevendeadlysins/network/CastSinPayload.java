package com.sevendeadlysins.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Сетевой пакет C2S (Клиент -> Сервер) для Minecraft 1.20.1 (Forge SimpleChannel):
 * - Активация текущего навыка Греха (клавиша V)
 * - Переключение подрежима (mode = -1, клавиша X)
 * - Репликация артефакта из Каталога Алчности (mode = 100 + slotIndex)
 * - Выбор похищенной способности из Арсенала Зависти (mode = 200 + slotIndex)
 */
public class CastSinPayload {
    private final int sinId;
    private final int mode;

    public CastSinPayload(int sinId, int mode) {
        this.sinId = sinId;
        this.mode = mode;
    }

    public static void encode(CastSinPayload msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.sinId);
        buf.writeVarInt(msg.mode);
    }

    public static CastSinPayload decode(FriendlyByteBuf buf) {
        return new CastSinPayload(buf.readVarInt(), buf.readVarInt());
    }

    public int sinId() {
        return sinId;
    }

    public int mode() {
        return mode;
    }
}
