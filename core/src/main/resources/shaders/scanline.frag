#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_time;
uniform float u_scanlineIntensity;
uniform float u_scanlineCount;

void main() {
    vec4 color = texture2D(u_texture, v_texCoords);
    
    // Create scanlines
    float scanline = sin(v_texCoords.y * u_scanlineCount * 3.14159) * 0.5 + 0.5;
    scanline = pow(scanline, 2.0);
    
    // Apply scanline effect
    color.rgb *= (1.0 - u_scanlineIntensity * (1.0 - scanline));
    
    // Add subtle flicker
    float flicker = sin(u_time * 10.0) * 0.02 + 0.98;
    color.rgb *= flicker;
    
    gl_FragColor = color;
}

