package com.falsaalucinacion.client;

import com.falsaalucinacion.LaFalsaAlucinacion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.util.RandomSource;

@Mod.EventBusSubscriber(modid = LaFalsaAlucinacion.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class FalsaClientEvents {
    private static int endingTicks = 0;
    private static int glitchTicks = 0;
    private static int realityTicks = 0;
    private static boolean wasInFalseReality = false;

    private FalsaClientEvents() {}
    public static void bootstrap() { /* Loads this client-only class through DistExecutor on physical clients. */ }
    public static void beginEnding() { endingTicks = 240; }

    private static boolean inFalseReality() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.level.dimension().equals(LaFalsaAlucinacion.FALSE_REALITY);
    }

    private static int currentPhase() {
        if (!inFalseReality()) return 0;
        return Math.min(3, realityTicks / 6000);
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        boolean altered = inFalseReality();
        if (altered && !wasInFalseReality) realityTicks = 0;
        if (altered) realityTicks++;
        else if (wasInFalseReality) realityTicks = 0;
        wasInFalseReality = altered;
        if (endingTicks > 0) endingTicks--;
        if (glitchTicks > 0) glitchTicks--;
        if (altered && Minecraft.getInstance().level != null && Minecraft.getInstance().level.random.nextInt(90) == 0) {
            glitchTicks = 5 + Minecraft.getInstance().level.random.nextInt(9);
        }
    }

    @SubscribeEvent
    public static void renderCorruptedOverlay(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;
        boolean altered = inFalseReality();
        if (!altered && endingTicks <= 0) return;
        Minecraft mc = Minecraft.getInstance();
        GuiGraphics gui = event.getGuiGraphics();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        if (altered && endingTicks <= 70) {
            int phase = currentPhase();
            int tint = switch (phase) {
                case 0 -> 0x20B00010; // blood-red night
                case 1 -> 0x20002EFF; // phosphorescent blue
                case 2 -> (mc.level.random.nextInt(6) == 0 ? 0x55FFFFFF : 0x10EFFFFF);
                default -> 0x2100FF36; // neon-green night
            };
            gui.fill(0, 0, width, height, tint);

            if (phase == 0 || phase == 3) {
                for (int y = 3; y < height; y += 17) {
                    if (((y + (int) mc.level.getGameTime()) % 3) == 0) {
                        int x = mc.level.random.nextInt(Math.max(1, width - 10));
                        int barWidth = 10 + mc.level.random.nextInt(Math.max(11, width / 3));
                        gui.fill(x, y, Math.min(width, x + barWidth), y + 1 + mc.level.random.nextInt(3),
                                phase == 0 ? 0x30FF1A1A : 0x4000FF4A);
                    }
                }
            }
            if (glitchTicks > 0 && mc.level.random.nextBoolean()) {
                int y = mc.level.random.nextInt(Math.max(1, height));
                gui.fill(0, y, width, Math.min(height, y + 1 + mc.level.random.nextInt(5)), 0x55F7FFFF);
            }

            if (endingTicks == 0 || endingTicks > 70) {
                int wobble = (int) (Math.sin(mc.level.getGameTime() * 0.15D) * 2.0D);
                gui.drawCenteredString(mc.font, "LA FALSA ALUCINACIÓN", width / 2 + wobble, height / 5,
                        phase == 3 ? 0xFF5CFF82 : 0xFFB8FFEE);
                gui.drawCenteredString(mc.font, "NO CONFÍES EN LO QUE RECUERDAS", width / 2, height / 5 + 13, 0xAAFFFFFF);
                if (mc.player != null && mc.player.getInventory().contains(new net.minecraft.world.item.ItemStack(
                        com.falsaalucinacion.item.FalsaItems.GEMA_LIBERTAD.get()))) {
                    gui.drawCenteredString(mc.font, "SIGUE LAS CHISPAS", width / 2, height - 45, 0xFF74F7E3);
                }
            }
        }

        // This black fade deliberately remains visible for a few ticks after the teleport back.
        if (endingTicks > 0 && endingTicks <= 70) {
            int alpha = (int) (255.0F * (1.0F - endingTicks / 70.0F));
            alpha = Math.max(0, Math.min(255, alpha));
            gui.fill(0, 0, width, height, (alpha << 24));
        }
    }

    @SubscribeEvent
    public static void hideFoodBar(RenderGuiOverlayEvent.Pre event) {
        if (inFalseReality() && endingTicks <= 70 && event.getOverlay().id().equals(VanillaGuiOverlay.FOOD_LEVEL.id())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void alterFog(ViewportEvent.ComputeFogColor event) {
        if (!inFalseReality() || endingTicks > 70) return;
        int phase = currentPhase();
        float r = event.getRed(), g = event.getGreen(), b = event.getBlue();
        if (phase == 0) { event.setRed(Math.min(1.0F, r * 0.7F + 0.25F)); event.setGreen(g * 0.35F); event.setBlue(b * 0.35F); }
        else if (phase == 1) { event.setRed(r * 0.18F); event.setGreen(g * 0.55F + 0.1F); event.setBlue(Math.min(1.0F, b * 0.9F + 0.22F)); }
        else if (phase == 2) { event.setRed(Math.min(1.0F, r * 0.7F + 0.2F)); event.setGreen(Math.min(1.0F, g * 0.7F + 0.2F)); event.setBlue(Math.min(1.0F, b * 0.7F + 0.2F)); }
        else { event.setRed(r * 0.22F); event.setGreen(Math.min(1.0F, g * 0.85F + 0.2F)); event.setBlue(b * 0.25F); }
    }

    @SubscribeEvent
    public static void blockPauseMenu(ScreenEvent.Opening event) {
        if (inFalseReality() && event.getNewScreen() instanceof PauseScreen) event.setCanceled(true);
    }
}
