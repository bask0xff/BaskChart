package ru19july.baskchart.view.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import ru19july.baskchart.data.ChartData;
import ru19july.baskchart.interfaces.IChartTheme;
import ru19july.baskchart.interfaces.IChartView;
import ru19july.baskchart.view.ChartEngine;

public class ChartGLView extends GLSurfaceView implements IChartView, View.OnTouchListener  {
    private static final String TAG = ChartGLView.class.getSimpleName();
    private ChartGLRenderer mRenderer;

    private ChartEngine chartEngine;

    public ChartGLView(Context context) {
        super(context);
        init();
    }

    public ChartGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        chartEngine = new ChartEngine(getContext(), this);
        setOnTouchListener(this);
    }

    @Override
    public void setData(ChartData mChartData) {
        mRenderer.setData(mChartData);
    }

    @Override
    public void updateSlideFrameWindow(int xStart, int xEnd) {
        mRenderer.updateSlideFrameWindow(xStart, xEnd);
    }

    @Override
    public void showChart(int position, float v, float v1) {
        mRenderer.showChart(position, v, v1);
    }

    @Override
    public void animateChanges(ChartData oldChartData, ChartData newChartData) {
        mRenderer.animateChanges(oldChartData, newChartData);
    }

    @Override
    public void setTheme(IChartTheme mTheme) {
        mRenderer.setTheme(mTheme);
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
