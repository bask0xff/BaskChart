package ru19july.tgchart.view.theme;

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
        return "#F5F8F9";
    }

    @Override
    public String sliderBorder() {
        return "#DBE7F0";
    }

    @Override
    public String sliderInner() {
        return "#FFFFFF";
    }
}