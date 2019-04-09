package ru19july.tgchart.view.canvas;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.R;
import ru19july.tgchart.interfaces.IChartTheme;
import ru19july.tgchart.interfaces.IChartView;
import ru19july.tgchart.view.ChartEngine;
import ru19july.tgchart.view.opengl.ChartGLRenderer;
import ru19july.tgchart.view.theme.DarkTheme;

public class ChartCanvasView extends View implements IChartView, View.OnTouchListener {

    private final String TAG = ChartCanvasView.class.getSimpleName();

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
        chartEngine.DrawChart(canvas);
    }

    @Override
    public void updateSlideFrameWindow(int startX, int endX) {
        chartEngine.updateSlideFrameWindow(startX, endX);

        invalidate();
    }

    public void setData(ChartData chartData) {
        chartEngine.setData(chartData);
    }

    public void animateChanges(final ChartData oldChartData, final ChartData newChartData) {
        chartEngine.animateChanges(this, oldChartData, newChartData);
    }
    @Override
    public void setRenderer(ChartGLRenderer mRenderer) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
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

    public void showChart(final int k, float from, float to) { chartEngine.showChart(this, k, from, to); }

    public void setTheme(IChartTheme theme) {
        chartEngine.setTheme(theme);
    }
}

