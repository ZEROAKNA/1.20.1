package com.sevendeadlysins.registry;

import com.sevendeadlysins.SevenDeadlySinsMod;
import com.sevendeadlysins.item.ForbiddenFruitItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SevenDeadlySinsMod.MODID);

    public static final RegistryObject<ForbiddenFruitItem> FORBIDDEN_FRUIT =
            ITEMS.register("forbidden_fruit", ForbiddenFruitItem::new);
}
