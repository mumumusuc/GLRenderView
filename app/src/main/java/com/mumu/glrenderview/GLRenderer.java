package com.mumu.glrenderview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;


import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {

    private Context mContext;
    private int g_progHandle;
    private int VIEW_SIZE = 3;
    private final int _R = 800;
    private final int _R_NEAR = 40;
    private boolean bDrawBound = true;
    private float[] g_ProjMatrix = new float[16],
            g_CameraMatrix = new float[16],
            temp = new float[16];
    private ViewWrapper[] mViews;
    private float WIDTH, HEIGHT;
    private float[] mBoundColor = {1f, 1f, 1f, 1f};
    private int mSelected = -1;
    private final float FAR = 0.5f;
    private int mIndex = VIEW_SIZE / 2;
    private final int ANIM_DURATION = 500;
    private final String VERTEXT_SAHDER_FILE = "vert.glsl";
    private final String FRAGMENT_SAHDER_FILE = "frag.glsl";
    private GLSurfaceView mHost = null;
    private Queue<Runnable> mQueue;
    private boolean ON_SURFACE_CHANGED = false;

    public GLRenderer(@NonNull Context context) {
        mContext = context;
        mQueue = new LinkedList<>();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        ON_SURFACE_CHANGED = false;
        g_progHandle = GLUtil.createProgram(
                GLUtil.readAsset(mContext.getAssets(), VERTEXT_SAHDER_FILE),
                GLUtil.readAsset(mContext.getAssets(), FRAGMENT_SAHDER_FILE));
        //配置GL
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        WIDTH = width;
        HEIGHT = height;
        Log.i("GLRenderer", "w,h=(" + WIDTH + "," + HEIGHT + ")");
        GLES20.glViewport(0, 0, width, height);
        float ratio = HEIGHT / WIDTH / 2f;
        Matrix.setIdentityM(g_ProjMatrix, 0);
        Matrix.frustumM(g_ProjMatrix, 0, -0.5f, 0.5f, -ratio, ratio, 0.1f, 10f);
        Matrix.setIdentityM(g_CameraMatrix, 0);
        Matrix.setLookAtM(g_CameraMatrix, 0, 0, 0, 0.1f, 0, 0, 0, 0, 1, 0);
        Matrix.multiplyMM(g_CameraMatrix, 0, g_ProjMatrix, 0, g_CameraMatrix, 0);
        ON_SURFACE_CHANGED = true;
        exec();
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
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

    private void zoom(
            @NonNull final GLSurfaceView view,
            final int obj_from, final int obj_to,
            final float near, final float far,
            final float obj_near, final float obj_far,
            Callback callback) {
        final Callback mCallback = callback;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(near, far);
        valueAnimator.setDuration(ANIM_DURATION);
        valueAnimator.setInterpolator(new DecelerateInterpolator());//Decelerate
        valueAnimator.start();
        bDrawBound = true;
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                for (int i = 0; i < VIEW_SIZE; i++) {
                    if (mViews[i] == null) {
                        continue;
                    }
                    float d = mViews[i].VIEW_WIDTH * 1.02f / value;
                    float to_rad = (obj_to - obj_from) * d;
                    float _d = getValue(value, near, far, obj_near, obj_far);
                    float _r = getValue(value, near, far, 0, to_rad);
                    int t = obj_from - i;
                    Matrix.setIdentityM(mViews[i].mModelMatrix, 0);
                    Matrix.translateM(mViews[i].mModelMatrix, 0, 0, 0, value);
                    Matrix.rotateM(mViews[i].mModelMatrix, 0, (float) ((t * d + _r) / Math.PI * 180f), 0, 1, 0);
                    Matrix.translateM(mViews[i].mModelMatrix, 0, 0, 0, -(value + _d));
                }
                view.requestRender();
                if (value == far && mCallback != null) {
                    mCallback.onAnimEnd();
                }
            }
        });
    }

    private float getValue(float value, float follow_from, float follow_to, float from, float to) {
        return (value - follow_from) / (follow_to - follow_from) * (to - from) + from;
    }

    public void zoomOut(@NonNull final GLSurfaceView view) {
        zoom(view, mIndex, VIEW_SIZE / 2, _R, _R_NEAR, 0, FAR, null);
    }

    public void zoomIn(@NonNull final GLSurfaceView view, int index, Callback callback) {
        mIndex = index;
        zoom(view, VIEW_SIZE / 2, mIndex, _R_NEAR, _R, FAR, 0, callback);
    }

    public RectF getViewBound(@NonNull ViewWrapper view) {
        // print(view.mModelMatrix);
        RectF rect = new RectF();
        Matrix.setIdentityM(temp, 0);
        Matrix.multiplyMM(temp, 0, g_CameraMatrix, 0, view.mModelMatrix, 0);
        print("camera[]", g_CameraMatrix);
        print("model[]", view.mModelMatrix);
        float[] result = new float[4];
        RectF src_rect = view.getRect();
        Log.d("GLRenderer", "src -> " + src_rect);
        float[] src = {src_rect.left, src_rect.top, 0, 1};
        Matrix.multiplyMV(result, 0, temp, 0, src, 0);
        rect.left = WIDTH * result[0] + 0.5f * WIDTH;
        rect.top = HEIGHT * 0.5f - result[1] * WIDTH;
        src[0] = src_rect.right;
        src[1] = src_rect.bottom;
        src[2] = 0;
        src[3] = 1;
        Matrix.multiplyMV(result, 0, temp, 0, src, 0);
        rect.right = WIDTH * result[0] + 0.5f * WIDTH;
        rect.bottom = HEIGHT * 0.5f - result[1] * WIDTH;
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

    private void print(String tag, float[] dst) {
        StringBuilder sb = new StringBuilder();
        sb.append(tag).append(" = {");
        for (float f : dst) {
            sb.append(f).append(", ");
        }
        sb.append("}");
        Log.d("GLRenderer", sb.toString());
    }

    public void createViews(@NonNull GLSurfaceView host, int capacity) {
        VIEW_SIZE = capacity;
        mHost = host;
        if (mViews != null) {
            for (int i = 0; i < mViews.length; i++) {
                if (mViews[i] == null)
                    continue;
                mViews[i].destroy();
            }
        }
        mViews = new ViewWrapper[VIEW_SIZE];
    }

    public void bindView(int index, View view) {
        Log.i("GLRenderer", "current thread is " + Thread.currentThread().getName());
        if (mViews[index] == null)
            mViews[index] = new ViewWrapper();
        mViews[index].create(view, WIDTH, HEIGHT);
        if (mViews[index].getTextureHandle() > 0) {
            mViews[index].invalidate(false);
        } else {
            mViews[index].initTexture(index);
            mViews[index].invalidate(true);
        }
        int t = index - VIEW_SIZE / 2;
        float d = mViews[index].VIEW_WIDTH * 1.02f / _R;
        Matrix.setIdentityM(mViews[index].mModelMatrix, 0);
        Matrix.translateM(mViews[index].mModelMatrix, 0, 0, 0, _R);
        Matrix.rotateM(mViews[index].mModelMatrix, 0, (float) (t * d / Math.PI * 180f), 0, 1, 0);
        Matrix.translateM(mViews[index].mModelMatrix, 0, 0, 0, -_R);
    }

    /**
     * test only
     *
     * @param index
     * @param w
     * @param h
     * @param str
     */
    public void bindView(int index, float l, float t, float w, float h, String str) {
        Log.i("GLRenderer", "current thread is " + Thread.currentThread().getName());
        if (mViews[index] == null)
            mViews[index] = new ViewWrapper();
        mViews[index].create((int) l, (int) t, (int) w, (int) h, WIDTH, HEIGHT);
        if (mViews[index].getTextureHandle() > 0) {
            mViews[index].invalidate(str, false);
        } else {
            mViews[index].initTexture(index);
            mViews[index].invalidate(str, true);
        }
        int _t = index - VIEW_SIZE / 2;
        float d = mViews[index].VIEW_WIDTH * 1.02f / _R;
        Matrix.setIdentityM(mViews[index].mModelMatrix, 0);
        Matrix.translateM(mViews[index].mModelMatrix, 0, 0, 0, _R);
        Matrix.rotateM(mViews[index].mModelMatrix, 0, (float) (_t * d / Math.PI * 180f), 0, 1, 0);
        Matrix.translateM(mViews[index].mModelMatrix, 0, 0, 0, -_R);
    }

    public void post(@NonNull Runnable r) {
        if (ON_SURFACE_CHANGED) {
            if (mHost != null) {
                mHost.queueEvent(r);
            } else {
                //this MAY CAUSE ERROR !
                r.run();
            }
        } else {
            mQueue.offer(r);
        }
    }

    private void exec() {
        if (ON_SURFACE_CHANGED) {
            while (mQueue.size() > 0) {
                final Runnable r = mQueue.poll();
                if (mHost != null) {
                    mHost.queueEvent(r);
                } else {
                    //this MAY CAUSE ERROR !
                    r.run();
                }
            }
        } else {
            Log.w("GLRenderer", "exec -> warning, surface has'nt been created");
        }
    }

    public interface Callback {
        void onAnimEnd();
    }
}
