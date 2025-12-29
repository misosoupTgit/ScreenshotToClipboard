package com.misosouptgit.util;

import java.awt.image.BufferedImage;
import java.io.File;

public interface ClipboardHandler {
    boolean copyToClipboard(BufferedImage image, File file);
}