package ru19july.tgchart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import ru19july.tgchart.utils.Logger;
import ru19july.tgchart.utils.NiceScale;
import ru19july.tgchart.utils.Utils;

public class ChartViewTg extends View implements View.OnTouchListener {

    private final String TAG = ChartViewTg.class.getSimpleName();

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector detector;
    private float mScaleFactor = 1.f;
    Paint paint;

    private double minQuote = Double.MAX_VALUE;
    private double maxQuote = Double.MIN_VALUE;

    private int W, ChartLineWidth, H;

    private int fps;
    private float fpt;
    private int frames = 0;
    private long startTime = 0;

    private float lastX = 0;
    private float lastY = 0;

    private long lastDrawTime = 0;

    private boolean drawing = false;
    private float xStart = 50.0f;
    private float xEnd = 450.0f;
    private ChartData mChartData;

    float startNormalized = 0.0f;
    float endNormalized = 0.0f;
    private float xTouched = 0.0f;

    public ChartViewTg(Context context) {
        super(context);
        Log.d(TAG, "ChartView(Context context)");

        initView(context, null);
    }

    public ChartViewTg(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "ChartView(Context context, AttributeSet attrs) ");

        initView(context, attrs);
    }

    public ChartViewTg(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(TAG, "ChartView(Context context, AttributeSet attrs, int defStyleAttr)");

        initView(context, attrs);
    }

    public void initView(Context context, AttributeSet attrs) {
        setOnTouchListener(this);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        detector = new GestureDetector(ChartViewTg.this.getContext(), new MyListener());

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);

        //startTime = BinaryStationClient.Instance().CurrentTime();
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.ChartViewTg,
                    0, 0);

            try {
                boolean mShowText = a.getBoolean(R.styleable.ChartViewTg_showLegend, false);
                int mTextPos = a.getInteger(R.styleable.ChartViewTg_labelPosition, 0);

                Log.d(TAG, "initView: showLegend: " + mShowText);
                Log.d(TAG, "initView: textPos: " + mTextPos);
            } finally {
                a.recycle();
            }
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        frames++;

        canvas.save();
        canvas.scale(mScaleFactor, mScaleFactor);

        //long timeLeft = BinaryStationClient.Instance().CurrentTime() - startTime;

        //if(timeLeft>1000)
        {
            fps = frames;
            frames = 0;
            //startTime = BinaryStationClient.Instance().CurrentTime();
        }
        //else
        //if(timeLeft>0)
        //    fpt = 1000 * frames / (float)timeLeft;

        W = canvas.getWidth();
        H = canvas.getHeight();
        ChartLineWidth = canvas.getWidth() * 25 / 40;//место, где заканчивается график

        PrepareCanvas(canvas);

        canvas.restore();
    }

    private float GetY(double y) {
        float realY = 0;
        if ((maxQuote - minQuote) > 0)
            realY = (float) (H * (1 - 0.2 - 0.6 * (y - minQuote) / (maxQuote - minQuote)));
        return realY;
    }

    public Canvas PrepareCanvas(Canvas canvas) {
        if (drawing) return canvas;

        //long startDrawing = BinaryStationClient.Instance().CurrentTime();

        drawing = true;

        if (mChartData == null) return null;

        /*HookTimeframe htf = BinaryStationClient.Instance().CurrentHookTimeframe();
        int optionKind = BinaryStationClient.Instance().OptionKind();
        Tool tool = BinaryStationClient.Instance().CurrentTool();
        int decimalCount = tool == null ? Utils.DEFAULT_DECIMAL_COUNT : tool.DecimalCount;
        //quotes = GroupBy(60, quotes);//M1:60; M5:300; H1:3600
*/
        int decimalCount = Utils.DEFAULT_DECIMAL_COUNT;

        //очищаем график
        Paint fp = new Paint();
        fp.setAntiAlias(false);
        fp.setStyle(Paint.Style.FILL_AND_STROKE);
        long ms = (new Date()).getTime();

        fp.setColor(Color.parseColor("#333333"));

        canvas.drawRect(0, 0, W, H, fp);

        //drawing graph quote
        if (mChartData.series.get(0).values.size() > 0) {
            double quoteValue = mChartData.series.get(1).values.get(mChartData.series.get(1).values.size() - 1);

            //find minQuote, maxQuote by last period
            minQuote = Double.MAX_VALUE;
            maxQuote = Double.MIN_VALUE;

            //FindMinMaxByHookTimeframe(quotes, htf, optionKind);

            MinMax minmax = new MinMax();
            minmax.min = Float.MAX_VALUE;
            minmax.max = Float.MIN_VALUE;

            for(int i=1; i<mChartData.series.size(); i++) {
                if(!mChartData.series.get(i).isChecked()) continue;
                MinMax mnmx = FindMinMax(mChartData.series.get(i).values);
                Log.d(TAG, "PrepareCanvas: mnmx: " + mnmx.min + " / " + mnmx.max);
                if (mnmx.min < minmax.min) minmax.min = mnmx.min;
                if (mnmx.max > minmax.max) minmax.max = mnmx.max;
            }

            Log.d(TAG, "PrepareCanvas: minmax: " + minmax.min + " / " + minmax.max);

            NiceScale numScale = new NiceScale(minmax.min, minmax.max);
            minQuote = numScale.niceMin;
            maxQuote = numScale.niceMax;

            Log.d(TAG, "PrepareCanvas: minQuote: " + minQuote + " / maxQuote: " + maxQuote);

            if (Double.isNaN(minQuote))
                minQuote = quoteValue - 0.01;
            if (Double.isNaN(maxQuote))
                maxQuote = quoteValue + 0.01;

            //Log.i("ChartView",  quoteValue + "; min:" + minQuote + ", max:" + maxQuote);

            numScale = new NiceScale(minQuote, maxQuote);

            DrawChart(mChartData.series, canvas);

            DrawHorizontalLines(numScale, decimalCount, canvas);
/*
            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setStyle(Paint.Style.FILL_AND_STROKE);
            p.setFakeBoldText(true);
            String str = xStart + ""; //String.format("+%.0f%%", xStart);
            p.setTextSize(H / 2);
            int xw = (int) p.measureText(str);
            p.setColor(Utils.PROFIT_COLOR);
            canvas.drawText(str, (W - xw) * Utils.PROFIT_TEXT_X_POSITION_RATIO, H * Utils.PROFIT_TEXT_Y_POSITION_RATIO, p);
*/
        }

        drawing = false;
        //lastDrawTime = BinaryStationClient.Instance().CurrentTime();
        return canvas;
    }


    private void DrawChart(List<Series> quotes, Canvas canvas) {
        Paint lp = new Paint();
        lp.setAntiAlias(false);
        lp.setStrokeWidth(1);
        lp.setPathEffect(new DashPathEffect(new float[]{1, 1}, 0));

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setColor(Color.BLUE);

        //очищаем график
        Paint fp = new Paint();
        fp.setAntiAlias(true);
        fp.setStyle(Paint.Style.FILL_AND_STROKE);
        fp.setStrokeWidth(5.0f);

        Random r = new Random();

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        int startIndex = (int) (startNormalized * mChartData.series.get(0).values.size());
        int endIndex = (int) (endNormalized * mChartData.series.get(0).values.size());

        int touchIndex = (int) (startIndex + xTouched * (endIndex - startIndex)/W);

        for (int j = 1; j < quotes.size(); j++) {
            if (!quotes.get(j).isChecked()) continue;
            for (int i = startIndex + 1; i < endIndex; i++) {
                int x1 = (int) (((i - 1.f - startIndex) / (endIndex - startIndex)) * W);
                int x2 = (int) (((i - 0.f - startIndex) / (endIndex - startIndex)) * W);


                int y1 = (int) ((1 - quotes.get(j).values.get(i - 1) / maxQuote) * H);
                int y2 = (int) ((1 - quotes.get(j).values.get(i) / maxQuote) * H);

                fp.setColor(Color.parseColor(quotes.get(j).color));

                //path.moveTo(point[0].x, point[0].y);
                //path.lineTo(point[1].x, point[1].y);
                //path.close();
                //canvas.drawPath(path, paint);

                //canvas.drawRect(x - 2, y - 2, x + 2, y + 2, fp);
                canvas.drawLine(x1, y1, x2, y2, fp);
            }

            float xk = (((touchIndex - startIndex - 0.f) / (endIndex - startIndex)) * W);
            float yk = (float) ((1.0f - quotes.get(j).values.get(touchIndex) / maxQuote) * H);

            fp.setColor(Color.parseColor(quotes.get(j).color));
            canvas.drawCircle(xk, yk, 10, fp);
            fp.setColor(Color.parseColor("#333333"));
            canvas.drawCircle(xk, yk, 5, fp);
        }


    }

    private MinMax FindMinMax(List<Long> quotes) {
        MinMax result = new MinMax();
        result.min = Float.MAX_VALUE;
        result.max = Float.MIN_VALUE;

        for (int i = 0; i < quotes.size(); i++) {
            int k = quotes.size() - i - 1;
            if (k >= 0 && k < quotes.size()) {
                Long q = quotes.get(k);
                if (q > result.max) result.max = q;
                if (q < result.min) result.min = q;
            } else {
                Logger.e(TAG, "quote is null, k=" + k + "; quotes=" + quotes.size());
                return null;
            }
        }
        return result;
    }

    private void DrawHorizontalLines(NiceScale numScale, int decimalCount, Canvas canvas) {
        //drawing horizontal lines
        double yLine = numScale.niceMin;
        while (yLine <= numScale.niceMax) {
            float yL = GetY(yLine);

            Path mPath = new Path();
            mPath.moveTo(0, yL);
            mPath.quadTo(W / 2, yL, W, yL);
            Paint mPaint = new Paint();
            mPaint.setAntiAlias(false);
            //  `mPaint.setColor(Utils.MARKER_BG_COLOR);
            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setPathEffect(new DashPathEffect(new float[]{1, 1}, 0));
            canvas.drawPath(mPath, mPaint);

            String strFmt = String.format("%%.%df", decimalCount);
            String str = String.format(strFmt, (float) yLine);

            Paint p = new Paint();
            float textSize = H * 0.033f;
            int xw = (int) p.measureText(str);
            p.setTextSize(textSize);
            p.setAntiAlias(true);
            p.setColor(Utils.NICESCALE_TEXT_COLOR);
            canvas.drawText(str, W * 0.90f, yL - textSize * 0.3f, p);

            yLine += numScale.tickSpacing;
        }
    }

    private void DrawPoly(Point[] point, Canvas canvas, Paint paint) {
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(point[0].x, point[0].y);
        path.lineTo(point[1].x, point[1].y);
        path.lineTo(point[2].x, point[2].y);
        path.lineTo(point[3].x, point[3].y);
        path.close();
        canvas.drawPath(path, paint);
    }

    public void updateSlideFrameWindow(int startX, int endX) {
        xStart = startX;
        xEnd = endX;

        startNormalized = (xStart + 0.f) / W;
        endNormalized = (xEnd + 0.f) / W;

        invalidate();
    }

    public void setData(ChartData chartData) {
        mChartData = chartData;
        if (endNormalized <= 0.0E-10)
            endNormalized = 1.0f;

        invalidate();
    }

    class MyListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            invalidate();
            return true;
        }
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
        boolean result = detector.onTouchEvent(event);

        xTouched = event.getX();

        invalidate();

        if (!result) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                //stopScrolling();
                result = true;
            }
        }
        return result;
    }

}