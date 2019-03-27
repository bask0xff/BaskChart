package ru19july.tgchart.view;

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
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru19july.tgchart.R;
import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.interfaces.IChartTheme;
import ru19july.tgchart.interfaces.IChartView;
import ru19july.tgchart.interfaces.IOnThemeChange;
import ru19july.tgchart.view.canvas.ChartCanvasView;
import ru19july.tgchart.view.opengl.ChartGLView;
import ru19july.tgchart.view.opengl.ChartGLRenderer;
import ru19july.tgchart.view.theme.DarkTheme;
import ru19july.tgchart.view.theme.LightTheme;

public class BaskChartView extends LinearLayout {

    private Class<?> mChartViewClass;
    private Context mContext;
    private TextView title;
    private IChartTheme mTheme;

    private IChartView chartView;
    private ChartSliderView chartSliderView;
    private String TAG = BaskChartView.class.getSimpleName();
    private ChartData mChartData;
    private IOnThemeChange mOnThemeChange;
    private LinearLayout linearlayout;

    public BaskChartView(Context context, Class<?> chartViewClass) {
        super(context, null);

        Log.d(TAG, "BaskChartView: chartViewClass: " + chartViewClass.getSimpleName());

        mChartViewClass = chartViewClass;

        init(context);
    }

    public BaskChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.BaskChartView, 0, 0);

            try {
                String titleText = a.getString(R.styleable.BaskChartView_titleText);
                boolean mShowText = a.getBoolean(R.styleable.ChartCanvasView_showLegend, false);

                int mode = a.getInteger(R.styleable.BaskChartView_renderType, 0);
                if(mode == 0)
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

        init(context);

        setChartTheme(mTheme);

    }

    private void setTitle(String text) {
        if (title != null)
            title.setText(text);
    }

    private void init(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.TOP);

        mContext = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.chart_view, this, true);

        linearlayout = (LinearLayout) getChildAt(0);

        //dynamically add canvas/OpenGL chart
        if(mChartViewClass.getCanonicalName().equals(ChartGLView.class.getCanonicalName())) {
            chartView = new ChartGLView(context);
            ChartGLRenderer mRenderer = new ChartGLRenderer(context);
            chartView.setRenderer(mRenderer);
            linearlayout.addView((ChartGLView) chartView, 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 500));
        }
        else
        {
            chartView = new ChartCanvasView(context);
            linearlayout.addView((ChartCanvasView) chartView, 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 1100));
        }

        chartSliderView = (ChartSliderView) linearlayout.getChildAt(2);
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

    public void setChartTheme(IChartTheme theme) {
        Log.d(TAG, "setTheme: " + theme.getClass().getSimpleName());
        mTheme = theme;
        updateTheme();
        setTitle(theme.getClass().getSimpleName());
        chartView.setTheme(mTheme);
        chartSliderView.setTheme(mTheme);

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
        chartSliderView.invalidate();
        chartView.invalidate();
    }

    public ChartData getData() {
        return mChartData;
    }

    public void setMode(int mode) {
        //init(mContext);

        if(mode == 0) {
            chartView = new ChartGLView(mContext);
            ChartGLRenderer mRenderer = new ChartGLRenderer(mContext);
            chartView.setRenderer(mRenderer);
            //linearlayout.addView((ChartGLView) chartView, 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 500));
        }
        else
        {
            chartView = new ChartCanvasView(mContext);
            //linearlayout.addView((ChartCanvasView) chartView, 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 1100));
        }

        chartView.invalidate();
    }
}

