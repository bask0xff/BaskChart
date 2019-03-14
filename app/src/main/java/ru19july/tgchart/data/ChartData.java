package ru19july.tgchart.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChartData implements Serializable {

    private List<Series> mSeries;
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
}
