#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_time;
uniform float u_intensity;

void main() {
    vec4 color = texture2D(u_texture, v_texCoords);
    
    // Extract bright areas
    float brightness = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    float bloom = smoothstep(0.3, 1.0, brightness);
    
    // Apply neon glow
    vec3 neonColor = color.rgb * (1.0 + bloom * u_intensity);
    
    // Add subtle pulsing
    float pulse = sin(u_time * 3.0) * 0.1 + 0.9;
    neonColor *= pulse;
    
    gl_FragColor = vec4(neonColor, color.a);
}
