package ru19july.tgchart;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.data.Series;
import ru19july.tgchart.view.BaskChartView;


public class TestChartsActivity extends Activity {

    private static final String TAG = TestChartsActivity.class.getSimpleName();
    private List<ChartData> chartsData;
    private BaskChartView baskChartView;
    private BaskChartView baskChartView2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_opengl_activity);

        String json = loadJSONFromAsset();
        Log.d(TAG, "JSON: " + json);

        chartsData = new ArrayList<>();
        chartsData.addAll(readJson(json));

        //BaskChartView bcv = new BaskChartView(this, ChartGLView.class);
/*
        baskChartView = findViewById(R.id.baskChartView1);
        baskChartView.setData(chartsData.get(0));

        baskChartView2 = findViewById(R.id.baskChartView2);
        baskChartView2.setData(chartsData.get(1));*/
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

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("chart_data.json");
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



}