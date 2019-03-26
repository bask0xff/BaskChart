package ru19july.tgchart.view.opengl;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import ru19july.tgchart.R;

public class TutorialOnOpenGL extends Activity {

    private GLSurfaceView mView;
    private MyRenderer mRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_opengl_activity);

        //mView = new GLSurfaceView(this);
        mView = findViewById(R.id.gl_surface_view);
        mRenderer = new MyRenderer(this);
        mView.setRenderer(mRenderer);

        //layout.addView(mView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 300));
        //setContentView(mView);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return mRenderer.onTouchEvent(event);
    }
}