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
import com.yesricokaboom.core.Minefield;

/**
 * Main game screen with neon visual effects
 */
public class GameScreen implements Screen {
    
    private final YesRicoKaBoomGame game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final GlyphLayout layout;
    
    private final int TILE_SIZE = 32;
    private final int UI_HEIGHT = 80;
    
    private float time;
    private int hoverRow = -1, hoverCol = -1;
    
    public GameScreen(YesRicoKaBoomGame game) {
        this.game = game;
        this.batch = game.getSpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.layout = new GlyphLayout();
        
        this.time = 0f;
    }
    
    @Override
    public void show() {
        // Input handling is managed by InputManager
    }
    
    @Override
    public void render(float delta) {
        time += delta;
        
        GameManager gameManager = game.getGameManager();
        Minefield minefield = gameManager.getMinefield();
        
        if (minefield == null) return;
        
        // Begin shader rendering
        game.getShaderManager().beginSceneRender();
        
        // Clear with dark background
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        
        // Calculate board position (centered)
        int boardWidth = minefield.getCols() * TILE_SIZE;
        int boardHeight = minefield.getRows() * TILE_SIZE;
        int boardX = (width - boardWidth) / 2;
        int boardY = (height - boardHeight - UI_HEIGHT) / 2 + UI_HEIGHT;
        
        // Render UI panel
        renderUI(boardX, boardY, boardWidth, boardHeight);
        
        // Render game board
        renderBoard(minefield, boardX, boardY);
        
        // Render hover effects
        if (hoverRow >= 0 && hoverCol >= 0) {
            renderHoverEffect(hoverRow, hoverCol, boardX, boardY);
        }
        
        // End shader rendering
        game.getShaderManager().endSceneRender();
    }
    
    private void renderUI(int boardX, int boardY, int boardWidth, int boardHeight) {
        GameManager gameManager = game.getGameManager();
        Minefield minefield = gameManager.getMinefield();
        
        // UI background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.9f);
        shapeRenderer.rect(boardX, boardY - UI_HEIGHT, boardWidth, UI_HEIGHT);
        shapeRenderer.end();
        
        // UI text
        batch.begin();
        
        font.getData().setScale(1.2f);
        font.setColor(0.8f, 0.8f, 1f, 1f);
        
        // Mine counter
        String minesText = String.format("Mines: %03d", minefield.getRemainingMines());
        layout.setText(font, minesText);
        font.draw(batch, layout, boardX + 10, boardY - 20);
        
        // Timer
        String timeText = String.format("Time: %03.0f", gameManager.getGameTime());
        layout.setText(font, timeText);
        font.draw(batch, layout, boardX + boardWidth - layout.width - 10, boardY - 20);
        
        // Game status
        String statusText = "";
        if (gameManager.getCurrentState() == GameManager.GameState.GAME_OVER) {
            statusText = "GAME OVER";
            font.setColor(1f, 0.2f, 0.2f, 1f);
        } else if (gameManager.getCurrentState() == GameManager.GameState.VICTORY) {
            statusText = "VICTORY!";
            font.setColor(0.2f, 1f, 0.2f, 1f);
        } else {
            statusText = "PLAYING";
            font.setColor(0.6f, 0.8f, 1f, 1f);
        }
        
        layout.setText(font, statusText);
        font.draw(batch, layout, boardX + (boardWidth - layout.width) / 2, boardY - 20);
        
        batch.end();
    }
    
