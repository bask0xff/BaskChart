package ru19july.tgchart.view;

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

import java.util.Date;
import java.util.List;
import java.util.Random;

import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.data.MinMax;
import ru19july.tgchart.R;
import ru19july.tgchart.data.Series;
import ru19july.tgchart.utils.NiceScale;
import ru19july.tgchart.utils.Utils;

public class ChartViewTg extends View implements View.OnTouchListener {

    private final String TAG = ChartViewTg.class.getSimpleName();

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector detector;
    private float mScaleFactor = 1.f;
    Paint paint;

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
    private float realW = 1.0f;
    private float leftMinValue = 0;
    private float rightMaxValue = 1;


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
        setMeasuredDimension(width, height);
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public void initView(Context context, AttributeSet attrs) {
        setOnTouchListener(this);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        detector = new GestureDetector(ChartViewTg.this.getContext(), new MyListener());

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);

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
        if ((mChartData.getMaxQuote() - mChartData.getMinQuote()) > 0)
            realY = (float) (H * (1 - 0.2 - 0.6 * (y - mChartData.getMinQuote()) / (mChartData.getMaxQuote() - mChartData.getMinQuote())));
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
/*
* switch (mTheme) {
            case 1:
                setBackgroundColor(getResources().getColor(R.color.light_bg));
                break;
            default:
                setBackgroundColor(getResources().getColor(R.color.dark_bg));
                break;
        }
* */

        canvas.drawRect(0, 0, W, H, fp);

        //drawing graph quote
        if (mChartData.getSeries().get(0).getValues().size() > 0) {
            NiceScale numScale = mChartData.getNiceScale();
            DrawChart(mChartData.getSeries(), canvas);
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

        MinMaxIndex minmaxIndexes = findIndexes(mChartData.getSeries().get(0), startNormalized, endNormalized);

        float[] markerValues = new float[quotes.size() - 1];
        String[] markerColors = new String[quotes.size() - 1];

        float xk = 0;
        if (touchIndex >= 0)
            xk = (int) (((quotes.get(0).getValues().get(touchIndex) - leftMinValue) / (rightMaxValue - leftMinValue)) * W);

        int yMin = H;

        for (int j = 1; j < quotes.size(); j++) {
            if (!quotes.get(j).isChecked()) continue;
            for (int i = minmaxIndexes.min + 1; i < minmaxIndexes.max + 1; i++) {
                //float deltaX = ()
                int x1 = (int) (((quotes.get(0).getValues().get(i - 1) - leftMinValue) / (rightMaxValue - leftMinValue)) * W);
                int x2 = (int) (((quotes.get(0).getValues().get(i) - leftMinValue) / (rightMaxValue - leftMinValue)) * W);

                int y1 = (int) ((1 - quotes.get(j).getValues().get(i - 1) / mChartData.getMaxQuote()) * H);
                int y2 = (int) ((1 - quotes.get(j).getValues().get(i) / mChartData.getMaxQuote()) * H);

                fp.setColor(Color.parseColor(quotes.get(j).getColor()));

                canvas.drawLine(x1, y1, x2, y2, fp);
            }

            if (touchIndex > 0 && touchIndex < quotes.get(j).getValues().size()) {
                markerValues[j - 1] = quotes.get(j).getValues().get(touchIndex);
                markerColors[j - 1] = quotes.get(j).getColor();

                float yk = (float) ((1.0f - quotes.get(j).getValues().get(touchIndex) / mChartData.getMaxQuote()) * H);
                if (yk < yMin && yk > 50)
                    yMin = (int) yk;

                fp.setColor(Color.parseColor(quotes.get(j).getColor()));
                canvas.drawCircle(xk, yk, 10, fp);
                fp.setColor(Color.parseColor("#333333"));
                canvas.drawCircle(xk, yk, 5, fp);

            }
        }

        yMin = yMin - 120;
        if (yMin < 100) yMin = 100;
        if (touchIndex > 0)
            DrawMarker(canvas, markerValues, markerColors, xk + 20, yMin);
    }

