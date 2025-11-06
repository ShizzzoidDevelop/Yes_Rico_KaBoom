#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_time;
uniform float u_noiseIntensity;
uniform float u_distortion;

void main() {
    // Add VHS-style distortion
    vec2 distortedCoords = v_texCoords;
    distortedCoords.x += sin(v_texCoords.y * 100.0 + u_time * 10.0) * u_distortion;
    
    vec4 color = texture2D(u_texture, distortedCoords);
    
    // Add noise
    float noise = fract(sin(dot(v_texCoords + u_time, vec2(12.9898, 78.233))) * 43758.5453);
    color.rgb += (noise - 0.5) * u_noiseIntensity;
    
    // Add subtle color shift
    color.r += sin(u_time * 5.0) * 0.05;
    color.b += cos(u_time * 3.0) * 0.05;
    
    gl_FragColor = color;
}
