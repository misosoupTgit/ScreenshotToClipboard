package com.misosouptgit.mixin;

import com.misosouptgit.config.ModConfig;
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

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public class ScreenshotMixin {
    @Unique
    private static final ModConfig CONFIG = ModConfig.load();
    @Unique
    private static final int RETRY_COUNT = 10;
    @Unique
    private static final int RETRY_DELAY_MS = 100;
    @Unique
    private static final int NOTIFICATION_DELAY_MS = 200;

    @Inject(method = "saveScreenshot", at = @At("RETURN"))
    private static void onSaveScreenshot(File gameDirectory, @SuppressWarnings("unused") Framebuffer framebuffer, Consumer<Text> messageReceiver, @SuppressWarnings("unused") CallbackInfo ci) {
        File screenshotsDir = new File(gameDirectory, "screenshots");

        new Thread(() -> {
            try {
                processScreenshotCopy(screenshotsDir, messageReceiver);
            } catch (Exception e) {
                System.err.println("[ScreenshotToClipboard] Unexpected error: " + e.getMessage());
            }
        }).start();
    }

    @Unique
    private static void processScreenshotCopy(File screenshotsDir, Consumer<Text> messageReceiver) throws InterruptedException {
        Optional<File> screenshotFile = waitForFileReady(screenshotsDir);

        if (screenshotFile.isEmpty()) {
            return;
        }

        String absolutePath = screenshotFile.get().getAbsolutePath();
        if (!copyToClipboardByOS(absolutePath)) {
            return;
        }

        if (CONFIG.showMessage) {
            Thread.sleep(NOTIFICATION_DELAY_MS);
            sendNotification(messageReceiver);
        }
    }

    @Unique
    private static Optional<File> waitForFileReady(File dir) throws InterruptedException {
        for (int i = 0; i < RETRY_COUNT; i++) {
            File latest = getLatestScreenshotFile(dir);
            if (isImageFileReady(latest)) {
                return Optional.of(latest);
            }
            Thread.sleep(RETRY_DELAY_MS);
        }
        return Optional.empty();
    }

    @Unique
    private static boolean isImageFileReady(File file) {
        return file != null && file.exists() && file.length() > 0 && file.canRead();
    }

    @Unique
    private static void sendNotification(Consumer<Text> chatReceiver) {
        // Use translation keys for both title and message content
        Text title = Text.translatable("text.screenshottoclipboard.success");
        Text message = Text.translatable("text.screenshottoclipboard.copied_message");

        if (CONFIG.notificationType == ModConfig.NotificationType.TOAST) {
            MinecraftClient client = MinecraftClient.getInstance();
            // Passing translated text objects to the toast
            client.execute(() -> client.getToastManager().add(
                    new SystemToast(SystemToast.Type.WORLD_BACKUP, title, message)
            ));
            return;
        }
        // For chat notifications
        chatReceiver.accept(title);
    }

    @Unique
    private static boolean copyToClipboardByOS(String filePath) {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder pb = createProcessForOS(os, filePath);

        if (pb == null) {
            return false;
        }

        try {
            return pb.start().waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Unique
    private static ProcessBuilder createProcessForOS(String os, String filePath) {
        if (os.contains("win")) {
            String command = String.format(
                    "Add-Type -AssemblyName System.Windows.Forms; [System.Windows.Forms.Clipboard]::SetImage([System.Drawing.Image]::FromFile('%s'))",
                    filePath
            );
            return new ProcessBuilder("powershell", "-Command", command);
        }

        if (os.contains("mac")) {
            String command = String.format("set the clipboard to (read (POSIX file \"%s\") as «class PNGf»)", filePath);
            return new ProcessBuilder("osascript", "-e", command);
        }

        if (os.contains("nix") || os.contains("nux")) {
            return new ProcessBuilder("sh", "-c", "xclip -selection clipboard -t image/png -i " + filePath);
        }

        return null;
    }

    @Unique
    private static File getLatestScreenshotFile(File dir) {
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null || files.length == 0) {
            return null;
        }

        File latest = files[0];
        for (File file : files) {
            if (file.lastModified() > latest.lastModified()) {
                latest = file;
            }
        }
        return latest;
    }
}