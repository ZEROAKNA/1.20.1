package com.sevendeadlysins.registry;

import com.sevendeadlysins.SevenDeadlySinsMod;
import com.sevendeadlysins.entity.SoulOrbEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SevenDeadlySinsMod.MODID);

    public static final RegistryObject<EntityType<SoulOrbEntity>> SOUL_ORB =
            ENTITIES.register("soul_orb", () -> EntityType.Builder.<SoulOrbEntity>of(SoulOrbEntity::new, MobCategory.MISC)
                    .sized(0.45F, 0.45F)
                    .clientTrackingRange(32)
                    .updateInterval(2)
                    .build("soul_orb"));
}
