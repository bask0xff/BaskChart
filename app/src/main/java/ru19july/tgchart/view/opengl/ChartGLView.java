package ru19july.tgchart.view.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.view.IChartView;
import ru19july.tgchart.view.theme.IChartTheme;

public class ChartGLView extends GLSurfaceView implements IChartView {
    public ChartGLView(Context context) {
        super(context);
    }

    public ChartGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
    public void setRenderer(MyRenderer mRenderer) {
        super.setRenderer(mRenderer);
    }
}
