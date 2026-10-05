package com.sevendeadlysins.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Сетевой пакет C2S (Клиент -> Сервер) для Minecraft 1.20.1 (Forge SimpleChannel):
 * Выбор активного Греха из радиального меню (клавиша R или клавиши 1-7).
 */
public class SelectSinPayload {
    private final int sinId;

    public SelectSinPayload(int sinId) {
        this.sinId = sinId;
    }

    public static void encode(SelectSinPayload msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.sinId);
    }

    public static SelectSinPayload decode(FriendlyByteBuf buf) {
        return new SelectSinPayload(buf.readVarInt());
    }

    public int sinId() {
        return sinId;
    }
}
