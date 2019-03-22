package ru19july.tgchart.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import ru19july.tgchart.R;
import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.utils.NiceScale;
import ru19july.tgchart.view.theme.DarkTheme;
import ru19july.tgchart.view.theme.IChartTheme;

public class ChartSliderView extends View implements View.OnTouchListener {

    Paint paint;

    int slLeft;
    int slRight;
    private int W, H;
    private final String TAG = "ChartViewSlider";

    ISliderListener mOnSliderListener;

    private boolean drawing = false;
    private float xStart = 50.0f;
    private float xEnd = 450.0f;
    private boolean slideMoving = false;
    private float startMoveX = 0.0f;
    private float xStartSaved = 0.0f;
    private float xEndSaved = 0.f;
    private ChartData chartData;
    private IChartTheme mTheme = new DarkTheme();

    public ChartSliderView(Context context) {
        super(context);
        Log.d(TAG, "ChartView(Context context)");

        initView(context, null);
    }

    public ChartSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "ChartView(Context context, AttributeSet attrs) ");

        initView(context, attrs);
    }

    public ChartSliderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(TAG, "ChartView(Context context, AttributeSet attrs, int defStyleAttr)");

        initView(context, attrs);
    }

    public void initView(Context context, AttributeSet attrs) {
        setOnTouchListener(this);

        slRight = W;
        slLeft = W * 3 / 4;

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);

        //startTime = BinaryStationClient.Instance().CurrentTime();
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

        canvas.save();

        W = canvas.getWidth();
        H = canvas.getHeight();

        if (chartData != null)
            PrepareCanvas(canvas);

        canvas.restore();
    }

    public Canvas PrepareCanvas(Canvas canvas) {
        if (drawing) return canvas;

        drawing = true;

        Paint fp = new Paint();

        NiceScale numScale = new NiceScale(chartData.getSeries());

        for (int i = 1; i < chartData.getSeries().get(0).getValues().size(); i++) {
            for (int j = 1; j < chartData.getSeries().size(); j++) {
                if (!chartData.getSeries().get(j).isChecked()) continue;

                int x1 = (int) (W * ((i - 1.f) / (chartData.getSeries().get(0).getValues().size() - 1)));
                int x2 = (int) (W * ((i - 0.f) / (chartData.getSeries().get(0).getValues().size() - 1)));

                int y1 = (int) ((1 - chartData.getSeries().get(j).getValues().get(i - 1) / numScale.niceMax) * H);
                int y2 = (int) ((1 - chartData.getSeries().get(j).getValues().get(i) / numScale.niceMax) * H);

                fp.setColor(Color.parseColor(chartData.getSeries().get(j).getColor()));

                canvas.drawLine(x1, y1, x2, y2, fp);
            }
        }

        //left part
        fp.setAntiAlias(false);
        fp.setStyle(Paint.Style.FILL_AND_STROKE);
        fp.setColor(Color.parseColor(mTheme.sliderBackground()));

        canvas.drawRect(0, 0, xStart, H, fp);

        //right part
        canvas.drawRect(xEnd, 0, W, H, fp);

        //slider window
        fp.setColor(Color.parseColor(mTheme.sliderInner()));
        canvas.drawRect(xStart + 16, 4, xEnd - 16, H - 4, fp);

        drawing = false;
        return canvas;
    }

    public void setSliderListener(ISliderListener sliderListener) {
        mOnSliderListener = sliderListener;
    }

    public void setData(ChartData chartData) {
        this.chartData = chartData;

        invalidate();
    }

    public void animateChanges(ChartData oldChartData, ChartData newChartData) {
        invalidate();
    }

    public void setTheme(IChartTheme theme) {
        mTheme = theme;
        invalidate();
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
        int x = (int) event.getX();
        //Log.d(TAG, "onTouchEvent; ACTION: " + event.getAction() + ";  x=" + x + "; result: " + result);

        float xx = event.getX();
        float dx1 = Math.abs(xx - xStart);
        float dx2 = Math.abs(xx - xEnd);

        if (dx1 < 50 && dx1 < dx2) xStart = xx;
        if (dx2 < 50 && dx2 < dx1) xEnd = xx;

        if (((dx1 > 50 && (dx1 < dx2)) || (dx2 > 50 && (dx2 < dx1))) && event.getAction() == MotionEvent.ACTION_DOWN) {
            slideMoving = true;
            xStartSaved = xStart;
            xEndSaved = xEnd;
            startMoveX = xx;
            //result = true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE && slideMoving) {
            xStart = xStartSaved + (xx - startMoveX);
            if (xStart < 0) xStart = 0;
            xEnd = xEndSaved + (xx - startMoveX);
            if (xEnd >= W) xEnd = W;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            slideMoving = false;
        }

        if (mOnSliderListener != null)
            mOnSliderListener.onSlide((int) xStart, (int) xEnd);

        return true;
    }

    interface ISliderListener {
        void onSlide(int xStart, int xEnd);
    }

}