package com.yesricokaboom.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MinesweeperScreen extends ScreenAdapter {
    private final MinesweeperGame game;
    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Board board;
    private final BitmapFont font;
    private final GlyphLayout glyph;
    private final BitmapFont uiFont;
    private final GlyphLayout uiGlyph;

    private static final int TILE_PIXEL_SIZE = 16; // базовый размер "пикселя" клетки
    private static final int SCALE = 3; // коэффициент скейла для пиксель-арта
    private static final int UI_HEIGHT = 60; // высота UI панели

    private final int cols = 16;
    private final int rows = 16;
    private final int mines = 40;
    private int remainingMines;
    private float gameTime;
    private boolean gameStarted;
    private boolean gameWon;

    public MinesweeperScreen(MinesweeperGame game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
        this.camera = new OrthographicCamera();
        int worldWidth = cols * TILE_PIXEL_SIZE * SCALE;
        int worldHeight = rows * TILE_PIXEL_SIZE * SCALE + UI_HEIGHT;
        this.viewport = new FitViewport(worldWidth, worldHeight, camera);
        this.board = new Board(cols, rows, mines);
        this.remainingMines = mines;
        this.gameTime = 0f;
        this.gameStarted = false;
        this.gameWon = false;
        
        // Создаём шрифты
        this.font = new BitmapFont();
        this.uiFont = new BitmapFont();
        
        this.glyph = new GlyphLayout();
        this.uiGlyph = new GlyphLayout();
        setupInput();
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (board.isGameOver() && !gameWon) {
                    board.reset();
                    resetGame();
                    return true;
                }
                
                // Проверяем клик по кнопке рестарта
                int buttonX = cols * TILE_PIXEL_SIZE * SCALE / 2 - 40;
                int buttonY = rows * TILE_PIXEL_SIZE * SCALE + 10;
                int buttonW = 80;
                int buttonH = 30;
                
                viewport.unproject(tmpVec.set(screenX, screenY, 0));
                if (tmpVec.x >= buttonX && tmpVec.x <= buttonX + buttonW && 
                    tmpVec.y >= buttonY && tmpVec.y <= buttonY + buttonH) {
                    board.reset();
                    resetGame();
                    return true;
                }
                
                // Игровое поле
                if (tmpVec.y >= rows * TILE_PIXEL_SIZE * SCALE) return false;
                
                int tileSize = TILE_PIXEL_SIZE * SCALE;
                int x = MathUtils.floor(tmpVec.x / tileSize);
                int y = MathUtils.floor(tmpVec.y / tileSize);
                if (!board.isInside(x, y)) return false;

                if (!gameStarted) {
                    gameStarted = true;
                }

                if (button == Input.Buttons.LEFT) {
                    board.reveal(x, y);
                } else if (button == Input.Buttons.RIGHT) {
                    board.toggleFlag(x, y);
                    updateMineCount();
                }
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.R || keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
                    board.reset();
                    resetGame();
                    return true;
                }
                return false;
            }
        });
    }

    private void resetGame() {
        remainingMines = mines;
        gameTime = 0f;
        gameStarted = false;
        gameWon = false;
    }
    
    private void updateMineCount() {
        int flaggedCount = 0;
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                if (board.getCell(x, y).flagged) {
                    flaggedCount++;
                }
            }
        }
        remainingMines = mines - flaggedCount;
    }

    private final com.badlogic.gdx.math.Vector3 tmpVec = new com.badlogic.gdx.math.Vector3();

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        // Обновляем таймер
        if (gameStarted && !board.isGameOver()) {
            gameTime += delta;
        }
        
        // Проверяем победу
        if (!gameWon && board.isGameOver() && !board.hasMineExploded()) {
            gameWon = true;
        }

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        int tileSize = TILE_PIXEL_SIZE * SCALE;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // UI панель
        shapeRenderer.setColor(0.08f, 0.08f, 0.12f, 1f);
        shapeRenderer.rect(0, rows * tileSize, cols * tileSize, UI_HEIGHT);
        
        // фон игрового поля
        shapeRenderer.setColor(0.12f, 0.12f, 0.15f, 1f);
        shapeRenderer.rect(0, 0, cols * tileSize, rows * tileSize);

        // рендер клеток
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Board.Cell cell = board.getCell(x, y);
                int px = x * tileSize;
                int py = y * tileSize;

                // базовый цвет плитки в зависимости от состояния
                if (!cell.revealed) {
                    if (cell.flagged) {
                        shapeRenderer.setColor(0.25f, 0.20f, 0.15f, 1f); // флажок
                    } else {
                        shapeRenderer.setColor(0.22f, 0.24f, 0.30f, 1f); // закрытая
                    }
                } else {
                    shapeRenderer.setColor(0.16f, 0.18f, 0.22f, 1f); // открытая
                }
                shapeRenderer.rect(px, py, tileSize, tileSize);

                // граница/рамка для пиксель-арта
                if (!cell.revealed) {
                    shapeRenderer.setColor(0.35f, 0.37f, 0.40f, 1f); // светлая граница для закрытых
                } else {
                    shapeRenderer.setColor(0.08f, 0.09f, 0.11f, 1f); // тёмная граница для открытых
                }
                shapeRenderer.rect(px, py, tileSize, 2); // низ
                shapeRenderer.rect(px, py + tileSize - 2, tileSize, 2); // верх
                shapeRenderer.rect(px, py, 2, tileSize); // лев
                shapeRenderer.rect(px + tileSize - 2, py, 2, tileSize); // прав

                // содержимое
                if (cell.revealed) {
                    if (cell.mine) {
                        // бомба: пиксельный квадрат с крестом
                        shapeRenderer.setColor(0.8f, 0.2f, 0.2f, 1f);
                        int bombSize = tileSize / 3;
                        shapeRenderer.rect(px + (tileSize - bombSize) / 2, py + (tileSize - bombSize) / 2, bombSize, bombSize);
                        // крест на бомбе
                        shapeRenderer.setColor(0.9f, 0.9f, 0.9f, 1f);
                        shapeRenderer.rect(px + tileSize / 2 - 1, py + tileSize / 4, 2, tileSize / 2);
                        shapeRenderer.rect(px + tileSize / 4, py + tileSize / 2 - 1, tileSize / 2, 2);
                    }
                } else if (cell.flagged) {
                    // флаг: пиксельный флажок
                    shapeRenderer.setColor(0.9f, 0.1f, 0.1f, 1f);
                    int flagSize = tileSize / 3;
                    shapeRenderer.rect(px + (tileSize - flagSize) / 2, py + (tileSize - flagSize) / 2, flagSize, flagSize);
                }
            }
        }

        shapeRenderer.end();

        // UI элементы
        game.spriteBatch.setProjectionMatrix(camera.combined);
        game.spriteBatch.begin();

        // Счётчик мин
        uiFont.setColor(0.9f, 0.9f, 0.9f, 1f);
        uiFont.getData().setScale(1.2f);
        String minesText = String.format("Mines: %03d", remainingMines);
        uiGlyph.setText(uiFont, minesText);
        uiFont.draw(game.spriteBatch, uiGlyph, 10, rows * tileSize + UI_HEIGHT - 15);

        // Таймер
        String timeText = String.format("Time: %03.0f", gameTime);
        uiGlyph.setText(uiFont, timeText);
        uiFont.draw(game.spriteBatch, uiGlyph, cols * tileSize - uiGlyph.width - 10, rows * tileSize + UI_HEIGHT - 15);

        // Кнопка рестарта
        int buttonX = cols * tileSize / 2 - 40;
        int buttonY = rows * tileSize + 10;
        int buttonW = 80;
        int buttonH = 30;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.3f, 0.3f, 0.4f, 1f);
        shapeRenderer.rect(buttonX, buttonY, buttonW, buttonH);
        shapeRenderer.setColor(0.5f, 0.5f, 0.6f, 1f);
        shapeRenderer.rect(buttonX, buttonY, buttonW, 2);
        shapeRenderer.rect(buttonX, buttonY + buttonH - 2, buttonW, 2);
        shapeRenderer.rect(buttonX, buttonY, 2, buttonH);
        shapeRenderer.rect(buttonX + buttonW - 2, buttonY, 2, buttonH);
        shapeRenderer.end();
        uiFont.setColor(0.9f, 0.9f, 0.9f, 1f);
        uiFont.getData().setScale(0.8f);
        String buttonText = "RESTART";
        uiGlyph.setText(uiFont, buttonText);
        uiFont.draw(game.spriteBatch, uiGlyph, buttonX + (buttonW - uiGlyph.width) / 2, buttonY + (buttonH + uiGlyph.height) / 2);

        // Числа на клетках
        float baseScale = (TILE_PIXEL_SIZE * SCALE) / 16f;
        font.getData().setScale(baseScale);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Board.Cell cell = board.getCell(x, y);
                if (cell.revealed && !cell.mine && cell.adjacentMines > 0) {
                    String s = Integer.toString(cell.adjacentMines);
                    glyph.setText(font, s, numberColor(cell.adjacentMines), 0, com.badlogic.gdx.utils.Align.left, false);
                    float tx = x * tileSize + (tileSize - glyph.width) / 2f;
                    float ty = y * tileSize + (tileSize + glyph.height) / 2f;
                    font.setColor(numberColor(cell.adjacentMines));
                    font.draw(game.spriteBatch, glyph, tx, ty);
                }
            }
        }

        // Статус игры
        font.setColor(0.7f, 0.7f, 0.7f, 1f);
        font.getData().setScale(baseScale * 0.6f);
        String statusMsg;
        if (gameWon) {
            statusMsg = "VICTORY!";
            font.setColor(0.2f, 0.8f, 0.2f, 1f);
        } else if (board.isGameOver()) {
            statusMsg = "GAME OVER";
            font.setColor(0.8f, 0.2f, 0.2f, 1f);
        } else {
            statusMsg = "LMB: Open  RMB: Flag";
        }
        glyph.setText(font, statusMsg);
        float mx = (cols * tileSize - glyph.width) / 2f;
        float my = rows * tileSize + 5;
        font.draw(game.spriteBatch, glyph, mx, my);

        game.spriteBatch.end();
    }

    private Color numberColor(int n) {
        switch (n) {
            case 1: return new Color(0.2f, 0.6f, 1f, 1f);
            case 2: return new Color(0.3f, 0.85f, 0.4f, 1f);
            case 3: return new Color(1f, 0.4f, 0.4f, 1f);
            case 4: return new Color(0.4f, 0.4f, 1f, 1f);
            case 5: return new Color(1f, 0.2f, 0.2f, 1f);
            case 6: return new Color(0.2f, 1f, 1f, 1f);
            case 7: return new Color(1f, 1f, 0.2f, 1f);
            default: return new Color(0.9f, 0.9f, 0.9f, 1f);
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
        uiFont.dispose();
    }
}