    private void renderBoard(Minefield minefield, int boardX, int boardY) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int row = 0; row < minefield.getRows(); row++) {
            for (int col = 0; col < minefield.getCols(); col++) {
                Minefield.Cell cell = minefield.getCell(row, col);
                int x = boardX + col * TILE_SIZE;
                int y = boardY + (minefield.getRows() - 1 - row) * TILE_SIZE;
                
                // Base tile color
                if (cell.state == Minefield.CellState.OPEN) {
                    if (cell.isMine) {
                        // Mine - red with glow
                        shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1f);
                    } else {
                        // Open cell - dark
                        shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 1f);
                    }
                } else if (cell.state == Minefield.CellState.FLAGGED) {
                    // Flagged - orange
                    shapeRenderer.setColor(0.8f, 0.4f, 0.1f, 1f);
                } else {
                    // Closed - default
                    shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
                }
                
                shapeRenderer.rect(x, y, TILE_SIZE, TILE_SIZE);
                
                // Neon border
                shapeRenderer.setColor(0.3f, 0.6f, 1f, 0.3f);
                shapeRenderer.rect(x, y, TILE_SIZE, 2);
                shapeRenderer.rect(x, y + TILE_SIZE - 2, TILE_SIZE, 2);
                shapeRenderer.rect(x, y, 2, TILE_SIZE);
                shapeRenderer.rect(x + TILE_SIZE - 2, y, 2, TILE_SIZE);
            }
        }
        
        shapeRenderer.end();
        
        // Render numbers and symbols
        batch.begin();
        
        font.getData().setScale(1.5f);
        
        for (int row = 0; row < minefield.getRows(); row++) {
            for (int col = 0; col < minefield.getCols(); col++) {
                Minefield.Cell cell = minefield.getCell(row, col);
                int x = boardX + col * TILE_SIZE;
                int y = boardY + (minefield.getRows() - 1 - row) * TILE_SIZE;
                
                if (cell.state == Minefield.CellState.OPEN && !cell.isMine && cell.adjacentMines > 0) {
                    // Number
                    String number = String.valueOf(cell.adjacentMines);
                    font.setColor(getNumberColor(cell.adjacentMines));
                    layout.setText(font, number);
                    font.draw(batch, layout, x + (TILE_SIZE - layout.width) / 2, y + (TILE_SIZE + layout.height) / 2);
                } else if (cell.state == Minefield.CellState.OPEN && cell.isMine) {
                    // Mine symbol
                    font.setColor(1f, 0.2f, 0.2f, 1f);
                    layout.setText(font, "*");
                    font.draw(batch, layout, x + (TILE_SIZE - layout.width) / 2, y + (TILE_SIZE + layout.height) / 2);
                } else if (cell.state == Minefield.CellState.FLAGGED) {
                    // Flag symbol
                    font.setColor(1f, 0.5f, 0.2f, 1f);
                    layout.setText(font, "F");
                    font.draw(batch, layout, x + (TILE_SIZE - layout.width) / 2, y + (TILE_SIZE + layout.height) / 2);
                }
            }
        }
        
        batch.end();
    }
    
    private void renderHoverEffect(int row, int col, int boardX, int boardY) {
        Minefield minefield = game.getGameManager().getMinefield();
        if (minefield == null) return;
        
        int x = boardX + col * TILE_SIZE;
        int y = boardY + (minefield.getRows() - 1 - row) * TILE_SIZE;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Pulsing glow effect
        float glow = (float) (Math.sin(time * 8) * 0.3 + 0.7);
        shapeRenderer.setColor(0.2f, 0.8f, 1f, glow * 0.5f);
        shapeRenderer.rect(x - 2, y - 2, TILE_SIZE + 4, TILE_SIZE + 4);
        
        shapeRenderer.end();
    }
    
    private com.badlogic.gdx.graphics.Color getNumberColor(int number) {
        switch (number) {
            case 1: return new com.badlogic.gdx.graphics.Color(0.2f, 0.6f, 1f, 1f);
            case 2: return new com.badlogic.gdx.graphics.Color(0.3f, 0.8f, 0.3f, 1f);
            case 3: return new com.badlogic.gdx.graphics.Color(1f, 0.3f, 0.3f, 1f);
            case 4: return new com.badlogic.gdx.graphics.Color(0.5f, 0.3f, 1f, 1f);
            case 5: return new com.badlogic.gdx.graphics.Color(1f, 0.5f, 0.2f, 1f);
            case 6: return new com.badlogic.gdx.graphics.Color(0.2f, 1f, 1f, 1f);
            case 7: return new com.badlogic.gdx.graphics.Color(1f, 1f, 0.2f, 1f);
            case 8: return new com.badlogic.gdx.graphics.Color(0.8f, 0.8f, 0.8f, 1f);
            default: return new com.badlogic.gdx.graphics.Color(1f, 1f, 1f, 1f);
        }
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
        font.dispose();
    }
}

