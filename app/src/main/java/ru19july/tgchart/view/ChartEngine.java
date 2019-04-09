package ru19july.tgchart.view;

import android.animation.ValueAnimator;
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
import android.view.View;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ru19july.tgchart.ICanvas;
import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.data.MinMaxIndex;
import ru19july.tgchart.data.Series;
import ru19july.tgchart.interfaces.IChartTheme;
import ru19july.tgchart.utils.NiceDate;
import ru19july.tgchart.utils.NiceScale;
import ru19july.tgchart.utils.Utils;
import ru19july.tgchart.view.canvas.ChartCanvasView;
import ru19july.tgchart.view.opengl.CubeColorSides;
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

    private FloatBuffer mVertexBuffer = null;
    private ShortBuffer mTriangleBorderIndicesBuffer = null;
    private int mNumOfTriangleBorderIndices = 0;

    public float mAngleX = 0.0f;
    private float mPreviousX;
    private float mPreviousY;
    private final float TOUCH_SCALE_FACTOR = 0.6f;

    private int ticks = 0;

    private void DrawPixels(GL10 gl) {
        //chart
        Log.d(TAG, "DrawPixels: " + mChartData.getSeries().get(0).getValues().size());
        /*if (mVertexBuffer != null) {
            gl.glLoadIdentity();
            gl.glTranslatef(0, 0, 0);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
            gl.glColor4f(1.0f, 1.0f, 0.0f, 1.0f);
            gl.glDrawElements(GL10.GL_LINES, mNumOfTriangleBorderIndices,
                    GL10.GL_UNSIGNED_SHORT, mTriangleBorderIndicesBuffer);
        }*/

        for(int i=0;i < W; i++)
        {
            pixel(gl, i, i / 2, 1f, Color.RED);

        }

        for (int j = 1; j < mChartData.getSeries().size(); j++) {
            for (int i = 0; i < mChartData.getSeries().get(0).getValues().size() - 1; i++) {
                int x1 = (int) (W * (i + 0f) / mChartData.getSeries().get(0).getValues().size());
                int y1 = (int) (H * (mChartData.getSeries().get(j).getValues().get(i) - mChartData.getSeries().get(j).getMinValue() - 0f) / (mChartData.getSeries().get(j).getMaxValue() - mChartData.getSeries().get(j).getMinValue()));

                int x2 = (int) (W * (i + 1f) / mChartData.getSeries().get(0).getValues().size());
                int y2 = (int) (H * (mChartData.getSeries().get(j).getValues().get(i + 1) - mChartData.getSeries().get(j).getMinValue() - 0f) / (mChartData.getSeries().get(j).getMaxValue() - mChartData.getSeries().get(j).getMinValue()));
                pixel(gl, x1, y1, 1f, j < 1 ? Color.BLUE : (j < 2 ? Color.RED : Color.GREEN));
                //line(gl, x, y, x + j*3, y + j*2, j < 1 ? Color.BLUE : (j < 2 ? Color.RED : Color.GREEN));

                //drawLine(gl, x1, y1, x2, y2, 3f, j < 2 ? Color.BLUE : (j < 3 ? Color.RED : Color.GREEN)/*Color.parseColor(mChartData.getSeries().get(j).getColor())*/);
            }
        }

    }

    public void DrawChart(Object canvas) {
        ChartData chartData = mChartData;

        if (chartData == null) return;

        if(canvas instanceof Canvas) {
            ((Canvas)canvas).save();
            W = ((Canvas) canvas).getWidth();
            H = ((Canvas) canvas).getHeight();
        }

        if(canvas instanceof GL10) {
            ((GL10)canvas).glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            ((GL10)canvas).glLoadIdentity();
            //DrawPixels(((GL10)canvas));
            //ticks++;
            //((GL10)canvas).glLoadIdentity();
        }

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

        if(canvas instanceof Canvas)
            ((Canvas)canvas).restore();

        //drawing = false;
        return;//.getCanvas();
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
        Log.d(TAG, "updateSlideFrameWindow: " + startX + " / " + endX);
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

    public ChartData getData() {
        return mChartData;
    }

    public void animateChanges(final View view, ChartData oldChartData, ChartData newChartData) {
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

                view.invalidate();
            }
        });

        va.start();

        view.invalidate();
    }

    public void showChart(final View view, final int k, float from, float to) {
        ValueAnimator va = ValueAnimator.ofFloat(from, to);
        int mDuration = 1000;
        va.setDuration(mDuration);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if(mChartData.getSeries() != null)
                    mChartData.getSeries().get(k).setAlpha((float)animation.getAnimatedValue());
                view.invalidate();
            }
        });
        va.start();
    }


    ///////////////////////////////////
    // Draw methods
    ///////////////////////////////////

    private void drawText(Object canvas, String str, float x, float y, Paint p) {
        if(canvas instanceof Canvas)
            ((Canvas)canvas).drawText(str, x, y, p);
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
        if(canvas instanceof GL10)
            drawLine((GL10)canvas, x1, y1, x2, y2, 1f, fp.getColor());
        //pixel((GL10)canvas, x1, y1, 1f, fp.getColor());

    }

    private void pixel(GL10 gl, int x, int y, float w, int color) {
        x = x - W / 2;
        y = y - H / 2;
        gl.glLoadIdentity();
        Random r = new Random();
        gl.glTranslatef(x, y, 0);
        //gl.glScalef(r.nextFloat()*20f, r.nextFloat()*20f, 1);
        gl.glScalef(w, w, 1);
        new CubeColorSides().draw(gl, color);
    }

    private void drawLine(GL10 g, int x1, int y1, int x2, int y2, float w, int color) {
        int d = 0;
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int dx2 = 2 * dx;
        int dy2 = 2 * dy;
        int ix = x1 < x2 ? 1 : -1;
        int iy = y1 < y2 ? 1 : -1;

        int x = x1;
        int y = y1;

        if (dx >= dy) {
            while (true) {
                pixel(g, x, y, w, color);
                if (x == x2)
                    break;
                x += ix;
                d += dy2;
                if (d > dx) {
                    y += iy;
                    d -= dx2;
                }
            }
        } else {
            while (true) {
                pixel(g, x, y, w, color);
                if (y == y2)
                    break;
                y += iy;
                d += dx2;
                if (d > dy) {
                    x += ix;
                    d -= dy2;
                }
            }
        }
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glEnable(GL10.GL_TEXTURE_2D);            //Enable Texture Mapping ( NEW )
        gl.glShadeModel(GL10.GL_SMOOTH);            //Enable Smooth Shading
        //mTheme.backgroundColor()
        gl.glClearColor(0.0f, .2f, 0.0f, 0.5f); 	//Background
        //gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
        gl.glEnable(GL10.GL_DEPTH_TEST);            //Enables Depth Testing
        gl.glDepthFunc(GL10.GL_LEQUAL);            //The Type Of Depth Testing To Do

        //Really Nice Perspective Calculations
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        W = width;
        H = height;
        Log.d(TAG, "onSurfaceChanged: " + W + "x" + H );

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        gl.glOrthof(-width / 2, width / 2, -height / 2, height / 2, -1000.0f, 1000.0f);
        gl.glShadeModel(GL10.GL_SMOOTH);

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnable(GL10.GL_DEPTH_TEST);


        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LESS);
        gl.glDisable(GL10.GL_DITHER);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
}
