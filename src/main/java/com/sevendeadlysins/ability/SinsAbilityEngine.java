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

    public static void executeActiveSin(ServerPlayer player, PlayerSinsData data, int sinId, int mode) {
        ServerLevel level = player.serverLevel();
        long gameTime = level.getGameTime();

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
                executeGreed(player, data, level, mode);
                break;
            case 2:
                executeLust(player, data, level);
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
     * 1. ГОРДЫНЯ (PRIDE) — БАЛАНС DRAGONFYRE 1.20.1
     * - Закон 0 (ОСТАНОВКА ВРЕМЕНИ): Полностью замораживает врагов и летящие снаряды в радиусе 56 блоков.
     *   Повторное нажатие [V] в любой момент досрочно снимает остановку времени!
     * - Закон 1 (ГРАВИТАЦИОННЫЙ КОЛЛАПС): Подбрасывает всех врагов в радиусе 45 блоков и обрушивает их о землю
     *   с гибридным уроном (85 + % от макс. HP босса) и ударом молнии.
     * - Закон 2 (СОЛНЕЧНЫЙ ЗЕНИТ): Устанавливает полдень (6000L), даёт полную неуязвимость, Силу IV и солнечную ауру.
     */
    private static void executePride(ServerPlayer player, PlayerSinsData data, ServerLevel level, long gameTime, int lawMode) {
        int chosenLaw = Mth.clamp(lawMode, 0, 2);

        if (chosenLaw == 0 && data.getActivePrideLaw() == 0 && gameTime < data.getPrideActiveUntil()) {
            data.setActivePrideLaw(-1);
            data.setPrideActiveUntil(0L);
            unfreezeTimeInArea(player, level, 72.0D);
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 2.0F, 1.2F);
            player.displayClientMessage(
                    Component.literal("Гордыня: Ход времени возобновлён!")
                            .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD),
                    true
            );
            return;
        }

        if (gameTime < data.getPrideCooldownUntil()) {
            long ticksLeft = data.getPrideCooldownUntil() - gameTime;
            player.displayClientMessage(
                    Component.literal("Гордыня восстанавливается: " + (ticksLeft / 20L) + " сек.")
                            .withStyle(ChatFormatting.GOLD),
                    true
            );
            return;
        }

        if (!data.consumeMana(35.0F)) {
            player.displayClientMessage(Component.literal("Недостаточно маны (требуется 35).").withStyle(ChatFormatting.RED), true);
            return;
        }

        data.setActivePrideLaw(chosenLaw);
        data.setPrideActiveUntil(gameTime + 900L);
        data.setPrideCooldownUntil(gameTime + 200L);

        if (chosenLaw == 0) {
            AABB box = player.getBoundingBox().inflate(56.0D);
            int frozenCount = 0;
            for (Mob mob : level.getEntitiesOfClass(Mob.class, box)) {
                if (mob.getPersistentData().hasUUID("sds_lust_master")
                        && player.getUUID().equals(mob.getPersistentData().getUUID("sds_lust_master"))) {
                    continue;
                }
                mob.setNoAi(true);
                mob.setNoGravity(true);
                mob.setDeltaMovement(Vec3.ZERO);
                mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 900, 0, false, false));
                mob.getPersistentData().putBoolean("sds_time_stopped", true);
                mob.getPersistentData().putDouble("sds_freeze_x", mob.getX());
                mob.getPersistentData().putDouble("sds_freeze_y", mob.getY());
                mob.getPersistentData().putDouble("sds_freeze_z", mob.getZ());
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
            level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 65, 4.5D, 2.0D, 4.5D, 0.02D);
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0F, 0.5F);
            level.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 2.0F, 0.5F);
            player.displayClientMessage(
                    Component.literal("Закон Гордыни: ОСТАНОВКА ВРЕМЕНИ! Заморожено целей: " + frozenCount + " (Повтор [V] — снять)")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    true
            );

        } else if (chosenLaw == 1) {
            AABB box = player.getBoundingBox().inflate(45.0D);
            int lifted = 0;
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
                if (target.getPersistentData().hasUUID("sds_lust_master")
                        && player.getUUID().equals(target.getPersistentData().getUUID("sds_lust_master"))) {
                    continue;
                }
                target.setDeltaMovement(target.getDeltaMovement().x, 2.15D, target.getDeltaMovement().z);
                target.hurtMarked = true;
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false));
                target.getPersistentData().putLong("sds_gravity_slam_tick", gameTime + 35L);
                lifted++;
            }
            level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 75, 4.5D, 2.0D, 4.5D, 0.25D);
            level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.PLAYERS, 2.0F, 0.6F);
            player.displayClientMessage(
                    Component.literal("Закон Гордыни: Гравитационный Коллапс! Поднято врагов: " + lifted + " (85 + 12% Max HP урона)")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    true
            );

        } else {
            level.setDayTime(6000L);
            level.setWeatherParameters(12000, 0, false, false);
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 900, 3, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 2, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 900, 0, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 900, 4, false, true, true));
            level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 65, 2.2D, 1.2D, 2.2D, 0.1D);
            level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.8F, 1.1F);
            player.displayClientMessage(
                    Component.literal("Закон Гордыни: Солнечный Зенит! (Полдень + Полная Неуязвимость + Выжигающая Аура)")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    true
            );
        }
    }

    public static void unfreezeTimeInArea(ServerPlayer player, ServerLevel level, double radius) {
        AABB box = player.getBoundingBox().inflate(radius);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, box)) {
            if (mob.getPersistentData().getBoolean("sds_time_stopped")) {
                mob.setNoAi(false);
                mob.setNoGravity(false);
                mob.getPersistentData().remove("sds_time_stopped");
                mob.getPersistentData().remove("sds_freeze_x");
                mob.getPersistentData().remove("sds_freeze_y");
                mob.getPersistentData().remove("sds_freeze_z");
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
     * 2. АЛЧНОСТЬ (GREED) — ПОДДЕРЖКА АФФИКСОВ APOTHEOSIS И БЛОКОВ 1.20.1
     * Копирует блоки по взгляду прямо в инвентарь, сканирует экипировку боссов/мобов и сохраняет все
     * NBT-аффиксы Apotheosis, сокеты самоцветов, свитки Iron's Spells и мифическое оружие Simply Swords.
     */
    private static void executeGreed(ServerPlayer player, PlayerSinsData data, ServerLevel level, int mode) {
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
                            Component.literal("Каталог Алчности пуст. Наведите прицел на блок/моба в Режиме 1 или возьмите артефакт в руку!")
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

            float manaCost = 30.0F;
            if (!data.consumeMana(manaCost)) {
                player.displayClientMessage(Component.literal("Недостаточно маны для репликации (требуется 30 ед.).").withStyle(ChatFormatting.RED), true);
                return;
            }

            ItemStack exactCopy = reconstructed.copy();
            exactCopy.setCount(1);
            CompatManager.onGreedItemReplicated(player, exactCopy);

            if (!player.getInventory().add(exactCopy)) {
                player.drop(exactCopy, false);
            }
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 28, 0.5D, 0.5D, 0.5D, 0.15D);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.8F, 1.2F);
            player.displayClientMessage(
                    Component.literal("Алчность материализовала копию со всеми NBT/Apotheosis чарами: ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                            .append(exactCopy.getHoverName()),
                    true
            );
            return;
        }

        // Режим 0: Везение III (увеличивает шанс мифического лута Apotheosis!), Спешка II и магнит лута (24 блока)
        player.addEffect(new MobEffectInstance(MobEffects.LUCK, 1200, 2, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 1, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 1200, 1, false, true, true));

        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(24.0D))) {
            itemEntity.setNoPickUpDelay();
            itemEntity.teleportTo(player.getX(), player.getY() + 0.5D, player.getZ());
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
     * 3. ПОХОТЬ (LUST) — ПРИРУЧЕНИЕ КАК ВЕРНОЙ СОБАКИ
     */
    public static void executeLust(ServerPlayer player, PlayerSinsData data, ServerLevel level) {
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

        if (!data.consumeMana(25.0F)) {
            player.displayClientMessage(Component.literal("Недостаточно маны (требуется 25).").withStyle(ChatFormatting.RED), true);
            return;
        }

        final LivingEntity centerTarget = primaryTarget;
        applyLustControl(player, centerTarget, level);
        int extraCount = 0;
        for (Mob extraMob : level.getEntitiesOfClass(Mob.class, centerTarget.getBoundingBox().inflate(8.0D), e -> e != centerTarget && e.isAlive())) {
            applyLustControl(player, extraMob, level);
            extraCount++;
            if (extraCount >= 5) break;
        }
        level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.5F, 1.2F);
    }

    public static void applyLustControl(ServerPlayer caster, LivingEntity target, ServerLevel level) {
        if (target instanceof Mob) {
            Mob mob = (Mob) target;
            mob.setTarget(null);
            mob.setPersistenceRequired();

            if (mob instanceof TamableAnimal) {
                TamableAnimal tamable = (TamableAnimal) mob;
                tamable.tame(caster);
                tamable.setOrderedToSit(false);
                level.broadcastEntityEvent(tamable, (byte) 7);
            }

            mob.setHealth(mob.getMaxHealth());
            mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 24000, 1, false, true));
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 24000, 1, false, true));
            mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 24000, 1, false, true));
            mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 24000, 1, false, true));

            String baseName = mob.getType().getDescription().getString();
            mob.setCustomName(Component.literal("❤ Питомец " + caster.getName().getString() + " [" + baseName + "]").withStyle(ChatFormatting.LIGHT_PURPLE));
            mob.setCustomNameVisible(true);

            AttributeInstance atk = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (atk != null && atk.getModifier(LUST_THRALL_DAMAGE_UUID) == null) {
                atk.addTransientModifier(new AttributeModifier(LUST_THRALL_DAMAGE_UUID, "Lust Thrall Damage", 0.85D, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }

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
                    Component.literal("Похоть: " + baseName + " приручён как верный пёс! Следует за вами, телепортируется и защищает хозяина!")
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                    true
            );

        } else if (target instanceof ServerPlayer) {
            ServerPlayer victimPlayer = (ServerPlayer) target;
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
     * 4. ЗАВИСТЬ (ENVY) — КРАЖА СПОСОБНОСТЕЙ ИЗ МОДОВ CISCO'S RPG [DRAGONFYRE]
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

        if (mode == 0 && hit != null && hit.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) hit.getEntity();
            if (!data.consumeMana(20.0F)) {
                player.displayClientMessage(Component.literal("Недостаточно маны для кражи способности (20 ед.).").withStyle(ChatFormatting.RED), true);
                return;
            }

            float drainedHp = Math.min(180.0F, Math.max(10.0F, target.getHealth() * 0.20F));
            target.hurt(level.damageSources().magic(), drainedHp);
            player.heal(Math.min(20.0F, drainedHp * 0.5F));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 1, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 1, false, true));

            String moddedSkill = CompatManager.tryStealModdedAbility(player, target, data);
            if (!moddedSkill.isEmpty()) {
                player.displayClientMessage(
                        Component.literal("Зависть [Dragonfyre]: Похищена способность «" + moddedSkill + "»! (В арсенале: " + data.getStolenAbilities().size() + ")")
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

            } else if (target instanceof ServerPlayer) {
                ServerPlayer victim = (ServerPlayer) target;
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
                if (!data.consumeMana(12.0F)) return;
                Vec3 look = player.getLookAngle();
                Vec3 dest = player.position().add(look.scale(22.0D));
                player.teleportTo(dest.x, dest.y + 0.5D, dest.z);
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false, true));
                level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 0.5D, 0.8D, 0.5D, 0.2D);
                level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.5F, 1.0F);
                for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(4.0D), e -> e != player)) {
                    float dmg = CompatManager.calculateDragonfyreDamage(player, nearby, 28.0F, 0.04F, mult);
                    nearby.hurt(level.damageSources().magic(), dmg);
                }
                break;
            }
            case "warden_sonic_boom": {
                if (!data.consumeMana(25.0F)) return;
                Vec3 start = player.getEyePosition();
                Vec3 dir = player.getLookAngle().normalize();
                level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.5F, 1.0F);
                for (int i = 1; i <= 24; i++) {
                    Vec3 point = start.add(dir.scale(i));
                    level.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(point, point).inflate(2.4D), e -> e != player)) {
                        float dmg = CompatManager.calculateDragonfyreDamage(player, victim, 55.0F, 0.08F, mult);
                        victim.hurt(level.damageSources().sonicBoom(player), dmg);
                        victim.knockback(2.5D, -dir.x, -dir.z);
                    }
                }
                break;
            }
            case "creeper_blast_guard": {
                if (!data.consumeMana(20.0F)) return;
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 1.0D, player.getZ(), 3, 1.0D, 0.5D, 1.0D, 0.0D);
                level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0F, 0.7F);
                for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0D), e -> e != player && e.isAlive())) {
                    float dmg = CompatManager.calculateDragonfyreDamage(player, victim, 65.0F, 0.08F, mult);
                    victim.hurt(level.damageSources().explosion(player, player), dmg);
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
            victim.hurt(level.damageSources().magic(), drainDmg);
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
     * 6. ГНЕВ (WRATH) — КАТАКЛИЗМ БЕРСЕРКА DRAGONFYRE
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
            enemy.setSecondsOnFire(8);
            float dmg = CompatManager.calculateDragonfyreDamage(player, enemy, 60.0F, 0.08F, mult);
            enemy.hurt(level.damageSources().playerAttack(player), dmg);
            Vec3 push = enemy.position().subtract(player.position()).normalize().scale(1.8D);
            enemy.setDeltaMovement(push.x, 0.85D, push.z);
            hitCount++;
        }

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 2, false, true, true));
        player.displayClientMessage(
                Component.literal("КАТАКЛИЗМ ГНЕВА (" + (int) boostedStacks + "%): Сокрушено врагов: " + hitCount + " (Множитель x" + String.format("%.1f", mult) + ")!")
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

        double attackPct = tiers * 0.35D + (data.getDragonfyreSoulRank() * 0.05D);
        if (berserk) {
            attackPct = Math.max(attackPct, 3.0D);
        }
        double speedPct = tiers * 0.12D;

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
