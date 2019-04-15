package ru19july.tgchart.view.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.interfaces.IChartTheme;
import ru19july.tgchart.interfaces.IChartView;
import ru19july.tgchart.view.ChartEngine;

public class ChartGLRenderer implements IChartView, GLSurfaceView.Renderer, View.OnTouchListener  {
    private static final String TAG = ChartGLRenderer.class.getSimpleName();
    private final View view;
    private Context mContext;
    private FloatBuffer mVertexBuffer = null;
    private ShortBuffer mTriangleBorderIndicesBuffer = null;
    private int mNumOfTriangleBorderIndices = 0;

    public float mAngleX = 0.0f;
    private float mPreviousX;
    private float mPreviousY;
    private final float TOUCH_SCALE_FACTOR = 0.6f;

    private int ticks = 0;

    private ChartData mChartData;
    private ChartEngine chartEngine;

    private int Width;
    private int Height;

    public ChartGLRenderer(View chartView, Context context) {
        mContext = context;
        chartEngine = new ChartEngine(context, chartView);
        this.view = chartView;
    }

    public void onDrawFrame(GL10 gl) {
        chartEngine.DrawChart(gl);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        chartEngine.onSurfaceCreated(gl, config);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        chartEngine.onSurfaceChanged(gl, width, height);
    }

    public boolean onTouchEvent(MotionEvent e) {
        this.view.getParent().requestDisallowInterceptTouchEvent(true);
        return chartEngine.onTouchEvent(e);
    }

    public void setData(ChartData chartData) {
        chartEngine.setData(chartData);
    }

    @Override
    public void updateSlideFrameWindow(int startX, int endX) {
        chartEngine.updateSlideFrameWindow(startX, endX);
    }

    @Override
    public void showChart(int position, float v, float v1) {
        chartEngine.showChart(view, position, v, v1);
    }

    @Override
    public void animateChanges(ChartData oldChartData, ChartData newChartData) {
        chartEngine.animateChanges(view, oldChartData, newChartData);
    }

    @Override
    public void invalidate() {
    }

    @Override
    public void setTheme(IChartTheme mTheme) {
        chartEngine.setTheme(mTheme);
    }

    @Override
    public void setRenderer(ChartGLRenderer mRenderer) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            invalidate();
        }
        return false;
    }
}