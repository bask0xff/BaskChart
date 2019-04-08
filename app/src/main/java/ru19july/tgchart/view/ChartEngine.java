package ru19july.tgchart.view;

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
import android.util.Log;
import android.view.MotionEvent;

import java.util.Date;
import java.util.List;
import java.util.Random;

import ru19july.tgchart.ICanvas;
import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.data.MinMaxIndex;
import ru19july.tgchart.data.Series;
import ru19july.tgchart.interfaces.IChartTheme;
import ru19july.tgchart.utils.NiceDate;
import ru19july.tgchart.utils.NiceScale;
import ru19july.tgchart.utils.Utils;
import ru19july.tgchart.view.theme.DarkTheme;

public class ChartEngine {

    private static final String TAG = ChartEngine.class.getSimpleName();
    private int W, H;

    Paint paint;
    Random r = new Random();

    private boolean drawing = false;
    private float xStart = 50.0f;
    private float xEnd = 450.0f;
    private IChartTheme mTheme = new DarkTheme();

    float startNormalized = 0.0f;
    float endNormalized = 0.0f;
    private float xTouched = 0.0f;
    int touchIndex = -1;
    private int oldTouchIndex = -111;
    private float realW = 1.0f;
    private float leftMinValue = 0;
    private float rightMaxValue = 1;
    private boolean mShowVerticalLines = false;
    private String themeName;
    private ChartData mChartData;

    public Canvas DrawChart(Object canvas, ChartData chartData) {
        mChartData = chartData;

        ((Canvas)canvas).save();

        if(canvas instanceof Canvas) {
            W = ((Canvas) canvas).getWidth();
            H = ((Canvas) canvas).getHeight();
        }

        if (chartData == null) return ((Canvas)canvas);//.getCanvas();

        int decimalCount = Utils.DEFAULT_DECIMAL_COUNT;

        long ms = (new Date()).getTime();

        //canvas.drawRect( 0, 0, W, H, Color.parseColor(mTheme.backgroundColor()));

        if (chartData.getSeries().get(0).getValues().size() > 0) {

            NiceScale numScaleV = chartData.getNiceScale(leftMinValue, rightMaxValue);
            DrawHorizontalLines(numScaleV, decimalCount, canvas);

            NiceDate numScaleH = new NiceDate(leftMinValue, rightMaxValue);
            DrawVerticalLines(numScaleH, canvas);

            DrawChart(chartData.getSeries(), canvas);
        }

        ((Canvas)canvas).restore();
        //drawing = false;
        return ((Canvas)canvas);//.getCanvas();
    }


    private int GetX(double x) {
        return (int) (((x - leftMinValue) / (rightMaxValue - leftMinValue)) * W * 1f + W * 0.0f);
    }

    private float GetY(double y, float scale) {
        float realY = 0;
        if ((mChartData.getMaxValue() - mChartData.getMinValue()) > 0)
            realY = (float) (H * (1 - 0.2 - 0.6 * scale * (y - mChartData.getMinValue()) / (mChartData.getMaxValue() - mChartData.getMinValue())));
        return realY;
    }

