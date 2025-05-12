package com.rt.robotexcel.demo.util;

import java.awt.Toolkit;
import java.awt.datatransfer.*;

public class ClipboardManager {
    private static final int MAX_RETRIES = 10;
    private static final int RETRY_DELAY = 100; // milliseconds

    public static String getContent() {
        Exception lastException = null;
        
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable contents = clipboard.getContents(null);
                
                if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    return (String) contents.getTransferData(DataFlavor.stringFlavor);
                }
                return null;
            } catch (Exception e) {
                lastException = e;
                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        System.err.println("Falha ao acessar clipboard após " + MAX_RETRIES + " tentativas");
        if (lastException != null) {
            lastException.printStackTrace();
        }
        return null;
    }

    public static void setContent(String text) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection selection = new StringSelection(text);
                clipboard.setContents(selection, null);
                return;
            } catch (Exception e) {
                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public static void clear() {
        setContent("");
    }
}