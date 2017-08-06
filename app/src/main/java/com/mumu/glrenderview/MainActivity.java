package com.mumu.glrenderview;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;
    private GLRenderer mRenderer;
    private boolean zoom = false;
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(2); // Pick an OpenGL ES 2.0 context.
        //mGLSurfaceView.setZOrderOnTop(true);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mRenderer = new GLRenderer(this);
        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(mGLSurfaceView);
        mGLSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (zoom) {
                    mRenderer.zoomIn(mGLSurfaceView, index);
                    //index = (index + 1) % 5;
                } else {
                    mRenderer.zoomOut(mGLSurfaceView);
                }
                zoom = !zoom;
            }
        });
        mGLSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        float x = motionEvent.getX();
                        float y = motionEvent.getY();
                        index = mRenderer.showTouchedView(x, y);
                        mGLSurfaceView.performClick();
                        mGLSurfaceView.requestRender();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }
}
