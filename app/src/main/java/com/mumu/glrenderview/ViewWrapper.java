package com.mumu.glrenderview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.support.annotation.NonNull;
import android.view.View;

import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;

import static android.graphics.Bitmap.Config.ARGB_8888;

public class ViewWrapper {
    private int[] mTexureHandle = {0};
    private int mTextureUnit = -1;
    private FloatBuffer mVertexBuffer, mTextureBuffer;
    public float[] mModelMatrix = new float[16];

    private float[] vertices;
    private float[] uvs;

    public int VERTEX_SIZE;
    public int TEXTURE_SIZE;
    public float VIEW_WIDTH;
    public float VIEW_HEIGHT;

    private int _view_width, _view_height;

    private WeakReference<View> mViewCache = null;
    private Bitmap mViewBuffer = null;
    private Canvas mCanvas;


    public void create(@NonNull View v, float parentWidth, float parentHeight) {
        if (v == null) {
            throw new IllegalArgumentException("input view is null");
        }
        mViewCache = new WeakReference<>(v);
        create(v.getLeft(), v.getTop(), v.getWidth(), v.getHeight(), parentWidth, parentHeight);
    }

    public void create(int left, int top, int w, int h, float parentWidth, float parentHeight) {
        _view_width = w;
        _view_height = h;
        float ratio = parentHeight / parentWidth / 2;
        VIEW_WIDTH = w / parentWidth;
        VIEW_HEIGHT = h / parentWidth;
        float _left = left / parentWidth - 0.5f;
        float _top = ratio - top / parentWidth;
        vertices = new float[]{
                _left, _top, 0.0f,
                _left, _top - VIEW_HEIGHT, 0.0f,
                _left + VIEW_WIDTH, _top, 0.0f,
                _left + VIEW_WIDTH, _top - VIEW_HEIGHT, 0.0f,
        };
        uvs = new float[]{
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
        };
        VERTEX_SIZE = vertices.length;
        TEXTURE_SIZE = uvs.length;
        //顶点缓存
        mVertexBuffer = GLUtil.genFloatBuffer(vertices, mVertexBuffer);
        //纹理缓存
        mTextureBuffer = GLUtil.genFloatBuffer(uvs, mTextureBuffer);
        Matrix.setIdentityM(mModelMatrix, 0);
    }

    public void destroy() {
        if (mViewBuffer != null && !mViewBuffer.isRecycled()) {
            //mViewBuffer.recycle();
        }
        if (mViewCache != null) {
            mViewCache.clear();
            mViewCache = null;
        }
    }

    public RectF getRect() {
        return new RectF(vertices[0], vertices[1], vertices[9], vertices[10]);
    }

    public void initTexture(int texture_unit) {
        if (mViewBuffer != null
                && mViewBuffer.getWidth() == _view_width
                && mViewBuffer.getHeight() == _view_height
                && !mViewBuffer.isRecycled()) {
            mViewBuffer.eraseColor(0);
        } else {
            if (mViewBuffer != null && !mViewBuffer.isRecycled()) {
                mViewBuffer.recycle();
            }
            if (_view_width != 0 && _view_height != 0) {
                mViewBuffer = Bitmap.createBitmap(_view_width, _view_height, ARGB_8888);
                mCanvas = new Canvas(mViewBuffer);
            }
        }
        if (mTextureUnit != texture_unit) {
            mTextureUnit = texture_unit;
            mTexureHandle = GLUtil.initTexture(mTextureUnit);
        }
    }

    Paint mPaint = new Paint();

    /**
     * test only
     *
     * @param str string to render
     */
    public void invalidate(String str, boolean reload) {
        if (mCanvas != null) {
            mPaint.setTextSize(Math.min(_view_width, _view_height) * 0.8f);
            mPaint.setColor(Color.WHITE);
            mPaint.setTextAlign(Paint.Align.CENTER);
            Rect r = new Rect();
            mPaint.getTextBounds(str, 0, str.length(), r);
            mCanvas.drawText(str, (_view_width) / 2, (_view_height + r.height()) / 2, mPaint);
            GLUtil.loadTextures(mTexureHandle[0], mTextureUnit, mViewBuffer, reload, false);
        }
    }

    public void invalidate(boolean reload) {
        if (mCanvas != null && mViewCache != null) {
            final View view = mViewCache.get();
            if (view != null) {
                view.draw(mCanvas);
                GLUtil.loadTextures(mTexureHandle[0], mTextureUnit, mViewBuffer, reload, false);
            }
        }
    }

    public FloatBuffer getVertexBuffer() {
        return mVertexBuffer;
    }

    public FloatBuffer getTextureBuffer() {
        return mTextureBuffer;
    }

    public int getTextureHandle() {
        return mTexureHandle[0];
    }

    public int getTextureUnit() {
        return mTextureUnit;
    }

}
