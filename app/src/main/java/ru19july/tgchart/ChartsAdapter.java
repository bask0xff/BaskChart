package ru19july.tgchart;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import ru19july.tgchart.view.BaskChartView;
import ru19july.tgchart.view.canvas.ChartCanvasView;
import ru19july.tgchart.view.theme.LightTheme;

public class ChartsAdapter extends ArrayAdapter<BaskChartView> {
    private static final String TAG = ChartsAdapter.class.getSimpleName();

    private List<BaskChartView> baskChartViews;
    private Activity context;
    //private List<ChartData> charts;
    private  List<BaskChartView> charts;

    public ChartsAdapter(MainActivity context, List<BaskChartView> baskChartViews) {
        super(context,  R.layout.chart_item, baskChartViews);
        this.context = context;
        charts = baskChartViews;
    }

    public void remove(int position) {
        charts.remove(position);
    }

    static class ViewHolder {
        public BaskChartView baskChartView;
    }
/*
    public ChartsAdapter(Activity context, List<ChartData> charts) {
        super(context, R.layout.chart_item, charts);
        this.context = context;
        this.charts = charts;
    }
*/
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        Log.d(TAG, "getView: convertView: " + convertView);
        BaskChartView chartData = charts.get(position);

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.chart_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.baskChartView = rowView.findViewById(R.id.chartControlsView);
            //TODO: here!
            viewHolder.baskChartView.setChartTheme(chartData.getTheme());
            viewHolder.baskChartView.setRenderType(chartData.getRenderType());


            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.baskChartView.setData(chartData.getData());
        //holder.baskChartView.setChartTheme(getT);
        holder.baskChartView.invalidate();
        holder.baskChartView.update();

        return rowView;
    }


}