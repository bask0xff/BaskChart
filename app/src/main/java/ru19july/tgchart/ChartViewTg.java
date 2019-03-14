package ru19july.tgchart;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
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
import ru19july.tgchart.view.ChartManager;

import static ru19july.tgchart.utils.Utils.FindMinMax;

public class ChartViewTg extends View implements ChartManager.AnimationListener, View.OnTouchListener {

    private final String TAG = ChartViewTg.class.getSimpleName();

    private ChartManager chartManager;

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
    int touchIndex = -1;
    private int oldTouchIndex = -111;


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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec) / 2;
        chartManager.chart().setWidth(width);
        chartManager.chart().setHeight(height);
        setMeasuredDimension(width, height);
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public void initView(Context context, AttributeSet attrs) {
        setOnTouchListener(this);

        chartManager = new ChartManager(getContext(), this);

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

        {
            fps = frames;
            frames = 0;
        }

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

        drawing = true;

        if (mChartData == null) return null;

        int decimalCount = Utils.DEFAULT_DECIMAL_COUNT;

        //очищаем график
        Paint fp = new Paint();
        fp.setAntiAlias(false);
        fp.setStyle(Paint.Style.FILL_AND_STROKE);
        long ms = (new Date()).getTime();

        fp.setColor(Color.parseColor("#333333"));

        canvas.drawRect(0, 0, W, H, fp);

        //drawing graph quote
        if (mChartData.series.get(0).getValues().size() > 0) {
            double quoteValue = mChartData.series.get(1).getValues().get(mChartData.series.get(1).getValues().size() - 1);

            minQuote = Double.MAX_VALUE;
            maxQuote = Double.MIN_VALUE;

            MinMax minmax = new MinMax();
            minmax.min = Float.MAX_VALUE;
            minmax.max = Float.MIN_VALUE;

            for(int i=1; i<mChartData.series.size(); i++) {
                if(!mChartData.series.get(i).isChecked()) continue;
                MinMax mnmx = FindMinMax(mChartData.series.get(i).getValues());
                if (mnmx.min < minmax.min) minmax.min = mnmx.min;
                if (mnmx.max > minmax.max) minmax.max = mnmx.max;
            }

            NiceScale numScale = new NiceScale(minmax.min, minmax.max);
            minQuote = numScale.niceMin;
            maxQuote = numScale.niceMax;

            if (Double.isNaN(minQuote))
                minQuote = quoteValue - 0.01;
            if (Double.isNaN(maxQuote))
                maxQuote = quoteValue + 0.01;

            numScale = new NiceScale(minQuote, maxQuote);

            DrawChart(mChartData.series, canvas);

            DrawHorizontalLines(numScale, decimalCount, canvas);

        }

        drawing = false;
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

        int startIndex = (int) (startNormalized * mChartData.series.get(0).getValues().size());
        int endIndex = (int) (endNormalized * mChartData.series.get(0).getValues().size());

        float[] markerValues = new float[quotes.size() - 1];
        String[] markerColors = new String[quotes.size() - 1];

        float xk = 0;
        if (touchIndex >= 0)
            xk = (((touchIndex - startIndex - 0.f) / (endIndex - startIndex)) * W);

        int yMin = H;


        for (int j = 1; j < quotes.size(); j++) {
            if (!quotes.get(j).isChecked()) continue;
            for (int i = startIndex + 1; i < endIndex; i++) {
                int x1 = (int) (((i - 1.f - startIndex) / (endIndex - startIndex)) * W);
                int x2 = (int) (((i - 0.f - startIndex) / (endIndex - startIndex)) * W);


                int y1 = (int) ((1 - quotes.get(j).getValues().get(i - 1) / maxQuote) * H);
                int y2 = (int) ((1 - quotes.get(j).getValues().get(i) / maxQuote) * H);

                fp.setColor(Color.parseColor(quotes.get(j).getColor()));

                canvas.drawLine(x1, y1, x2, y2, fp);
            }

            if (touchIndex > 0 && touchIndex < quotes.get(j).getValues().size()) {
                markerValues[j - 1] = quotes.get(j).getValues().get(touchIndex);
                markerColors[j - 1] = quotes.get(j).getColor();

                float yk = (float) ((1.0f - quotes.get(j).getValues().get(touchIndex) / maxQuote) * H);
                if (yk < yMin && yk > 50)
                    yMin = (int) yk;

                fp.setColor(Color.parseColor(quotes.get(j).getColor()));
                canvas.drawCircle(xk, yk, 10, fp);
                fp.setColor(Color.parseColor("#333333"));
                canvas.drawCircle(xk, yk, 5, fp);

            }
        }

        yMin = yMin - 120;
        if(yMin < 100) yMin = 100;
        if (touchIndex > 0)
            DrawMarker(canvas, markerValues, markerColors, xk + 20, yMin);
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
            //mPaint.setColor(Utils.MARKER_BG_COLOR);
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

    private void DrawMarker(Canvas canvas, float[] values, String[] colors, float lastX, float lastY) {
        int decimalCount = 0;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setStrokeWidth(2);
        paint.setColor(Color.parseColor("#cccccc"));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);

        //floating quote
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setFakeBoldText(true);
        p.setStrokeWidth(1);

        //чёрный фон для текста плавающей текущей котировки
        p.setTextSize(H * Utils.FLOATING_QUOTE_TEXT_SIZE_RATIO);
        int xw = 0;
        int activeCounter = 0;
        for(int i=0; i<values.length; i++) {
            if(colors[i] == null) continue;
            int sz =(int) p.measureText(values[i] + "");
            if (xw < sz ) xw = sz;
            activeCounter++;
        }

        paint.setColor(Color.parseColor("#222222"));
        RectF rect = new RectF(
                lastX + Utils.FLOATING_QUOTE_MARGIN_LEFT,
                lastY - H * Utils.FLOATING_QUOTE_MARGIN_TOP_RATIO,
                lastX + Utils.FLOATING_QUOTE_MARGIN_LEFT + xw * Utils.FLOATING_QUOTE_WIDTH_RATIO,
                lastY + H * Utils.FLOATING_QUOTE_MARGIN_BOTTOM_RATIO + (activeCounter-1) * 105);
        canvas.drawRoundRect(rect, 8, 8, paint);

        int k = 0;
        for(int i=0; i<values.length; i++) {
            String strFmt = String.format("%%.%df", decimalCount);
            String str = String.format(strFmt, (float) values[i]);
            if(colors[i] == null) continue;
            p.setColor(Color.parseColor(colors[i]));
            canvas.drawText(str, lastX + 70, lastY + 16 + k * 105, p);
            k++;
        }

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
        touchIndex = -1;

        invalidate();
    }

    @Override
    public void onAnimationUpdated() {

    }

    public void animateChanges(final ChartData oldChartData, final ChartData newChartData) {

        post(new Runnable() {
            @Override
            public void run() {
                for(int i = 1; i< oldChartData.series.size(); i++)
                    Log.d(TAG, "run: oldChartData " + oldChartData.series.get(i).getTitle() + ": " + oldChartData.series.get(i).isChecked());

                for(int i = 1; i< newChartData.series.size(); i++)
                    Log.d(TAG, "run: newChartData " + newChartData.series.get(i).getTitle() + ": " + newChartData.series.get(i).isChecked());
            }
        });

        invalidate();
    }

    class MyListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
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
        int startIndex = (int) (startNormalized * mChartData.series.get(0).getValues().size());
        int endIndex = (int) (endNormalized * mChartData.series.get(0).getValues().size());
        touchIndex = (int) (startIndex + xTouched * (endIndex - startIndex)/W);

        oldTouchIndex = touchIndex;

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

//try it
//http://www.java2s.com/Code/Android/2D-Graphics/DrawPolygon.htm
