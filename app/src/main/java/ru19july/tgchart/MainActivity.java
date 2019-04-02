package ru19july.tgchart;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.data.Series;
import ru19july.tgchart.interfaces.IChartTheme;
import ru19july.tgchart.view.BaskChartView;
import ru19july.tgchart.view.canvas.ChartCanvasView;
import ru19july.tgchart.view.opengl.ChartGLView;
import ru19july.tgchart.view.theme.DarkTheme;
import ru19july.tgchart.view.theme.LightTheme;

public class MainActivity extends Activity {
    private String TAG = MainActivity.class.getSimpleName();
    private boolean nightTheme = true;
    public static List<ChartData> chartsData = new ArrayList<>();
    private List<BaskChartView> baskChartViews = new ArrayList<>();
    //private ChartsAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int[] rcids = new int[]{1561, 1557, 1528, 1513, 1502, 1471, 1455, 1451, 1443, 1411, 1402, 1397, 1386, 1378, 1374, 1372, 1351, 1346, 1345, 1338, 1329, 1328, 1318, 1306, 1300, 1255, 1247, 1151, 1122, 1085, 1071, 1059, 1017, 1013, 1005, 999, 945, 942, 899, 896, 882, 871, 858, 849, 838, 821, 801, 800, 792, 768, 761, 712, 704, 683, 679, 677, 652, 608, 540, 505, 470, 360, 341, 262, 252, 214, 212, 165, 155, 135, 122, 120, 115, 113, 2421, 2386, 2373, 2370, 2366, 2355, 2351, 2336, 2287, 2278, 2246, 2163, 2151, 2143, 2098, 2094, 2004, 1911, 1910, 1899, 1897, 1876, 1865, 1810, 1776, 1771, 1768, 1738, 1709, 1687, 1686, 1676, 1670, 1669, 1657, 1607, 1606};

        String json = loadJSONFromAsset();
        Log.d(TAG, "JSON: " + json);

        chartsData = new ArrayList<>();
        chartsData.addAll(readJson(json));

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        BaskChartView baskChartView = findViewById(R.id.baskChartView1);
        baskChartView.setData(chartsData.get(0));
        baskChartView.invalidate();


        initChartView();

//        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecyclerViewAdapter(baskChartViews);
        recyclerView.setAdapter(mAdapter);
    }

    private void initChartView() {
        int charts = chartsData.size();
        //charts = 1;

        for (int i = 0; i < charts * 10; i++) {

            Class<?> chartClass = i % 2 == 0 ? ChartCanvasView.class : ChartGLView.class;
            IChartTheme theme = i % 2 == 0 ? new DarkTheme() : new LightTheme();

            Log.d(TAG, "initChartView: ------------------ CHART #" + i + " => " + theme.getClass().getSimpleName());
            final BaskChartView baskChartView = new BaskChartView(this, chartClass);
            baskChartView.setChartTheme(theme);
            baskChartView.setData(chartsData.get(i % charts));

            baskChartViews.add(baskChartView);
        }
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
                        series.get(j).setTitle("Chart #" + i + ", Serie #" + j + " " + namesObj.getString(seriesName));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_theme:
                toggleTheme();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleTheme() {
        nightTheme = !nightTheme;
        Log.d(TAG, "toggleTheme: " + nightTheme + " => " + chartsData.size());

        IChartTheme theme = nightTheme ? new DarkTheme() : new LightTheme();

        for(int i=0; i< baskChartViews.size(); i++) {
            Log.d(TAG, "toggleTheme, baskChartViews[" + i + "]: " + theme.getClass().getSimpleName());
            updateChart(baskChartViews.get(i), theme, i);
        }

    }

    //https://stackoverflow.com/questions/3724874/how-can-i-update-a-single-row-in-a-listview
    private void updateChart(BaskChartView baskChartView, IChartTheme theme, int position) {
        baskChartView.setChartTheme(theme);
        //adapter.updateTheme(position, theme);
//        adapter.notifyDataSetChanged();

    }
}
