package com.sevendeadlysins.ability;

import com.sevendeadlysins.compat.CompatManager;
import com.sevendeadlysins.data.PlayerSinsData;
import com.sevendeadlysins.entity.SoulOrbEntity;
import com.sevendeadlysins.network.ModNetwork;
import com.sevendeadlysins.registry.ModAttachmentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Серверный движок всех 7 Смертных Грехов для Minecraft 1.20.1 (Forge 47.3.0 / Java 17).
 * Оптимизирован под баланс и высокую производительность модпака Cisco's Fantasy Medieval RPG [Dragonfyre].
 */
public class SinsAbilityEngine {

    private static final UUID WRATH_ATTACK_MODIFIER_UUID = UUID.fromString("d48f6011-7a91-43d1-98b2-112233445501");
    private static final UUID WRATH_SPEED_MODIFIER_UUID = UUID.fromString("d48f6011-7a91-43d1-98b2-112233445502");
    private static final UUID LUST_THRALL_DAMAGE_UUID = UUID.fromString("d48f6011-7a91-43d1-98b2-112233445503");
    private static final UUID LUST_THRALL_HEALTH_UUID = UUID.fromString("d48f6011-7a91-43d1-98b2-112233445504");

    public static void executeActiveSin(ServerPlayer player, PlayerSinsData data, int sinId, int mode) {
        ServerLevel level = player.serverLevel();
        long gameTime = level.getGameTime();

        // Специальный код -2: Ультимативный режим «Метка Демона / Первородный Грех» (Shift + V)
        if (mode == -2) {
            activateSinOverdrive(player, data, level, gameTime);
            return;
        }

        if (gameTime < data.getSlothLockedUntil()) {
            long remainingSec = (data.getSlothLockedUntil() - gameTime) / 20L;
            player.displayClientMessage(
                    Component.literal("Печать Лени сковывает активные навыки ещё " + remainingSec + " сек.")
                            .withStyle(ChatFormatting.AQUA),
                    true
            );
            return;
        }

        switch (sinId) {
            case 0:
                executePride(player, data, level, gameTime, mode);
                break;
            case 1:
                executeGreed(player, data, level, gameTime, mode);
                break;
            case 2:
                executeLust(player, data, level, gameTime);
                break;
            case 3:
                executeEnvy(player, data, level, gameTime, mode);
                break;
            case 4:
                executeGluttonyActive(player, data, level, gameTime);
                break;
            case 5:
                executeWrathShockwave(player, data, level, gameTime);
                break;
            case 6:
                executeSlothBurst(player, data, level, gameTime);
                break;
            default:
                break;
        }
    }

