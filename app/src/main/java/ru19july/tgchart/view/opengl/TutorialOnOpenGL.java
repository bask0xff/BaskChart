package ru19july.tgchart.view.opengl;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

public class TutorialOnOpenGL extends Activity {

    private GLSurfaceView mView;
    private MyRenderer mRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = new GLSurfaceView(this);
        mRenderer = new MyRenderer(this);
        mView.setRenderer(mRenderer);
        setContentView(mView);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return mRenderer.onTouchEvent(event);
    }
}