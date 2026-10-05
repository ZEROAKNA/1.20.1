package com.sevendeadlysins.compat;

import com.sevendeadlysins.SevenDeadlySinsMod;
import com.sevendeadlysins.data.PlayerSinsData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Модуль глубокой оптимизации и интеграции с модпаком
 * Cisco's Fantasy Medieval RPG [Dragonfyre] (Minecraft 1.20.1 / Forge 47.3.0).
 *
 * ОБНОВЛЕНИЯ:
 * 1. Пробитие лимита урона боссов (Boss Damage Cap Bypass) и трейтов L2Hostility (Adaptive, Reflect, Undying):
 *    Метод dealMultiPhaseDamage(...) наносит многофазный каскадный урон (обнуляя invulnerableTime между фазами
 *    и добавляя чистый урон fellOutOfWorld) + срывает щиты и регенерацию L2Hostility.
 * 2. Поддержка Ультимативного Режима «Метка Демона / Первородный Грех» (Sin Overdrive x1.5).
 */
public class CompatManager {

    public static final String IRONS_SPELLBOOKS_ID = "irons_spellbooks";
    public static final String ATTRIBUTESLIB_ID = "attributeslib";
    public static final String APOTHEOSIS_ID = "apotheosis";
    public static final String ICE_AND_FIRE_ID = "iceandfire";
    public static final String CATACLYSM_ID = "cataclysm";
    public static final String SIMPLY_SWORDS_ID = "simplyswords";
    public static final String L2_HOSTILITY_ID = "l2hostility";
    public static final String TENSURA_ID = "tensura";

    private static boolean ironsLoaded = false;
    private static boolean attributesLibLoaded = false;
    private static boolean iceAndFireLoaded = false;
    private static boolean cataclysmLoaded = false;
    private static boolean simplySwordsLoaded = false;
    private static boolean l2HostilityLoaded = false;
    private static boolean tensuraLoaded = false;

    private static final UUID WRATH_SPELL_POWER_UUID = UUID.fromString("a81b4501-91c2-43a5-b211-001122334401");
    private static final UUID WRATH_CRIT_CHANCE_UUID = UUID.fromString("a81b4501-91c2-43a5-b211-001122334402");
    private static final UUID WRATH_CRIT_DAMAGE_UUID = UUID.fromString("a81b4501-91c2-43a5-b211-001122334403");
    private static final UUID WRATH_ARMOR_SHRED_UUID = UUID.fromString("a81b4501-91c2-43a5-b211-001122334404");
    private static final UUID WRATH_LIFE_STEAL_UUID = UUID.fromString("a81b4501-91c2-43a5-b211-001122334405");
    private static final UUID FRUIT_MAX_MANA_UUID = UUID.fromString("a81b4501-91c2-43a5-b211-001122334406");

    public static void init() {
        ModList modList = ModList.get();
        ironsLoaded = modList.isLoaded(IRONS_SPELLBOOKS_ID);
        attributesLibLoaded = modList.isLoaded(ATTRIBUTESLIB_ID) || modList.isLoaded(APOTHEOSIS_ID);
        iceAndFireLoaded = modList.isLoaded(ICE_AND_FIRE_ID);
        cataclysmLoaded = modList.isLoaded(CATACLYSM_ID);
        simplySwordsLoaded = modList.isLoaded(SIMPLY_SWORDS_ID);
        l2HostilityLoaded = modList.isLoaded(L2_HOSTILITY_ID);
        tensuraLoaded = modList.isLoaded(TENSURA_ID);

        SevenDeadlySinsMod.LOGGER.info(
                "[Dragonfyre Compat 1.20.1] Loaded integrations -> Iron's Spells: {}, Apotheosis/AttributesLib: {}, Ice&Fire: {}, Cataclysm: {}, SimplySwords: {}, L2Hostility: {}",
                ironsLoaded, attributesLibLoaded, iceAndFireLoaded, cataclysmLoaded, simplySwordsLoaded, l2HostilityLoaded
        );
    }

