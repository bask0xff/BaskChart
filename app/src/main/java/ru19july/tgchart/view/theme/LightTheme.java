package ru19july.tgchart.view.theme;

import ru19july.tgchart.interfaces.IChartTheme;

public class LightTheme implements IChartTheme {
    @Override
    public String fontColor() { return "#96A2AA"; }

    @Override
    public String backgroundColor() { return "#FFFFFF"; }

    @Override
    public String linesColor() {
        return "#E5EBEF";
    }

    @Override
    public String legendBackgroundColor() {
        return "#FFFFFF";
    }

    @Override
    public String markerFontColor() {
        return "#222222";
    }

    @Override
    public String sliderBackground() {
        return "#77777777";
    }

    @Override
    public String sliderBorder() {
        return "#aa777777";
    }

    @Override
    public String sliderInner() {
        return "#00ffffff";
    }
}