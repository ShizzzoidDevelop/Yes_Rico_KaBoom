package com.yesricokaboom.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.yesricokaboom.YesRicoKaBoomGame;
import com.yesricokaboom.core.GameManager;

/**
 * Main menu screen with neon styling
 */
public class MenuScreen implements Screen {
    
    private final YesRicoKaBoomGame game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont menuFont;
    private final GlyphLayout layout;
    
    private float time;
    private int selectedOption;
    private final String[] menuOptions = {
        "BEGINNER (9x9, 10 mines)",
        "INTERMEDIATE (16x16, 40 mines)", 
        "EXPERT (16x30, 99 mines)",
        "CUSTOM GAME",
        "SETTINGS",
        "CREDITS"
    };
    
    public MenuScreen(YesRicoKaBoomGame game) {
        this.game = game;
        this.batch = game.getSpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.titleFont = new BitmapFont();
        this.menuFont = new BitmapFont();
        this.layout = new GlyphLayout();
        
        this.time = 0f;
        this.selectedOption = 0;
    }
    
    @Override
    public void show() {
        // Setup input handling
        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case com.badlogic.gdx.Input.Keys.UP:
                        selectedOption = Math.max(0, selectedOption - 1);
                        return true;
                    case com.badlogic.gdx.Input.Keys.DOWN:
                        selectedOption = Math.min(menuOptions.length - 1, selectedOption + 1);
                        return true;
                    case com.badlogic.gdx.Input.Keys.ENTER:
                    case com.badlogic.gdx.Input.Keys.SPACE:
                        selectOption();
                        return true;
                }
                return false;
            }
        });
    }
    
    private void selectOption() {
        switch (selectedOption) {
            case 0:
                game.getGameManager().setDifficulty(GameManager.Difficulty.BEGINNER);
                game.onNewGame();
                break;
            case 1:
                game.getGameManager().setDifficulty(GameManager.Difficulty.INTERMEDIATE);
                game.onNewGame();
                break;
            case 2:
                game.getGameManager().setDifficulty(GameManager.Difficulty.EXPERT);
                game.onNewGame();
                break;
            case 3:
                // Custom game - would open dialog
                break;
            case 4:
                // Settings - would open settings screen
                break;
            case 5:
                // Credits - would show credits
                break;
        }
    }
    
    @Override
    public void render(float delta) {
        time += delta;
        
        // Clear screen with dark background
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        
        // Render animated background
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        
        // Animated neon grid background
        for (int x = 0; x < width; x += 40) {
            for (int y = 0; y < height; y += 40) {
                float alpha = (float) (Math.sin(time * 2 + x * 0.01 + y * 0.01) * 0.1 + 0.1);
                shapeRenderer.setColor(0.2f, 0.8f, 1f, alpha);
                shapeRenderer.rect(x, y, 2, 2);
            }
        }
        
        shapeRenderer.end();
        
        // Render title
        batch.begin();
        
        titleFont.getData().setScale(3f);
        titleFont.setColor(0.8f, 0.2f, 0.8f, 1f);
        String title = "YES RICO KABOOM";
        layout.setText(titleFont, title);
        float titleX = (width - layout.width) / 2;
        float titleY = height - 100;
        titleFont.draw(batch, layout, titleX, titleY);
        
        // Pulsing effect for title
        float pulse = (float) (Math.sin(time * 3) * 0.2 + 0.8);
        titleFont.setColor(0.8f * pulse, 0.2f * pulse, 0.8f * pulse, 1f);
        titleFont.draw(batch, layout, titleX, titleY);
        
        // Render menu options
        menuFont.getData().setScale(1.5f);
        float startY = titleY - 100;
        float spacing = 50;
        
        for (int i = 0; i < menuOptions.length; i++) {
            float y = startY - i * spacing;
            
            if (i == selectedOption) {
                // Highlight selected option
                menuFont.setColor(1f, 0.5f, 0.2f, 1f);
                float glow = (float) (Math.sin(time * 5) * 0.3 + 0.7);
                menuFont.setColor(1f * glow, 0.5f * glow, 0.2f * glow, 1f);
            } else {
                menuFont.setColor(0.6f, 0.6f, 0.8f, 1f);
            }
            
            layout.setText(menuFont, menuOptions[i]);
            float x = (width - layout.width) / 2;
            menuFont.draw(batch, layout, x, y);
        }
        
        // Render instructions
        menuFont.getData().setScale(1f);
        menuFont.setColor(0.4f, 0.4f, 0.6f, 1f);
        String instructions = "Use UP/DOWN arrows to navigate, ENTER to select";
        layout.setText(menuFont, instructions);
        float instX = (width - layout.width) / 2;
        float instY = 50;
        menuFont.draw(batch, layout, instX, instY);
        
        batch.end();
    }
    
    @Override
    public void resize(int width, int height) {
        // Handle resize if needed
    }
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void hide() {}
    
    @Override
    public void dispose() {
        shapeRenderer.dispose();
        titleFont.dispose();
        menuFont.dispose();
    }
}

