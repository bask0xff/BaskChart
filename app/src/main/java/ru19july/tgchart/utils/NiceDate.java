package ru19july.tgchart.utils;

import java.util.List;

import ru19july.tgchart.data.Series;

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

    /**
     * Instantiates a new instance of the NiceDate class.
     *
     * @param min the minimum data point on the axis
     * @param max the maximum data point on the axis
     */

    public NiceDate(double min, double max)
    {
        this.minPoint = min;
        this.maxPoint = max;
        calculate();
    }

    public NiceDate(List<Series> series) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;

        for(int i=1; i<series.size(); i++) {
            if(!series.get(i).isChecked()) continue;
            //Log.d(TAG, "NiceDate (" + i + "): " + series.get(i).toString());
            if (series.get(i).getMinValue() < min) min = series.get(i).getMinValue();
            if (series.get(i).getMaxValue() > max) max = series.get(i).getMaxValue();
        }

        //Log.d(TAG, "NiceDate: " + min + " / " + max);
        this.minPoint = min;
        this.maxPoint = max;

        calculate();
    }

    /**
     * Calculate and update values for tick spacing and nice
     * minimum and maximum data points on the axis.
     */

    private void calculate()
    {
        this.range = niceNum(maxPoint - minPoint, false);
        this.tickSpacing = niceNum(range / (maxTicks - 1), true);
        this.niceMin =
                Math.floor(minPoint / tickSpacing) * tickSpacing;
        this.niceMax =
                Math.ceil(maxPoint / tickSpacing) * tickSpacing;
    }

    /**
     * Returns a "nice" number approximately equal to range Rounds
     * the number if round = true Takes the ceiling if round = false.
     *
     * @param range the data range
     * @param round whether to round the result
     * @return a "nice" number to be used for the data range
     */

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
            else
                niceFraction = 14;
        }
        else
        {
            if (fraction <= 1)
                niceFraction = 1;
            else if (fraction <= 2)
                niceFraction = 2;
            else if (fraction <= 7)
                niceFraction = 7;
            else
                niceFraction = 14;
        }

        return niceFraction * Math.pow(10, exponent);
    }

    /**
     * Sets the minimum and maximum data points for the axis.
     *
     * @param minPoint the minimum data point on the axis
     * @param maxPoint the maximum data point on the axis
     */

    public void setMinMaxPoints(double minPoint, double maxPoint)
    {
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
        calculate();
    }

    /**
     * Sets maximum number of tick marks we're comfortable with
     *
     * @param maxTicks the maximum number of tick marks for the axis
     */

    public void setMaxTicks(double maxTicks)
    {
        this.maxTicks = maxTicks;
        calculate();
    }
}