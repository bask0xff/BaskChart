package ru19july.tgchart.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru19july.tgchart.R;
import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.view.theme.DarkTheme;
import ru19july.tgchart.view.theme.IChartTheme;
import ru19july.tgchart.view.theme.LightTheme;

public class ContestChartView extends LinearLayout {

    private final Context mContext;
    private IChartTheme mTheme;

    private ChartCanvasView chartView;
    private ChartSliderView chartSliderView;
    private String TAG = ContestChartView.class.getSimpleName();
    private ChartData mChartData;

    public ContestChartView(Context context) {
        this(context, null);
        init(context);
    }

    public ContestChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.ContestChartView, 0, 0);

            try {
                String titleText = a.getString(R.styleable.ContestChartView_titleText);
                boolean mShowText = a.getBoolean(R.styleable.ChartCanvasView_showLegend, false);
                int themeId = a.getInteger(R.styleable.ContestChartView_themeType, 0);
                switch (themeId) {
                    case 1:
                        mTheme = new DarkTheme();
                    default:
                        mTheme = new LightTheme();
                }

                setTheme(mTheme);

                TextView title = (TextView) getChildAt(0);
                title.setText(titleText);
            } finally {
                a.recycle();
            }
        }

    }

    private void init(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.TOP);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.chart_view, this, true);

        chartView = (ChartCanvasView) getChildAt(1);

        chartSliderView = (ChartSliderView) getChildAt(2);
        chartSliderView.setSliderListener(new ChartSliderView.ISliderListener() {
            @Override
            public void onSlide(int xStart, int xEnd) {
                chartView.updateSlideFrameWindow(xStart, xEnd);
            }
        });


    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setData(ChartData chartData) {
        mChartData = chartData;
        chartView.setData(mChartData);
        chartSliderView.setData(mChartData);

        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.insert_point);
        insertPoint.removeAllViews();

        for (int i = 1; i < mChartData.getSeries().size(); i++) {
            CheckBox checkBox = new CheckBox(mContext);
            checkBox.setText(mChartData.getSeries().get(i).getTitle());
            checkBox.setTextColor(Color.parseColor(mChartData.getSeries().get(i).getColor()));

            ColorStateList colorStateList = new ColorStateList(
                    new int[][] {
                            new int[] { -android.R.attr.state_checked }, // unchecked
                            new int[] {  android.R.attr.state_checked }  // checked
                    },
                    new int[] {
                            Color.parseColor(mChartData.getSeries().get(i).getColor()),//unchecked
                            Color.parseColor(mChartData.getSeries().get(i).getColor())//checked
                    }

            );
            checkBox.setButtonTintList(colorStateList);

            checkBox.setChecked(true);
            mChartData.getSeries().get(i).setChecked(true);

            insertPoint.addView(checkBox, i - 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            final int finalI = i;
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    ChartData oldChartData = new ChartData();
                    oldChartData.setSeries(mChartData.getSeries());
                    oldChartData.copyFrom(mChartData);

                    mChartData.getSeries().get(finalI).setChecked(isChecked);

                    if(!isChecked)
                        chartView.showChart(finalI, 1f, 0f);
                    if(isChecked)
                        chartView.showChart(finalI, 0f, 1f);

                    chartView.animateChanges(oldChartData, mChartData);
                    chartSliderView.animateChanges(oldChartData, mChartData);

                }
            });
        }

        chartView.invalidate();
        invalidate();
    }

    private int getColor(String color) {
        return Color.parseColor(color);
    }

    public IChartTheme getTheme() {
        return mTheme;
    }

    public void setTheme(IChartTheme theme) {
        mTheme = theme;
        updateTheme();
        chartView.setTheme(mTheme);
        chartSliderView.setTheme(mTheme);
        invalidate();
    }

    private void updateTheme() {
        setBackgroundColor(Color.parseColor(mTheme.backgroundColor()));
    }
}

