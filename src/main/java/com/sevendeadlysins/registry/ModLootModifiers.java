package com.sevendeadlysins.registry;

import com.mojang.serialization.Codec;
import com.sevendeadlysins.SevenDeadlySinsMod;
import com.sevendeadlysins.loot.AddLootModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, SevenDeadlySinsMod.MODID);

    public static final RegistryObject<Codec<AddLootModifier>> ADD_ITEM =
            GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("add_item", AddLootModifier.CODEC);
}
