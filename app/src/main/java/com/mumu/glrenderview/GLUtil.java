package com.mumu.glrenderview;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.support.annotation.NonNull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLUtil {
    public static String readAsset(@NonNull AssetManager assetManager, @NonNull String fileName) {
        StringBuilder sb = new StringBuilder();
        InputStream is = null;
        byte[] buffer = new byte[1024];
        try {
            is = assetManager.open(fileName);
            int size = 0;
            while ((size = is.read(buffer)) > 0) {
                for (int i = 0; i < size; i++)
                    sb.append((char) buffer[i]);
            }
            return sb.toString();
        } catch (IOException e) {

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    public static int createProgram(String vert, String frag) {
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vert);
        GLES20.glCompileShader(vertexShader);
        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, frag);
        GLES20.glCompileShader(fragmentShader);
        int programHandle = GLES20.glCreateProgram();
        GLES20.glAttachShader(programHandle, vertexShader);
        GLES20.glAttachShader(programHandle, fragmentShader);
        GLES20.glLinkProgram(programHandle);
        return programHandle;
    }

    public static int[] initTexture(int TEXTURE_UNIT) {
        int[] texture = new int[1];
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + TEXTURE_UNIT);
        GLES20.glGenTextures(1, texture, 0);
        if (texture[0] != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
        } else {
            throw new RuntimeException("Error loading texture.");
        }
        GLES20.glDisable(GLES20.GL_TEXTURE_2D);
        GLES20.glActiveTexture(0);
        return texture;
    }

    public static void loadTextures(int _texture, int TEXTURE_UNIT, Bitmap bmp, boolean recycle) {
        if (bmp != null) {
            GLES20.glEnable(GLES20.GL_TEXTURE_2D);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + TEXTURE_UNIT);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _texture);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
            GLES20.glDisable(GLES20.GL_TEXTURE_2D);
            GLES20.glActiveTexture(0);
            if (recycle) {
                bmp.recycle();
            }
            return;
        }
    }

    public static FloatBuffer genFloatBuffer(@NonNull float[] datas) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(datas.length * Float.SIZE / 8)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(datas);
        buffer.position(0);
        return buffer;
    }
}
