package com.yesricokaboom.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MinesweeperGame extends Game {
    public SpriteBatch spriteBatch;

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        setScreen(new MinesweeperScreen(this));
    }

    @Override
    public void dispose() {
        if (spriteBatch != null) {
            spriteBatch.dispose();
        }
        if (getScreen() != null) {
            getScreen().dispose();
        }
        Gdx.app.log("Minesweeper", "Disposed");
    }
}

