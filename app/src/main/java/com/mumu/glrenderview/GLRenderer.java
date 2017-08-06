package com.mumu.glrenderview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {

    private Context mContext;
    private int g_progHandle;
    private final int VIEW_SIZE = 5;
    private final int _R = 800;
    private final int _R_NEAR = 60;
    private boolean bDrawBound = true;
    private float[] g_ProjMatrix = new float[16],
            g_CameraMatrix = new float[16],
            temp = new float[16];
    private ViewWrapper[] mViews = new ViewWrapper[VIEW_SIZE];
    private float WIDTH, HEIGHT;
    private float[] mBoundColor = {1f, 1f, 1f, 1f};
    private int mSelected = -1;
    final float FAR = 0.45f;
    int mIndex = VIEW_SIZE / 2;

    private final String vertexShaderCode =
            "attribute vec4 a_position;"
                    + "attribute vec2 a_texture;"
                    + "uniform mat4 u_m_model;"
                    + "uniform mat4 u_m_camera;"
                    //+ "varying vec4 v_color;"
                    + "varying vec2 v_texture;"
                    + "void main() {"
                    + "  gl_Position = u_m_camera * (u_m_model * a_position);"
                    //+ "  gl_Position = a_position;"
                    + "  v_texture = a_texture;"
                    //+ "  v_color = vec4(1.0,1.0,1.0,1.0);"
                    + "}";

    private final String fragmentShaderCode =
            "precision highp float;"
                    //+ "varying vec4 v_color;"
                    + "varying vec2 v_texture;"
                    + "uniform sampler2D u_sampler; "
                    + "uniform bool u_draw_bound; "
                    + "uniform vec4 u_bound_color; "
                    + "void main() {"
                    + "  if(u_draw_bound && (v_texture.s < 0.01 || v_texture.s > 0.99 || v_texture.t < 0.01 || v_texture.t > 0.99)){"
                    + "    gl_FragColor = u_bound_color;"
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
        //配置GL
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        WIDTH = width;
        HEIGHT = height;
        GLES20.glViewport(0, 0, width, height);
        float ratio = HEIGHT / WIDTH / 2f;
        Matrix.frustumM(g_ProjMatrix, 0, -0.5f, 0.5f, -ratio, ratio, 0.1f, 10f);
        Matrix.setLookAtM(g_CameraMatrix, 0, 0, 0, 0.1f, 0, 0, 0, 0, 1, 0);
        Matrix.multiplyMM(g_CameraMatrix, 0, g_ProjMatrix, 0, g_CameraMatrix, 0);
        for (int i = 0; i < VIEW_SIZE; i++) {
            mViews[i] = new ViewWrapper();
            mViews[i].create((int) (WIDTH * 0.9f), (int) (HEIGHT * 0.9f), WIDTH, HEIGHT);
           // if (i == 2) {
                mViews[i].initTexture(i);
                mViews[i].invalidate(String.valueOf(i));
           // }
            int t = i - VIEW_SIZE / 2;
            float d = mViews[i].VIEW_WIDTH * 1.05f / _R;
            Matrix.translateM(mViews[i].mModelMatrix, 0, 0, 0, _R);
            Matrix.rotateM(mViews[i].mModelMatrix, 0, (float) (t * d / Math.PI * 180f), 0, 1, 0);
            Matrix.translateM(mViews[i].mModelMatrix, 0, 0, 0, -_R);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(g_progHandle);
        for (int i = 0; i < VIEW_SIZE; i++) {
            if (mViews[i] == null) {
                continue;
            }
            if (mSelected == i) {
                mBoundColor[0] = 0f;
                mBoundColor[1] = 0f;
                mBoundColor[2] = 1f;
                mBoundColor[3] = 1f;
            } else {
                mBoundColor[0] = 1f;
                mBoundColor[1] = 1f;
                mBoundColor[2] = 1f;
                mBoundColor[3] = 1f;
            }
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

        if (view.getTextureUnit() >= 0) {
            GLES20.glEnable(GLES20.GL_TEXTURE_2D);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + view.getTextureUnit());
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, view.getTextureHandle());
            int _u_sampler = GLES20.glGetUniformLocation(g_progHandle, "u_sampler");
            GLES20.glUniform1i(_u_sampler, view.getTextureUnit());
        }
        int _a_texture = GLES20.glGetAttribLocation(g_progHandle, "a_texture");
        GLES20.glEnableVertexAttribArray(_a_texture);
        GLES20.glVertexAttribPointer(_a_texture, 2, GLES20.GL_FLOAT, false, view.TEXTURE_SIZE, view.getTextureBuffer());
        int _u_bound_color = GLES20.glGetUniformLocation(g_progHandle, "u_bound_color");
        GLES20.glUniform4fv(_u_bound_color, 1, mBoundColor, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, view.VERTEX_SIZE / 3);
        GLES20.glDisable(GLES20.GL_TEXTURE_2D);
        GLES20.glActiveTexture(0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDisableVertexAttribArray(_a_texture);
        GLES20.glDisableVertexAttribArray(_a_position);
    }

    private void zoom(@NonNull final GLSurfaceView view, final int obj_from, final int obj_to, final float near, final float far, final float obj_near, final float obj_far) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(near, far);
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new DecelerateInterpolator());//Decelerate
        valueAnimator.start();
        bDrawBound = true;
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                float d = mViews[0].VIEW_WIDTH * 1.05f / value;
                float to_rad = (obj_to - obj_from) * d;
                float _d = getValue(value, near, far, obj_near, obj_far);
                float _r = getValue(value, near, far, 0, to_rad);
                for (int i = 0; i < VIEW_SIZE; i++) {
                    int t = obj_from - i;
                    Matrix.setIdentityM(mViews[i].mModelMatrix, 0);
                    Matrix.translateM(mViews[i].mModelMatrix, 0, 0, 0, value);
                    Matrix.rotateM(mViews[i].mModelMatrix, 0, (float) ((t * d + _r) / Math.PI * 180f), 0, 1, 0);
                    Matrix.translateM(mViews[i].mModelMatrix, 0, 0, 0, -(value + _d));
                }
                view.requestRender();
            }
        });
    }

    private float getValue(float value, float follow_from, float follow_to, float from, float to) {
        return (value - follow_from) / (follow_to - follow_from) * (to - from) + from;
    }

    public void zoomOut(@NonNull final GLSurfaceView view) {
        zoom(view, mIndex, VIEW_SIZE / 2, _R, _R_NEAR, 0, FAR);
        getViewBound(mViews[2]);
    }

    public void zoomIn(@NonNull final GLSurfaceView view, int index) {
        mIndex = index;
        zoom(view, VIEW_SIZE / 2, mIndex, _R_NEAR, _R, FAR, 0);
        getViewBound(mViews[2]);
    }

    public RectF getViewBound(@NonNull ViewWrapper view) {
        // print(view.mModelMatrix);
        RectF rect;
        Matrix.setIdentityM(temp, 0);
        Matrix.multiplyMM(temp, 0, g_CameraMatrix, 0, view.mModelMatrix, 0);
        float[] result = new float[4];
        rect = view.getRect();
        float[] src = {rect.left, rect.top, 0, 1};
        Matrix.multiplyMV(result, 0, temp, 0, src, 0);
        //print(result);
        result[0] = (result[0] / result[3] + 1f) * WIDTH / 2;
        result[1] = (-result[1] / result[3] + 1f) * HEIGHT / 2;
        rect.left = result[0];
        rect.top = result[1];
        src = new float[]{rect.right, rect.bottom, 0, 1};
        Matrix.multiplyMV(result, 0, temp, 0, src, 0);
        result[0] = (result[0] / result[3] + 1f) * WIDTH / 2;
        result[1] = (-result[1] / result[3] + 1f) * HEIGHT / 2;
        //print(result);
        rect.right = result[0];
        rect.bottom = result[1];
        Log.d("GLRenderer", "getViewBound -> " + rect);
        return rect;
    }

    public int showTouchedView(float x, float y) {
        // mSelected = -1;
        for (int i = 0; i < VIEW_SIZE; i++) {
            if (getViewBound(mViews[i]).contains(x, y)) {
                mSelected = i;
                break;
            }
        }
        Log.d("GLRenderer", "touch -> (" + x + "," + y + "), get = " + mSelected);
        bDrawBound = true;
        return mSelected;
    }

    private void print(float[] dst) {
        StringBuilder sb = new StringBuilder();
        sb.append("float[] = {");
        for (float f : dst) {
            sb.append(f).append(", ");
        }
        sb.append("}");
        Log.d("GLRenderer", sb.toString());
    }
}
