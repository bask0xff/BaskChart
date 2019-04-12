package ru19july.tgchart;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
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
    private int chartType = 0;

    int contestChartNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        chartType = intent.getIntExtra("chart_type", 0);
        contestChartNumber = intent.getIntExtra("chart_number", 1);

        chartsData = new ArrayList<>();

        //String json = loadJSONFromAsset("chart_data.json");
        //Log.d(TAG, "JSON: " + json);
        //chartsData.addAll(readJson(json));
        //for(contestChartNumber = 0 ; contestChartNumber < 5; contestChartNumber++) {
            ChartData chartData = new ChartData();
            String folder = "contest/" + (contestChartNumber+0) + "/";
            chartData = chartData.loadData(this, folder + "overview.json");
            chartData.setType(ChartData.CHART_TYPE.CHART_TYPE_FILLEDPOLY);
            chartsData.add(chartData);
        //}

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

        for (int i = 0; i < charts; i++) {

            Class<?> chartClass =
                    //i % 2 != 0
                    chartType == 1
                    ? ChartCanvasView.class : ChartGLView.class;
            IChartTheme theme = i % 1 == 0 ? new DarkTheme() : new LightTheme();

            Log.d(TAG, "initChartView: ------------------ CHART #" + i + " => " + theme.getClass().getSimpleName());
            final BaskChartView baskChartView = new BaskChartView(this, chartClass);
            baskChartView.setChartTheme(theme);
            baskChartView.setData(chartsData.get(i % charts));

            baskChartViews.add(baskChartView);
        }
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
        //mAdapter.updateData();
        //mAdapter.notifyItemChanged(position);
    }
}
