package com.sevendeadlysins.entity;

import com.sevendeadlysins.data.PlayerSinsData;
import com.sevendeadlysins.network.ModNetwork;
import com.sevendeadlysins.registry.ModAttachmentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

/**
 * Сущность сферы души Чревоугодия (SoulOrbEntity) для Minecraft 1.20.1 (Forge 47.3.0).
 * Оптимизирована под боссов Cisco's Fantasy Medieval RPG [Dragonfyre].
 */
public class SoulOrbEntity extends Entity {
    private static final int MAX_LIFETIME_TICKS = 400; // 20 секунд жизни сферы
    private int ageTicks = 0;
    private boolean bossSoul = false;
    private UUID ownerUuid = null;

    public SoulOrbEntity(EntityType<? extends SoulOrbEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        this.ageTicks++;
        if (this.ageTicks >= MAX_LIFETIME_TICKS) {
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.35D,
                        this.getY() + 0.2D + (this.random.nextDouble() - 0.5D) * 0.35D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.35D,
                        0.0D, 0.02D, 0.0D
                );
            }
            return;
        }

        Player nearest = this.level().getNearestPlayer(this, 10.0D);
        if (nearest instanceof ServerPlayer serverPlayer && serverPlayer.isAlive()) {
            if (this.ownerUuid == null || this.ownerUuid.equals(serverPlayer.getUUID())) {
                this.setDeltaMovement(serverPlayer.position().add(0.0D, 1.0D, 0.0D).subtract(this.position()).scale(0.25D));
                this.setPos(
                        this.getX() + this.getDeltaMovement().x,
                        this.getY() + this.getDeltaMovement().y,
                        this.getZ() + this.getDeltaMovement().z
                );

                if (this.distanceToSqr(serverPlayer) < 2.8D) {
                    absorbIntoPlayer(serverPlayer);
                }
            }
        }
    }

    public void absorbIntoPlayer(ServerPlayer player) {
        if (!this.isAlive()) return;
        PlayerSinsData data = ModAttachmentTypes.get(player);
        if (!data.isUnlocked()) return;

        float maxManaGain = this.bossSoul ? 50.0F : 3.0F;
        data.setMaxMana(data.getMaxMana() + maxManaGain);
        data.addMana(data.getMaxMana() * 0.30F);
        player.getFoodData().eat(6, 1.2F);
        player.heal(this.bossSoul ? 25.0F : 5.0F);
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, this.bossSoul ? 3 : 0, false, true, true));

        this.level().playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.2F, 1.2F);
        this.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 0.9F);
        player.displayClientMessage(
                Component.literal("Чревоугодие: Душа поглощена! Макс. мана +" + (int) maxManaGain + " (Пул: " + (int) data.getMaxMana() + ", Ранг Dragonfyre: " + data.getDragonfyreSoulRank() + ")!")
                        .withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD),
                true
        );

        ModAttachmentTypes.save(player, data);
        ModNetwork.syncToPlayer(player, data);
        this.discard();
    }

    public void setBossSoul(boolean bossSoul) {
        this.bossSoul = bossSoul;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.ageTicks = compound.getInt("ageTicks");
        this.bossSoul = compound.getBoolean("bossSoul");
        if (compound.hasUUID("ownerUuid")) {
            this.ownerUuid = compound.getUUID("ownerUuid");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("ageTicks", this.ageTicks);
        compound.putBoolean("bossSoul", this.bossSoul);
        if (this.ownerUuid != null) {
            compound.putUUID("ownerUuid", this.ownerUuid);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