    private void DrawChart(List<Series> series, Object canvas) {
        Paint lp = new Paint();
        lp.setAntiAlias(false);
        lp.setStrokeWidth(1);
        lp.setPathEffect(new DashPathEffect(new float[]{1, 1}, 0));

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setColor(Color.BLUE);

        Paint fp = new Paint();
        fp.setAntiAlias(true);
        fp.setStyle(Paint.Style.FILL_AND_STROKE);
        fp.setStrokeWidth(5.0f);
        Paint fpc = new Paint();
        fpc.setAntiAlias(true);
        fpc.setStyle(Paint.Style.FILL_AND_STROKE);
        fpc.setStrokeWidth(1.0f);

        Random r = new Random();

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        MinMaxIndex minmaxIndexes = findIndexes(mChartData.getSeries().get(0), startNormalized, endNormalized);

        float[] markerValues = new float[series.size() - 1];
        String[] markerColors = new String[series.size() - 1];
        long timestamp = 0L;

        float xk = 0;
        if (touchIndex >= 0)
            xk = GetX(series.get(0).getValues().get(touchIndex));

        int yMin = H;

        Path mPath = new Path();
        mPath.moveTo(xk, H * 0.f);
        mPath.quadTo(xk, H / 2, xk, H * 0.8f);
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(false);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(3f);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setPathEffect(new DashPathEffect(new float[]{1, 1}, 0));

        drawPath(canvas, mPath, mPaint);

        for (int j = 1; j < series.size(); j++) {
            //if (!series.get(j).isChecked()) continue;
            for (int i = minmaxIndexes.min + 1; i < minmaxIndexes.max + 1; i++) {
                //float deltaX = ()
                int x1 = GetX(series.get(0).getValues().get(i - 1));
                int x2 = GetX(series.get(0).getValues().get(i));

                int y1 = (int) GetY(series.get(j).getValues().get(i - 1), series.get(j).getScale());
                int y2 = (int) GetY(series.get(j).getValues().get(i), series.get(j).getScale());

                fp.setColor(Color.parseColor(series.get(j).getColor()));
                fpc.setColor(Color.parseColor(series.get(j).getColor()));

                fp.setAlpha((int) (series.get(j).getAlpha() * 255));
                fpc.setAlpha((int) (series.get(j).getAlpha() * 255));

                drawLine(canvas, x1, y1, x2, y2, fp);
                if(series.get(j).getAlpha() > 0.95)
                    drawCircle(canvas, x1, y1, 2.0f, fpc);
            }

            if (touchIndex >= 0 && touchIndex < series.get(j).getValues().size()) {
                timestamp = series.get(0).getValues().get(touchIndex);
                markerValues[j - 1] = series.get(j).getValues().get(touchIndex);
                markerColors[j - 1] = series.get(j).getColor();

                float yk = GetY(series.get(j).getValues().get(touchIndex), series.get(j).getScale());

                if (yk < yMin && yk > 50)
                    yMin = (int) yk;

                if(series.get(j).isChecked()) {
                    fp.setColor(Color.parseColor(series.get(j).getColor()));
                    drawCircle(canvas, xk, yk, 10f, fp);
                    fp.setColor(Color.parseColor(mTheme.backgroundColor()));
                    drawCircle(canvas, xk, yk, 5f, fp);
                }
            }
        }

        yMin = 50;
        if (touchIndex > 0)
            DrawMarker(canvas, timestamp, markerValues, markerColors, xk + 20, yMin);
    }

    private void drawText(Object canvas, String str, float x, float y, Paint p) {
        if(canvas instanceof Canvas)
            ((Canvas)canvas).drawText(str, x, y,  p);
    }

    private void drawRoundRect(Object canvas, RectF rect, int x, int y, Paint paint) {
        if(canvas instanceof Canvas)
            ((Canvas)canvas).drawRoundRect(rect, x, y,  paint);
    }


    private void drawPath(Object canvas, Path mPath, Paint mPaint) {
        if(canvas instanceof Canvas)
            ((Canvas)canvas).drawPath(mPath, mPaint);
    }

    private void drawCircle(Object canvas, float x1, float y1, float v, Paint fp) {
        if(canvas instanceof Canvas)
            ((Canvas)canvas).drawCircle(x1, y1, v, fp);
    }

    private void drawLine(Object canvas, int x1, int y1, int x2, int y2, Paint fp) {
        if(canvas instanceof Canvas)
            ((Canvas)canvas).drawLine(x1, y1, x2, y2, fp);
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

        if (result.min < 0) result.min = 0;
        if (result.max >= values.getValues().size()) result.max = values.getValues().size() - 1;

        return result;
    }

    private void DrawHorizontalLines(NiceScale numScale, int decimalCount, Object canvas) {
        //drawing horizontal lines
        double yLine = numScale.niceMin;
        while (yLine <= numScale.niceMax) {
            float yL = GetY(yLine, 1f);

            Path mPath = new Path();
            mPath.moveTo(0, yL);
            mPath.quadTo(W / 2, yL, W, yL);
            Paint mPaint = new Paint();
            mPaint.setAntiAlias(false);
            //mPaint.setColor(Utils.MARKER_BG_COLOR);
            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setPathEffect(new DashPathEffect(new float[]{1, 1}, 0));
            drawPath(canvas, mPath, mPaint);

            String strFmt = String.format("%%.%df", decimalCount);
            String str = String.format(strFmt, (float) yLine);

            Paint p = new Paint();
            float textSize = H * 0.033f;
            int xw = (int) p.measureText(str);
            p.setTextSize(textSize);
            p.setAntiAlias(true);
            p.setColor(Color.parseColor(mTheme.fontColor()));
            drawText(canvas, str, 40f, yL - textSize * 0.3f, p);

            yLine += numScale.tickSpacing;
        }
    }

