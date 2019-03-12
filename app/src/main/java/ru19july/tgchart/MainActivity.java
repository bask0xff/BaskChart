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
import java.util.List;

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

        //chartView = findViewById(R.id.chartView);

        String json = loadJSONFromAsset();
        Log.d(TAG, "JSON: " + json);

        final List<ChartData> charts = readJson(json);

        chartControlsView = findViewById(R.id.chartControlsView);
        //chartControlsView.setData(charts.get(0));

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

    private List<ChartData> readJson(String json) {
        List<ChartData> charts = new ArrayList<>();// parseJson(json);

        try {
            JSONArray jsonColumns = new JSONArray(json);
            for (int i = 0; i < jsonColumns.length(); i++) {
                JSONObject jsonColumn = jsonColumns.getJSONObject(i);
                //Log.d(TAG, "onCreate: jsonColumn(" + i + "): " + jsonColumn);

                ChartData chartData = new ChartData();
                chartData.series = new ArrayList<>();

                JSONArray columnsArray = jsonColumn.getJSONArray("columns");
                boolean columnsLengthEquals = true;
                int columnsLength = 0;
                for (int j = 0; j < columnsArray.length(); j++) {
                    JSONArray arrColumns = columnsArray.getJSONArray(j);
                    //Log.d(TAG, "\t\tarrColumns(" + j + ":" + arrColumns.length() + "): " + arrColumns);

                    Series ser = new Series();
                    ser.name = arrColumns.getString(0);
                    ser.values = new ArrayList<>();
                    for (int k = 1; k < arrColumns.length(); k++) {
                        ser.values.add(arrColumns.getLong(k));
                    }
                    if (columnsLength == 0)
                        columnsLength = ser.values.size();
                    columnsLengthEquals = columnsLengthEquals && (ser.values.size() == columnsLength);

                    //Log.d(TAG, "\t\t: " + ser.name + " (" + ser.values.size() + ") " + columnsLengthEquals + " => " + ser.values);

                    if (!columnsLengthEquals) {
                        Log.e(TAG, "JSON error! columns are different size!");
                    }
                    chartData.series.add(ser);
                    chartData.isColumnsSizeEquals = columnsLengthEquals;

                }

                JSONObject typesObj = jsonColumn.getJSONObject("types");
                //Log.d(TAG, "\ttypesObj: " + typesObj);

                JSONObject namesObj = jsonColumn.getJSONObject("names");
                //Log.d(TAG, "\tnamesObj: " + namesObj);

                JSONObject colorsObj = jsonColumn.getJSONObject("colors");
                //Log.d(TAG, "\tcolorsObj: " + colorsObj);

                Log.d(TAG, "---- chartData.isColumnsSizeEquals: " + chartData.isColumnsSizeEquals);
                for(int j=0; j<chartData.series.size(); j++){
                    String seriesName = chartData.series.get(j).name;
                    if(namesObj.has(seriesName))
                        chartData.series.get(j).title = namesObj.getString(seriesName);
                    if(colorsObj.has(seriesName))
                        chartData.series.get(j).color = colorsObj.getString(seriesName);
                    if(typesObj.has(seriesName))
                        chartData.series.get(j).type = typesObj.getString(seriesName);

                    Log.d(TAG, "\t\t:  chartData.series(" + j + ") => " + chartData.series.get(j).toString());
                }

                charts.add(chartData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return charts;
    }

    private ChartData parseJson(String json) {
        Log.d(TAG, "json(" + json.length() + "): " + json + "");
        int arrays = 0;
        int structs = 0;
        for(int i=0; i<json.length(); i++) {
            String s = json.substring(i, i + 1);
            //Log.d(TAG, "s: " + s);
            if (s.equals("[")) {
                arrays++;

            }
            if (s.equals("]")) {
                Log.d(TAG, "parseJson: ARRAYS: " + arrays);
                arrays--;
            }
            if (s.equals("{")) {
                structs++;

            }
            if (s.equals("}")) {
                Log.d(TAG, "parseJson: STRUCTS: " + structs);
                structs--;
            }
                //Gson gson = new Gson();
            //Reader reader = new InputStreamReader(ims);

            //gsonObj = gson.fromJson(json, ChartData.class);
            //Log.d(TAG, "parseJson: " + gsonObj);
        }




        return null;
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
