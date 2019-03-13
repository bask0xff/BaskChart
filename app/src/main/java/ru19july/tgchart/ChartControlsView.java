package ru19july.tgchart;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChartControlsView extends LinearLayout {

    private final ChartViewSlider mSlider;
    private final Context mContext;

    private ChartViewTg chartView;
    private String TAG = ChartControlsView.class.getSimpleName();
    private ChartData mChartData;

    public ChartControlsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ChartControlsView, 0, 0);
        String titleText = a.getString(R.styleable.ChartControlsView_titleText);
        //@SuppressWarnings("ResourceAsColor")
        //int valueColor = a.getColor(R.styleable.ChartControlsView_valueColor,
        //        android.R.color.holo_blue_light);
        a.recycle();

        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.TOP);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.chart_view, this, true);

        TextView title = (TextView) getChildAt(0);
        title.setText(titleText);

        chartView = (ChartViewTg) getChildAt(1);
        //chartView.setVisibility(GONE);

        mSlider = (ChartViewSlider) getChildAt(2);
        mSlider.setSliderListener(new ChartViewSlider.ISliderListener() {
            @Override
            public void onSlide(int xStart, int xEnd) {
                chartView.updateSlideFrameWindow(xStart, xEnd);
            }
        });

    }

    public ChartControlsView(Context context) {
        this(context, null);
    }

    //public void setValueColor(int color) {
    //    mValue.setBackgroundColor(color);
    //}

    public void setImageVisible(boolean visible) {
        chartView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setData(ChartData chartData) {
        mChartData = chartData;
        chartView.setData(mChartData);

        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.insert_point);
        insertPoint.removeAllViews();

        for (int i = 1; i < mChartData.series.size(); i++) {
            CheckBox checkBox = new CheckBox(mContext);
            checkBox.setText(mChartData.series.get(i).title + " ("+ mChartData.series.get(i).color +")");
            checkBox.setTextColor(Color.WHITE);

            ColorStateList colorStateList = new ColorStateList(
                    new int[][] {
                            new int[] { -android.R.attr.state_checked }, // unchecked
                            new int[] {  android.R.attr.state_checked }  // checked
                    },
                    new int[] {
                            Color.parseColor("#555555"),//unchecked
                            Color.parseColor(mChartData.series.get(i).color)//checked
                    }

            );
            checkBox.setButtonTintList(colorStateList);

            checkBox.setChecked(true);
            mChartData.series.get(i).setChecked(true);

            insertPoint.addView(checkBox, i - 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            final int finalI = i;
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG, "onCheckedChanged: " + isChecked);

                    mChartData.series.get(finalI).setChecked(isChecked);
                    chartView.invalidate();
                }
            });
        }

        invalidate();
    }

    private int getColor(String color) {
        return Color.parseColor(color);
    }
}