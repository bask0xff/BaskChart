package ru19july.tgchart;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.List;

import ru19july.tgchart.view.BaskChartView;
import ru19july.tgchart.view.canvas.ChartCanvasView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = RecyclerViewAdapter.class.getSimpleName();
    private List<BaskChartView> values;
    private Context context;

    public RecyclerViewAdapter(List<BaskChartView> records) {
        this.values = records;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_item, viewGroup, false);
        context = viewGroup.getContext();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        BaskChartView chartView = values.get(i);

        viewHolder.layout_chart.removeAllViews();

        viewHolder.baskChartView = chartView;

        Log.d(TAG, "onBindViewHolder: (" + i + ")" + chartView.getRenderType().getSimpleName());

        viewHolder.baskChartView.setData(chartView.getData());
        viewHolder.baskChartView.setRenderType(chartView.getRenderType());
        viewHolder.baskChartView.setChartTheme(chartView.getTheme());

        viewHolder.baskChartView.invalidate();
        viewHolder.baskChartView.update();
        viewHolder.layout_chart.addView(viewHolder.baskChartView);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout layout_chart;
        public BaskChartView baskChartView;
        public View mView;

        public ViewHolder(View v) {
            super(v);
            mView = v;
            baskChartView = new BaskChartView(context, ChartCanvasView.class);
            layout_chart = v.findViewById(R.id.layout_chart);
        }
    }


    public void add(int position, BaskChartView item) {
        values.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        values.remove(position);
        notifyItemRemoved(position);
    }


}