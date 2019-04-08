package ru19july.tgchart.view.canvas;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Date;
import java.util.List;
import java.util.Random;

import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.R;
import ru19july.tgchart.data.MinMaxIndex;
import ru19july.tgchart.data.Series;
import ru19july.tgchart.interfaces.IChartTheme;
import ru19july.tgchart.interfaces.IChartView;
import ru19july.tgchart.utils.NiceDate;
import ru19july.tgchart.utils.NiceScale;
import ru19july.tgchart.utils.Utils;
import ru19july.tgchart.view.ChartEngine;
import ru19july.tgchart.view.opengl.ChartGLRenderer;
import ru19july.tgchart.view.theme.DarkTheme;

public class ChartCanvasView extends View implements IChartView, View.OnTouchListener {

    private final String TAG = ChartCanvasView.class.getSimpleName();

    private ChartData mChartData;

    private Graphix graphics;
    private ChartEngine chartEngine = new ChartEngine();

    public ChartCanvasView(Context context) {
        super(context);
        initView(context, null);
    }

    public ChartCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public ChartCanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec) / 2;
        setMeasuredDimension(width, height);
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public void initView(Context context, AttributeSet attrs) {
        setOnTouchListener(this);

        graphics = new Graphix();

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.ChartCanvasView,
                    0, 0);

            try {
                boolean mShowText = a.getBoolean(R.styleable.ChartCanvasView_showLegend, false);
                int mTextPos = a.getInteger(R.styleable.ChartCanvasView_labelPosition, 0);

                Log.d(TAG, "initView: showLegend: " + mShowText);
                Log.d(TAG, "initView: textPos: " + mTextPos);
            } finally {
                a.recycle();
            }
        }

    }

    @Override
    protected void onDraw(final Canvas canvas) {
        chartEngine.DrawChart(canvas, mChartData);
    }


    public void updateSlideFrameWindow(int startX, int endX) {
        chartEngine.updateSlideFrameWindow(startX, endX);

        invalidate();
    }
/*
    public void setData(ChartData chartData) {
        chartEngine.setData(chartData);

        invalidate();
    }
*/
    public void animateChanges(final ChartData oldChartData, final ChartData newChartData) {

        oldChartData.getNiceScale();
        newChartData.getNiceScale();

        float scaleFrom = (float) (oldChartData.getMaxValue() - (oldChartData.getMinValue() + 0f));
        float scaleTo = (float) (newChartData.getMaxValue() - (newChartData.getMinValue() + 0f));
        float ratio = scaleTo / scaleFrom;

        ValueAnimator va = ValueAnimator.ofFloat(ratio, 1f);
        int mDuration = 1000;
        va.setDuration(mDuration);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                for (int i = 1; i < mChartData.getSeries().size(); i++)
                    mChartData.getSeries().get(i).setScale((float) animation.getAnimatedValue());

                invalidate();
            }
        });

        va.start();

        invalidate();
    }
/*
    public void setTheme(IChartTheme theme) {
        chartEngine.setTheme(theme);
    }
*/
    @Override
    public void setRenderer(ChartGLRenderer mRenderer) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            invalidate();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        invalidate();

        return chartEngine.onTouchEvent(event);
    }

    public void showChart(final int k, float from, float to) {
        ValueAnimator va = ValueAnimator.ofFloat(from, to);
        int mDuration = 1000;
        va.setDuration(mDuration);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if(mChartData.getSeries() != null)
                    mChartData.getSeries().get(k).setAlpha((float)animation.getAnimatedValue());
                invalidate();
            }
        });
        va.start();
    }

    private int W, H;

    Paint paint;
    Random r = new Random();

    private float xStart = 50.0f;
    private float xEnd = 450.0f;
    private IChartTheme mTheme = new DarkTheme();

    float startNormalized = 0.0f;
    float endNormalized = 0.0f;
    private float xTouched = 0.0f;
    int touchIndex = -1;
    private String themeName;
/*
    public void updateSlideFrameWindow(int startX, int endX) {
        xStart = startX;
        xEnd = endX;

        startNormalized = (xStart + 0.f) / W;
        endNormalized = (xEnd + 0.f) / W;
    }
*/
    public void setData(ChartData chartData) {
        Log.d(TAG, "setData: " + chartData);
        mChartData = chartData;
        if (endNormalized <= 0.0E-10)
            endNormalized = 1.0f;
        touchIndex = -1;
    }

    public void setTheme(IChartTheme theme) {
        mTheme = theme;
        themeName = mTheme.getClass().getSimpleName() + ":" + r.nextDouble();

        Log.d(TAG, "setTheme: " + mTheme + " / " + mTheme.getClass().getSimpleName() + " => " + themeName);

    }
}

