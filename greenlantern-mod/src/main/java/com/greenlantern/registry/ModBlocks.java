package com.greenlantern.registry;

import com.greenlantern.GreenLanternMod;
import com.greenlantern.block.EnergyConstructBlock;
import com.greenlantern.block.PowerBatteryBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
    public static final Block POWER_BATTERY = reg("power_battery", new PowerBatteryBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.EMERALD)
                    .strength(3.0f, 1200.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 15)
                    .noOcclusion()
                    .sound(SoundType.LANTERN)));

    public static final Block ENERGY_CONSTRUCT = reg("energy_construct", new EnergyConstructBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.EMERALD)
                    .strength(0.6f)
                    .lightLevel(s -> 10)
                    .noOcclusion()
                    .noLootTable()
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .isValidSpawn((s, g, p, t) -> false)
                    .isRedstoneConductor(EnergyConstructBlock::never)
                    .isSuffocating(EnergyConstructBlock::never)
                    .isViewBlocking(EnergyConstructBlock::never)));

    private static Block reg(String name, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, GreenLanternMod.id(name), block);
    }

    public static void register() {}
    private ModBlocks() {}
}
