package com.example.addon.hud;

import com.example.addon.ItemFrameDupeAddon;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static net.minecraft.stat.StatFormatter.DECIMAL_FORMAT;

public class HudExample extends HudElement {
    public static final HudElementInfo<HudExample> INFO = new HudElementInfo<>(ItemFrameDupeAddon.HUD_GROUP, "Duper", "HUD element for duper.", HudExample::new);

    public HudExample() {
        super(INFO);
    }

    private int r;
    private int g;
    private int b;

    // Change color to look like Rainbow
    @Override
    public void tick(HudRenderer renderer) {
        r = (int) (Math.sin(System.currentTimeMillis() / 1000.0) * 127 + 128);
        g = (int) (Math.sin(System.currentTimeMillis() / 1000.0 + 2) * 127 + 128);
        b = (int) (Math.sin(System.currentTimeMillis() / 1000.0 + 4) * 127 + 128);
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(renderer.textWidth("AutoDuper", true), renderer.textHeight(true));

        // Render background
        renderer.quad(x, y, getWidth(), getHeight(), new Color(r, g, b, 100));

        // Render text
        renderer.text("Example element", x, y, Color.WHITE, true);
    }
}
