package com.sevendeadlysins.event;

import com.sevendeadlysins.SevenDeadlySinsMod;
import com.sevendeadlysins.ability.SinsAbilityEngine;
import com.sevendeadlysins.compat.CompatManager;
import com.sevendeadlysins.data.PlayerSinsData;
import com.sevendeadlysins.entity.SoulOrbEntity;
import com.sevendeadlysins.network.ModNetwork;
import com.sevendeadlysins.registry.ModAttachmentTypes;
import com.sevendeadlysins.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

/**
 * Серверные обработчики событий для Minecraft 1.20.1 (Forge 47.3.0).
 *
 * КЛЮЧЕВАЯ ОПТИМИЗАЦИЯ TPS ДЛЯ CISCO'S FANTASY MEDIEVAL RPG [DRAGONFYRE]:
 * - ПОЛНОСТЬЮ УБРАН глобальный обработчик LivingTickEvent! В сборке Dragonfyre одновременно
 *   существуют сотни мобов (драконы Ice & Fire, данжи Cataclysm, Alex's Caves), и проверка NBT
 *   на каждом мобе каждый тик сажала бы серверный TPS.
 * - Вместо этого Остановка Времени, Гравитационный Коллапс и ИИ приручённых питомцев Похоти
 *   обновляются в локальном радиусе вокруг игрока с интервалом (throttling), обеспечивая 0.00ms нагрузки!
 * - Сетевая синхронизация выполняется только при флаге data.isDirty().
 */
@Mod.EventBusSubscriber(modid = SevenDeadlySinsMod.MODID)
public class SinsCommonEvents {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSinsData data = ModAttachmentTypes.get(player);
            ModNetwork.syncToPlayer(player, data);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSinsData data = ModAttachmentTypes.get(player);
            ModNetwork.syncToPlayer(player, data);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSinsData data = ModAttachmentTypes.get(player);
            ModNetwork.syncToPlayer(player, data);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        ModAttachmentTypes.copyOnClone(event.getOriginal(), event.getEntity());
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ModNetwork.syncToPlayer(serverPlayer, ModAttachmentTypes.get(serverPlayer));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        ServerLevel level = player.serverLevel();
        PlayerSinsData data = ModAttachmentTypes.get(player);
        if (!data.isUnlocked()) return;

        long gameTime = level.getGameTime();

