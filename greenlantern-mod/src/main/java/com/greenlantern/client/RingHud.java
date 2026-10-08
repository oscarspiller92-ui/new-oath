package com.greenlantern.client;

import com.greenlantern.item.PowerRingItem;
import com.greenlantern.item.PowerRingItem.Mode;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Left-side panel: shows every construct, the selected one, its cost, and your charge. */
public final class RingHud {
    private static final int GREEN = 0xFF66FF99;

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if (p == null || mc.options.hideGui || mc.screen instanceof ConstructWheelScreen) return;
        if (!(p.getMainHandItem().getItem() instanceof PowerRingItem)
                && !(p.getOffhandItem().getItem() instanceof PowerRingItem)) return;

        ItemStack ring = PowerRingItem.find(p);
        Mode current = PowerRingItem.getMode(ring);
        int charge = PowerRingItem.getCharge(ring);
        Font font = mc.font;
        Mode[] modes = Mode.values();

        int w = 124, rowH = 18;
        int h = 20 + modes.length * rowH + 28;
        int x = 8, y = (g.guiHeight() - h) / 2;

        // panel
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF1F8A4A);
        g.fill(x, y, x + w, y + h, 0xCC07140C);

        // title + keybind hint
        g.drawString(font, Component.translatable("hud.greenlantern.title"), x + 6, y + 6, GREEN, true);
        Component key = Component.literal("[").append(GreenLanternClient.WHEEL_KEY.getTranslatedKeyMessage()).append("]");
        g.drawString(font, key, x + w - 6 - font.width(key), y + 6, 0xFF7FA58C, false);

        // rows
        for (int i = 0; i < modes.length; i++) {
            Mode m = modes[i];
            int ry = y + 18 + i * rowH;
            boolean sel = m == current;
            boolean afford = m == Mode.FLIGHT || charge >= m.cost;
            if (sel) {
                g.fill(x + 3, ry, x + w - 3, ry + rowH - 1, 0x5538FF7A);
                g.fill(x + 3, ry, x + 5, ry + rowH - 1, 0xFF66FFAA);
            }
            g.renderItem(ModeUi.stack(m), x + 8, ry + 1);
            int textColor = sel ? 0xFFFFFFFF : (afford ? 0xFFB6E8C6 : 0xFF7A6666);
            g.drawString(font, m.displayName(), x + 28, ry + 5, textColor, sel);
            String cost = ModeUi.costText(m);
            g.drawString(font, cost, x + w - 8 - font.width(cost), ry + 5, afford ? 0xFF66FF99 : 0xFFFF6666, false);
        }

        // charge bar
        int by = y + h - 14;
        String label = charge + " / " + PowerRingItem.MAX_CHARGE;
        g.drawString(font, Component.translatable("hud.greenlantern.charge"), x + 6, by - 12, 0xFF9DDBB0, false);
        g.drawString(font, label, x + w - 6 - font.width(label), by - 12, 0xFFFFFFFF, false);
        g.fill(x + 6, by, x + w - 6, by + 6, 0xFF0A2A14);
        int fillW = (int) ((w - 12) * (charge / (float) PowerRingItem.MAX_CHARGE));
        int color = charge < 40 ? 0xFFFF5555 : 0xFF38FF7A;
        g.fill(x + 6, by, x + 6 + fillW, by + 6, color);
        g.fill(x + 6, by, x + 6 + fillW, by + 2, 0x55FFFFFF);
    }

    private RingHud() {}
}
