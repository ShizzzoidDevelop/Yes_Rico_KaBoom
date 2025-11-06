package com.yesricokaboom.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.yesricokaboom.YesRicoKaBoomGame;

public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Yes Rico KaBoom - Neon Minesweeper");
        config.setWindowedMode(1200, 800);
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.setResizable(true);
        
        // Use default OpenGL version for compatibility
        
        new Lwjgl3Application(new YesRicoKaBoomGame(), config);
    }
}