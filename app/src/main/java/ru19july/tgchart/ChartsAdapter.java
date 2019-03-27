package ru19july.tgchart;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import ru19july.tgchart.view.BaskChartView;

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
        Log.d(TAG, "getView: convertView: " + convertView);
        BaskChartView chartView = baskChartViewList.get(position);

        Log.d(TAG, "getView, baskChartViewList.get("+position+").getRenderType(): " + chartView.getRenderType().getSimpleName());

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.chart_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.baskChartView = rowView.findViewById(R.id.chartControlsView);

            //TODO: here!
            viewHolder.baskChartView.setChartTheme(chartView.getTheme());
            viewHolder.baskChartView.setRenderType(chartView.getRenderType());


            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.baskChartView.setData(chartView.getData());
        //holder.baskChartView.setChartTheme(getT);
        holder.baskChartView.invalidate();
        holder.baskChartView.update();

        return rowView;
    }


}