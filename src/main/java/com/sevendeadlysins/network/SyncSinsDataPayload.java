package com.sevendeadlysins.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Сетевой пакет S2C (Сервер -> Клиент) для Minecraft 1.20.1 (Forge SimpleChannel):
 * Синхронизирует CompoundTag состояния PlayerSinsData с сервера на клиент.
 */
public class SyncSinsDataPayload {
    private final CompoundTag data;

    public SyncSinsDataPayload(CompoundTag data) {
        this.data = data;
    }

    public static void encode(SyncSinsDataPayload msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }

    public static SyncSinsDataPayload decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        return new SyncSinsDataPayload(tag != null ? tag : new CompoundTag());
    }

    public CompoundTag data() {
        return data;
    }
}
