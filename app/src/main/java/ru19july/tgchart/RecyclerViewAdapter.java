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
        //BaskChartView record = values.get(i);

        BaskChartView chartView = values.get(i);
        Class<?> renderType = chartView.getRenderType();

        //viewHolder.baskChartView.setData(record.getData());


        //viewHolder.layout_chart = findViewById(R.id.layout_chart);

        viewHolder.baskChartView = chartView;//new BaskChartView(context, renderType);

        //viewHolder.renderType = renderType;
        viewHolder.layout_chart.addView(viewHolder.baskChartView);

        Log.d(TAG, "onBindViewHolder: " + renderType.getSimpleName());

        viewHolder.baskChartView.setData(chartView.getData());
        viewHolder.baskChartView.setRenderType(renderType);
        viewHolder.baskChartView.setChartTheme(chartView.getTheme());

        viewHolder.baskChartView.invalidate();
        viewHolder.baskChartView.update();
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layout_chart;
        private BaskChartView baskChartView;

        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            //baskChartView = v.findViewById(R.id.baskChartView);

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