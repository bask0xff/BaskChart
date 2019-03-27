package ru19july.tgchart.interfaces;

import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.view.opengl.ChartGLRenderer;

public interface IChartView {
    void setData(ChartData mChartData);
    void updateSlideFrameWindow(int xStart, int xEnd);

    void showChart(int position, float v, float v1);

    void animateChanges(ChartData oldChartData, ChartData mChartData);

    void invalidate();

    void setTheme(IChartTheme mTheme);

    void setRenderer(ChartGLRenderer mRenderer);
}