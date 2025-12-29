package com.misosouptgit.mixin;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public class ScreenshotMixin {

    private static final String SUCCESS_KEY = "text.screenshottoclipboard.success";
    private static final int MAX_ATTEMPTS = 20;
    private static final long POLL_INTERVAL_MS = 100;
    private static final long MESSAGE_DELAY_MS = 150;
    private static final long TIME_BUFFER_MS = 1000;

    @Inject(method = "saveScreenshot", at = @At("RETURN"))
    private static void onSaveScreenshot(File gameDirectory, @SuppressWarnings("unused") Framebuffer framebuffer, Consumer<Text> messageReceiver, @SuppressWarnings("unused") CallbackInfo ci) {
        final File screenshotsDir = new File(gameDirectory, "screenshots");
        final long startTime = System.currentTimeMillis();

        new Thread(() -> {
            try {
                File targetFile = waitForLatestScreenshot(screenshotsDir, startTime);

                if (targetFile != null) {
                    // Slight delay to ensure Minecraft's default "Saved screenshot" message appears first
                    Thread.sleep(MESSAGE_DELAY_MS);

                    if (copyToClipboardByOS(targetFile.getAbsolutePath())) {
                        messageReceiver.accept(Text.translatable(SUCCESS_KEY));
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("[ScreenshotToClipboard] Unexpected error: " + e.getMessage());
            }
        }).start();
    }

    private static File waitForLatestScreenshot(File dir, long startTime) throws InterruptedException {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            File latest = getLatestScreenshotFileAfter(dir, startTime);

            if (latest != null && latest.exists() && latest.length() > 0 && latest.canRead()) {
                return latest;
            }
            Thread.sleep(POLL_INTERVAL_MS);
        }
        return null;
    }

    private static File getLatestScreenshotFileAfter(File dir, long startTime) {
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null || files.length == 0) return null;

        File latest = null;
        for (File f : files) {
            // Include a buffer to account for potential OS clock drift or filesystem latency
            if (f.lastModified() >= (startTime - TIME_BUFFER_MS)) {
                if (latest == null || f.lastModified() > latest.lastModified()) {
                    latest = f;
                }
            }
        }
        return latest;
    }

    private static boolean copyToClipboardByOS(String filePath) {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            ProcessBuilder pb = createProcessForOS(os, filePath);
            if (pb == null) return false;

            return pb.start().waitFor() == 0;
        } catch (Exception e) {
            System.err.println("[ScreenshotToClipboard] Native command failed: " + e.getMessage());
            return false;
        }
    }

    private static ProcessBuilder createProcessForOS(String os, String filePath) {
        if (os.contains("win")) {
            String command = String.format(
                    "Add-Type -AssemblyName System.Windows.Forms; [System.Windows.Forms.Clipboard]::SetImage([System.Drawing.Image]::FromFile('%s'))",
                    filePath
            );
            return new ProcessBuilder("powershell", "-Command", command);
        } else if (os.contains("mac")) {
            String command = String.format("set the clipboard to (read (POSIX file \"%s\") as «class PNGf»)", filePath);
            return new ProcessBuilder("osascript", "-e", command);
        } else if (os.contains("nix") || os.contains("nux")) {
            return new ProcessBuilder("sh", "-c", "xclip -selection clipboard -t image/png -i " + filePath);
        }
        return null;
    }
}