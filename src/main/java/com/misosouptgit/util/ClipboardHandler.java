package com.misosouptgit.util;

import java.awt.image.BufferedImage;
import java.io.File;

public interface ClipboardHandler {
    /**
     * @param image n
     * @param file n
     * @return n
     */
    boolean copyToClipboard(BufferedImage image, File file);
}