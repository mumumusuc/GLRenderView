package com.mumu.glrenderview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.Matrix;
import android.support.annotation.NonNull;

import java.nio.FloatBuffer;

public class ViewWrapper {
    private int[] mTexureHandle = {0};
    private int mTextureUnit = 0;
    private FloatBuffer mVertexBuffer, mTextureBuffer;
    public float[]  mModelMatrix = new float[16];

    private final float[] vertices;
    private final float[] uvs;

    public final int VERTEX_SIZE;
    public final int TEXTURE_SIZE;

    {
        vertices = new float[]{
                -1.0f, 1.0f, 0.0f,
                -1.0f, -1.0f, 0.0f,
                1.0f, 1.0f, 0.0f,
                1.0f, -1.0f, 0.0f,
        };
        uvs = new float[]{
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
        };
        VERTEX_SIZE = vertices.length;
        TEXTURE_SIZE = uvs.length;
    }

    public void init(){
        //顶点缓存
        mVertexBuffer =GLUtil.genFloatBuffer(vertices);
        //纹理缓存
        mTextureBuffer = GLUtil.genFloatBuffer(uvs);
        Matrix.setIdentityM(mModelMatrix,0);
    }

    public void initTexture(int texture_unit){
        mTexureHandle = GLUtil.initTexture(texture_unit);
    }

    public void updateTexture(@NonNull Bitmap bmp){
        GLUtil.loadTextures(mTexureHandle[0], 0, bmp, true);
    }

    public FloatBuffer getVertexBuffer(){
        return mVertexBuffer;
    }

    public FloatBuffer getTextureBuffer(){
        return mTextureBuffer;
    }

    public int getTextureHandle(){
        return mTexureHandle[0];
    }

    public int getTextureUnit(){
        return mTextureUnit;
    }

}
