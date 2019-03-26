package ru19july.tgchart.view.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.view.IChartView;
import ru19july.tgchart.view.theme.IChartTheme;

public class ChartGLView extends GLSurfaceView implements IChartView, View.OnTouchListener  {
    private MyRenderer mRenderer;

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

    }

    @Override
    public void updateSlideFrameWindow(int xStart, int xEnd) {

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
    public void setRenderer(MyRenderer renderer) {
        super.setRenderer(renderer);
        mRenderer = renderer;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mRenderer.onTouchEvent(event);
    }
}
