package com.yesricokaboom;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.yesricokaboom.core.GameManager;
import com.yesricokaboom.input.InputManager;
import com.yesricokaboom.render.ShaderManager;
import com.yesricokaboom.ui.GameScreen;
import com.yesricokaboom.ui.MenuScreen;

/**
 * Main game class - Katana ZERO inspired neon Minesweeper
 */
public class YesRicoKaBoomGame extends Game implements InputManager.InputListener {
    
    private SpriteBatch spriteBatch;
    private GameManager gameManager;
    private InputManager inputManager;
    private ShaderManager shaderManager;
    
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    
    @Override
    public void create() {
        // Initialize core systems
        spriteBatch = new SpriteBatch();
        gameManager = new GameManager();
        inputManager = new InputManager(gameManager);
        shaderManager = new ShaderManager();
        
        // Set up input listener
        inputManager.setListener(this);
        
        // Initialize screens
        menuScreen = new MenuScreen(this);
        gameScreen = new GameScreen(this);
        
        // Start with menu
        setScreen(menuScreen);
        
        Gdx.app.log("YesRicoKaBoom", "Game initialized - Neon Minesweeper ready!");
    }
    
    @Override
    public void render() {
        // Update game manager
        gameManager.update(Gdx.graphics.getDeltaTime());
        
        // Update shader time
        shaderManager.update(Gdx.graphics.getDeltaTime());
        
        // Render current screen
        super.render();
    }
    
    @Override
    public void dispose() {
        if (spriteBatch != null) spriteBatch.dispose();
        if (shaderManager != null) shaderManager.dispose();
        if (menuScreen != null) menuScreen.dispose();
        if (gameScreen != null) gameScreen.dispose();
        
        Gdx.app.log("YesRicoKaBoom", "Game disposed");
    }
    
    // Input callbacks
    @Override
    public void onCellReveal(int row, int col) {
        if (gameManager.getCurrentState() == GameManager.GameState.PLAYING) {
            gameManager.revealCell(row, col);
        }
    }
    
    @Override
    public void onCellFlag(int row, int col) {
        if (gameManager.getCurrentState() == GameManager.GameState.PLAYING) {
            gameManager.toggleFlag(row, col);
        }
    }
    
    @Override
    public void onCellQuestion(int row, int col) {
        if (gameManager.getCurrentState() == GameManager.GameState.PLAYING) {
            gameManager.toggleQuestion(row, col);
        }
    }
    
    @Override
    public void onCellChord(int row, int col) {
        if (gameManager.getCurrentState() == GameManager.GameState.PLAYING) {
            gameManager.chord(row, col);
        }
    }
    
    @Override
    public void onNewGame() {
        gameManager.startNewGame();
        setScreen(gameScreen);
    }
    
    @Override
    public void onPause() {
        gameManager.pauseGame();
    }
    
    @Override
    public void onResume() {
        gameManager.resumeGame();
    }
    
    @Override
    public void onReturnToMenu() {
        gameManager.returnToMenu();
        setScreen(menuScreen);
    }
    
    // Getters for screens
    public SpriteBatch getSpriteBatch() { return spriteBatch; }
    public GameManager getGameManager() { return gameManager; }
    public ShaderManager getShaderManager() { return shaderManager; }
}

