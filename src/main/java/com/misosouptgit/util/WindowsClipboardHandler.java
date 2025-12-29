package com.misosouptgit.util;

import com.sun.jna.Pointer;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;

public class WindowsClipboardHandler implements ClipboardHandler {
    private static final int GHND = 0x0042;

    @Override
    public boolean copyToClipboard(BufferedImage image, File file) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage conv = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        conv.getGraphics().drawImage(image, 0, 0, null);
        int[] pixels = ((DataBufferInt) conv.getRaster().getDataBuffer()).getData();

        int imageSize = w * h * 4;
        int totalSize = 40 + imageSize;

        Pointer hGlobal = WinClipboard.KERNEL.GlobalAlloc(GHND, (long) totalSize);
        if (hGlobal == null) return false;

        Pointer pMem = WinClipboard.KERNEL.GlobalLock(hGlobal);
        if (pMem == null) {
            WinClipboard.KERNEL.GlobalFree(hGlobal);
            return false;
        }

        try {
            pMem.setInt(0, 40);
            pMem.setInt(4, w);
            pMem.setInt(8, h);
            pMem.setShort(12, (short) 1);
            pMem.setShort(14, (short) 32);
            pMem.setInt(20, imageSize);
            for (int y = 0; y < h; y++) {
                int rowOff = 40 + (y * w * 4);
                int srcOff = (h - 1 - y) * w;
                pMem.write(rowOff, pixels, srcOff, w);
            }
        } finally {
            WinClipboard.KERNEL.GlobalUnlock(hGlobal);
        }

        boolean success = false;
        if (WinClipboard.INSTANCE.OpenClipboard(null)) {
            try {
                WinClipboard.INSTANCE.EmptyClipboard();
                Pointer hRes = WinClipboard.INSTANCE.SetClipboardData(8, hGlobal);
                success = (hRes != null);
            } finally {
                WinClipboard.INSTANCE.CloseClipboard();
            }
        }
        if (!success) WinClipboard.KERNEL.GlobalFree(hGlobal);
        return success;
    }
}