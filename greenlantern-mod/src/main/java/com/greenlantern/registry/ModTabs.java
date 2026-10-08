package com.greenlantern.registry;

import com.greenlantern.GreenLanternMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModTabs {
    public static final CreativeModeTab MAIN = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB, GreenLanternMod.id("main"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.greenlantern"))
                    .icon(() -> new ItemStack(ModItems.POWER_RING))
                    .displayItems((params, out) -> {
                        out.accept(ModItems.POWER_RING);
                        out.accept(ModItems.POWER_BATTERY);
                    })
                    .build());

    public static void register() {}
    private ModTabs() {}
}
