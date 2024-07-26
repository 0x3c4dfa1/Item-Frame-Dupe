package com.example.addon.modules;

import com.example.addon.ItemFrameDupeAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ItemFrameDupe extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> distance = sgGeneral.add(new IntSetting.Builder()
        .name("distance")
        .description("The max distance to search for pistons.")
        .min(1)
        .sliderMin(1)
        .defaultValue(5)
        .sliderMax(6)
        .max(6)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay between rotations in ticks.")
        .min(1)
        .defaultValue(1)
        .sliderMax(10)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Should the player rotate his head to the item frame.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotateItem = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate item")
        .description("Whether or not to keep rotating the item frame")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> swapBack = sgGeneral.add(new BoolSetting.Builder()
        .name("swap-back")
        .description("Whether or not to swap back to the previous held item after placing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> breakDelay = sgGeneral.add(new IntSetting.Builder()
        .name("item-break-delay")
        .description("The amount of delay between breaking the item in ticks.")
        .defaultValue(3)
        .min(3)
        .sliderMax(60)
        .build()
    );

    public ItemFrameDupe() {
        super(ItemFrameDupeAddon.CATEGORY, "Auto Dupe", "Automatically places item frames on pistons (or not) and performs the item frame dupe");
    }

    private int timer;
    private final ArrayList<BlockPos> positions = new ArrayList<>();
    private static final ArrayList<BlockPos> blocks = new ArrayList<>();
    private Thread placeThread = null;
    private int breakDelaytimer;
    private ItemStack previousMainHandItem;

    @Override
    public void onActivate() {
        timer = delay.get();
        breakDelaytimer = 0;
        previousMainHandItem = mc.player.getMainHandStack().copy();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        placeThread = null;
        breakDelaytimer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;

        ClientPlayerInteractionManager c = mc.interactionManager;
        assert mc.world != null;
        assert mc.player != null;

        if (timer > 0) {
            timer--;
            return;
        } else {
            timer = delay.get();
        }

        placeThread = new Thread(() -> {
            ItemFrameEntity itemFrame;
            Box box;
            box = new Box(mc.player.getEyePos().add(-3, -3, -3), mc.player.getEyePos().add(3, 3, 3));
            if (!mc.player.getWorld().getEntitiesByClass(ItemFrameEntity.class, box, itemFrameEntity -> true).isEmpty()) {
                itemFrame = mc.player.getWorld().getEntitiesByClass(ItemFrameEntity.class, box, itemFrameEntity -> true).get(0);

                assert c != null;
                c.interactEntity(mc.player, itemFrame, Hand.MAIN_HAND);
                if (itemFrame.getHeldItemStack().getCount() > 0) {
                    // Rotate the frame
                    if (rotateItem.get()) {
                        c.interactEntity(mc.player, itemFrame, Hand.MAIN_HAND);
                    }
                    // Delay before attacking the entity
                    try {
                        TimeUnit.MILLISECONDS.sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    breakDelaytimer++;
                    if (breakDelaytimer > breakDelay.get()) {
                        // If no item in hand and neither in item frame, DONT break the item frame
                        if (itemFrame.getHeldItemStack().isEmpty() && mc.player.getMainHandStack().isEmpty()) {
                            breakDelaytimer = 0;
                            return;
                        }
                        c.attackEntity(mc.player, itemFrame);
                        // Swap back to the previous item if needed
                        breakDelaytimer = 0;
                    }
//                    try {
//                        TimeUnit.MILLISECONDS.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        });
        placeThread.setName("PB-Thread");
        placeThread.start();
    }

    private static List<BlockPos> getSphere(BlockPos centerPos, int radius, int height) {
        blocks.clear();

        for (int i = centerPos.getX() - radius; i < centerPos.getX() + radius; i++) {
            for (int j = centerPos.getY() - height; j < centerPos.getY() + height; j++) {
                for (int k = centerPos.getZ() - radius; k < centerPos.getZ() + radius; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (distanceBetween(centerPos, pos) <= radius && !blocks.contains(pos)) blocks.add(pos);
                }
            }
        }

        return blocks;
    }

    private static double distanceBetween(BlockPos pos1, BlockPos pos2) {
        double d = pos1.getX() - pos2.getX();
        double e = pos1.getY() - pos2.getY();
        double f = pos1.getZ() - pos2.getZ();
        return MathHelper.sqrt((float) (d * d + e * e + f * f));
    }

}
