package com.greenlantern.client;

import com.greenlantern.item.PowerRingItem;
import com.greenlantern.item.PowerRingItem.Mode;
import com.greenlantern.net.SelectModePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/** Radial selector: hover (or press 1-5) and click to choose a construct. */
public class ConstructWheelScreen extends Screen {
    private static final int RADIUS = 78;
    private static final int HALF = 27;

    public ConstructWheelScreen() {
        super(Component.translatable("screen.greenlantern.wheel"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partial) {
        g.fillGradient(0, 0, width, height, 0xAA010A04, 0xCC02170A);
    }

    private int hovered(double mx, double my) {
        double dx = mx - width / 2.0, dy = my - height / 2.0;
        double dist = Math.hypot(dx, dy);
        if (dist < 26 || dist > RADIUS + 60) return -1;
        int n = Mode.values().length;
        double ang = Math.atan2(dy, dx) + Math.PI / 2;
        while (ang < 0) ang += Math.PI * 2;
        return (int) Math.round(ang / (Math.PI * 2 / n)) % n;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        super.render(g, mouseX, mouseY, partial);
        if (minecraft == null || minecraft.player == null) return;

        Mode[] modes = Mode.values();
        int n = modes.length;
        int cx = width / 2, cy = height / 2;
        ItemStack ring = PowerRingItem.find(minecraft.player);
        Mode current = ring.isEmpty() ? Mode.BLAST : PowerRingItem.getMode(ring);
        int charge = ring.isEmpty() ? 0 : PowerRingItem.getCharge(ring);
        int hov = hovered(mouseX, mouseY);

        // decorative rings
        for (int i = 0; i < 160; i++) {
            double a = i / 160.0 * Math.PI * 2;
            int x1 = cx + (int) (Math.cos(a) * (RADIUS - 6)), y1 = cy + (int) (Math.sin(a) * (RADIUS - 6));
            g.fill(x1, y1, x1 + 1, y1 + 1, 0x8822FF66);
            int x2 = cx + (int) (Math.cos(a) * 24), y2 = cy + (int) (Math.sin(a) * 24);
            g.fill(x2, y2, x2 + 1, y2 + 1, 0xAA66FFAA);
        }

        // slots
        for (int i = 0; i < n; i++) {
            Mode m = modes[i];
            double a = -Math.PI / 2 + i * Math.PI * 2 / n;
            int sx = cx + (int) (Math.cos(a) * RADIUS);
            int sy = cy + (int) (Math.sin(a) * RADIUS);
            boolean isHov = i == hov, isCur = m == current;
            boolean afford = m == Mode.FLIGHT || charge >= m.cost;

            int border = isCur ? 0xFF66FFAA : isHov ? 0xFFB6FFD0 : 0xFF1F6A3A;
            int fill = isHov ? 0xF02A7A44 : isCur ? 0xE0154A2A : 0xC00A1A10;
            g.fill(sx - HALF - 1, sy - 24, sx + HALF + 1, sy + 25, border);
            g.fill(sx - HALF, sy - 23, sx + HALF, sy + 24, fill);

            g.renderItem(ModeUi.stack(m), sx - 8, sy - 19);
            g.drawCenteredString(font, m.displayName(), sx, sy + 1, afford ? 0xFFFFFFFF : 0xFF9A7777);
            g.drawCenteredString(font, Component.literal(ModeUi.costText(m)), sx, sy + 12,
                    afford ? 0xFF66FF99 : 0xFFFF6666);
            g.drawString(font, String.valueOf(i + 1), sx - HALF + 3, sy - 21, 0xFF7FA58C, false);
        }

        // centre: charge + description of the hovered mode
        g.drawCenteredString(font, Component.translatable("hud.greenlantern.charge"), cx, cy - 12, 0xFF66FF99);
        g.drawCenteredString(font, Component.literal(charge + " / " + PowerRingItem.MAX_CHARGE), cx, cy - 1, 0xFFFFFFFF);
        int bw = 40;
        g.fill(cx - bw / 2, cy + 10, cx + bw / 2, cy + 14, 0xFF0A2A14);
        g.fill(cx - bw / 2, cy + 10, cx - bw / 2 + (int) (bw * charge / (float) PowerRingItem.MAX_CHARGE), cy + 14,
                charge < 40 ? 0xFFFF5555 : 0xFF38FF7A);

        if (hov >= 0) {
            g.drawCenteredString(font, ModeUi.description(modes[hov]), cx, cy + RADIUS + 46, 0xFFB6FFD0);
        } else {
            g.drawCenteredString(font, Component.translatable("screen.greenlantern.hint"), cx, cy + RADIUS + 46, 0xFF7FA58C);
        }
    }

    private void choose(int i) {
        ClientPlayNetworking.send(new SelectModePayload(i));
        onClose();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int i = hovered(mx, my);
        if (button == 0 && i >= 0) {
            choose(i);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int n = Mode.values().length;
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode < GLFW.GLFW_KEY_1 + n) {
            choose(keyCode - GLFW.GLFW_KEY_1);
            return true;
        }
        if (GreenLanternClient.WHEEL_KEY.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
