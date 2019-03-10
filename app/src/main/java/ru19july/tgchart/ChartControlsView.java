package ru19july.tgchart;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru19july.tgchart.R;

public class ChartControlsView extends LinearLayout {

    private View mValue;
    private ChartView mImage;

    public ChartControlsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ChartControlsView, 0, 0);
        String titleText = a.getString(R.styleable.ChartControlsView_titleText);
        //@SuppressWarnings("ResourceAsColor")
        //int valueColor = a.getColor(R.styleable.ChartControlsView_valueColor,
        //        android.R.color.holo_blue_light);
        a.recycle();

        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.chart_view, this, true);

        TextView title = (TextView) getChildAt(0);
        title.setText(titleText);

        //mValue = getChildAt(1);
        //mValue.setBackgroundColor(valueColor);

        mImage = (ChartView) getChildAt(1);
        //mImage.setVisibility(GONE);
    }

    public ChartControlsView(Context context) {
        this(context, null);
    }

    public void setValueColor(int color) {
        mValue.setBackgroundColor(color);
    }

    public void setImageVisible(boolean visible) {
        mImage.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

}