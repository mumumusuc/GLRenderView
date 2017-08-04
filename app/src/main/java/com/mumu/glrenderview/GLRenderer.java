package com.mumu.glrenderview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.support.annotation.NonNull;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {

    private Context mContext;
    private int g_progHandle;
    private final int VIEW_SIZE = 5;
    private final int _R = 200;
    private boolean bDrawBound = false;
    private float[] g_ProjMatrix = new float[16],
            g_CameraMatrix = new float[16];
    private ViewWrapper[] mViews = new ViewWrapper[VIEW_SIZE];

    private final String vertexShaderCode =
            "attribute vec4 a_position;"
                    + "attribute vec2 a_texture;"
                    + "uniform mat4 u_m_model;"
                    + "uniform mat4 u_m_camera;"
                    + "varying vec4 v_color;"
                    + "varying vec2 v_texture;"
                    + "void main() {"
                    + "  gl_Position = u_m_camera * (u_m_model * a_position);"
                    //+ "  gl_Position = a_position;"
                    + "  v_texture = a_texture;"
                    + "  v_color = vec4(1.0,1.0,1.0,1.0);"
                    + "}";

    private final String fragmentShaderCode =
            "precision mediump float;"
                    + "varying vec4 v_color;"
                    + "varying vec2 v_texture;"
                    + "uniform sampler2D u_sampler; "
                    + "uniform bool u_draw_bound; "
                    + "void main() {"
                    + "  if(u_draw_bound && (v_texture.s <= 0.005 || v_texture.s >= 0.995 || v_texture.t <= 0.005 || v_texture.t >= 0.995)){"
                    + "    gl_FragColor = v_color;"
                    + "  }else{"
                    + "    gl_FragColor = texture2D(u_sampler, v_texture);"
                    //+ " gl_FragColor = vec4(1.0f,1.0f,1.0f,1.0f);"
                    + "  }"
                    + "}";


    public GLRenderer(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        g_progHandle = GLUtil.createProgram(vertexShaderCode, fragmentShaderCode);
        for (int i = 0; i < VIEW_SIZE; i++) {
            mViews[i] = new ViewWrapper();
            mViews[i].init();
            mViews[i].initTexture(i);
            mViews[i].updateTexture(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher));
            Matrix.translateM(mViews[i].mModelMatrix, 0, (i - VIEW_SIZE / 2) * 2.1f, 0, 0);
        }
        //配置GL
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) height / width;
        Matrix.frustumM(g_ProjMatrix, 0, -1f, 1f, -ratio, ratio, 0.1f, 10f);
        Matrix.setLookAtM(g_CameraMatrix, 0, 0, 0, 0.1f, 0, 0, 0, 0, 1, 0);
        Matrix.multiplyMM(g_CameraMatrix, 0, g_ProjMatrix, 0, g_CameraMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(g_progHandle);
        for (int i = 0; i < VIEW_SIZE; i++) {
            draw(mViews[i]);
        }
    }

    private void draw(@NonNull ViewWrapper view) {
        int _u_m_camera = GLES20.glGetUniformLocation(g_progHandle, "u_m_camera");
        GLES20.glUniformMatrix4fv(_u_m_camera, 1, false, g_CameraMatrix, 0);
        int _u_m_model = GLES20.glGetUniformLocation(g_progHandle, "u_m_model");
        GLES20.glUniformMatrix4fv(_u_m_model, 1, false, view.mModelMatrix, 0);
        int _a_position = GLES20.glGetAttribLocation(g_progHandle, "a_position");
        GLES20.glEnableVertexAttribArray(_a_position);
        GLES20.glVertexAttribPointer(_a_position, 3, GLES20.GL_FLOAT, false, view.VERTEX_SIZE, view.getVertexBuffer());

        final int _u_draw_bound = GLES20.glGetUniformLocation(g_progHandle, "u_draw_bound");
        GLES20.glUniform1i(_u_draw_bound, bDrawBound ? 1 : 0);

        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + view.getTextureUnit());
        int _a_texture = GLES20.glGetAttribLocation(g_progHandle, "a_texture");
        GLES20.glEnableVertexAttribArray(_a_texture);
        GLES20.glVertexAttribPointer(_a_texture, 2, GLES20.GL_FLOAT, false, view.TEXTURE_SIZE, view.getTextureBuffer());
        int _u_sampler = GLES20.glGetUniformLocation(g_progHandle, "u_sampler");
        GLES20.glUniform1i(_u_sampler, view.getTextureUnit());

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, view.VERTEX_SIZE / 3);
        GLES20.glDisable(GLES20.GL_TEXTURE_2D);
        GLES20.glActiveTexture(0);
        GLES20.glDisableVertexAttribArray(_a_texture);
        GLES20.glDisableVertexAttribArray(_a_position);
    }


    public void zoom(@NonNull final GLSurfaceView view, final float far) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, far);
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.start();
        final float[] translate = new float[16], rotate = new float[16];
        Matrix.setIdentityM(translate, 0);
        Matrix.setIdentityM(rotate, 0);
        bDrawBound = true;
        final float z0 = _R - Math.abs(far);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            float lastValue = 0f;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                float d0 = (float) Math.atan(1f / (z0 - value));
                for (int i = 0; i < VIEW_SIZE; i++) {
                    float d = (i - VIEW_SIZE / 2) * d0;
                    Matrix.rotateM(mViews[i].mModelMatrix, 0, -d, 0, 1f, 0);
                    Matrix.translateM(mViews[i].mModelMatrix, 0, 0, 0, value);
                    //Matrix.translateM(mViews[i].mModelMatrix, 0, 0, 0, (Float) animation.getAnimatedValue());
                   // Matrix.multiplyMM(mViews[i].mModelMatrix, 0, rotate, 0, mViews[i].mModelMatrix, 0);
                   // Matrix.multiplyMM(mViews[i].mModelMatrix, 0, translate, 0, mViews[i].mModelMatrix, 0);
                }
                lastValue = value;
                view.requestRender();
            }
        });
    }
}
