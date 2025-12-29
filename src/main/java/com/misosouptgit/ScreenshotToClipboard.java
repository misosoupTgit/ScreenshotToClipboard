package com.misosouptgit;

import net.fabricmc.api.ClientModInitializer;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;

public class ScreenshotToClipboard implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
	}
	public static void copyToClipboard(BufferedImage image) {
		try {
			TransferableImage transferable = new TransferableImage(image);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(transferable, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private record TransferableImage(Image image) implements Transferable {
		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.imageFlavor};
		}
		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}
		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (isDataFlavorSupported(flavor)) return image;
			throw new UnsupportedFlavorException(flavor);
		}
	}
}