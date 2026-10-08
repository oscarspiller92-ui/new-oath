package com.greenlantern.client;

import com.greenlantern.item.PowerRingItem.Mode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Locale;

/** Shared icons / text for the HUD and the wheel. */
public final class ModeUi {
    private static final ItemStack[] STACKS = new ItemStack[Mode.values().length];

    public static ItemStack stack(Mode m) {
        ItemStack s = STACKS[m.ordinal()];
        if (s == null) {
            Item item = switch (m) {
                case BLAST -> Items.END_ROD;
                case FIST -> Items.EMERALD_BLOCK;
                case BRIDGE -> Items.LIME_STAINED_GLASS_PANE;
                case SHIELD -> Items.SHIELD;
                case FLIGHT -> Items.FEATHER;
            };
            s = new ItemStack(item);
            STACKS[m.ordinal()] = s;
        }
        return s;
    }

    public static Component description(Mode m) {
        return Component.translatable("mode.greenlantern." + m.name().toLowerCase(Locale.ROOT) + ".desc");
    }

    public static String costText(Mode m) {
        return m == Mode.FLIGHT ? "1/s" : String.valueOf(m.cost);
    }

    private ModeUi() {}
}
