package com.sevendeadlysins.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sevendeadlysins.registry.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Глобальный модификатор лута для Minecraft 1.20.1 (Forge 47.3.0).
 * Адаптирован под генерацию данжей модпака Cisco's Fantasy Medieval RPG [Dragonfyre]:
 * - Сундуки Древнего Города (chests/ancient_city): шанс 15%
 * - Сокровищницы Бастиона, Города Энда, Логова Драконов Ice & Fire и Данжи Cataclysm: шанс 12%
 */
public class AddLootModifier extends LootModifier {
    private static final ResourceLocation ANCIENT_CITY_CHEST = new ResourceLocation("minecraft", "chests/ancient_city");
    private static final ResourceLocation BASTION_TREASURE = new ResourceLocation("minecraft", "chests/bastion_treasure");
    private static final ResourceLocation END_CITY_TREASURE = new ResourceLocation("minecraft", "chests/end_city_treasure");

    public static final Supplier<Codec<AddLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst -> codecStart(inst)
                    .and(ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(m -> m.item))
                    .apply(inst, AddLootModifier::new))
    );

    private final Item item;

    public AddLootModifier(LootItemCondition[] conditionsIn, Item item) {
        super(conditionsIn);
        this.item = item;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation queriedTableId = context.getQueriedLootTableId();
        if (queriedTableId == null) {
            return generatedLoot;
        }

        String path = queriedTableId.getPath();
        String namespace = queriedTableId.getNamespace();

        float chance = 0.0F;
        if (ANCIENT_CITY_CHEST.equals(queriedTableId)) {
            chance = 0.15F;
        } else if (BASTION_TREASURE.equals(queriedTableId) || END_CITY_TREASURE.equals(queriedTableId)) {
            chance = 0.12F;
        } else if (("cataclysm".equals(namespace) || "iceandfire".equals(namespace) || "irons_spellbooks".equals(namespace))
                && path.contains("chest")) {
            // Поддержка сундуков боссов и логовищ драконов в Cisco's RPG [Dragonfyre]
            chance = 0.12F;
        }

        if (chance > 0.0F && context.getRandom().nextFloat() < chance) {
            generatedLoot.add(new ItemStack(this.item != null ? this.item : ModItems.FORBIDDEN_FRUIT.get(), 1));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
