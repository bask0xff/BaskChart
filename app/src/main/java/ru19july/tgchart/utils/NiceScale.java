package ru19july.tgchart.utils;

import java.util.List;

import ru19july.tgchart.data.Series;

public class NiceScale
{

    private static final String TAG = NiceScale.class.getSimpleName();
    public double minPoint;
    public double maxPoint;
    private double maxTicks = 10;
    public double tickSpacing;
    public double range;
    public double niceMin;
    public double niceMax;

    public NiceScale(double min, double max)
    {
        this.minPoint = min;
        this.maxPoint = max;
        calculate();
    }

    public NiceScale(List<Series> series) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;

        for(int i=1; i<series.size(); i++) {
            if(!series.get(i).isChecked()) continue;
            if (series.get(i).getMinValue() < min) min = series.get(i).getMinValue();
            if (series.get(i).getMaxValue() > max) max = series.get(i).getMaxValue();
        }

        this.minPoint = min;
        this.maxPoint = max;

        calculate();
    }

    private void calculate()
    {
        this.range = niceNum(maxPoint - minPoint, false);
        this.tickSpacing = niceNum(range / (maxTicks - 1), true);
        this.niceMin =
                Math.floor(minPoint / tickSpacing) * tickSpacing;
        this.niceMax =
                Math.ceil(maxPoint / tickSpacing) * tickSpacing;
    }

    private double niceNum(double range, boolean round)
    {
        double exponent; /** exponent of range */
        double fraction; /** fractional part of range */
        double niceFraction; /** nice, rounded fraction */

        exponent = Math.floor(Math.log10(range));
        fraction = range / Math.pow(10, exponent);

        if (round)
        {
            if (fraction < 1.5)
                niceFraction = 1;
            else if (fraction < 3)
                niceFraction = 2;
            else if (fraction < 7)
                niceFraction = 5;
            else
                niceFraction = 10;
        }
        else
        {
            if (fraction <= 1)
                niceFraction = 1;
            else if (fraction <= 2)
                niceFraction = 2;
            else if (fraction <= 5)
                niceFraction = 5;
            else
                niceFraction = 10;
        }

        return niceFraction * Math.pow(10, exponent);
    }

}