    /**
     * Вычисляет гибридный урон для баланса Cisco's Fantasy Medieval RPG [Dragonfyre].
     */
    public static float calculateDragonfyreDamage(ServerPlayer caster, LivingEntity target, float baseDamage, float targetMaxHpPct, float sinMultiplier) {
        double playerAtk = caster.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float weaponScaled = baseDamage + (float) (playerAtk * 1.35D);
        float maxHpBonus = Math.min(350.0F, target.getMaxHealth() * targetMaxHpPct);
        float vulnerabilityMult = target.getPersistentData().getLong("sds_lust_nightmare_until") > caster.level().getGameTime() ? 1.45F : 1.0F;
        return (weaponScaled + maxHpBonus) * sinMultiplier * vulnerabilityMult;
    }

    /**
     * ПРОБИТИЕ ЛИМИТА УРОНА БОССОВ (BOSS DAMAGE CAP BYPASS) И ТРЕЙТОВ L2HOSTILITY:
     * 1. Снимает адаптивные щиты, баффы регенерации, сопротивления и поглощения у элитных мобов L2Hostility.
     * 2. Разбивает высокий урон на несколько мгновенных микро-фаз с обнулением invulnerableTime = 0
     *    и чередованием физических, магических и пустотных (fellOutOfWorld) источников урона,
     *    чтобы обходить жесткий Damage Cap боссов Cataclysm и трейти Adaptive/Reflect/Undying!
     */
    public static void dealMultiPhaseDamage(ServerPlayer caster, LivingEntity target, float totalDamage, int phases) {
        if (target == null || !target.isAlive() || target == caster) return;
        ServerLevel level = caster.serverLevel();

        // 1. Подавление трейтов L2Hostility (Reflect / Undying / Adaptive / Regenerating)
        suppressL2HostilityTraits(target);

        int safePhases = Math.max(2, Math.min(phases, 6));
        float perPhaseDmg = totalDamage / safePhases;

        for (int i = 0; i < safePhases; i++) {
            if (!target.isAlive()) break;
            target.invulnerableTime = 0;
            if (i % 3 == 0) {
                target.hurt(level.damageSources().playerAttack(caster), perPhaseDmg);
            } else if (i % 3 == 1) {
                target.hurt(level.damageSources().magic(), perPhaseDmg);
            } else {
                target.hurt(level.damageSources().fellOutOfWorld(), perPhaseDmg);
            }
        }
        target.invulnerableTime = 0;
    }

    /**
     * Снимает защитные эффекты, щиты поглощения и подавляет трейты L2Hostility (Undying / Adaptive / Reflect).
     */
    public static void suppressL2HostilityTraits(LivingEntity target) {
        if (target == null) return;
        target.setAbsorptionAmount(0.0F);
        target.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        target.removeEffect(MobEffects.REGENERATION);
        target.removeEffect(MobEffects.FIRE_RESISTANCE);
        target.removeEffect(MobEffects.ABSORPTION);

        // Помечаем цель флагом подавления Reflect/Undying для обработчиков урона
        target.getPersistentData().putLong("sds_l2_suppressed_until", target.level().getGameTime() + 200L);
    }