    private void DrawVerticalLines(NiceDate numScale, Object canvas) {
        double xLine = numScale.niceMin;

        while (xLine <= numScale.niceMax) {
            float xL = GetX(xLine);

            if(mShowVerticalLines) {
                Path mPath = new Path();
                mPath.moveTo(xL, H * 0.2f);
                mPath.quadTo(xL, H / 2, xL, H * 0.8f);
                Paint mPaint = new Paint();
                mPaint.setAntiAlias(false);
                mPaint.setColor(Color.BLACK);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setPathEffect(new DashPathEffect(new float[]{1, 1}, 0));
                drawPath(canvas, mPath, mPaint);
            }

            String str = Utils.unixtimeToString((long)xLine, "MMM dd");
            Paint p = new Paint();
            float textSize = H * 0.033f;
            int xw = (int) p.measureText(str);
            p.setTextSize(textSize);
            p.setAntiAlias(true);
            p.setColor(Color.parseColor(mTheme.fontColor()));
            drawText(canvas, str, xL - xw, H* 0.85f, p);

            xLine += numScale.tickSpacing;
        }
    }

    private void DrawMarker(Object canvas, long timestamp, float[] values, String[] colors, float lastX, float lastY) {
        int decimalCount = 0;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setFakeBoldText(true);
        p.setStrokeWidth(1);

        String dat = Utils.convertTime(timestamp);

        //calculate text width
        p.setTextSize(H * Utils.FLOATING_TEXT_SIZE_RATIO);
        int xw = (int) p.measureText(dat);
        int activeCounter = 0;
        for (int i = 0; i < values.length; i++) {
            if (colors[i] == null) continue;
            int sz = (int) p.measureText(values[i] + "");
            if (xw < sz) xw = sz;
            activeCounter++;
        }

        int leftX = (int) (lastX + Utils.FLOATING_MARGIN_LEFT);
        int rightX = (int) (leftX + xw * Utils.FLOATING_WIDTH_RATIO);
        if(rightX > W) {
            rightX = W - 30;
            leftX = (int) (rightX - xw * Utils.FLOATING_WIDTH_RATIO);
        }

        //TODO hide on click legend
        paint.setColor(Color.parseColor(mTheme.backgroundColor()));
        RectF rect = new RectF(
                leftX,
                lastY - H * 1f/15f,
                rightX,
                lastY + H * Utils.FLOATING_MARGIN_BOTTOM_RATIO + (activeCounter) * 105);

        drawRoundRect(canvas, rect, 8, 8, paint);

        //date
        p.setColor(Color.parseColor(mTheme.markerFontColor()));
        drawText(canvas, dat, leftX + 50, lastY + 16, p);

        int k = 0;
        for (int i = 0; i < values.length; i++) {
            String strFmt = String.format("%%.%df", decimalCount);
            String str = String.format(strFmt, (float) values[i]);
            if (colors[i] == null) continue;
            p.setColor(Color.parseColor(colors[i]));
            drawText(canvas, str, leftX + 50, lastY + 16 + (k + 1) * 80, p);
            k++;
        }
    }

    public void updateSlideFrameWindow(int startX, int endX) {
        xStart = startX;
        xEnd = endX;

        startNormalized = (xStart + 0.f) / W;
        endNormalized = (xEnd + 0.f) / W;
    }

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

    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent: " + mChartData);
        if(mChartData == null) return false;

        xTouched = event.getX();
        int startIndex = (int) (startNormalized * mChartData.getSeries().get(0).getValues().size());
        int endIndex = (int) (endNormalized * mChartData.getSeries().get(0).getValues().size());

        float min = Float.MAX_VALUE;
        touchIndex = 0;

        for (int i = startIndex; i < endIndex; i++) {
            float xt = ((mChartData.getSeries().get(0).getValues().get(i) - leftMinValue) / (rightMaxValue - leftMinValue)) * W;
            if (Math.abs(xt - xTouched) <= min) {
                min = Math.abs(xt - xTouched);
                touchIndex = i;
            }
        }

        oldTouchIndex = touchIndex;

        return true;
    }
}