    /**
     * УЛЬТИМАТИВНЫЙ РЕЖИМ «МЕТКА ДЕМОНА / ПЕРВОРОДНЫЙ ГРЕХ» (SIN OVERDRIVE — Shift + V)
     * На 30 секунд (600 тиков) даёт x1.5 ко всем статам и урону, -50% расход маны и Адское Пламя (анти-реген боссов).
     */
    private static void activateSinOverdrive(ServerPlayer player, PlayerSinsData data, ServerLevel level, long gameTime) {
        if (data.isSinOverdriveActive(gameTime)) {
            long leftSec = (data.getSinOverdriveUntil() - gameTime) / 20L;
            player.displayClientMessage(
                    Component.literal("Метка Демона [Первородный Грех] уже активна! Осталось: " + leftSec + " сек.")
                            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                    true
            );
            return;
        }

        boolean hasCondition = data.getWrathStacks() >= 80.0F || data.getDragonfyreSoulRank() >= 2;
        if (!hasCondition && !data.consumeMana(50.0F)) {
            player.displayClientMessage(
                    Component.literal("Для Метки Демона [Shift + V] требуется 80% Гнева, 2+ Ранг Души или 50 ед. маны!")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        data.setSinOverdriveUntil(gameTime + 600L);
        data.setWrathStacks(100.0F);
        data.setWrathBerserkUntil(gameTime + 600L);

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 3, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 2, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 2, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0, false, true, true));

        updateWrathAttributes(player, data, gameTime);
        CompatManager.clearIronsSpellbooksCooldowns(player);

        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 80, 1.5D, 1.2D, 1.5D, 0.1D);
        level.sendParticles(ParticleTypes.SCULK_SOUL, player.getX(), player.getY() + 1.0D, player.getZ(), 45, 1.2D, 1.0D, 1.2D, 0.08D);
        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 2.0F, 0.65F);
        level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.5F, 0.75F);

        player.displayClientMessage(
                Component.literal("✦ МЕТКА ДЕМОНА: ПЕРВОРОДНЫЙ ГРЕХ АКТИВИРОВАН НА 30 СЕК! (x1.5 Мощность, -50% Маны, Адское Пламя анти-регена) ✦")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                true
        );
    }

    /**
     * 1. ГОРДЫНЯ (PRIDE) — ПЕРЕРАБОТАНО ПО ЗАПРОСУ:
     * - Способность 1 (chosenLaw == 0): ТАЙМ СТОП НА 20 СЕКУНД (400 тиков). Все кроме игрока останавливаются,
     *   никто не может двигаться, а регенерация у всех остановленных существ полностью заблокирована!
     * - Способность 2 (chosenLaw == 1): Сразу очищает все эффекты у того, на кого смотрит игрок.
     * - Способность 3 (chosenLaw == 2): МГНОВЕННАЯ СМЕРТЬ того, на кого смотрит игрок.
     */
    private static void executePride(ServerPlayer player, PlayerSinsData data, ServerLevel level, long gameTime, int lawMode) {
        int chosenLaw = Mth.clamp(lawMode, 0, 2);

        if (chosenLaw == 0 && data.getActivePrideLaw() == 0 && gameTime < data.getPrideActiveUntil()) {
            data.setActivePrideLaw(-1);
            data.setPrideActiveUntil(0L);
            unfreezeTimeInArea(player, level, 80.0D);
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 2.0F, 1.2F);
            player.displayClientMessage(
                    Component.literal("Гордыня: Ход времени возобновлён!")
                            .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD),
                    true
            );
            return;
        }

        if (chosenLaw == 0) {
            if (gameTime < data.getPrideCooldownUntil()) {
                long ticksLeft = data.getPrideCooldownUntil() - gameTime;
                player.displayClientMessage(
                        Component.literal("Гордыня восстанавливается: " + (ticksLeft / 20L) + " сек.")
                                .withStyle(ChatFormatting.GOLD),
                        true
                );
                return;
            }

            if (!data.consumeManaWithOverdrive(35.0F, gameTime)) {
                player.displayClientMessage(Component.literal("Недостаточно маны (требуется 35).").withStyle(ChatFormatting.RED), true);
                return;
            }

            // Тайм Стоп строго на 20 секунд (400 тиков)
            data.setActivePrideLaw(0);
            data.setPrideActiveUntil(gameTime + 400L);
            data.setPrideCooldownUntil(gameTime + 200L);

            AABB box = player.getBoundingBox().inflate(64.0D);
            int frozenCount = 0;
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
                if (entity.getPersistentData().hasUUID("sds_lust_master")
                        && player.getUUID().equals(entity.getPersistentData().getUUID("sds_lust_master"))) {
                    continue;
                }
                if (entity instanceof Mob mob) {
                    mob.setNoAi(true);
                    mob.setTarget(null);
                    mob.getNavigation().stop();
                }
                entity.setNoGravity(true);
                entity.setDeltaMovement(Vec3.ZERO);
                entity.hurtMarked = true;

                entity.removeEffect(MobEffects.REGENERATION);
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 255, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 400, 0, false, false));

                entity.getPersistentData().putBoolean("sds_time_stopped", true);
                entity.getPersistentData().putDouble("sds_freeze_x", entity.getX());
                entity.getPersistentData().putDouble("sds_freeze_y", entity.getY());
                entity.getPersistentData().putDouble("sds_freeze_z", entity.getZ());
                entity.getPersistentData().putFloat("sds_locked_hp", entity.getHealth());
                frozenCount++;
            }

            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, box)) {
                proj.setNoGravity(true);
                proj.setDeltaMovement(Vec3.ZERO);
                proj.getPersistentData().putBoolean("sds_time_stopped", true);
                proj.getPersistentData().putDouble("sds_freeze_x", proj.getX());
                proj.getPersistentData().putDouble("sds_freeze_y", proj.getY());
                proj.getPersistentData().putDouble("sds_freeze_z", proj.getZ());
            }

            level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 75, 5.0D, 2.0D, 5.0D, 0.02D);
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0F, 0.5F);
            level.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 2.0F, 0.5F);
            player.displayClientMessage(
                    Component.literal("Гордыня [Способность 1]: ТАЙМ СТОП НА 20 СЕКУНД! Остановлено целей: " + frozenCount + " (Движение и Регенерация отключены!)")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    true
            );
            return;
        }

        // Способность 2 (chosenLaw == 1): Сразу очищает все эффекты того, на кого смотрит игрок
        if (chosenLaw == 1) {
            EntityHitResult hit = raycastEntity(player, 48.0D);
            LivingEntity target = (hit != null && hit.getEntity() instanceof LivingEntity living) ? living : null;

            if (target == null) {
                player.displayClientMessage(
                        Component.literal("Гордыня [Очищение]: Наведите прицел на цель (до 48 блоков)!")
                                .withStyle(ChatFormatting.YELLOW),
                        true
                );
                return;
            }

            if (!data.consumeManaWithOverdrive(20.0F, gameTime)) {
                player.displayClientMessage(Component.literal("Недостаточно маны (требуется 20).").withStyle(ChatFormatting.RED), true);
                return;
            }

            int removedEffectsCount = target.getActiveEffects().size();
            target.removeAllEffects();
            target.setAbsorptionAmount(0.0F);
            target.clearFire();
            CompatManager.suppressL2HostilityTraits(target);
            target.getPersistentData().putLong("sds_no_regen_until", gameTime + 300L);
            target.getPersistentData().putFloat("sds_locked_hp", target.getHealth());

            level.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0D, target.getZ(), 45, 0.6D, 0.9D, 0.6D, 0.15D);
            level.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.0D, target.getZ(), 25, 0.5D, 0.8D, 0.5D, 0.06D);
            level.playSound(null, target.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.5F, 1.4F);

            player.displayClientMessage(
                    Component.literal("Гордыня [Способность 2]: С цели «" + target.getName().getString() + "» полностью стёрты ВСЕ эффекты (" + removedEffectsCount + " шт.), щиты и регенерация!")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    true
            );
            return;
        }

        // Способность 3 (chosenLaw == 2): МГНОВЕННАЯ СМЕРТЬ того, на кого смотрит игрок
        EntityHitResult hit = raycastEntity(player, 48.0D);
        LivingEntity target = (hit != null && hit.getEntity() instanceof LivingEntity living) ? living : null;

        if (target == null) {
            player.displayClientMessage(
                    Component.literal("Гордыня [Приговор Смерти]: Наведите прицел на цель (до 48 блоков)!")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        if (!data.consumeManaWithOverdrive(50.0F, gameTime)) {
            player.displayClientMessage(Component.literal("Недостаточно маны для Мгновенной Смерти (требуется 50).").withStyle(ChatFormatting.RED), true);
            return;
        }

        data.setPrideCooldownUntil(gameTime + 100L);
        String targetName = target.getName().getString();

        target.removeAllEffects();
        target.setAbsorptionAmount(0.0F);
        CompatManager.suppressL2HostilityTraits(target);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack eq = target.getItemBySlot(slot);
            if (eq.is(Items.TOTEM_OF_UNDYING)) {
                target.setItemSlot(slot, ItemStack.EMPTY);
            }
        }

        target.invulnerableTime = 0;
        CompatManager.dealMultiPhaseDamage(player, target, Math.max(1000000.0F, target.getMaxHealth() * 100.0F), 6);
        if (target.isAlive()) {
            target.setHealth(0.0F);
            target.die(level.damageSources().fellOutOfWorld());
            target.kill();
        }

        level.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + 1.0D, target.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.0D);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 75, 0.9D, 1.2D, 0.9D, 0.12D);
        level.playSound(null, target.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.5F, 0.6F);
        level.playSound(null, target.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.8F, 0.7F);

        player.displayClientMessage(
                Component.literal("Гордыня [Способность 3]: МГНОВЕННАЯ СМЕРТЬ! «" + targetName + "» уничтожен взглядом!")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                true
        );
    }

    public static void unfreezeTimeInArea(ServerPlayer player, ServerLevel level, double radius) {
        AABB box = player.getBoundingBox().inflate(radius);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity.getPersistentData().getBoolean("sds_time_stopped")) {
                if (entity instanceof Mob mob) {
                    mob.setNoAi(false);
                }
                entity.setNoGravity(false);
                entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                entity.getPersistentData().remove("sds_time_stopped");
                entity.getPersistentData().remove("sds_freeze_x");
                entity.getPersistentData().remove("sds_freeze_y");
                entity.getPersistentData().remove("sds_freeze_z");
                entity.getPersistentData().remove("sds_locked_hp");
            }
        }
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, box)) {
            if (proj.getPersistentData().getBoolean("sds_time_stopped")) {
                proj.setNoGravity(false);
                proj.getPersistentData().remove("sds_time_stopped");
                proj.getPersistentData().remove("sds_freeze_x");
                proj.getPersistentData().remove("sds_freeze_y");
                proj.getPersistentData().remove("sds_freeze_z");
            }
        }
    }

    /**
     * 2. АЛЧНОСТЬ (GREED) — КОПИРОВАНИЕ ПОЛНОГО СТАКА В РУКЕ (НАПР. 13 -> 13 ШТ.) И УДАЛЕНИЕ ИЗ КАТАЛОГА
     */
    private static void executeGreed(ServerPlayer player, PlayerSinsData data, ServerLevel level, long gameTime, int mode) {
        if (mode == 399) {
            data.clearObservedArtifacts();
            level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
            player.displayClientMessage(
                    Component.literal("Алчность: Все сохранённые предметы удалены из Каталога.")
                            .withStyle(ChatFormatting.YELLOW),
                    true
            );
            return;
        }
        if (mode >= 300 && mode < 399) {
            int removeIdx = mode - 300;
            if (data.removeObservedArtifact(removeIdx)) {
                level.playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.9F, 1.2F);
                player.displayClientMessage(
                        Component.literal("Алчность: Предмет #" + (removeIdx + 1) + " удалён из Каталога (осталось " + data.getObservedArtifacts().size() + "/20).")
                                .withStyle(ChatFormatting.YELLOW),
                        true
                );
            }
            return;
        }

        if (mode >= 100 || mode == 1) {
            List<CompoundTag> catalog = data.getObservedArtifacts();
            if (catalog.isEmpty()) {
                ItemStack held = player.getMainHandItem();
                if (!held.isEmpty()) {
                    CompoundTag saved = new CompoundTag();
                    held.save(saved);
                    data.addObservedArtifact(saved);
                } else {
                    player.displayClientMessage(
                            Component.literal("Каталог Алчности пуст. Возьмите предмет в руку или наведите прицел на блок/врага в Режиме 1!")
                                    .withStyle(ChatFormatting.YELLOW),
                            true
                    );
                    return;
                }
            }

            int index = mode >= 100 ? Mth.clamp(mode - 100, 0, catalog.size() - 1) : catalog.size() - 1;
            CompoundTag itemTag = catalog.get(index);
            ItemStack reconstructed = ItemStack.of(itemTag);
            if (reconstructed.isEmpty()) {
                player.displayClientMessage(Component.literal("Не удалось воссоздать матрицу предмета.").withStyle(ChatFormatting.RED), true);
                return;
            }

            if (!data.consumeManaWithOverdrive(30.0F, gameTime)) {
                player.displayClientMessage(Component.literal("Недостаточно маны для репликации (требуется 30 ед.).").withStyle(ChatFormatting.RED), true);
                return;
            }

            int stackAmount = Math.max(1, reconstructed.getCount());
            ItemStack exactCopy = reconstructed.copy();
            exactCopy.setCount(stackAmount);
            CompatManager.onGreedItemReplicated(player, exactCopy);

            if (!player.getInventory().add(exactCopy)) {
                player.drop(exactCopy, false);
            }
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 28, 0.5D, 0.5D, 0.5D, 0.15D);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.8F, 1.2F);
            player.displayClientMessage(
                    Component.literal("Алчность материализовала: ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                            .append(reconstructed.getHoverName())
                            .append(Component.literal(" x" + stackAmount + " шт.!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)),
                    true
            );
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.LUCK, 1200, 2, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 1, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 1200, 1, false, true, true));

        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(24.0D))) {
            itemEntity.setNoPickUpDelay();
            itemEntity.teleportTo(player.getX(), player.getY() + 0.5D, player.getZ());
        }

        // Если в руке есть предмет (например, в стаке 13 штук) — игрок сразу получает 13 штук и сохраняет стак в Каталог!
        ItemStack heldInHand = player.getMainHandItem();
        if (!heldInHand.isEmpty()) {
            int countInHand = heldInHand.getCount();
            CompoundTag heldNbt = new CompoundTag();
            heldInHand.save(heldNbt);
            data.addObservedArtifact(heldNbt);

            if (data.consumeManaWithOverdrive(15.0F, gameTime)) {
                ItemStack duplicatedStack = heldInHand.copy();
                duplicatedStack.setCount(countInHand);
                CompatManager.onGreedItemReplicated(player, duplicatedStack);

                if (!player.getInventory().add(duplicatedStack)) {
                    player.drop(duplicatedStack, false);
                }
                level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 25, 0.5D, 0.5D, 0.5D, 0.12D);
                level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.8F, 1.3F);
                player.displayClientMessage(
                        Component.literal("Алчность: Скопирован стак в руке «")
                                .append(heldInHand.getHoverName())
                                .append(Component.literal("» — получено +" + countInHand + " шт. (и сохранено в Каталог)!"))
                                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                        true
                );
                ModAttachmentTypes.save(player, data);
                ModNetwork.syncToPlayer(player, data);
                return;
            } else {
                player.displayClientMessage(
                        Component.literal("Стак «")
                                .append(heldInHand.getHoverName())
                                .append(Component.literal(" x" + countInHand + "» сохранён в Каталог Алчности (для выдачи нужно 15 маны)."))
                                .withStyle(ChatFormatting.YELLOW),
                        true
                );
                ModAttachmentTypes.save(player, data);
                ModNetwork.syncToPlayer(player, data);
                return;
            }
        }

        scanEquipmentByRaycast(player, data, level, true);
    }

    public static void scanEquipmentByRaycast(ServerPlayer player, PlayerSinsData data, ServerLevel level, boolean notifyEmpty) {
        EntityHitResult hit = raycastEntity(player, 24.0D);
        LivingEntity target = (hit != null && hit.getEntity() instanceof LivingEntity) ? (LivingEntity) hit.getEntity() : null;

        int addedCount = 0;
        if (target != null) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = target.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    CompoundTag savedNbt = new CompoundTag();
                    stack.save(savedNbt);
                    if (data.addObservedArtifact(savedNbt)) {
                        addedCount++;
                    }
                }
            }
        }

        // Копирование любого БЛОКА по взгляду (до 24 блоков) прямо в инвентарь и в Каталог Алчности
        if (target == null && notifyEmpty) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 endPos = eyePos.add(player.getLookAngle().scale(24.0D));
            BlockHitResult blockHit = level.clip(new ClipContext(
                    eyePos,
                    endPos,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player
            ));

            if (blockHit.getType() == HitResult.Type.BLOCK) {
                BlockState state = level.getBlockState(blockHit.getBlockPos());
                if (!state.isAir()) {
                    Item blockItem = state.getBlock().asItem();
                    if (blockItem != Items.AIR) {
                        ItemStack blockStack = new ItemStack(blockItem, 1);
                        CompoundTag savedBlockNbt = new CompoundTag();
                        blockStack.save(savedBlockNbt);
                        data.addObservedArtifact(savedBlockNbt);

                        if (data.consumeMana(10.0F)) {
                            ItemStack inventoryCopy = blockStack.copy();
                            if (!player.getInventory().add(inventoryCopy)) {
                                player.drop(inventoryCopy, false);
                            }
                            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                    blockHit.getBlockPos().getX() + 0.5D,
                                    blockHit.getBlockPos().getY() + 0.8D,
                                    blockHit.getBlockPos().getZ() + 0.5D,
                                    14, 0.4D, 0.4D, 0.4D, 0.1D);
                            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.3F);
                            player.displayClientMessage(
                                    Component.literal("Алчность: Блок «")
                                            .append(blockStack.getHoverName())
                                            .append(Component.literal("» скопирован в инвентарь и сохранён в Каталог!"))
                                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                                    true
                            );
                            ModAttachmentTypes.save(player, data);
                            ModNetwork.syncToPlayer(player, data);
                            return;
                        }
                    }
                }
            }
        }

        if (target == null && addedCount == 0) {
            List<LivingEntity> nearby = level.getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(12.0D),
                    e -> e != player && e.isAlive()
            );
            if (!nearby.isEmpty()) {
                LivingEntity fallback = nearby.get(0);
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemStack stack = fallback.getItemBySlot(slot);
                    if (!stack.isEmpty()) {
                        CompoundTag savedNbt = new CompoundTag();
                        stack.save(savedNbt);
                        if (data.addObservedArtifact(savedNbt)) {
                            addedCount++;
                        }
                    }
                }
            }
        }

        if (addedCount == 0 && notifyEmpty && !player.getMainHandItem().isEmpty()) {
            CompoundTag heldNbt = new CompoundTag();
            player.getMainHandItem().save(heldNbt);
            if (data.addObservedArtifact(heldNbt)) {
                addedCount++;
            }
        }

        if (addedCount > 0) {
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.4F);
            player.displayClientMessage(
                    Component.literal("Алчность: записано артефактов в каталог: +" + addedCount + " (всего " + data.getObservedArtifacts().size() + "/20). Нажмите [X] затем [V]!")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    true
            );
            ModAttachmentTypes.save(player, data);
            ModNetwork.syncToPlayer(player, data);
        } else if (notifyEmpty) {
            player.displayClientMessage(
                    Component.literal("Аура Алчности: наведите прицел на БЛОК или экипированного врага! (Каталог: " + data.getObservedArtifacts().size() + "/20)")
                            .withStyle(ChatFormatting.YELLOW),
                    true
            );
        }
    }

    /**
     * 3. ПОХОТЬ (LUST) — ЭВОЛЮЦИЯ: КРОВАВЫЙ КОНТРАКТ (+300% HP, +150% УРОНА) И ИЛЛЮЗИЯ КОШМАРА НА БОССОВ (+45% УРОНА)
     */
    public static void executeLust(ServerPlayer player, PlayerSinsData data, ServerLevel level, long gameTime) {
        LivingEntity primaryTarget = null;
        if (data.getLustTargetUuid() != null) {
            Entity entity = level.getEntity(data.getLustTargetUuid());
            if (entity instanceof LivingEntity && entity.isAlive()) {
                primaryTarget = (LivingEntity) entity;
            }
        }
        if (primaryTarget == null) {
            EntityHitResult hit = raycastEntity(player, 18.0D);
            if (hit != null && hit.getEntity() instanceof LivingEntity) {
                primaryTarget = (LivingEntity) hit.getEntity();
            }
        }
        if (primaryTarget == null) {
            List<LivingEntity> nearby = level.getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(14.0D),
                    e -> e != player && e.isAlive()
            );
            if (!nearby.isEmpty()) {
                primaryTarget = nearby.get(0);
            }
        }

        if (primaryTarget == null) {
            player.displayClientMessage(Component.literal("Рядом нет целей для очарования Похотью (14 блоков).").withStyle(ChatFormatting.LIGHT_PURPLE), true);
            return;
        }

        if (!data.consumeManaWithOverdrive(25.0F, gameTime)) {
            player.displayClientMessage(Component.literal("Недостаточно маны (требуется 25).").withStyle(ChatFormatting.RED), true);
            return;
        }

        final LivingEntity centerTarget = primaryTarget;
        applyLustControl(player, centerTarget, level, gameTime);
        int extraCount = 0;
        for (Mob extraMob : level.getEntitiesOfClass(Mob.class, centerTarget.getBoundingBox().inflate(8.0D), e -> e != centerTarget && e.isAlive())) {
            applyLustControl(player, extraMob, level, gameTime);
            extraCount++;
            if (extraCount >= 5) break;
        }
        level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.5F, 1.2F);
    }

    public static void applyLustControl(ServerPlayer caster, LivingEntity target, ServerLevel level, long gameTime) {
        if (target instanceof Mob mob) {
            boolean isBoss = !mob.canChangeDimensions() || mob.getMaxHealth() >= 300.0F;

            if (isBoss) {
                mob.getPersistentData().putLong("sds_lust_nightmare_until", gameTime + 240L);
                mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 240, 0, false, true));
                mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 1, false, true));

                List<Mob> otherMobs = level.getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(24.0D), e -> e != mob && e.isAlive());
                if (!otherMobs.isEmpty()) {
                    mob.setTarget(otherMobs.get(0));
                } else {
                    mob.setTarget(null);
                }

                level.sendParticles(ParticleTypes.WITCH, mob.getX(), mob.getEyeY(), mob.getZ(), 35, 0.8D, 0.8D, 0.8D, 0.1D);
                caster.displayClientMessage(
                        Component.literal("Похоть [Иллюзия Кошмара]: Босс «" + mob.getName().getString() + "» дезориентирован и получает +45% входящего урона (12 сек)!")
                                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                        true
                );
                return;
            }

            mob.setTarget(null);
            mob.setPersistenceRequired();

            if (mob instanceof TamableAnimal tamable) {
                tamable.tame(caster);
                tamable.setOrderedToSit(false);
                level.broadcastEntityEvent(tamable, (byte) 7);
            }

            AttributeInstance maxHpAttr = mob.getAttribute(Attributes.MAX_HEALTH);
            if (maxHpAttr != null && maxHpAttr.getModifier(LUST_THRALL_HEALTH_UUID) == null) {
                maxHpAttr.addTransientModifier(new AttributeModifier(LUST_THRALL_HEALTH_UUID, "Lust Blood Contract HP", 3.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }

            AttributeInstance atk = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (atk != null && atk.getModifier(LUST_THRALL_DAMAGE_UUID) == null) {
                atk.addTransientModifier(new AttributeModifier(LUST_THRALL_DAMAGE_UUID, "Lust Blood Contract Damage", 1.5D, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }

            mob.setHealth(mob.getMaxHealth());
            mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 24000, 2, false, true));
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 24000, 1, false, true));
            mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 24000, 2, false, true));
            mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 24000, 1, false, true));

            String baseName = mob.getType().getDescription().getString();
            mob.setCustomName(Component.literal("❤ Страж Контракта " + caster.getName().getString() + " [" + baseName + "]").withStyle(ChatFormatting.LIGHT_PURPLE));
            mob.setCustomNameVisible(true);

            mob.getPersistentData().putUUID("sds_lust_master", caster.getUUID());
            mob.targetSelector.removeAllGoals(goal -> true);
            mob.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                    mob,
                    Mob.class,
                    10,
                    true,
                    false,
                    candidate -> candidate != mob && candidate instanceof Enemy
                            && !(candidate.getPersistentData().hasUUID("sds_lust_master")
                            && caster.getUUID().equals(candidate.getPersistentData().getUUID("sds_lust_master")))
            ));

            List<Mob> nearbyEnemies = level.getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(24.0D),
                    e -> e != mob && e.isAlive() && e instanceof Enemy
                            && !(e.getPersistentData().hasUUID("sds_lust_master")
                            && caster.getUUID().equals(e.getPersistentData().getUUID("sds_lust_master"))));
            if (!nearbyEnemies.isEmpty()) {
                mob.setTarget(nearbyEnemies.get(0));
            }

            level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.4D, mob.getZ(), 18, 0.5D, 0.5D, 0.5D, 0.1D);
            level.playSound(null, mob.blockPosition(), SoundEvents.WOLF_PANT, SoundSource.PLAYERS, 1.2F, 1.0F);
            caster.displayClientMessage(
                    Component.literal("Похоть [Кровавый Контракт]: " + baseName + " получил +300% HP и +150% урона и служит вам!")
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                    true
            );

        } else if (target instanceof ServerPlayer victimPlayer) {
            victimPlayer.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 160, 0, false, true, true));
            victimPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 3, false, true, true));
            victimPlayer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 2, false, true, true));

            float invertedYaw = (victimPlayer.getYRot() + 180.0F) % 360.0F;
            float invertedPitch = -victimPlayer.getXRot();
            victimPlayer.connection.send(new ClientboundPlayerPositionPacket(
                    victimPlayer.getX(),
                    victimPlayer.getY(),
                    victimPlayer.getZ(),
                    invertedYaw,
                    invertedPitch,
                    Collections.emptySet(),
                    0
            ));
            caster.displayClientMessage(Component.literal("Взгляд и разум игрока " + victimPlayer.getName().getString() + " искажены!").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }
    }

    /**
     * 4. ЗАВИСТЬ (ENVY) — КРАЖА СПОСОБНОСТЕЙ + ПРОБИТИЕ ЗАЩИТЫ БОССОВ
     */
    private static void executeEnvy(ServerPlayer player, PlayerSinsData data, ServerLevel level, long gameTime, int mode) {
        if (mode >= 200) {
            List<String> list = data.getStolenAbilities();
            int idx = mode - 200;
            if (idx >= 0 && idx < list.size()) {
                String chosen = list.get(idx);
                data.setStolenAbilityId(chosen);
                data.setActiveSubMode(1);
                level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.4F, 1.3F);
                player.displayClientMessage(
                        Component.literal("Зависть: Выбрана способность «" + chosen + "»! Нажмите [V] для применения.")
                                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                        true
                );
            }
            return;
        }

        EntityHitResult hit = raycastEntity(player, 24.0D);

        if (mode == 0 && hit != null && hit.getEntity() instanceof LivingEntity target) {
            if (!data.consumeManaWithOverdrive(20.0F, gameTime)) {
                player.displayClientMessage(Component.literal("Недостаточно маны для кражи способности (20 ед.).").withStyle(ChatFormatting.RED), true);
                return;
            }

            float drainedHp = Math.min(220.0F, Math.max(15.0F, target.getHealth() * 0.20F));
            CompatManager.dealMultiPhaseDamage(player, target, drainedHp, 3);
            player.heal(Math.min(25.0F, drainedHp * 0.5F));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 1, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 1, false, true));

            String moddedSkill = CompatManager.tryStealModdedAbility(player, target, data);
            if (!moddedSkill.isEmpty()) {
                player.displayClientMessage(
                        Component.literal("Зависть [Dragonfyre]: Похищена способность «" + moddedSkill + "» + подавлены трейты L2Hostility!")
                                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                        true
                );
            } else if (target instanceof EnderMan) {
                target.getPersistentData().putBoolean("sds_no_teleport", true);
                data.addStolenAbility("enderman_blink");
                player.displayClientMessage(Component.literal("Зависть: Украдена Телепортация Эндермена!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);

            } else if (target instanceof Warden) {
                target.getPersistentData().putBoolean("sds_no_sonic_boom", true);
                data.addStolenAbility("warden_sonic_boom");
                player.displayClientMessage(Component.literal("Зависть: Украден Звуковой Луч Sonic Boom Вардена!").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), true);

            } else if (target instanceof Creeper) {
                target.discard();
                data.addStolenAbility("creeper_blast_guard");
                player.getPersistentData().putBoolean("sds_explosion_immune", true);
                player.displayClientMessage(Component.literal("Зависть: Крипер поглощён! Получен иммунитет к взрывам + Катаклизм-взрыв!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);

            } else if (target instanceof ServerPlayer victim) {
                PlayerSinsData victimData = ModAttachmentTypes.get(victim);
                float stolenMana = victimData.getCurrentMana() * 0.5F;
                victimData.setCurrentMana(victimData.getCurrentMana() - stolenMana);
                data.addMana(stolenMana);
                ModAttachmentTypes.save(victim, victimData);
                ModNetwork.syncToPlayer(victim, victimData);

                List<MobEffectInstance> beneficial = new ArrayList<>();
                for (MobEffectInstance effect : victim.getActiveEffects()) {
                    if (effect.getEffect().isBeneficial()) {
                        beneficial.add(new MobEffectInstance(effect));
                    }
                }
                for (MobEffectInstance eff : beneficial) {
                    victim.removeEffect(eff.getEffect());
                    player.addEffect(eff);
                }
                data.addStolenAbility("warden_sonic_boom");
                player.displayClientMessage(Component.literal("Зависть: Похищено " + (int) stolenMana + " маны и " + beneficial.size() + " баффов!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);

            } else {
                data.addStolenAbility("enderman_blink");
                data.addStolenAbility("warden_sonic_boom");
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 2, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1, false, true, true));
                player.displayClientMessage(
                        Component.literal("Зависть: Похищена сила " + target.getName().getString() + "! Выберите способность через [X] -> Режим 3.")
                                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                        true
                );
            }
            level.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getEyeY(), target.getZ(), 20, 0.4D, 0.5D, 0.4D, 0.08D);
            level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.5F, 1.0F);
            return;
        }

        String stolen = data.getStolenAbilityId();
        if (stolen == null || stolen.isEmpty()) {
            stolen = "enderman_blink";
            data.addStolenAbility(stolen);
        }

        float mult = data.getDamageMultiplier(gameTime);

        if (CompatManager.executeModdedAbility(player, data, level, stolen, mult)) {
            return;
        }

        switch (stolen) {
            case "enderman_blink": {
                if (!data.consumeManaWithOverdrive(12.0F, gameTime)) return;
                Vec3 look = player.getLookAngle();
                Vec3 dest = player.position().add(look.scale(22.0D));
                player.teleportTo(dest.x, dest.y + 0.5D, dest.z);
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false, true));
                level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 0.5D, 0.8D, 0.5D, 0.2D);
                level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.5F, 1.0F);
                for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(4.0D), e -> e != player)) {
                    float dmg = CompatManager.calculateDragonfyreDamage(player, nearby, 28.0F, 0.04F, mult);
                    CompatManager.dealMultiPhaseDamage(player, nearby, dmg, 3);
                }
                break;
            }
            case "warden_sonic_boom": {
                if (!data.consumeManaWithOverdrive(25.0F, gameTime)) return;
                Vec3 start = player.getEyePosition();
                Vec3 dir = player.getLookAngle().normalize();
                level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.5F, 1.0F);
                for (int i = 1; i <= 24; i++) {
                    Vec3 point = start.add(dir.scale(i));
                    level.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(point, point).inflate(2.4D), e -> e != player)) {
                        float dmg = CompatManager.calculateDragonfyreDamage(player, victim, 55.0F, 0.08F, mult);
                        CompatManager.dealMultiPhaseDamage(player, victim, dmg, 5);
                        victim.knockback(2.5D, -dir.x, -dir.z);
                    }
                }
                break;
            }
            case "creeper_blast_guard": {
                if (!data.consumeManaWithOverdrive(20.0F, gameTime)) return;
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 1.0D, player.getZ(), 3, 1.0D, 0.5D, 1.0D, 0.0D);
                level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0F, 0.7F);
                for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0D), e -> e != player && e.isAlive())) {
                    float dmg = CompatManager.calculateDragonfyreDamage(player, victim, 65.0F, 0.08F, mult);
                    CompatManager.dealMultiPhaseDamage(player, victim, dmg, 4);
                    Vec3 push = victim.position().subtract(player.position()).normalize().scale(2.0D);
                    victim.setDeltaMovement(push.x, 0.9D, push.z);
                }
                break;
            }
            default: {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 3, false, true, true));
                player.displayClientMessage(Component.literal("Зависть: Активировано усиление " + stolen).withStyle(ChatFormatting.GREEN), true);
                break;
            }
        }
    }

    /**
     * 5. ЧРЕВОУГОДИЕ (GLUTTONY) — БЕЗДНА ПОГЛОЩЕНИЯ ДУШ
     */
    private static void executeGluttonyActive(ServerPlayer player, PlayerSinsData data, ServerLevel level, long gameTime) {
        List<SoulOrbEntity> orbs = level.getEntitiesOfClass(SoulOrbEntity.class, player.getBoundingBox().inflate(32.0D));
        for (SoulOrbEntity orb : orbs) {
            orb.absorbIntoPlayer(player);
        }

        Vec3 vortexCenter = player.position().add(player.getLookAngle().scale(4.0D));
        level.sendParticles(ParticleTypes.SOUL, vortexCenter.x, vortexCenter.y + 1.0D, vortexCenter.z, 45, 2.0D, 1.5D, 2.0D, 0.08D);
        level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.4F, 0.6F);

        int devouredCount = 0;
        float mult = data.getDamageMultiplier(gameTime);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(14.0D), e -> e != player && e.isAlive())) {
            if (victim.getPersistentData().hasUUID("sds_lust_master")
                    && player.getUUID().equals(victim.getPersistentData().getUUID("sds_lust_master"))) {
                continue;
            }
            Vec3 pull = vortexCenter.subtract(victim.position()).scale(0.35D);
            victim.setDeltaMovement(pull.x, 0.3D, pull.z);
            victim.hurtMarked = true;

            float drainDmg = CompatManager.calculateDragonfyreDamage(player, victim, 28.0F, 0.05F, mult);
            CompatManager.dealMultiPhaseDamage(player, victim, drainDmg, 3);
            devouredCount++;
        }

        if (devouredCount > 0) {
            float manaGain = devouredCount * 15.0F;
            data.setMaxMana(data.getMaxMana() + devouredCount * 2.0F);
            data.addMana(manaGain);
            player.heal(devouredCount * 6.0F);
            player.getFoodData().eat(8, 1.5F);
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 2, false, true, true));
            player.displayClientMessage(
                    Component.literal("Чревоугодие: Поглощена жизненная сила " + devouredCount + " врагов! (+" + (devouredCount * 2) + " макс. маны, +" + (int) manaGain + " маны)")
                            .withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD),
                    true
            );
        } else if (orbs.isEmpty()) {
            player.displayClientMessage(Component.literal("Воронка Чревоугодия: в радиусе 14 блоков нет целей для поглощения.").withStyle(ChatFormatting.DARK_GREEN), true);
        }
    }

    /**
     * 6. ГНЕВ (WRATH) — КАТАКЛИЗМ БЕРСЕРКА С ПРОБИТИЕМ DAMAGE CAP БОССОВ
     */
    private static void executeWrathShockwave(ServerPlayer player, PlayerSinsData data, ServerLevel level, long gameTime) {
        float boostedStacks = Math.min(100.0F, data.getWrathStacks() + 35.0F);
        data.setWrathStacks(boostedStacks);
        if (boostedStacks >= 100.0F) {
            data.setWrathBerserkUntil(gameTime + 400L);
        }
        updateWrathAttributes(player, data, gameTime);

        float mult = data.getDamageMultiplier(gameTime);
        float radius = 12.0F;
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 0.5D, player.getZ(), 3, 1.5D, 0.5D, 1.5D, 0.0D);
        level.sendParticles(ParticleTypes.LAVA, player.getX(), player.getY() + 1.0D, player.getZ(), 45, 3.0D, 1.0D, 3.0D, 0.2D);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0F, 0.6F);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5F, 0.8F);

        int hitCount = 0;
        for (LivingEntity enemy : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), e -> e != player && e.isAlive())) {
            if (enemy.getPersistentData().hasUUID("sds_lust_master")
                    && player.getUUID().equals(enemy.getPersistentData().getUUID("sds_lust_master"))) {
                continue;
            }
            enemy.setSecondsOnFire(10);
            enemy.getPersistentData().putLong("sds_hellblaze_until", gameTime + 200L);
            enemy.getPersistentData().putFloat("sds_locked_hp", enemy.getHealth());

            float dmg = CompatManager.calculateDragonfyreDamage(player, enemy, 60.0F, 0.08F, mult);
            CompatManager.dealMultiPhaseDamage(player, enemy, dmg, 5);
            Vec3 push = enemy.position().subtract(player.position()).normalize().scale(1.8D);
            enemy.setDeltaMovement(push.x, 0.85D, push.z);
            hitCount++;
        }

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 2, false, true, true));
        player.displayClientMessage(
                Component.literal("КАТАКЛИЗМ ГНЕВА (" + (int) boostedStacks + "%): Сокрушено врагов: " + hitCount + " (Каскадный пробой Damage Cap x" + String.format("%.1f", mult) + ")!")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                true
        );
    }

    /**
     * 7. ЛЕНЬ (SLOTH) — ГИБЕРНАЦИЯ И 5x ПРОБУЖДЕНИЕ КОРОЛЯ
     */
    private static void executeSlothBurst(ServerPlayer player, PlayerSinsData data, ServerLevel level, long gameTime) {
        player.setHealth(player.getMaxHealth());
        data.setCurrentMana(data.getMaxMana());

        List<MobEffectInstance> toRemove = new ArrayList<>();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (!effect.getEffect().isBeneficial()) {
                toRemove.add(effect);
            }
        }
        for (MobEffectInstance debuff : toRemove) {
            player.removeEffect(debuff.getEffect());
        }

        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 4, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 3, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 3, false, true, true));

        for (LivingEntity enemy : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(20.0D), e -> e != player && e.isAlive())) {
            if (enemy.getPersistentData().hasUUID("sds_lust_master")
                    && player.getUUID().equals(enemy.getPersistentData().getUUID("sds_lust_master"))) {
                continue;
            }
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600, 6, false, true));
            enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 4, false, true));
            enemy.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 600, 4, false, true));
        }

        long lockEnd = gameTime + 200L;
        data.setSlothLockedUntil(lockEnd);
        data.setSlothBuffUntil(lockEnd + 3600L);

        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 1.0D, player.getZ(), 55, 3.0D, 1.0D, 3.0D, 0.05D);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 2.0F, 0.8F);
        player.displayClientMessage(
                Component.literal("Абсолютный Бурст Лени: 100% HP и Маны + 20 Золотых Сердец! Через 10 сек активируется 5x Пробуждение!")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                true
        );
    }

    public static void updateWrathAttributes(ServerPlayer player, PlayerSinsData data, long gameTime) {
        int tiers = (int) (data.getWrathStacks() / 10.0F);
        boolean berserk = gameTime < data.getWrathBerserkUntil();
        boolean slothAwakened = gameTime >= data.getSlothLockedUntil() && gameTime < data.getSlothBuffUntil();
        double overdriveMult = data.isSinOverdriveActive(gameTime) ? 1.5D : 1.0D;

        double attackPct = (tiers * 0.35D + (data.getDragonfyreSoulRank() * 0.05D)) * overdriveMult;
        if (berserk) {
            attackPct = Math.max(attackPct, 3.0D * overdriveMult);
        }
        double speedPct = (tiers * 0.12D) * overdriveMult;

        AttributeInstance attackAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            AttributeModifier existing = attackAttr.getModifier(WRATH_ATTACK_MODIFIER_UUID);
            if (existing == null || Math.abs(existing.getAmount() - attackPct) > 0.001D) {
                attackAttr.removeModifier(WRATH_ATTACK_MODIFIER_UUID);
                if (attackPct > 0.001D) {
                    attackAttr.addTransientModifier(new AttributeModifier(
                            WRATH_ATTACK_MODIFIER_UUID,
                            "SDS Wrath Attack Boost",
                            attackPct,
                            AttributeModifier.Operation.MULTIPLY_TOTAL
                    ));
                }
            }
        }

        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            AttributeModifier existing = speedAttr.getModifier(WRATH_SPEED_MODIFIER_UUID);
            if (existing == null || Math.abs(existing.getAmount() - speedPct) > 0.001D) {
                speedAttr.removeModifier(WRATH_SPEED_MODIFIER_UUID);
                if (speedPct > 0.001D) {
                    speedAttr.addTransientModifier(new AttributeModifier(
                            WRATH_SPEED_MODIFIER_UUID,
                            "SDS Wrath Speed Boost",
                            speedPct,
                            AttributeModifier.Operation.MULTIPLY_TOTAL
                    ));
                }
            }
        }

        CompatManager.updateDragonfyreAttributes(player, data, tiers, berserk, slothAwakened);
    }

    public static EntityHitResult raycastEntity(ServerPlayer player, double maxDistance) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle().scale(maxDistance);
        Vec3 endPos = eyePos.add(lookVec);
        AABB searchBox = player.getBoundingBox().expandTowards(lookVec).inflate(1.25D);
        return ProjectileUtil.getEntityHitResult(
                player,
                eyePos,
                endPos,
                searchBox,
                entity -> !entity.isSpectator() && entity.isPickable() && entity != player,
                maxDistance * maxDistance
        );
    }
}