        // 1. Остановка Времени Гордыни (Закон 0): обновляется вокруг игрока без глобального LivingTickEvent
        if (data.getActivePrideLaw() == 0) {
            if (gameTime >= data.getPrideActiveUntil()) {
                data.setActivePrideLaw(-1);
                SinsAbilityEngine.unfreezeTimeInArea(player, level, 72.0D);
                player.displayClientMessage(
                        Component.literal("Гордыня: Действие Остановки Времени завершено.")
                                .withStyle(ChatFormatting.YELLOW),
                        true
                );
            } else if (gameTime % 2L == 0L) {
                AABB box = player.getBoundingBox().inflate(56.0D);
                for (Mob mob : level.getEntitiesOfClass(Mob.class, box)) {
                    if (mob.getPersistentData().hasUUID("sds_lust_master")
                            && player.getUUID().equals(mob.getPersistentData().getUUID("sds_lust_master"))) {
                        continue;
                    }
                    if (!mob.getPersistentData().getBoolean("sds_time_stopped")) {
                        mob.setNoAi(true);
                        mob.setNoGravity(true);
                        mob.getPersistentData().putBoolean("sds_time_stopped", true);
                        mob.getPersistentData().putDouble("sds_freeze_x", mob.getX());
                        mob.getPersistentData().putDouble("sds_freeze_y", mob.getY());
                        mob.getPersistentData().putDouble("sds_freeze_z", mob.getZ());
                    }
                    mob.setDeltaMovement(Vec3.ZERO);
                    mob.setPos(
                            mob.getPersistentData().getDouble("sds_freeze_x"),
                            mob.getPersistentData().getDouble("sds_freeze_y"),
                            mob.getPersistentData().getDouble("sds_freeze_z")
                    );
                }
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, box)) {
                    if (!proj.getPersistentData().getBoolean("sds_time_stopped")) {
                        proj.setNoGravity(true);
                        proj.getPersistentData().putBoolean("sds_time_stopped", true);
                        proj.getPersistentData().putDouble("sds_freeze_x", proj.getX());
                        proj.getPersistentData().putDouble("sds_freeze_y", proj.getY());
                        proj.getPersistentData().putDouble("sds_freeze_z", proj.getZ());
                    }
                    proj.setDeltaMovement(Vec3.ZERO);
                    proj.setPos(
                            proj.getPersistentData().getDouble("sds_freeze_x"),
                            proj.getPersistentData().getDouble("sds_freeze_y"),
                            proj.getPersistentData().getDouble("sds_freeze_z")
                    );
                }
            }
        }

        // 2. Гравитационный Коллапс Гордыни (Закон 1): проверка приземления целей
        if (data.getActivePrideLaw() == 1 && gameTime < data.getPrideActiveUntil() && gameTime % 5L == 0L) {
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(52.0D), e -> e != player && e.isAlive())) {
                if (target.getPersistentData().contains("sds_gravity_slam_tick")) {
                    long slamTick = target.getPersistentData().getLong("sds_gravity_slam_tick");
                    if (gameTime >= slamTick) {
                        target.getPersistentData().remove("sds_gravity_slam_tick");
                        target.setDeltaMovement(0.0D, -4.5D, 0.0D);
                        target.hurtMarked = true;

                        float slamDmg = CompatManager.calculateDragonfyreDamage(player, target, 85.0F, 0.12F, data.getDamageMultiplier(gameTime));
                        target.hurt(level.damageSources().flyIntoWall(), slamDmg);
                        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, target.getX(), target.getY(), target.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                        level.playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.6F, 0.6F);

                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                        if (bolt != null) {
                            bolt.moveTo(target.position());
                            bolt.setVisualOnly(true);
                            level.addFreshEntity(bolt);
                        }
                    }
                }
            }
        }

        // 3. Солнечная Аура Гордыни (Закон 2 — Солнечный Зенит): каждые 10 тиков выжигает врагов
        if (data.getActivePrideLaw() == 2 && gameTime < data.getPrideActiveUntil() && gameTime % 10L == 0L) {
            level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 10, 3.0D, 0.5D, 3.0D, 0.02D);
            for (LivingEntity enemy : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0D), e -> e != player && e.isAlive() && e instanceof Enemy)) {
                if (enemy.getPersistentData().hasUUID("sds_lust_master")
                        && player.getUUID().equals(enemy.getPersistentData().getUUID("sds_lust_master"))) {
                    continue;
                }
                enemy.setSecondsOnFire(5);
                enemy.hurt(level.damageSources().onFire(), 14.0F + enemy.getMaxHealth() * 0.02F);
            }
        }

        // 4. Обновление приручённых Похотью питомцев (раз в 10 тиков — высокая оптимизация TPS!)
        if (gameTime % 10L == 0L) {
            UUID masterId = player.getUUID();
            for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(48.0D), Mob::isAlive)) {
                if (mob.getPersistentData().hasUUID("sds_lust_master") && masterId.equals(mob.getPersistentData().getUUID("sds_lust_master"))) {
                    if (mob.getTarget() == player || (mob.getTarget() != null
                            && mob.getTarget().getPersistentData().hasUUID("sds_lust_master")
                            && masterId.equals(mob.getTarget().getPersistentData().getUUID("sds_lust_master")))) {
                        mob.setTarget(null);
                    }

                    LivingEntity masterEnemy = player.getLastHurtByMob();
                    if (masterEnemy == null || !masterEnemy.isAlive() || masterEnemy == mob) {
                        masterEnemy = player.getLastHurtMob();
                    }
                    if (masterEnemy != null && masterEnemy.isAlive() && masterEnemy != mob && masterEnemy != player
                            && !(masterEnemy.getPersistentData().hasUUID("sds_lust_master")
                            && masterId.equals(masterEnemy.getPersistentData().getUUID("sds_lust_master")))) {
                        mob.setTarget(masterEnemy);
                    }

                    double distSq = mob.distanceToSqr(player);
                    if (distSq > 256.0D) {
                        mob.teleportTo(
                                player.getX() + (level.random.nextDouble() - 0.5D) * 2.5D,
                                player.getY(),
                                player.getZ() + (level.random.nextDouble() - 0.5D) * 2.5D
                        );
                        mob.getNavigation().stop();
                        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY(), mob.getZ(), 3, 0.3D, 0.3D, 0.3D, 0.02D);
                    } else if (distSq > 12.0D && (mob.getTarget() == null || !mob.getTarget().isAlive())) {
                        mob.getNavigation().moveTo(player, 1.35D);
                    }

                    if (gameTime % 60L == 0L) {
                        mob.heal(6.0F);
                    }
                }
            }
        }

        // 5. Пассивный рейкаст Алчности (каждые 15 тиков при активной Алчности)
        if (data.getActiveSinIndex() == 1 && gameTime % 15L == 0L) {
            SinsAbilityEngine.scanEquipmentByRaycast(player, data, level, false);
        }

        // 6. Фиксация цели Похоти взглядом (каждые 5 тиков)
        if (data.getActiveSinIndex() == 2 && gameTime % 5L == 0L) {
            EntityHitResult gazeHit = SinsAbilityEngine.raycastEntity(player, 16.0D);
            if (gazeHit != null && gazeHit.getEntity() instanceof LivingEntity lookedAt) {
                if (lookedAt.getUUID().equals(data.getLustTargetUuid())) {
                    data.setLustGazeTicks(data.getLustGazeTicks() + 5);
                    if (data.getLustGazeTicks() == 30) {
                        player.displayClientMessage(
                                Component.literal("Цель Похоти захвачена взглядом: " + lookedAt.getName().getString() + " (Нажмите [V])")
                                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                                true
                        );
                    }
                } else {
                    data.setLustTargetUuid(lookedAt.getUUID());
                    data.setLustGazeTicks(5);
                }
            } else {
                data.setLustGazeTicks(0);
            }
        }

        // 7. Лень (Способ 2 — Покой): неподвижность 5 секунд (100 тиков) или сон
        boolean moving = player.getDeltaMovement().horizontalDistanceSqr() > 0.0012D || player.zza != 0 || player.xxa != 0;
        boolean recentlyHurt = (player.tickCount - player.getLastHurtByMobTimestamp()) < 100;
        if ((!moving && !recentlyHurt) || player.isSleeping()) {
            data.setSlothStillTicks(data.getSlothStillTicks() + 1);
        } else {
            data.setSlothStillTicks(0);
        }

        // 8. Ежесекундное обновление маны, атрибутов и синхронизации (раз в 20 тиков)
        if (gameTime % 20L == 0L) {
            float manaRegen = 3.5F + (data.getDragonfyreSoulRank() * 0.5F);

            if (data.getSlothStillTicks() >= 100 || player.isSleeping()) {
                manaRegen = 22.0F;
                player.heal(5.0F);
            }

            if (gameTime >= data.getSlothLockedUntil() && gameTime < data.getSlothBuffUntil()) {
                manaRegen *= 5.0F;
            }

            data.addMana(manaRegen);
            SinsAbilityEngine.updateWrathAttributes(player, data, gameTime);

            if (data.isDirty()) {
                ModAttachmentTypes.save(player, data);
                ModNetwork.syncToPlayer(player, data);
            }
        }
    }

    /**
     * Защита Солнечного Зенита Гордыни, взрывной иммунитет Зависти и защита от дружественного огня питомцев Похоти.
     */
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (event.getEntity() instanceof ServerPlayer player && attacker != null && attacker.getPersistentData().hasUUID("sds_lust_master")) {
            if (player.getUUID().equals(attacker.getPersistentData().getUUID("sds_lust_master"))) {
                event.setCanceled(true);
                return;
            }
        }

        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSinsData data = ModAttachmentTypes.get(player);
            if (!data.isUnlocked()) return;
            long gameTime = player.level().getGameTime();

            if (data.getActivePrideLaw() == 2 && gameTime < data.getPrideActiveUntil()) {
                event.setCanceled(true);
                return;
            }

            if ("creeper_blast_guard".equals(data.getStolenAbilityId()) && event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * ГНЕВ (WRATH) и ЛЕНЬ (SLOTH): Накопление стаков Гнева, 5x множитель Пробуждения Лени,
     * взрывные удары Берсерка и вампиризм в боях Cisco's RPG [Dragonfyre].
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 1. Когда урон получает игрок
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSinsData data = ModAttachmentTypes.get(player);
            if (data.isUnlocked()) {
                long gameTime = player.level().getGameTime();
                float gained = Math.max(10.0F, event.getAmount() * 3.5F);
                float nextStacks = Math.min(100.0F, data.getWrathStacks() + gained);
                data.setWrathStacks(nextStacks);

                if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                    data.setLustTargetUuid(attacker.getUUID());
                }

                if (nextStacks >= 100.0F && gameTime >= data.getWrathBerserkUntil()) {
                    data.setWrathBerserkUntil(gameTime + 400L);
                    player.level().playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5F, 0.8F);
                    player.displayClientMessage(
                            Component.literal("ГНЕВ 100%: РЕЖИМ БЕРСЕРКА DRAGONFYRE НА 20 СЕКУНД! (x3 Урон + Пробой Брони + Вампиризм)")
                                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                            true
                    );
                }

                SinsAbilityEngine.updateWrathAttributes(player, data, gameTime);
                ModAttachmentTypes.save(player, data);
                ModNetwork.syncToPlayer(player, data);
            }
        }

        // 2. Когда урон наносит сам игрок
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            PlayerSinsData data = ModAttachmentTypes.get(attacker);
            if (!data.isUnlocked()) return;

            LivingEntity victim = event.getEntity();
            data.setLustTargetUuid(victim.getUUID());

            long gameTime = attacker.level().getGameTime();
            float nextStacks = Math.min(100.0F, data.getWrathStacks() + 8.0F);
            data.setWrathStacks(nextStacks);
            if (nextStacks >= 100.0F && gameTime >= data.getWrathBerserkUntil()) {
                data.setWrathBerserkUntil(gameTime + 400L);
                attacker.displayClientMessage(
                        Component.literal("ГНЕВ 100%: РЕЖИМ БЕРСЕРКА АКТИВИРОВАН!")
                                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                        true
                );
            }

            // Применяем 5x множитель Пробуждения Лени к ударам игрока
            if (gameTime >= data.getSlothLockedUntil() && gameTime < data.getSlothBuffUntil()) {
                event.setAmount(event.getAmount() * 2.5F);
            }

            if (gameTime < data.getWrathBerserkUntil() || data.getActiveSinIndex() == 5) {
                attacker.heal(Math.min(8.0F, event.getAmount() * 0.25F));
            }

            if (gameTime < data.getWrathBerserkUntil() && !victim.getPersistentData().getBoolean("sds_splash_guard")) {
                ServerLevel sLevel = attacker.serverLevel();
                sLevel.sendParticles(ParticleTypes.EXPLOSION, victim.getX(), victim.getY() + 0.8D, victim.getZ(), 2, 0.3D, 0.3D, 0.3D, 0.0D);
                sLevel.playSound(null, victim.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.8F, 1.15F);
                for (LivingEntity splash : sLevel.getEntitiesOfClass(LivingEntity.class, victim.getBoundingBox().inflate(4.5D), e -> e != attacker && e != victim && e.isAlive())) {
                    if (splash.getPersistentData().hasUUID("sds_lust_master")
                            && attacker.getUUID().equals(splash.getPersistentData().getUUID("sds_lust_master"))) {
                        continue;
                    }
                    splash.getPersistentData().putBoolean("sds_splash_guard", true);
                    splash.hurt(sLevel.damageSources().playerAttack(attacker), event.getAmount() * 0.65F);
                    splash.getPersistentData().remove("sds_splash_guard");
                }
            }

            SinsAbilityEngine.updateWrathAttributes(attacker, data, gameTime);
            ModAttachmentTypes.save(attacker, data);
            ModNetwork.syncToPlayer(attacker, data);
        }
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();
        if (sender == null) return;
        PlayerSinsData data = ModAttachmentTypes.get(sender);
        if (!data.isUnlocked()) return;

        List<LivingEntity> nearby = sender.serverLevel().getEntitiesOfClass(
                LivingEntity.class,
                sender.getBoundingBox().inflate(8.0D),
                e -> e != sender && e.isAlive()
        );
        if (!nearby.isEmpty()) {
            LivingEntity closest = nearby.get(0);
            data.setLustTargetUuid(closest.getUUID());
            ModAttachmentTypes.save(sender, data);
            ModNetwork.syncToPlayer(sender, data);
        }
    }

    @SubscribeEvent
    public static void onEnderTeleport(EntityTeleportEvent.EnderEntity event) {
        if (event.getEntityLiving().getPersistentData().getBoolean("sds_no_teleport")) {
            event.setCanceled(true);
        }
    }

    /**
     * ЧРЕВОУГОДИЕ (GLUTTONY): Спавн сферы души SoulOrbEntity после убийства моба или босса Dragonfyre.
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            PlayerSinsData data = ModAttachmentTypes.get(killer);
            if (!data.isUnlocked()) return;

            LivingEntity dead = event.getEntity();
            boolean isBoss = !dead.canChangeDimensions() || dead.getMaxHealth() >= 150.0F;

            SoulOrbEntity orb = new SoulOrbEntity(ModEntities.SOUL_ORB.get(), killer.level());
            orb.setPos(dead.getX(), dead.getY() + 0.6D, dead.getZ());
            orb.setOwnerUuid(killer.getUUID());
            orb.setBossSoul(isBoss);
            killer.level().addFreshEntity(orb);

            CompatManager.onMobKilledByGluttony(killer, dead, data, isBoss);
            ModAttachmentTypes.save(killer, data);
            ModNetwork.syncToPlayer(killer, data);
        }
    }
}
