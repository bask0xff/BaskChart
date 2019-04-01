package ru19july.tgchart;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ru19july.tgchart.view.BaskChartView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<BaskChartView> values;

    public RecyclerViewAdapter(List<BaskChartView> records) {
        this.values = records;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        BaskChartView record = values.get(i);

        //viewHolder.name.setText(record.getName());
        viewHolder.baskChartView.setData(record.getData());
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private BaskChartView baskChartView;

        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            baskChartView = (BaskChartView) v.findViewById(R.id.baskChartView);
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