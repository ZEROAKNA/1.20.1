package com.sevendeadlysins.item;

import com.sevendeadlysins.compat.CompatManager;
import com.sevendeadlysins.data.PlayerSinsData;
import com.sevendeadlysins.network.ModNetwork;
import com.sevendeadlysins.registry.ModAttachmentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ForbiddenFruitItem extends Item {

    public static final FoodProperties FORBIDDEN_FRUIT_FOOD = new FoodProperties.Builder()
            .nutrition(10)
            .saturationMod(1.5F)
            .alwaysEat()
            .build();

    public ForbiddenFruitItem() {
        super(new Item.Properties()
                .stacksTo(16)
                .rarity(Rarity.EPIC)
                .fireResistant()
                .food(FORBIDDEN_FRUIT_FOOD));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 64; // 64 тика (3.2 сек)
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);

        if (!level.isClientSide() && livingEntity instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
            PlayerSinsData data = ModAttachmentTypes.get(serverPlayer);

            boolean alreadyUnlocked = data.isUnlocked();
            data.setUnlocked(true);
            if (data.getMaxMana() < 150.0F) {
                data.setMaxMana(150.0F);
            } else if (alreadyUnlocked) {
                data.setMaxMana(data.getMaxMana() + 50.0F);
            }
            data.setCurrentMana(data.getMaxMana());
            data.setDragonfyreSoulRank(data.getDragonfyreSoulRank() + 1);

            data.setPrideCooldownUntil(0L);
            data.setSlothLockedUntil(0L);
            serverPlayer.setHealth(serverPlayer.getMaxHealth());
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, true, true));
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 4, false, true, true));
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 2, false, true, true));
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 1, false, true, true));
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2400, 0, false, true, true));

            // Интеграция с Cisco's RPG: сброс перезарядок Iron's Spells и бонус атрибутов Apotheosis
            CompatManager.onForbiddenFruitConsumed(serverPlayer, data);

            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, serverPlayer.getX(), serverPlayer.getY() + 1.0D, serverPlayer.getZ(), 55, 1.1D, 0.9D, 1.1D, 0.07D);
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, serverPlayer.getX(), serverPlayer.getY() + 1.0D, serverPlayer.getZ(), 32, 0.9D, 0.9D, 0.9D, 0.04D);

            level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.4F, 0.8F);
            level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 0.9F);

            ModAttachmentTypes.save(serverPlayer, data);
            ModNetwork.syncToPlayer(serverPlayer, data);

            serverPlayer.displayClientMessage(
                    Component.literal("Печать Dragonfyre сорвана! Семь Смертных Грехов пробудились (Пул Маны: " + (int) data.getMaxMana() + ", Ранг Души: " + data.getDragonfyreSoulRank() + "). Нажмите [R] и [V]!")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    true
            );
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.literal("Древний реликт Глубин и Драконьего Пламени [Cisco's RPG Dragonfyre].").withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.literal("• Пробуждает Семь Смертных Грехов и сбрасывает все перезарядки.").withStyle(ChatFormatting.RED));
        tooltipComponents.add(Component.literal("• Даёт +50 к Макс. Мане, +1 Ранг Души Dragonfyre и 10 золотых сердец.").withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.literal("• Интеграция: Iron's Spells, Apotheosis, Cataclysm, Ice & Fire, L2Hostility.").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
