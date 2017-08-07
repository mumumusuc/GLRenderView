package com.mumu.glrenderview;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private GLSurfaceView mGLSurfaceView;
    private GLRenderer mRenderer;
    private boolean zoom = false;
    private int index = -1;
    private View mView;
    private boolean first = true;
    private float x, y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mView = findViewById(R.id.test);
        mView.setOnClickListener(this);
        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.glsurfaceview);
        mGLSurfaceView.setEGLContextClientVersion(2); // Pick an OpenGL ES 2.0 context.
        //mGLSurfaceView.setZOrderOnTop(true);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mRenderer = new GLRenderer(this);
        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGLSurfaceView.setOnClickListener(this);
        mGLSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        x = motionEvent.getX();
                        y = motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float _x = motionEvent.getX();
                        float _y = motionEvent.getY();
                        if (Math.hypot(x - _x, y - _y) > 10) {
                            index = mRenderer.showTouchedView(_x, _y);
                            mGLSurfaceView.requestRender();
                        }
                        x = _x;
                        y = _y;
                        break;
                    case MotionEvent.ACTION_UP:
                        _x = motionEvent.getX();
                        _y = motionEvent.getY();
                        index = mRenderer.showTouchedView(_x, _y);
                        //mGLSurfaceView.requestRender();
                        mGLSurfaceView.performClick();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public void onClick(View view) {
        if (zoom) {
            mRenderer.zoomIn(mGLSurfaceView, index, mCallback);
        } else {
            mView.setVisibility(View.INVISIBLE);
            mGLSurfaceView.setVisibility(View.VISIBLE);
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 5; i++) {
                        if (i == 2)
                            mRenderer.bindView(2, mView);
                        else
                            mRenderer.bindView(i, mView.getLeft(), mView.getTop(), mView.getWidth(), mView.getHeight(), String.valueOf(i));
                    }
                }
            });
            mRenderer.zoomOut(mGLSurfaceView);
        }
        zoom = !zoom;
    }

    private GLRenderer.Callback mCallback = new GLRenderer.Callback() {
        @Override
        public void onAnimEnd() {
            if (index == 2) {
                mView.setVisibility(View.VISIBLE);
             //   mGLSurfaceView.setVisibility(View.INVISIBLE);
            }
        }
    };
}
