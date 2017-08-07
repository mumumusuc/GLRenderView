//using gles20

attribute vec4 a_position;
attribute vec2 a_texture;
uniform mat4 u_m_model;
uniform mat4 u_m_camera;
varying vec2 v_texture;
void main() {
    gl_Position = u_m_camera * (u_m_model * a_position);
    v_texture = a_texture;
}