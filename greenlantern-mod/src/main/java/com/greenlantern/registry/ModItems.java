package com.greenlantern.registry;

import com.greenlantern.GreenLanternMod;
import com.greenlantern.item.PowerRingItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class ModItems {
    public static final Item POWER_RING = reg("power_ring",
            new PowerRingItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final Item POWER_BATTERY = reg("power_battery",
            new BlockItem(ModBlocks.POWER_BATTERY, new Item.Properties().rarity(Rarity.RARE)));

    private static Item reg(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, GreenLanternMod.id(name), item);
    }

    public static void register() {}
    private ModItems() {}
}