    /**
     * Динамическое масштабирование атрибутов Iron's Spells 'n Spellbooks и Apotheosis (AttributesLib)
     * от стаков Гнева, режима Берсерка, Ранга Души Dragonfyre и режима Первородного Греха (Overdrive).
     */
    public static void updateDragonfyreAttributes(ServerPlayer player, PlayerSinsData data, int wrathTiers, boolean berserk, boolean slothAwakened) {
        long gameTime = player.level().getGameTime();
        double overdriveMult = data.isSinOverdriveActive(gameTime) ? 1.5D : 1.0D;
        double soulRankBonus = data.getDragonfyreSoulRank() * 0.03D;
        double spellBonus = (wrathTiers * 0.12D + (berserk ? 0.75D : 0.0D) + (slothAwakened ? 1.0D : 0.0D) + soulRankBonus) * overdriveMult;

        if (ironsLoaded) {
            applyDynamicAttribute(
                    player,
                    new ResourceLocation(IRONS_SPELLBOOKS_ID, "spell_power"),
                    WRATH_SPELL_POWER_UUID,
                    "SDS Dragonfyre Spell Power",
                    spellBonus,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            applyDynamicAttribute(
                    player,
                    new ResourceLocation(IRONS_SPELLBOOKS_ID, "max_mana"),
                    FRUIT_MAX_MANA_UUID,
                    "SDS Dragonfyre Bonus Mana",
                    Math.max(0.0D, data.getMaxMana() - 100.0D),
                    AttributeModifier.Operation.ADDITION
            );
        }

        if (attributesLibLoaded) {
            double critChance = (wrathTiers * 0.04D + (berserk ? 0.25D : 0.0D)) * overdriveMult;
            double critDamage = (wrathTiers * 0.12D + (berserk ? 0.60D : 0.0D)) * overdriveMult;
            double armorShred = (wrathTiers * 0.05D + (berserk ? 0.35D : 0.0D)) * overdriveMult;
            double lifeSteal = (berserk ? 0.20D : wrathTiers * 0.02D) * overdriveMult;

            applyDynamicAttribute(
                    player,
                    new ResourceLocation(ATTRIBUTESLIB_ID, "crit_chance"),
                    WRATH_CRIT_CHANCE_UUID,
                    "SDS Wrath Crit Chance",
                    critChance,
                    AttributeModifier.Operation.ADDITION
            );
            applyDynamicAttribute(
                    player,
                    new ResourceLocation(ATTRIBUTESLIB_ID, "crit_damage"),
                    WRATH_CRIT_DAMAGE_UUID,
                    "SDS Wrath Crit Damage",
                    critDamage,
                    AttributeModifier.Operation.ADDITION
            );
            applyDynamicAttribute(
                    player,
                    new ResourceLocation(ATTRIBUTESLIB_ID, "armor_shred"),
                    WRATH_ARMOR_SHRED_UUID,
                    "SDS Wrath Armor Shred",
                    armorShred,
                    AttributeModifier.Operation.ADDITION
            );
            applyDynamicAttribute(
                    player,
                    new ResourceLocation(ATTRIBUTESLIB_ID, "life_steal"),
                    WRATH_LIFE_STEAL_UUID,
                    "SDS Wrath Life Steal",
                    lifeSteal,
                    AttributeModifier.Operation.ADDITION
            );
        }
    }

    private static void applyDynamicAttribute(
            ServerPlayer player,
            ResourceLocation attrId,
            UUID modifierUuid,
            String name,
            double bonus,
            AttributeModifier.Operation operation
    ) {
        Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(attrId);
        if (attr != null) {
            AttributeInstance instance = player.getAttribute(attr);
            if (instance != null) {
                AttributeModifier existing = instance.getModifier(modifierUuid);
                if (existing != null && Math.abs(existing.getAmount() - bonus) < 0.001D) {
                    return;
                }
                instance.removeModifier(modifierUuid);
                if (bonus > 0.001D) {
                    instance.addTransientModifier(new AttributeModifier(modifierUuid, name, bonus, operation));
                }
            }
        }
    }

    public static void onForbiddenFruitConsumed(ServerPlayer player, PlayerSinsData data) {
        clearIronsSpellbooksCooldowns(player);
        updateDragonfyreAttributes(player, data, (int) (data.getWrathStacks() / 10.0F), false, false);
    }

    public static void clearIronsSpellbooksCooldowns(ServerPlayer player) {
        if (!ironsLoaded) return;
        try {
            Class<?> magicDataClass = Class.forName("io.redspace.ironsspellbooks.api.magic.MagicData");
            Method getPlayerMagicData = magicDataClass.getMethod("getPlayerMagicData", LivingEntity.class);
            Object magicData = getPlayerMagicData.invoke(null, player);
            if (magicData != null) {
                try {
                    Method setMana = magicDataClass.getMethod("setMana", float.class);
                    setMana.invoke(magicData, 1000.0F);
                } catch (Exception ignored) {
                }
                Object cooldowns = magicDataClass.getMethod("getPlayerCooldowns").invoke(magicData);
                if (cooldowns != null) {
                    cooldowns.getClass().getMethod("clearCooldowns").invoke(cooldowns);
                    cooldowns.getClass().getMethod("syncToPlayer", ServerPlayer.class).invoke(cooldowns, player);
                }
            }
        } catch (Exception ignored) {
            player.getPersistentData().remove("irons_spellbooks_cooldowns");
        }
    }

    public static void onMobKilledByGluttony(ServerPlayer player, LivingEntity killedMob, PlayerSinsData data, boolean isBoss) {
        if (isBoss) {
            data.setDragonfyreSoulRank(data.getDragonfyreSoulRank() + 1);
            clearIronsSpellbooksCooldowns(player);
        }

        if (tensuraLoaded) {
            CompoundTag persistent = player.getPersistentData();
            double currentMagicules = persistent.getDouble("tensura:magicules");
            double currentEp = persistent.getDouble("tensura:existence_points");
            double gain = isBoss ? 2500.0D : Math.max(25.0D, killedMob.getMaxHealth() * 4.0D);

            persistent.putDouble("tensura:magicules", currentMagicules + gain);
            persistent.putDouble("tensura:existence_points", currentEp + gain);
        }
    }

    public static void onGreedItemReplicated(ServerPlayer player, ItemStack copiedStack) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(copiedStack.getItem());
        if (itemId != null && !("minecraft".equals(itemId.getNamespace()))) {
            SevenDeadlySinsMod.LOGGER.debug("[Dragonfyre Greed] Скопирован RPG-артефакт x{} со всеми NBT-тегами Apotheosis/чарами: {}", copiedStack.getCount(), itemId);
        }
    }

