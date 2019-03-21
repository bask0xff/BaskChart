package ru19july.tgchart.view;

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
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.R;
import ru19july.tgchart.data.Series;
import ru19july.tgchart.utils.NiceDate;
import ru19july.tgchart.utils.NiceScale;
import ru19july.tgchart.utils.Utils;
import ru19july.tgchart.view.theme.IChartTheme;

public class ChartViewTg extends View implements View.OnTouchListener {

    private final String TAG = ChartViewTg.class.getSimpleName();

    private float mScaleFactor = 1.f;
    Paint paint;

    private int W, ChartLineWidth, H;

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
    private IChartTheme mTheme;

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
        canvas.save();
        canvas.scale(mScaleFactor, mScaleFactor);

        W = canvas.getWidth();
        H = canvas.getHeight();
        ChartLineWidth = canvas.getWidth() * 25 / 40;//место, где заканчивается график

        PrepareCanvas(canvas);

        canvas.restore();
    }

    private int GetX(double x) {
        return (int) (((x - leftMinValue) / (rightMaxValue - leftMinValue)) * W * 1f + W * 0.0f);
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

        fp.setColor(Color.parseColor(mTheme.backgroundColor()));

        canvas.drawRect(0, 0, W, H, fp);

        if (mChartData.getSeries().get(0).getValues().size() > 0) {

            NiceScale numScaleV = mChartData.getNiceScale(leftMinValue, rightMaxValue);
            DrawHorizontalLines(numScaleV, decimalCount, canvas);

            NiceDate numScaleH = new NiceDate(leftMinValue, rightMaxValue);
            DrawVerticalLines(numScaleH, canvas);

            DrawChart(mChartData.getSeries(), canvas);
        }

        drawing = false;
        return canvas;
    }

    private void DrawChart(List<Series> series, Canvas canvas) {
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
        mPath.moveTo(xk, H* 0.f);
        mPath.quadTo(xk, H/2, xk, H * 0.8f);
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(false);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(3f);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setPathEffect(new DashPathEffect(new float[]{1, 1}, 0));
        canvas.drawPath(mPath, mPaint);

        for (int j = 1; j < series.size(); j++) {
            if (!series.get(j).isChecked()) continue;
            for (int i = minmaxIndexes.min + 1; i < minmaxIndexes.max + 1; i++) {
                //float deltaX = ()
                int x1 = GetX(series.get(0).getValues().get(i - 1));
                int x2 = GetX(series.get(0).getValues().get(i));

                int y1 = (int) GetY(series.get(j).getValues().get(i - 1));
                int y2 = (int) GetY(series.get(j).getValues().get(i));

                fp.setColor(Color.parseColor(series.get(j).getColor()));
                fpc.setColor(Color.parseColor(series.get(j).getColor()));

                canvas.drawLine(x1, y1, x2, y2, fp);
                canvas.drawCircle(x1, y1, 2.0f, fpc);
            }

            if (touchIndex >= 0 && touchIndex < series.get(j).getValues().size()) {
                timestamp = series.get(0).getValues().get(touchIndex);
                markerValues[j - 1] = series.get(j).getValues().get(touchIndex);
                markerColors[j - 1] = series.get(j).getColor();

                float yk = GetY(series.get(j).getValues().get(touchIndex));

                if (yk < yMin && yk > 50)
                    yMin = (int) yk;

                fp.setColor(Color.parseColor(series.get(j).getColor()));
                canvas.drawCircle(xk, yk, 10, fp);
                fp.setColor(Color.parseColor(mTheme.backgroundColor()));
                canvas.drawCircle(xk, yk, 5, fp);
            }
        }

        yMin = 50;
        if (touchIndex > 0)
            DrawMarker(canvas, timestamp, markerValues, markerColors, xk + 20, yMin);
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

        //result.min--;
        //result.max++;

        if (result.min < 0) result.min = 0;
        if (result.max >= values.getValues().size()) result.max = values.getValues().size() - 1;

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

    private void DrawVerticalLines(NiceDate numScale, Canvas canvas) {
        double xLine = numScale.niceMin;

        while (xLine <= numScale.niceMax) {
            float xL = GetX(xLine);

            Path mPath = new Path();
            mPath.moveTo(xL, H* 0.2f);
            mPath.quadTo(xL, H/2, xL, H * 0.8f);
            Paint mPaint = new Paint();
            mPaint.setAntiAlias(false);
            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setPathEffect(new DashPathEffect(new float[]{1, 1}, 0));
            //canvas.drawPath(mPath, mPaint);

            String str = convertTime((long)xLine, "MMM dd");
            Paint p = new Paint();
            float textSize = H * 0.033f;
            int xw = (int) p.measureText(str);
            p.setTextSize(textSize);
            p.setAntiAlias(true);
            p.setColor(Utils.NICESCALE_TEXT_COLOR);
            canvas.drawText(str, xL - xw, H* 0.85f, p);

            xLine += numScale.tickSpacing;
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

    public String convertTime(long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("MMM dd HH:mm", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(date);
    }

    public String convertTime(long time, String fmt) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat(fmt, Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(date);
    }

    private void DrawMarker(Canvas canvas, long timestamp, float[] values, String[] colors, float lastX, float lastY) {
        int decimalCount = 0;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);

        //floating quote
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setFakeBoldText(true);
        p.setStrokeWidth(1);

        String dat = convertTime(timestamp);

        //calculate text width
        p.setTextSize(H * Utils.FLOATING_QUOTE_TEXT_SIZE_RATIO);
        int xw = (int) p.measureText(dat);
        int activeCounter = 0;
        for (int i = 0; i < values.length; i++) {
            if (colors[i] == null) continue;
            int sz = (int) p.measureText(values[i] + "");
            if (xw < sz) xw = sz;
            activeCounter++;
        }

        int leftX = (int) (lastX + Utils.FLOATING_QUOTE_MARGIN_LEFT);
        int rightX = (int) (leftX + xw * Utils.FLOATING_QUOTE_WIDTH_RATIO);
        if(rightX > W) {
            rightX = W - 30;
            leftX = (int) (rightX - xw * Utils.FLOATING_QUOTE_WIDTH_RATIO);
        }

        //TODO hide on click legend
        paint.setColor(Color.parseColor(mTheme.backgroundColor()));
        RectF rect = new RectF(
                leftX,
                lastY - H * Utils.FLOATING_QUOTE_MARGIN_TOP_RATIO,
                rightX,
                lastY + H * Utils.FLOATING_QUOTE_MARGIN_BOTTOM_RATIO + (activeCounter) * 105);

        canvas.drawRoundRect(rect, 8, 8, paint);

        //date
        p.setColor(Color.parseColor(mTheme.markerFontColor()));
        canvas.drawText(dat, leftX + 50, lastY + 16, p);

        int k = 0;
        for (int i = 0; i < values.length; i++) {
            String strFmt = String.format("%%.%df", decimalCount);
            String str = String.format(strFmt, (float) values[i]);
            if (colors[i] == null) continue;
            p.setColor(Color.parseColor(colors[i]));
            canvas.drawText(str, leftX + 50, lastY + 16 + (k + 1) * 80, p);
            k++;
        }
    }

    public Bitmap addShadow(final Bitmap bm, final int dstHeight, final int dstWidth, int color, int size, float dx, float dy) {
        final Bitmap mask = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ALPHA_8);

        final Matrix scaleToFit = new Matrix();
        final RectF src = new RectF(0, 0, bm.getWidth(), bm.getHeight());
        final RectF dst = new RectF(0, 0, dstWidth - dx, dstHeight - dy);
        scaleToFit.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);

        final Matrix dropShadow = new Matrix(scaleToFit);
        dropShadow.postTranslate(dx, dy);

        final Canvas maskCanvas = new Canvas(mask);
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskCanvas.drawBitmap(bm, scaleToFit, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        maskCanvas.drawBitmap(bm, dropShadow, paint);

        final BlurMaskFilter filter = new BlurMaskFilter(size, BlurMaskFilter.Blur.NORMAL);
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setMaskFilter(filter);
        paint.setFilterBitmap(true);

        final Bitmap ret = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);
        final Canvas retCanvas = new Canvas(ret);
        retCanvas.drawBitmap(mask, 0, 0, paint);
        retCanvas.drawBitmap(bm, scaleToFit, null);
        mask.recycle();
        return ret;
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

    public void drawText(String text) {
    }

    public void setTheme(IChartTheme theme) {
        mTheme = theme;

        updateTheme();
    }

    private void updateTheme() {
        invalidate();
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

        xTouched = event.getX();
        int startIndex = (int) (startNormalized * mChartData.getSeries().get(0).getValues().size());
        int endIndex = (int) (endNormalized * mChartData.getSeries().get(0).getValues().size());

        //xk = ((quotes.get(0).getValues().get(touchIndex) - leftMinValue) / (rightMaxValue - leftMinValue)) * W;
        //touchIndex = (int) (startIndex + xTouched * (endIndex - startIndex) / W);

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

        invalidate();

        return true;
    }

}

//try it
//http://www.java2s.com/Code/Android/2D-Graphics/DrawPolygon.htm
