package ru19july.tgchart.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ru19july.tgchart.utils.NiceScale;

public class ChartData implements Serializable {

    private static final String TAG = ChartData.class.getSimpleName();
    private List<Series> mSeries;
    private double minValue;
    private double maxValue;

    public boolean isColumnsSizeEquals;

    public ChartData(){
    }

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

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public NiceScale getNiceScale() {
        minValue = Double.MAX_VALUE;
        maxValue = Double.MIN_VALUE;

        MinMax minmax = new MinMax();
        minmax.min = Float.MAX_VALUE;
        minmax.max = Float.MIN_VALUE;

        for(int i=1; i<getSeries().size(); i++) {
            if(!getSeries().get(i).isChecked()) continue;
            if (getSeries().get(i).getMinValue() < minmax.min) minmax.min = getSeries().get(i).getMinValue();
            if (getSeries().get(i).getMaxValue() > minmax.max) minmax.max = getSeries().get(i).getMaxValue();
        }

        NiceScale numScale = new NiceScale(minmax.min, minmax.max);
        minValue = numScale.niceMin;
        maxValue = numScale.niceMax;

        return new NiceScale(minValue, maxValue);
    }

    public NiceScale getNiceScale(float leftMinValue, float rightMaxValue) {
        minValue = Double.MAX_VALUE;
        maxValue = Double.MIN_VALUE;

        MinMax minmax = new MinMax();
        minmax.min = Float.MAX_VALUE;
        minmax.max = Float.MIN_VALUE;

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
        minValue = numScale.niceMin;
        maxValue = numScale.niceMax;

        return new NiceScale(minValue, maxValue);
    }

}