    public static String tryStealModdedAbility(ServerPlayer player, LivingEntity target, PlayerSinsData data) {
        ResourceLocation entityKey = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        String namespace = entityKey != null ? entityKey.getNamespace() : "minecraft";
        String path = entityKey != null ? entityKey.getPath() : "unknown";

        suppressL2HostilityTraits(target);
        List<MobEffectInstance> activeEffects = new ArrayList<>(target.getActiveEffects());
        for (MobEffectInstance eff : activeEffects) {
            if (eff.getEffect().isBeneficial()) {
                target.removeEffect(eff.getEffect());
                player.addEffect(new MobEffectInstance(eff));
            }
        }

        // 1. Ice and Fire: Dragons ([Dragonfyre] Core)
        if (ICE_AND_FIRE_ID.equals(namespace) || path.contains("dragon") || path.contains("gorgon") || path.contains("hydra")) {
            String dragonSkill;
            if (path.contains("ice_dragon")) {
                dragonSkill = "dragonfyre:ice_dragon_breath";
            } else if (path.contains("lightning_dragon")) {
                dragonSkill = "dragonfyre:lightning_dragon_breath";
            } else if (path.contains("gorgon")) {
                dragonSkill = "dragonfyre:gorgon_petrify_gaze";
            } else {
                dragonSkill = "dragonfyre:fire_dragon_breath";
            }
            data.addStolenAbility(dragonSkill);
            return dragonSkill;
        }

        // 2. L_Ender's Cataclysm Bosses
        if (CATACLYSM_ID.equals(namespace)) {
            String cataclysmSkill;
            if (path.contains("ignis")) {
                cataclysmSkill = "cataclysm:ignis_abyssal_burn";
            } else if (path.contains("maledictus")) {
                cataclysmSkill = "cataclysm:maledictus_phantom_halberd";
            } else if (path.contains("harbinger")) {
                cataclysmSkill = "cataclysm:harbinger_death_laser";
            } else if (path.contains("leviathan")) {
                cataclysmSkill = "cataclysm:leviathan_abyss_blast";
            } else {
                cataclysmSkill = "cataclysm:" + path;
            }
            data.addStolenAbility(cataclysmSkill);
            return cataclysmSkill;
        }

        // 3. Проверка оружия в руке цели
        ItemStack held = target.getMainHandItem();
        if (!held.isEmpty()) {
            ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(held.getItem());
            if (itemKey != null && !"minecraft".equals(itemKey.getNamespace())) {
                String stolenWeaponSkill = "mod_weapon:" + itemKey.getNamespace() + ":" + itemKey.getPath();
                data.addStolenAbility(stolenWeaponSkill);
                return stolenWeaponSkill;
            }
        }

        // 4. Iron's Spells 'n Spellbooks
        if (IRONS_SPELLBOOKS_ID.equals(namespace)) {
            String spellSkill = "irons_spell:" + path;
            data.addStolenAbility(spellSkill);
            return spellSkill;
        }

        // 5. Tensura / L2Hostility / Любой другой мод
        if (TENSURA_ID.equals(namespace) || target.getPersistentData().contains("tensura:race")) {
            String race = target.getPersistentData().getString("tensura:race");
            if (race.isEmpty()) race = path;
            String skillId = "tensura_racial:" + race;
            data.addStolenAbility(skillId);
            return skillId;
        }

        if (!"minecraft".equals(namespace)) {
            String skillId = "mod_skill:" + namespace + ":" + path;
            data.addStolenAbility(skillId);
            return skillId;
        }

        return "";
    }

