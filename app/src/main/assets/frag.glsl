//using gles20
precision mediump float;

varying vec2 v_texture;
uniform sampler2D u_sampler;
uniform bool u_draw_bound;
uniform vec4 u_bound_color;
void main() {
    if(u_draw_bound && (v_texture.s < 0.01 || v_texture.s > 0.99 || v_texture.t < 0.01 || v_texture.t > 0.99)){
        gl_FragColor = u_bound_color;
    }else{
        gl_FragColor = texture2D(u_sampler, v_texture);
        //gl_FragColor = vec4(1.0f,1.0f,1.0f,1.0f);
    }
}