    private MinMaxIndex findIndexes(Series values, float start, float end) {
        MinMaxIndex result = new MinMaxIndex();
        result.min = 0;
        result.max = values.getValues().size() - 1;

        leftMinValue = (values.getMaxValue() - values.getMinValue()) * start + values.getMinValue();
        rightMaxValue = (values.getMaxValue() - values.getMinValue()) * end + values.getMinValue();
        realW = (rightMaxValue - leftMinValue) / W;

        for (int i = 0; i < values.getValues().size() - 1; i++) {
            if (values.getValues().get(i) <= leftMinValue && values.getValues().get(i + 1) > leftMinValue)
                result.min = i;
            if (values.getValues().get(i) < rightMaxValue && values.getValues().get(i + 1) >= rightMaxValue)
                result.max = i + 1;
        }

        Log.d(TAG, "findIndexes: [" + values.getMinValue() + ";" + values.getMaxValue() + "] -> [(" + leftMinValue + "," + rightMaxValue + ")] => {" + result.min + "," + result.max + "}");

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
            canvas.drawText(str, 40f, yL - textSize * 0.3f, p);

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
        for (int i = 0; i < values.length; i++) {
            if (colors[i] == null) continue;
            int sz = (int) p.measureText(values[i] + "");
            if (xw < sz) xw = sz;
            activeCounter++;
        }

        paint.setColor(Color.parseColor("#222222"));
        RectF rect = new RectF(
                lastX + Utils.FLOATING_QUOTE_MARGIN_LEFT,
                lastY - H * Utils.FLOATING_QUOTE_MARGIN_TOP_RATIO,
                lastX + Utils.FLOATING_QUOTE_MARGIN_LEFT + xw * Utils.FLOATING_QUOTE_WIDTH_RATIO,
                lastY + H * Utils.FLOATING_QUOTE_MARGIN_BOTTOM_RATIO + (activeCounter - 1) * 105);
        canvas.drawRoundRect(rect, 8, 8, paint);

        int k = 0;
        for (int i = 0; i < values.length; i++) {
            String strFmt = String.format("%%.%df", decimalCount);
            String str = String.format(strFmt, (float) values[i]);
            if (colors[i] == null) continue;
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

    public void animateChanges(final ChartData oldChartData, final ChartData newChartData) {

        post(new Runnable() {
            @Override
            public void run() {
                oldChartData.getNiceScale();
                newChartData.getNiceScale();

                //logging
                Log.d(TAG, "oldChartData [" + oldChartData.getMinQuote() + "; " + oldChartData.getMaxQuote() + "]");
                for (int i = 1; i < oldChartData.getSeries().size(); i++)
                    Log.d(TAG, "run: oldChartData " + oldChartData.getSeries().get(i).getTitle() + ": " + oldChartData.getSeries().get(i).isChecked() + " => " + oldChartData.getSeries().get(i).toString());

                Log.d(TAG, "newChartData [" + newChartData.getMinQuote() + "; " + newChartData.getMaxQuote() + "]");
                for (int i = 1; i < newChartData.getSeries().size(); i++)
                    Log.d(TAG, "run: newChartData " + newChartData.getSeries().get(i).getTitle() + ": " + newChartData.getSeries().get(i).isChecked() + " => " + newChartData.getSeries().get(i).toString());


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
        int startIndex = (int) (startNormalized * mChartData.getSeries().get(0).getValues().size());
        int endIndex = (int) (endNormalized * mChartData.getSeries().get(0).getValues().size());

        //xk = ((quotes.get(0).getValues().get(touchIndex) - leftMinValue) / (rightMaxValue - leftMinValue)) * W;
        touchIndex = (int) (startIndex + xTouched * (endIndex - startIndex) / W);

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