    public static boolean executeModdedAbility(ServerPlayer player, PlayerSinsData data, ServerLevel level, String abilityId, float damageMult) {
        if (abilityId == null || abilityId.isEmpty()) return false;
        long gameTime = level.getGameTime();

        // 1. Дыхание Драконов Ice and Fire [Dragonfyre]
        if (abilityId.startsWith("dragonfyre:")) {
            if (!data.consumeManaWithOverdrive(24.0F, gameTime)) return true;

            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle().normalize();
            boolean isIce = abilityId.contains("ice_dragon");
            boolean isLightning = abilityId.contains("lightning_dragon");
            boolean isGorgon = abilityId.contains("gorgon");

            level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 2.0F, isIce ? 1.3F : 0.75F);

            for (int i = 1; i <= 28; i++) {
                Vec3 pt = eye.add(look.scale(i));
                if (isIce) {
                    level.sendParticles(ParticleTypes.SNOWFLAKE, pt.x, pt.y, pt.z, 5, 0.35D, 0.35D, 0.35D, 0.03D);
                } else if (isLightning) {
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pt.x, pt.y, pt.z, 6, 0.35D, 0.35D, 0.35D, 0.05D);
                } else if (isGorgon) {
                    level.sendParticles(ParticleTypes.SCULK_SOUL, pt.x, pt.y, pt.z, 4, 0.25D, 0.25D, 0.25D, 0.02D);
                } else {
                    level.sendParticles(ParticleTypes.FLAME, pt.x, pt.y, pt.z, 6, 0.35D, 0.35D, 0.35D, 0.04D);
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pt.x, pt.y, pt.z, 3, 0.25D, 0.25D, 0.25D, 0.02D);
                }

