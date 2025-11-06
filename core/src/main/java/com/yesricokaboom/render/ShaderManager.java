package com.yesricokaboom.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Manages post-processing shaders for neon effects
 */
public class ShaderManager {
    
    private ShaderProgram bloomShader;
    private ShaderProgram scanlineShader;
    private ShaderProgram chromaticAberrationShader;
    private ShaderProgram vhsShader;
    
    private FrameBuffer sceneBuffer;
    private FrameBuffer bloomBuffer;
    private FrameBuffer finalBuffer;
    
    private SpriteBatch postBatch;
    private TextureRegion sceneTexture;
    private TextureRegion bloomTexture;
    private TextureRegion finalTexture;
    
    private float time;
    private boolean effectsEnabled;
    
    public ShaderManager() {
        initializeShaders();
        initializeBuffers();
        this.time = 0f;
        this.effectsEnabled = true;
    }
    
    private void initializeShaders() {
        // For now, disable complex shaders until all files are ready
        // Will implement basic neon effects with built-in shaders
        postBatch = new SpriteBatch();
    }
    
    private void initializeBuffers() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        
        sceneBuffer = new FrameBuffer(com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888, width, height, false);
        bloomBuffer = new FrameBuffer(com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888, width, height, false);
        finalBuffer = new FrameBuffer(com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888, width, height, false);
        
        sceneTexture = new TextureRegion(sceneBuffer.getColorBufferTexture());
        bloomTexture = new TextureRegion(bloomBuffer.getColorBufferTexture());
        finalTexture = new TextureRegion(finalBuffer.getColorBufferTexture());
    }
    
    public void beginSceneRender() {
        // For now, just clear the screen
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
    
    public void endSceneRender() {
        // Basic neon effects will be applied in individual screens
        // Complex post-processing will be added later
    }
    
    private void applyPostProcessing() {
        // Step 1: Extract bright areas for bloom
        applyBloom();
        
        // Step 2: Apply scanlines
        applyScanlines();
        
        // Step 3: Apply chromatic aberration
        applyChromaticAberration();
        
        // Step 4: Apply VHS effects
        applyVHSEffects();
        
        // Step 5: Render final result to screen
        renderFinal();
    }
    
    private void applyBloom() {
        bloomBuffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        postBatch.setShader(bloomShader);
        postBatch.begin();
        bloomShader.setUniformf("u_time", time);
        bloomShader.setUniformf("u_intensity", 1.5f);
        postBatch.draw(sceneTexture, 0, 0);
        postBatch.end();
        
        bloomBuffer.end();
    }
    
    private void applyScanlines() {
        finalBuffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        postBatch.setShader(scanlineShader);
        postBatch.begin();
        scanlineShader.setUniformf("u_time", time);
        scanlineShader.setUniformf("u_scanlineIntensity", 0.1f);
        scanlineShader.setUniformf("u_scanlineCount", 240f);
        postBatch.draw(sceneTexture, 0, 0);
        postBatch.end();
        
        finalBuffer.end();
    }
    
    private void applyChromaticAberration() {
        // Apply chromatic aberration to final buffer
        postBatch.setShader(chromaticAberrationShader);
        postBatch.begin();
        chromaticAberrationShader.setUniformf("u_time", time);
        chromaticAberrationShader.setUniformf("u_aberration", 0.002f);
        postBatch.draw(finalTexture, 0, 0);
        postBatch.end();
    }
    
    private void applyVHSEffects() {
        postBatch.setShader(vhsShader);
        postBatch.begin();
        vhsShader.setUniformf("u_time", time);
        vhsShader.setUniformf("u_noiseIntensity", 0.05f);
        vhsShader.setUniformf("u_distortion", 0.01f);
        postBatch.draw(finalTexture, 0, 0);
        postBatch.end();
    }
    
    private void renderFinal() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        postBatch.setShader(null);
        postBatch.begin();
        postBatch.draw(finalTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        postBatch.end();
    }
    
    public void update(float deltaTime) {
        time += deltaTime;
    }
    
    public void setEffectsEnabled(boolean enabled) {
        this.effectsEnabled = enabled;
    }
    
    public boolean isEffectsEnabled() {
        return effectsEnabled;
    }
    
    public void dispose() {
        if (bloomShader != null) bloomShader.dispose();
        if (scanlineShader != null) scanlineShader.dispose();
        if (chromaticAberrationShader != null) chromaticAberrationShader.dispose();
        if (vhsShader != null) vhsShader.dispose();
        if (sceneBuffer != null) sceneBuffer.dispose();
        if (bloomBuffer != null) bloomBuffer.dispose();
        if (finalBuffer != null) finalBuffer.dispose();
        if (postBatch != null) postBatch.dispose();
    }
}
