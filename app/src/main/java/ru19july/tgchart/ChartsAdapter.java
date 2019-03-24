package ru19july.tgchart;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.view.BaskChartView;
import ru19july.tgchart.view.theme.DarkTheme;

public class ChartsAdapter extends ArrayAdapter<ChartData> {
    private List<BaskChartView> baskChartViews;
    private Activity context;
    private List<ChartData> charts;

    static class ViewHolder {
        public BaskChartView baskChartView;
    }

    public ChartsAdapter(Activity context, List<ChartData> charts) {
        super(context, R.layout.chart_item, charts);
        this.context = context;
        this.charts = charts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.chart_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.baskChartView = rowView.findViewById(R.id.chartControlsView);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        ChartData chartData = charts.get(position);

        holder.baskChartView.setData(chartData);
        holder.baskChartView.setChartTheme(new DarkTheme());
        holder.baskChartView.invalidate();
        holder.baskChartView.update();

        return rowView;
    }
}