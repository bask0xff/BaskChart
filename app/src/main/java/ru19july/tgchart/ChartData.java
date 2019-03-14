package ru19july.tgchart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChartData implements Serializable {
    public List<Series> series;
    public boolean isColumnsSizeEquals;

    public void copyFrom(ChartData mChartData) {
        series = new ArrayList<>();
        for (int i = 0; i < mChartData.series.size(); i++) {
            series.add(new Series(
                    mChartData.series.get(i).getName(),
                    mChartData.series.get(i).getTitle(),
                    mChartData.series.get(i).getType(),
                    mChartData.series.get(i).getColor(),
                    mChartData.series.get(i).getValues()
            ));

            series.get(i).setTitle(mChartData.series.get(i).getTitle());
            series.get(i).setChecked(mChartData.series.get(i).isChecked());
        }

    }
}
