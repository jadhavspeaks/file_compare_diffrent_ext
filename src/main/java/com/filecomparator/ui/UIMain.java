package com.filecomparator.ui;

import javax.swing.SwingUtilities;

public class UIMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.init();
        });
    }
}
