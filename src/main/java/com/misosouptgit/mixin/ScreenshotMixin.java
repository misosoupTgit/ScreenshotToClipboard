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
    @Unique private static final ClipboardHandler HANDLER = createHandler();

    @Unique
    private static ClipboardHandler createHandler() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return new WindowsClipboardHandler();
        return new UnixClipboardHandler();
    }

    @Inject(method = "saveScreenshot", at = @At("RETURN"))
    private static void onSaveScreenshot(File gameDir, @SuppressWarnings("unused") Framebuffer fb, Consumer<Text> messageReceiver, CallbackInfo ci) {
        if (IS_PROCESSING.getAndSet(true)) return;

        final File screenshotsDir = new File(gameDir, "screenshots");
        new Thread(() -> {
            try {
                Thread.sleep(800);
                File latest = getLatestFile(screenshotsDir);
                if (latest != null && latest.exists()) {
                    BufferedImage img = ImageIO.read(latest);
                    // ハンドラ経由でコピー。画像とファイル両方を渡すことで全OSに対応
                    if (img != null && HANDLER.copyToClipboard(img, latest)) {
                        // Configの showMessage が true の時だけ通知
                        if (CONFIG.showMessage) {
                            sendNotification(messageReceiver);
                        }
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
    private static void sendNotification(Consumer<Text> chatReceiver) {
        MinecraftClient client = MinecraftClient.getInstance();
        Text title = Text.translatable("text.screenshottoclipboard.success");
        Text content = Text.literal("Copied to Clipboard!");

        client.execute(() -> {
            // notificationType が "CHAT" ならチャットへ、それ以外ならトーストへ
            if ("CHAT".equalsIgnoreCase(String.valueOf(CONFIG.notificationType))) {
                chatReceiver.accept(title);
            } else {
                client.getToastManager().add(
                        new SystemToast(SystemToast.Type.WORLD_BACKUP, title, content)
                );
            }
        });
    }

    @Unique
    private static File getLatestFile(File dir) {
        File[] fs = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
        if (fs == null || fs.length == 0) return null;
        File l = fs[0];
        for (File f : fs) if (f.lastModified() > l.lastModified()) l = f;
        return l;
    }
}