package com.misosouptgit.util;

import java.awt.image.BufferedImage;
import java.io.File;

public class UnixClipboardHandler implements ClipboardHandler {
    @Override
    public boolean copyToClipboard(BufferedImage image, File file) {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("mac")) {
                // Mac: 標準の pbcopy を使用。FileTypeを指定して画像として流し込む
                return runCommand(file, "osascript", "-e", "set the clipboard to (read (POSIX file \"" + file.getAbsolutePath() + "\") as «class PNGf»)");
            } else {
                // Linux: Wayland(wl-copy) か X11(xclip) を環境に応じて使い分ける
                boolean isWayland = System.getenv("WAYLAND_DISPLAY") != null;
                if (isWayland && canRun("wl-copy")) {
                    return runCommand(file, "wl-copy", "--type", "image/png");
                } else if (canRun("xclip")) {
                    return runCommand(file, "xclip", "-selection", "clipboard", "-t", "image/png");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean canRun(String cmd) {
        try {
            return Runtime.getRuntime().exec(new String[]{"which", cmd}).waitFor() == 0;
        } catch (Exception e) { return false; }
    }

    private boolean runCommand(File file, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        if (!command[0].equals("osascript")) {
            pb.redirectInput(file);
        }
        Process p = pb.start();
        return p.waitFor() == 0;
    }
}