package ru19july.baskchart.view.theme;

import ru19july.baskchart.interfaces.IChartTheme;

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
        return "#bb000000";
    }

    @Override
    public String sliderBorder() {
        return "#11000000";
    }

    @Override
    public String sliderInner() {
        return "#88000000";
    }
}