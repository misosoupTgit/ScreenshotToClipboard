package com.misosouptgit.mixin;

import com.misosouptgit.config.ModConfig;
import com.misosouptgit.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public class ScreenshotMixin {
    @Unique private static final ModConfig CONFIG = ModConfig.load();
    @Unique private static final AtomicBoolean IS_PROCESSING = new AtomicBoolean(false);

    // OS判定に基づいたハンドラの選択
    @Unique private static final ClipboardHandler HANDLER = createHandler();

    @Unique
    private static ClipboardHandler createHandler() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return new WindowsClipboardHandler();
        return new UnixClipboardHandler(); // MacとLinuxを共通で扱う
    }

    @Inject(method = "saveScreenshot", at = @At("RETURN"))
    private static void onSaveScreenshot(File gameDirectory, @SuppressWarnings("unused") Framebuffer framebuffer, Consumer<Text> messageReceiver, CallbackInfo ci) {
        if (IS_PROCESSING.getAndSet(true)) return;

        final File screenshotsDir = new File(gameDirectory, "screenshots");
        new Thread(() -> {
            try {
                Thread.sleep(800); // 保存完了待ち
                File latest = getLatestFile(screenshotsDir);
                if (latest != null && latest.exists()) {
                    BufferedImage img = ImageIO.read(latest);
                    if (img != null && HANDLER.copyToClipboard(img, latest)) {
                        if (CONFIG.showMessage) sendNotification(messageReceiver);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IS_PROCESSING.set(false);
            }
        }, "Screenshot-Clipboard-Thread").start();
    }

    @Unique
    private static File getLatestFile(File dir) {
        File[] fs = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
        if (fs == null || fs.length == 0) return null;
        File l = fs[0];
        for (File f : fs) if (f.lastModified() > l.lastModified()) l = f;
        return l;
    }

    @Unique
    private static void sendNotification(Consumer<Text> chatReceiver) {
        MinecraftClient.getInstance().execute(() ->
                MinecraftClient.getInstance().getToastManager().add(
                        new SystemToast(SystemToast.Type.WORLD_BACKUP,
                                Text.translatable("text.screenshottoclipboard.copied_message"),
                                Text.literal("Success!"))
                )
        );
    }
}