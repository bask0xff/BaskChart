package ru19july.tgchart.view.theme;

public class DarkTheme implements IChartTheme {
    @Override
    public String fontColor() { return "#506372"; }

    @Override
    public String backgroundColor() {
        return "#1D2733";
    }

    @Override
    public String linesColor() {
        return "#131C26";
    }

    @Override
    public String legendBackgroundColor() {
        return "#202B38";
    }

    @Override
    public String markerFontColor() {
        return "#E5EFF5";
    }

    @Override
    public String sliderBackground() {
        return "#1119232E";
    }

    @Override
    public String sliderBorder() {
        return "#222B4256";
    }

    @Override
    public String sliderInner() {
        return "#1D2733";
    }
}