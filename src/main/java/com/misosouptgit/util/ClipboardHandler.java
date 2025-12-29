package com.misosouptgit.util;

import java.awt.image.BufferedImage;
import java.io.File;

public interface ClipboardHandler {
    /**
     * @param image 読み込まれた画像オブジェクト
     * @param file 保存済みのファイルオブジェクト（Linux/Macのコマンド入力に使用）
     * @return 成功した場合はtrue
     */
    boolean copyToClipboard(BufferedImage image, File file);
}