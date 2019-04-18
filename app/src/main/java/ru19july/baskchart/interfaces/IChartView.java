package ru19july.baskchart.interfaces;

import ru19july.baskchart.data.ChartData;
import ru19july.baskchart.view.opengl.ChartGLRenderer;

public interface IChartView {
    void setData(ChartData mChartData);
    void updateSlideFrameWindow(int xStart, int xEnd);

    void showChart(int position, float v, float v1);

    void animateChanges(ChartData oldChartData, ChartData mChartData);

    void invalidate();

    void setTheme(IChartTheme mTheme);

    void setRenderer(ChartGLRenderer mRenderer);
}
