package com.misosouptgit.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface WinClipboard extends StdCallLibrary {
    WinClipboard INSTANCE = Native.load("user32", WinClipboard.class, W32APIOptions.DEFAULT_OPTIONS);
    WinKernel KERNEL = Native.load("kernel32", WinKernel.class, W32APIOptions.DEFAULT_OPTIONS);

    boolean OpenClipboard(Pointer hWndNewOwner);
    boolean EmptyClipboard();
    Pointer SetClipboardData(int uFormat, Pointer hMem);
    boolean CloseClipboard();

    interface WinKernel extends StdCallLibrary {
        Pointer GlobalAlloc(int uFlags, long dwBytes);
        Pointer GlobalLock(Pointer hMem);
        boolean GlobalUnlock(Pointer hMem);
        Pointer GlobalFree(Pointer hMem);
    }
}