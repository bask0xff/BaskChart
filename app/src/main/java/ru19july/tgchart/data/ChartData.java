package ru19july.tgchart.data;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ru19july.tgchart.utils.NiceScale;
import ru19july.tgchart.utils.Utils;

public class ChartData implements Serializable {

    public static enum CHART_TYPE {CHART_TYPE_LINE, CHART_TYPE_BAR, CHART_TYPE_FILLEDPOLY}

    private static final String TAG = ChartData.class.getSimpleName();

    private CHART_TYPE chartType;

    private List<Series> mSeries;
    private double minValue;
    private double maxValue;

    private String mFilepath;

    public boolean isColumnsSizeEquals;
    private Context mContext;

    public ChartData() {
    }

    public List<Series> getSeries() {
        return mSeries;
    }

    public void setSeries(List<Series> series) {
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

        for (int i = 1; i < getSeries().size(); i++) {
            if (!getSeries().get(i).isChecked()) continue;
            if (getSeries().get(i).getMinValue() < minmax.min)
                minmax.min = getSeries().get(i).getMinValue();
            if (getSeries().get(i).getMaxValue() > minmax.max)
                minmax.max = getSeries().get(i).getMaxValue();
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

        for (int i = 1; i < getSeries().size(); i++) {
            if (!getSeries().get(i).isChecked()) continue;

            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;

            for (int j = 0; j < getSeries().get(i).getValues().size(); j++) {
                if (getSeries().get(0).getValues().get(j) < leftMinValue || getSeries().get(0).getValues().get(j) > rightMaxValue)
                    continue;
                if (getSeries().get(i).getValues().get(j) < min)
                    min = getSeries().get(i).getValues().get(j);
                if (getSeries().get(i).getValues().get(j) > max)
                    max = getSeries().get(i).getValues().get(j);
            }

            if (min < minmax.min) minmax.min = min;
            if (max > minmax.max) minmax.max = max;
        }

        NiceScale numScale = new NiceScale(minmax.min, minmax.max);
        minValue = numScale.niceMin;
        maxValue = numScale.niceMax;

        return new NiceScale(minValue, maxValue);
    }

    public ChartData loadMonth(Context context, long selectedTimestamp) {
        String dateFolder = Utils.unixtimeToString(selectedTimestamp, "yyyy-MM");
        Log.d(TAG, "loadMonth: " + selectedTimestamp + " => " + mFilepath + " => " + dateFolder);

        for (int i = 0; i < 31; i++) {
            String dd = "00";
            if (i < 10) dd = "0" + i;
            else dd = "" + i;
            String filename = mFilepath + dateFolder + "/" + dd + ".json";
            try {
                Log.d(TAG, "loadMonth: " + filename);
                ChartData dayChart = loadData(context, filename);
            } catch (Exception e) {
            }
        }

        return null;
    }

    public ChartData loadData(Context context, String filename) {
        mContext = context;
        String json2 = loadJSONFromAsset(context, filename);
        ChartData cd = readJsonContest2(json2);
        return cd;
    }

    private List<ChartData> readJson(String json) {
        List<ChartData> charts = new ArrayList<>();// parseJson(json);

        try {
            JSONArray jsonColumns = new JSONArray(json);
            for (int i = 0; i < jsonColumns.length(); i++) {
                JSONObject jsonColumn = jsonColumns.getJSONObject(i);
                //Log.d(TAG, "onCreate: jsonColumn(" + i + "): " + jsonColumn);

                ChartData chartData = new ChartData();
                List<Series> series = new ArrayList<>();

                JSONArray columnsArray = jsonColumn.getJSONArray("columns");
                boolean columnsLengthEquals = true;
                int columnsLength = 0;
                for (int j = 0; j < columnsArray.length(); j++) {
                    JSONArray arrColumns = columnsArray.getJSONArray(j);
                    //Log.d(TAG, "\t\tarrColumns(" + j + ":" + arrColumns.length() + "): " + arrColumns);

                    Series ser = new Series();
                    ser.setName(arrColumns.getString(0));
                    List<Long> vals = new ArrayList<>();
                    for (int k = 1; k < arrColumns.length(); k++) {
                        vals.add(arrColumns.getLong(k));
                    }
                    ser.setValues(vals);

                    if (columnsLength == 0)
                        columnsLength = ser.getValues().size();
                    columnsLengthEquals = columnsLengthEquals && (ser.getValues().size() == columnsLength);

                    //Log.d(TAG, "\t\t: " + ser.name + " (" + ser.values.size() + ") " + columnsLengthEquals + " => " + ser.values);

                    if (!columnsLengthEquals) {
                        Log.e(TAG, "JSON error! columns are different size!");
                    }
                    series.add(ser);
                    chartData.isColumnsSizeEquals = columnsLengthEquals;
                }

                JSONObject typesObj = jsonColumn.getJSONObject("types");
                JSONObject namesObj = jsonColumn.getJSONObject("names");
                JSONObject colorsObj = jsonColumn.getJSONObject("colors");

                Log.d(TAG, "---- chartData.isColumnsSizeEquals: " + chartData.isColumnsSizeEquals);
                for (int j = 0; j < series.size(); j++) {
                    String seriesName = series.get(j).getName();
                    if (namesObj.has(seriesName))
                        series.get(j).setTitle(namesObj.getString(seriesName));
                    if (colorsObj.has(seriesName))
                        series.get(j).setColor(colorsObj.getString(seriesName));
                    if (typesObj.has(seriesName))
                        series.get(j).setType(typesObj.getString(seriesName));

                    Log.d(TAG, "\t\t:  chartData.series(" + j + ") => " + series.get(j).toString());
                }

                chartData.setSeries(series);

                charts.add(chartData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return charts;
    }

    private ChartData readJsonContest2(String json) {
        ChartData chartData = new ChartData();
        try {
            JSONObject jsonColumn = new JSONObject(json);
            Log.d(TAG, "readJsonContest2: " + json);

            List<Series> series = new ArrayList<>();

            JSONArray columnsArray = jsonColumn.getJSONArray("columns");
            boolean columnsLengthEquals = true;
            int columnsLength = 0;
            for (int j = 0; j < columnsArray.length(); j++) {
                JSONArray arrColumns = columnsArray.getJSONArray(j);
                Log.d(TAG, "\t\tarrColumns(" + j + ":" + arrColumns.length() + "): " + arrColumns);

                Series ser = new Series();
                ser.setName(arrColumns.getString(0));
                List<Long> vals = new ArrayList<>();
                for (int k = 1; k < arrColumns.length(); k++) {
                    vals.add(arrColumns.getLong(k));
                }
                ser.setValues(vals);

                if (columnsLength == 0)
                    columnsLength = ser.getValues().size();
                columnsLengthEquals = columnsLengthEquals && (ser.getValues().size() == columnsLength);

                //Log.d(TAG, "\t\t: " + ser.name + " (" + ser.values.size() + ") " + columnsLengthEquals + " => " + ser.values);

                if (!columnsLengthEquals) {
                    Log.e(TAG, "JSON error! columns are different size!");
                }
                series.add(ser);
                chartData.isColumnsSizeEquals = columnsLengthEquals;
            }

            JSONObject typesObj = jsonColumn.getJSONObject("types");
            JSONObject namesObj = jsonColumn.getJSONObject("names");
            JSONObject colorsObj = jsonColumn.getJSONObject("colors");

            Log.d(TAG, "---- chartData.isColumnsSizeEquals: " + chartData.isColumnsSizeEquals + "; series:" + series.size());
            for (int j = 0; j < series.size(); j++) {
                String seriesName = series.get(j).getName();
                if (namesObj.has(seriesName))
                    series.get(j).setTitle(namesObj.getString(seriesName));
                if (colorsObj.has(seriesName))
                    series.get(j).setColor(colorsObj.getString(seriesName));
                if (typesObj.has(seriesName))
                    series.get(j).setType(typesObj.getString(seriesName));

                Log.d(TAG, "\t\t:  chartData.series(" + j + ") => " + series.get(j).toString());
            }

            chartData.setSeries(series);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return chartData;
    }

    public String loadJSONFromAsset(Context context, String filename) {
        String json = null;
        Log.d(TAG, "loadJSONFromAsset: " + filename);
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public String getFilepath() {
        return mFilepath;
    }

    public void setFilepath(String mFilepath) {
        this.mFilepath = mFilepath;
    }

    public void setType(CHART_TYPE chartType) {
        this.chartType = chartType;
    }

    public CHART_TYPE getChartType() {
        return chartType;
    }

    public void recalc() {
        for(int j = 2; j<mSeries.size(); j++){
            for(int i=0; i<mSeries.get(0).getValues().size(); i++){
                long newValue = 0;

                for(int k = 1; k<j; k++)
                    newValue += mSeries.get(k).getValues().get(i);

                mSeries.get(j).getValues().set(i, newValue);
            }
        }
    }



}