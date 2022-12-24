package ru19july.baskchart.view.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ru19july.baskchart.data.ChartData;
import ru19july.baskchart.interfaces.IChartTheme;
import ru19july.baskchart.interfaces.IChartView;
import ru19july.baskchart.view.ChartEngine;

/*
 Created by Sergey V. Baskov in 2019
 */

public class ChartGLRenderer implements IChartView, GLSurfaceView.Renderer, View.OnTouchListener {
    private static final String TAG = ChartGLRenderer.class.getSimpleName();
    private final View view;

    private ChartEngine chartEngine;

    public ChartGLRenderer(View chartView, Context context) {
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