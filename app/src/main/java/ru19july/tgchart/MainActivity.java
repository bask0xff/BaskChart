package ru19july.tgchart;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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
import ru19july.tgchart.interfaces.IOnThemeChange;
import ru19july.tgchart.view.canvas.ChartCanvasView;
import ru19july.tgchart.view.opengl.ChartGLView;
import ru19july.tgchart.view.theme.DarkTheme;
import ru19july.tgchart.view.theme.LightTheme;

public class MainActivity extends ListActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private boolean nightTheme = true;
    private List<ChartData> chartsData = new ArrayList<>();
    private List<BaskChartView> baskChartViews = new ArrayList<>();
    private ChartsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initChartView();
    }

    private void initChartView() {

        String json = loadJSONFromAsset();
        Log.d(TAG, "JSON: " + json);

        chartsData = new ArrayList<>();
        chartsData.addAll(readJson(json));

        for (int i = 0; i < chartsData.size(); i++) {

            //Class<?> chartClass = ChartGLView.class;
            Class<?> chartClass = ChartCanvasView.class;

            final BaskChartView baskChartView = new BaskChartView(this, ChartGLView.class);
            baskChartView.setChartTheme(new LightTheme());
            baskChartView.setData(chartsData.get(i));
            /*baskChartView.setOnThemeChange(new IOnThemeChange() {
                @Override
                public void OnThemeChange(IChartTheme theme) {
                    Log.d(TAG, "OnThemeChange-1: " + theme);
                    //baskChartView.setChartTheme(theme);

                    adapter.notifyDataSetChanged();
                }
            });*/

            baskChartViews.add(baskChartView);

        }

        adapter = new ChartsAdapter(this, baskChartViews);
        setListAdapter(adapter);

        adapter.notifyDataSetChanged();

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

        for(int i=0; i<1/* baskChartViews.size()*/; i++) {
            Log.d(TAG, "toggleTheme, baskChartViews[" + i + "]: " + theme.getClass().getSimpleName());
            updateChart(baskChartViews.get(i), theme, i);
        }

    }

    //https://stackoverflow.com/questions/3724874/how-can-i-update-a-single-row-in-a-listview
    private void updateChart(BaskChartView baskChartView, IChartTheme theme, int position) {

        //adapter.updateTheme(position, theme);
        adapter.notifyDataSetChanged();

    }
}
