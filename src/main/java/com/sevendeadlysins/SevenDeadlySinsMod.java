package com.sevendeadlysins;

import com.mojang.logging.LogUtils;
import com.sevendeadlysins.compat.CompatManager;
import com.sevendeadlysins.network.ModNetwork;
import com.sevendeadlysins.registry.ModEntities;
import com.sevendeadlysins.registry.ModItems;
import com.sevendeadlysins.registry.ModLootModifiers;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SevenDeadlySinsMod.MODID)
public class SevenDeadlySinsMod {
    public static final String MODID = "sevendeadlysins";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SevenDeadlySinsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 1. Регистрация предметов, сущностей и Global Loot Modifiers для Forge 1.20.1
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModLootModifiers.GLOBAL_LOOT_MODIFIER_SERIALIZERS.register(modEventBus);

        // 2. Инициализация сетевого канала SimpleChannel и мостовых интеграций Cisco's RPG [Dragonfyre]
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Seven Deadly Sins [Dragonfyre Edition] initialized for Minecraft 1.20.1 (Forge 47.3.0 / Java 17)");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetwork.register();
            CompatManager.init();
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS || event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.FORBIDDEN_FRUIT.get());
        }
    }
}
