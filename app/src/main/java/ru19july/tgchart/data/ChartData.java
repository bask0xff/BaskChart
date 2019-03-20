package ru19july.tgchart.data;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ru19july.tgchart.utils.NiceScale;

public class ChartData implements Serializable {

    private static final String TAG = ChartData.class.getSimpleName();
    private List<Series> mSeries;
    private double minQuote;
    private double maxQuote;

    public boolean isColumnsSizeEquals;

    public ChartData(){
    }
    /*
    *  double minQuote = Double.MAX_VALUE;
        double maxQuote = Double.MIN_VALUE;

        NiceScale numScale = new NiceScale(series);
        minQuote = numScale.niceMin;
        maxQuote = numScale.niceMax;
    * */

    public List<Series> getSeries(){
        return mSeries;
    }

    public void setSeries(List<Series> series){
        mSeries = series;
    }

    public void copyFrom(ChartData mChartData) {
        mSeries = new ArrayList<>();
        for (int i = 0; i < mChartData.mSeries.size(); i++) {
            mSeries.add(new Series(
                    mChartData.mSeries.get(i).getName(),
                    mChartData.mSeries.get(i).getTitle(),
                    mChartData.mSeries.get(i).getType(),
                    mChartData.mSeries.get(i).getColor(),
                    mChartData.mSeries.get(i).getValues()
            ));

            mSeries.get(i).setTitle(mChartData.mSeries.get(i).getTitle());
            mSeries.get(i).setChecked(mChartData.mSeries.get(i).isChecked());
        }

    }

    public double getMinQuote() {
        return minQuote;
    }

    public double getMaxQuote() {
        return maxQuote;
    }

    public NiceScale getNiceScale() {
        minQuote = Double.MAX_VALUE;
        maxQuote = Double.MIN_VALUE;

        MinMax minmax = new MinMax();
        minmax.min = Float.MAX_VALUE;
        minmax.max = Float.MIN_VALUE;

        for(int i=1; i<getSeries().size(); i++) {
            if(!getSeries().get(i).isChecked()) continue;
            if (getSeries().get(i).getMinValue() < minmax.min) minmax.min = getSeries().get(i).getMinValue();
            if (getSeries().get(i).getMaxValue() > minmax.max) minmax.max = getSeries().get(i).getMaxValue();
        }

        NiceScale numScale = new NiceScale(minmax.min, minmax.max);
        minQuote = numScale.niceMin;
        maxQuote = numScale.niceMax;

        return new NiceScale(minQuote, maxQuote);
    }

    public NiceScale getNiceScale(float leftMinValue, float rightMaxValue) {
        minQuote = Double.MAX_VALUE;
        maxQuote = Double.MIN_VALUE;

        MinMax minmax = new MinMax();
        minmax.min = Float.MAX_VALUE;
        minmax.max = Float.MIN_VALUE;

        Log.d(TAG, "getNiceScale: " + leftMinValue + "/" + rightMaxValue);

        for(int i=1; i<getSeries().size(); i++) {
            if(!getSeries().get(i).isChecked()) continue;

            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;

            for(int j = 0; j<getSeries().get(i).getValues().size(); j++){
                if(getSeries().get(0).getValues().get(j) < leftMinValue || getSeries().get(0).getValues().get(j) > rightMaxValue) continue;
                if(getSeries().get(i).getValues().get(j) < min) min =getSeries().get(i).getValues().get(j);
                if(getSeries().get(i).getValues().get(j) > max) max =getSeries().get(i).getValues().get(j);
            }

            if (min < minmax.min) minmax.min = min;
            if (max > minmax.max) minmax.max = max;
        }

        NiceScale numScale = new NiceScale(minmax.min, minmax.max);
        minQuote = numScale.niceMin;
        maxQuote = numScale.niceMax;

        return new NiceScale(minQuote, maxQuote);
    }
}
