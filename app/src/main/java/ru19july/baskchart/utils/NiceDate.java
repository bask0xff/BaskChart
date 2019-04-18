package ru19july.baskchart.utils;

import java.util.List;

import ru19july.baskchart.data.Series;

public class NiceDate
{

    private static final String TAG = NiceDate.class.getSimpleName();
    public double minPoint;
    public double maxPoint;
    private double maxTicks = 10;
    public double tickSpacing;
    public double range;
    public double niceMin;
    public double niceMax;

    public NiceDate(double min, double max)
    {
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
            else if (fraction < 8)
                niceFraction = 7;
            else if (fraction < 11)
                niceFraction = 10;
            else if (fraction < 13)
                niceFraction = 12;
            else if (fraction < 15)
                niceFraction = 14;
            else if (fraction < 22)
                niceFraction = 21;
            else
                niceFraction = 30;
        }
        else
        {
            if (fraction <= 1)
                niceFraction = 1;
            else if (fraction <= 2)
                niceFraction = 2;
            else if (fraction <= 7)
                niceFraction = 7;
            else if (fraction <= 10)
                niceFraction = 10;
            else if (fraction <= 12)
                niceFraction = 12;
            else if (fraction <= 14)
                niceFraction = 14;
            else if (fraction <= 21)
                niceFraction = 21;
            else
                niceFraction = 30;
        }

        return niceFraction * Math.pow(10, exponent);
    }

    public void setMinMaxPoints(double minPoint, double maxPoint)
    {
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
        calculate();
    }

    public void setMaxTicks(double maxTicks)
    {
        this.maxTicks = maxTicks;
        calculate();
    }
}