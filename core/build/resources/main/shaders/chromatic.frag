#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_time;
uniform float u_aberration;

void main() {
    // Sample RGB channels with slight offset for chromatic aberration
    float r = texture2D(u_texture, v_texCoords + vec2(u_aberration, 0.0)).r;
    float g = texture2D(u_texture, v_texCoords).g;
    float b = texture2D(u_texture, v_texCoords - vec2(u_aberration, 0.0)).b;
    
    vec4 color = texture2D(u_texture, v_texCoords);
    color.r = r;
    color.b = b;
    
    gl_FragColor = color;
}
