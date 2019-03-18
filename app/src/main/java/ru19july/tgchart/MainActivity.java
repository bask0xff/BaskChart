package ru19july.tgchart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.data.Series;
import ru19july.tgchart.view.ChartControlsView;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private ChartData gsonObj;
    private ChartControlsView chartControlsView;
    private Spinner dropdown;
    //ChartView chartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chartControlsView = findViewById(R.id.chartControlsView);

        initChartView();
    }

    private void initChartView() {
        chartControlsView.setVisibility(View.VISIBLE);

        String json = loadJSONFromAsset();
        Log.d(TAG, "JSON: " + json);

        final List<ChartData> charts = new ArrayList<>();


        ChartData chartData = createTestChart();
        charts.add(chartData);

        charts.addAll(readJson(json));

        chartControlsView.setData(charts.get(0));

        dropdown = findViewById(R.id.spinner1);

        String[] items = new String[charts.size()];
        for (int i = 0; i < charts.size(); i++)
            items[i] = "Chart #" + (i + 1);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chartControlsView.setData(charts.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private ChartData createTestChart() {
        ChartData chartData = new ChartData();
        List<Series> series = new ArrayList<>();

        Series serie = new Series();
        serie.setName("x");
        serie.setTitle("x");
        serie.setType("x");
        serie.setChecked(true);
        Long[] arr = new Long[]{1542412800000L,1542499200000L,1542585600000L,1542672000000L,1542758400000L,1542844800000L,1542931200000L};
        List<Long> values = Arrays.asList(arr);
        serie.setValues(values);
        series.add(serie);

        serie = new Series();
        serie.setName("y1");
        serie.setTitle("y1");
        serie.setType("line");
        serie.setChecked(true);
        serie.setColor("#aa0000");
        arr = new Long[]{5173L,5145L,5209L,4967L,5030L,5120L,5030L};
        values = Arrays.asList(arr);
        serie.setValues(values);
        series.add(serie);

        serie = new Series();
        serie.setName("y2");
        serie.setTitle("y2");
        serie.setType("line");
        serie.setChecked(true);
        serie.setColor("#007700");
        arr = new Long[]{4497L,503L,520L,2530L,3173L,5514L,529L};
        values = Arrays.asList(arr);
        serie.setValues(values);
        series.add(serie);

        chartData.setSeries(series);
        chartData.isColumnsSizeEquals = true;

        return chartData;

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
                for(int j=0; j<series.size(); j++){
                    String seriesName = series.get(j).getName();
                    if(namesObj.has(seriesName))
                        series.get(j).setTitle(namesObj.getString(seriesName));
                    if(colorsObj.has(seriesName))
                        series.get(j).setColor(colorsObj.getString(seriesName));
                    if(typesObj.has(seriesName))
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
