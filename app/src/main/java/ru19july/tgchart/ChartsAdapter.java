package ru19july.tgchart;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import java.util.List;

import ru19july.tgchart.view.BaskChartView;
import ru19july.tgchart.view.canvas.ChartCanvasView;
import ru19july.tgchart.view.opengl.ChartGLView;
import ru19july.tgchart.view.theme.LightTheme;

public class ChartsAdapter extends ArrayAdapter<BaskChartView> {
    private static final String TAG = ChartsAdapter.class.getSimpleName();

    private List<BaskChartView> baskChartViews;
    private Activity context;
    private  List<BaskChartView> baskChartViewList;

    public ChartsAdapter(MainActivity context, List<BaskChartView> baskChartViews) {
        super(context,  R.layout.chart_item, baskChartViews);
        this.context = context;
        baskChartViewList = baskChartViews;
    }

    public void remove(int position) {
        baskChartViewList.remove(position);
    }

    static class ViewHolder {
        public BaskChartView baskChartView;
        public LinearLayout layout_chart;
        public Class<?> renderType;
    }

/*
    public ChartsAdapter(Activity context, List<ChartData> baskChartViewList) {
        super(context, R.layout.chart_item, baskChartViewList);
        this.context = context;
        this.baskChartViewList = baskChartViewList;
    }
*/

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        Log.d(TAG, "getView #" + position + ": convertView: " + convertView);

        BaskChartView chartView = baskChartViewList.get(position);
        Class<?> renderType = chartView.getRenderType();

        Log.d(TAG, "getView, baskChartViewList.get("+position+").getRenderType(): " + chartView.getRenderType().getSimpleName());

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.chart_item, null);
            ViewHolder viewHolder = new ViewHolder();

            viewHolder.layout_chart = rowView.findViewById(R.id.layout_chart);

            viewHolder.baskChartView = new BaskChartView(context, renderType);

            viewHolder.renderType = renderType;
            viewHolder.layout_chart.addView(viewHolder.baskChartView);

            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.baskChartView.setData(chartView.getData());
        holder.baskChartView.setRenderType(renderType);
        holder.baskChartView.setChartTheme(chartView.getTheme());

        holder.baskChartView.invalidate();
        holder.baskChartView.update();

        return rowView;
    }


}