                for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(pt, pt).inflate(2.5D), e -> e != player && e.isAlive())) {
                    float dmg = calculateDragonfyreDamage(player, victim, 65.0F, 0.08F, damageMult);
                    dealMultiPhaseDamage(player, victim, dmg, 4);

                    if (isIce) {
                        victim.setTicksFrozen(300);
                        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 4));
                    } else if (isLightning) {
                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                        if (bolt != null) {
                            bolt.moveTo(victim.position());
                            bolt.setVisualOnly(true);
                            level.addFreshEntity(bolt);
                        }
                    } else if (isGorgon) {
                        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 6));
                        victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 3));
                    } else {
                        victim.setSecondsOnFire(10);
                    }
                }
            }
            player.displayClientMessage(
                    Component.literal("Зависть [Dragonfyre]: Обрушено Дыхание Дракона «" + abilityId + "» (Пробитие Damage Cap)!")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    true
            );
            return true;
        }

        // 2. Способности Боссов L_Ender's Cataclysm
        if (abilityId.startsWith("cataclysm:")) {
            if (!data.consumeManaWithOverdrive(25.0F, gameTime)) return true;

            Vec3 center = player.position().add(player.getLookAngle().scale(5.0D));
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 0.5D, center.z, 3, 1.5D, 0.5D, 1.5D, 0.0D);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y + 1.0D, center.z, 65, 3.0D, 1.2D, 3.0D, 0.12D);
            level.playSound(null, player.blockPosition(), SoundEvents.WITHER_BREAK_BLOCK, SoundSource.PLAYERS, 2.0F, 0.7F);

            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(11.0D), e -> e != player && e.isAlive())) {
                float dmg = calculateDragonfyreDamage(player, victim, 72.0F, 0.10F, damageMult);
                dealMultiPhaseDamage(player, victim, dmg, 5);
                victim.setSecondsOnFire(10);
                victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 2));
                player.heal(Math.min(12.0F, dmg * 0.10F));
            }
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 2, false, true, true));
            player.displayClientMessage(
                    Component.literal("Зависть [Cataclysm]: Катаклизм Босса «" + abilityId.substring("cataclysm:".length()) + "» пробил защиту врагов!")
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                    true
            );
            return true;
        }

        // 3. Заклинания Iron's Spells 'n Spellbooks
        if (abilityId.startsWith("irons_spell:") || abilityId.startsWith("mod_weapon:irons_spellbooks:")) {
            if (!data.consumeManaWithOverdrive(20.0F, gameTime)) return true;
            clearIronsSpellbooksCooldowns(player);

            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle().normalize();
            level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 2.0F, 0.85F);
            for (int i = 1; i <= 26; i++) {
                Vec3 pt = eye.add(look.scale(i));
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pt.x, pt.y, pt.z, 4, 0.22D, 0.22D, 0.22D, 0.02D);
                level.sendParticles(ParticleTypes.WITCH, pt.x, pt.y, pt.z, 3, 0.2D, 0.2D, 0.2D, 0.02D);
                for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(pt, pt).inflate(2.2D), e -> e != player && e.isAlive())) {
                    float dmg = calculateDragonfyreDamage(player, victim, 54.0F, 0.06F, damageMult);
                    dealMultiPhaseDamage(player, victim, dmg, 4);
                    victim.setSecondsOnFire(6);
                }
            }
            player.displayClientMessage(
                    Component.literal("Зависть [Iron's Spells]: Выпущен Архимагический Луч + Сброшены КД заклинаний!")
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                    true
            );
            return true;
        }

        // 4. Расовые способности Tensura
        if (abilityId.startsWith("tensura_racial:")) {
            if (!data.consumeManaWithOverdrive(20.0F, gameTime)) return true;
            level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.5F, 1.3F);
            level.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 1.0D, player.getZ(), 65, 3.0D, 1.5D, 3.0D, 0.08D);
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(14.0D), e -> e != player && e.isAlive())) {
                float dmg = calculateDragonfyreDamage(player, victim, 58.0F, 0.07F, damageMult);
                dealMultiPhaseDamage(player, victim, dmg, 4);
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 4, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 3, false, true));
            }
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 3, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 4, false, true, true));
            player.displayClientMessage(
                    Component.literal("Зависть [Tensura]: Активирована Расовая Способность «" + abilityId.substring("tensura_racial:".length()) + "»!")
                            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                    true
            );
            return true;
        }

        // 5. Оружие Simply Swords, Cisco's RPG и навыки любых других модов
        if (abilityId.startsWith("mod_skill:") || abilityId.startsWith("mod_weapon:")) {
            if (!data.consumeManaWithOverdrive(22.0F, gameTime)) return true;
            String[] parts = abilityId.split(":");
            String modName = parts.length > 1 ? parts[1] : "mod";
            String skillName = parts.length > 2 ? parts[2] : abilityId;

            Vec3 look = player.getLookAngle().normalize();
            Vec3 strikeCenter = player.position().add(look.scale(6.0D));
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, strikeCenter.x, strikeCenter.y + 0.5D, strikeCenter.z, 2, 1.2D, 0.5D, 1.2D, 0.0D);
            level.sendParticles(ParticleTypes.END_ROD, strikeCenter.x, strikeCenter.y + 1.0D, strikeCenter.z, 50, 2.2D, 1.2D, 2.2D, 0.12D);
            level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 2.0F, 0.85F);

            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(strikeCenter, strikeCenter).inflate(10.0D), e -> e != player && e.isAlive())) {
                float dmg = calculateDragonfyreDamage(player, victim, 62.0F, 0.07F, damageMult);
                dealMultiPhaseDamage(player, victim, dmg, 4);
                victim.setSecondsOnFire(8);
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(victim.position());
                    bolt.setVisualOnly(true);
                    level.addFreshEntity(bolt);
                }
            }
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 3, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 2, false, true, true));
            player.displayClientMessage(
                    Component.literal("Зависть [" + modName.toUpperCase() + "]: Применена техника «" + skillName + "»!")
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                    true
            );
            return true;
        }

        return false;
    }
}
