package ru19july.baskchart.view;

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

import ru19july.baskchart.R;
import ru19july.baskchart.data.ChartData;
import ru19july.baskchart.interfaces.IChartTheme;
import ru19july.baskchart.interfaces.IChartView;
import ru19july.baskchart.interfaces.IOnThemeChange;
import ru19july.baskchart.view.canvas.ChartCanvasView;
import ru19july.baskchart.view.opengl.ChartGLView;
import ru19july.baskchart.view.opengl.ChartGLRenderer;
import ru19july.baskchart.view.theme.DarkTheme;
import ru19july.baskchart.view.theme.LightTheme;

/*
 Created by Sergey V. Baskov in 2019
 */

public class BaskChartView extends LinearLayout {

    private String TAG = BaskChartView.class.getSimpleName();

    private Context mContext;
    private TextView title;
    private IChartTheme mTheme;

    //default
    private Class<?> mChartViewClass;
    private IChartView chartView;

    private ChartData mChartData;

    private IOnThemeChange mOnThemeChange;
    private LinearLayout linearlayout;
    private TextView titleView;

    public BaskChartView(Context context, Class<?> chartViewClass) {
        super(context, null);

        Log.d(TAG, "BaskChartView: chartViewClass: " + chartViewClass.getSimpleName());

        mChartViewClass = chartViewClass;

        init(context, chartViewClass);
    }


    public BaskChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        Log.d(TAG, "BaskChartView(Context context, AttributeSet attrs: " );

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.BaskChartView, 0, 0);

            try {
                String titleText = a.getString(R.styleable.BaskChartView_titleText);
                boolean mShowText = a.getBoolean(R.styleable.ChartCanvasView_showLegend, false);

                //TODO: FIXIT! PROBLEM IS HERE! NULL VALUES IF VIEW CREATED PROGRAMMATICALLY
                int renderType = a.getInteger(R.styleable.BaskChartView_renderType, -1);
                Log.d(TAG, "BaskChartView: renderType: " + renderType);

                if(renderType == -1)
                {
                    //renderType = 1;
                    //take the parameters from chart data
                    mChartViewClass = getRenderType();
                }

                Log.d(TAG, "BaskChartView: old mChartViewClass: " + mChartViewClass);

                //TODO: here is wrong works!!!
                //if(renderType != -1)
                    if(renderType == 0)
                        mChartViewClass = ChartGLView.class;
                    else
                        mChartViewClass = ChartCanvasView.class;

                int themeId = a.getInteger(R.styleable.BaskChartView_themeType, 0);
                Log.d(TAG, "BaskChartView: themeId=" + themeId);
                switch (themeId) {
                    case 0:
                        mTheme = new LightTheme();
                    default:
                        mTheme = new DarkTheme();
                }

//                title = (TextView) linearlayout.getChildAt(0);
//                setTitle(titleText);
            } finally {
                a.recycle();
            }
        }

        Log.d(TAG, "BaskChartView: mChartViewClass: " + mChartViewClass.getSimpleName());

        init(context, mChartViewClass);

        Log.d(TAG, "BaskChartView: setChartTheme(mTheme); " + mTheme);
        //setChartTheme(mTheme);
        //setRenderType(mChartViewClass);

    }

    private void setTitle(String text) {
        if (title != null)
            title.setText(text);
    }

    private void init(Context context, Class<?> chartViewClass) {
        Log.d(TAG, "init: ");
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.TOP);

        mContext = context;
        mChartViewClass = chartViewClass;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.chart_view, this, true);

        linearlayout = (LinearLayout) getChildAt(0);

        int height = 700;

        //dynamically add canvas/OpenGL chart
        if (chartViewClass.getCanonicalName().equals(ChartGLView.class.getCanonicalName())) {
            chartView = new ChartGLView(context);
            ChartGLRenderer mRenderer = new ChartGLRenderer((View) chartView, context);
            chartView.setRenderer(mRenderer);
        } else {
            chartView = new ChartCanvasView(context);
            height = (int) (height * 2);
        }

        linearlayout.addView((View) chartView, 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height));

        titleView = (TextView) linearlayout.getChildAt(0);
        titleView.setText("render type: " + chartView.getClass().getSimpleName());

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setData(ChartData chartData) {
        mChartData = chartData;
        chartView.setData(mChartData);

        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.insert_point);
        insertPoint.removeAllViews();
        if(mChartData.getSeries() == null || mChartData.getSeries().size()<1) return;

        for (int i = 1; i < mChartData.getSeries().size(); i++) {
            CheckBox checkBox = new CheckBox(mContext);
            checkBox.setText(mChartData.getSeries().get(i).getTitle());
            checkBox.setTextColor(Color.parseColor(mChartData.getSeries().get(i).getColor()));

            ColorStateList colorStateList = new ColorStateList(
                    new int[][] {
                            new int[] { -android.R.attr.state_checked },
                            new int[] {  android.R.attr.state_checked }
                    },
                    new int[] {
                            Color.parseColor(mChartData.getSeries().get(i).getColor()),
                            Color.parseColor(mChartData.getSeries().get(i).getColor())
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
        Log.d(TAG, "getTheme: " + mTheme);
        return mTheme;
    }

    public void setChartTheme(IChartTheme theme) {
        Log.d(TAG, "setChartTheme: " + theme.getClass().getSimpleName());
        mTheme = theme;
        updateTheme();
        setTitle(theme.getClass().getSimpleName());
        Log.d(TAG, "setChartTheme: chartView: " + chartView);

        chartView.setTheme(mTheme);

        if(mOnThemeChange != null)
            mOnThemeChange.OnThemeChange(theme);
        else
            Log.e(TAG, "setChartTheme: mOnThemeChange is null" );

        invalidate();
    }

    private void updateTheme() {
        setBackgroundColor(Color.parseColor(mTheme.backgroundColor()));
    }

    public void setOnThemeChange(IOnThemeChange onThemeChange){
        mOnThemeChange = onThemeChange;
    }

    public void update() {
        chartView.invalidate();
    }

    public ChartData getData() {
        return mChartData;
    }

    public void setRenderType(Class<?> chartViewClass) {
        mChartViewClass = chartViewClass;
        Log.d(TAG, "setRenderType: " + chartViewClass);
        invalidate();
    }

    public Class<?> getRenderType() {
        Log.d(TAG, "getRenderType => " + mChartViewClass);
        return mChartViewClass;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null || obj.getClass() != this.getClass()) return false;
        if (obj == this) return true;
        BaskChartView bcv = (BaskChartView)obj;
        return bcv.getData() == getData();
    }

    @Override
    public int hashCode() {
        int hash = getHash(1, title);
        hash = getHash(hash, mContext);
        hash = getHash(hash, mTheme);
        hash = getHash(hash, mChartViewClass);
        hash = getHash(hash, chartView);
        hash = getHash(hash, mChartData);
        hash = getHash(hash, mOnThemeChange);
        hash = getHash(hash, linearlayout);

        return hash;
    }

    private int getHash(int hash, Object obj) {
        if (obj == null)
            return 0;
        int result = 31 * hash + (obj == null ? 0 : obj.hashCode());
        return result;
    }
}

