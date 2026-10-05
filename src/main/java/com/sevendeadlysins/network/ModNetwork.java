package com.sevendeadlysins.network;

import com.sevendeadlysins.SevenDeadlySinsMod;
import com.sevendeadlysins.ability.SinsAbilityEngine;
import com.sevendeadlysins.data.PlayerSinsData;
import com.sevendeadlysins.registry.ModAttachmentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Сетевой менеджер SimpleChannel для Minecraft 1.20.1 (Forge 47.3.0).
 * Обеспечивает быструю и безопасную передачу пакетов в одиночной игре и на серверах Cisco's RPG [Dragonfyre].
 */
public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1.2.0";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SevenDeadlySinsMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(
                packetId++,
                CastSinPayload.class,
                CastSinPayload::encode,
                CastSinPayload::decode,
                ModNetwork::handleCastSinOnServer,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(
                packetId++,
                SelectSinPayload.class,
                SelectSinPayload::encode,
                SelectSinPayload::decode,
                ModNetwork::handleSelectSinOnServer,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(
                packetId++,
                SyncSinsDataPayload.class,
                SyncSinsDataPayload::encode,
                SyncSinsDataPayload::decode,
                ModNetwork::handleSyncDataOnClient,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    private static void handleCastSinOnServer(final CastSinPayload payload, final Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer != null) {
                PlayerSinsData data = ModAttachmentTypes.get(serverPlayer);
                if (!data.isUnlocked()) {
                    serverPlayer.displayClientMessage(
                            Component.literal("Ваши грехи ещё скованы. Найдите Запретный Плод в Древнем Городе или сокровищницах Dragonfyre!")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                    return;
                }

                if (payload.mode() == -1) {
                    data.cycleSubMode();
                    ModAttachmentTypes.save(serverPlayer, data);
                    syncToPlayer(serverPlayer, data);
                    return;
                }

                SinsAbilityEngine.executeActiveSin(serverPlayer, data, payload.sinId(), payload.mode());
                ModAttachmentTypes.save(serverPlayer, data);
                syncToPlayer(serverPlayer, data);
            }
        });
        context.setPacketHandled(true);
    }

    private static void handleSelectSinOnServer(final SelectSinPayload payload, final Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer != null) {
                PlayerSinsData data = ModAttachmentTypes.get(serverPlayer);
                if (!data.isUnlocked()) return;

                data.setActiveSinIndex(payload.sinId());
                ModAttachmentTypes.save(serverPlayer, data);
                syncToPlayer(serverPlayer, data);
            }
        });
        context.setPacketHandled(true);
    }

    private static void handleSyncDataOnClient(final SyncSinsDataPayload payload, final Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            PlayerSinsData clientData = ModAttachmentTypes.get(null);
            clientData.readRawNbt(payload.data());
        });
        context.setPacketHandled(true);
    }

    public static void syncToPlayer(ServerPlayer player, PlayerSinsData data) {
        if (player == null || data == null) return;
        CompoundTag tag = new CompoundTag();
        data.writeRawNbt(tag);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncSinsDataPayload(tag));
        data.clearDirty();
    }

    public static void sendToServer(Object payload) {
        CHANNEL.sendToServer(payload);
    }
}
