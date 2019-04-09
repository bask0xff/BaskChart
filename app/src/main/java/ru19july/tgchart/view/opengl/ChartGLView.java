package ru19july.tgchart.view.opengl;

import android.content.Context;
import android.graphics.Canvas;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import javax.microedition.khronos.opengles.GL10;

import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.interfaces.IChartTheme;
import ru19july.tgchart.interfaces.IChartView;
import ru19july.tgchart.view.ChartEngine;

public class ChartGLView extends GLSurfaceView implements IChartView, View.OnTouchListener  {
    private ChartGLRenderer mRenderer;

    private ChartEngine chartEngine = new ChartEngine();

    public ChartGLView(Context context) {
        super(context);
        init();
    }

    public ChartGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOnTouchListener(this);
    }

    @Override
    public void setData(ChartData mChartData) {
        mRenderer.setData(mChartData);
    }

    @Override
    public void updateSlideFrameWindow(int xStart, int xEnd) {
        mRenderer.slideFrame(xStart, xEnd);
    }

    @Override
    public void showChart(int position, float v, float v1) {

    }

    @Override
    public void animateChanges(ChartData oldChartData, ChartData mChartData) {

    }

    @Override
    public void setTheme(IChartTheme mTheme) {

    }

    @Override
    public void setRenderer(ChartGLRenderer renderer) {
        super.setRenderer(renderer);
        mRenderer = renderer;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mRenderer.onTouchEvent(event);
    }

}
