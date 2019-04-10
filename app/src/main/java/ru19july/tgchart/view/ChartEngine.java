package ru19july.tgchart.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.data.MinMaxIndex;
import ru19july.tgchart.data.Series;
import ru19july.tgchart.interfaces.IChartTheme;
import ru19july.tgchart.utils.NiceDate;
import ru19july.tgchart.utils.NiceScale;
import ru19july.tgchart.utils.Utils;
import ru19july.tgchart.view.opengl.CubeColorSides;
import ru19july.tgchart.view.theme.DarkTheme;

import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static javax.microedition.khronos.opengles.GL10.GL_FLOAT;
import static javax.microedition.khronos.opengles.GL10.GL_LINES;
import static javax.microedition.khronos.opengles.GL10.GL_VERTEX_ARRAY;

public class ChartEngine {

    private static final String TAG = ChartEngine.class.getSimpleName();
    private final Context mContext;
    private int W, H;

    Paint paint;
    Random r = new Random();

    private boolean drawing = false;
    private float xStart = 0.0f;
    private float xEnd = 0.0f;
    private IChartTheme mTheme = new DarkTheme();

    float startNormalized = 0.0f;
    float endNormalized = 0.0f;
    private float catchX = 0.0f;
    private float xTouched = 0.0f;
    private float yTouched = 0.0f;
    int touchIndex = -1;
    private int oldTouchIndex = -111;
    private float realW = 1.0f;
    private float leftMinValue = 0;
    private float rightMaxValue = 1;
    private boolean mShowVerticalLines = false;
    private String themeName;
    private ChartData mChartData;

    private float chartYstartsFactor = .2f;
    private float chartYfinishFactor = .5f;
    private float chartYendsFactor = .8f;
    private float textYFactor = 0.82f;
    private float textAxisSize = 0.033f;
    private float sliderYfactor = textYFactor + textAxisSize + 0.01f;
    private boolean catchedLeft = false;
    private boolean catchedRight = false;
    private boolean movingSlider = false;
    private float xStartTouched = 0.0f;
    private float xEndTouched = 0.0f;
    private float xMoveTouched = 0.0f;

    public ChartEngine(Context ctx) {
        mContext = ctx;
    }

    public void DrawChart(Object canvas) {
        ChartData chartData = mChartData;

        if (chartData == null) return;

        if (canvas instanceof Canvas) {
            ((Canvas) canvas).save();
            W = ((Canvas) canvas).getWidth();
            H = ((Canvas) canvas).getHeight();
        }
        if (xEnd < 1) xEnd = W;

        if (canvas instanceof GL10) {
            ((GL10) canvas).glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            ((GL10) canvas).glLoadIdentity();
            //DrawPixels(((GL10)canvas));
            //ticks++;
            //((GL10)canvas).glLoadIdentity();
        }

        int decimalCount = Utils.DEFAULT_DECIMAL_COUNT;

        setBackground(canvas, mTheme.backgroundColor());

        if (chartData.getSeries().get(0).getValues().size() > 0) {

            NiceScale numScaleV = chartData.getNiceScale(leftMinValue, rightMaxValue);
            DrawHorizontalLines(numScaleV, decimalCount, canvas);

            NiceDate numScaleH = new NiceDate(leftMinValue, rightMaxValue);
            DrawVerticalLines(numScaleH, canvas);

            DrawChart(chartData.getSeries(), canvas);
        }

        drawSlider(canvas);

        if (canvas instanceof Canvas)
            ((Canvas) canvas).restore();

        //drawing = false;
        return;//.getCanvas();
    }

    private int GetX(double x) {
        return (int) (((x - leftMinValue) / (rightMaxValue - leftMinValue)) * W * 1f + W * 0.0f);
    }

    private float GetY(double y, float scale) {
        float realY = 0;
        if ((mChartData.getMaxValue() - mChartData.getMinValue()) > 0)
            realY = (float) (H * (1 - chartYstartsFactor - chartYfinishFactor * scale * (y - mChartData.getMinValue()) / (mChartData.getMaxValue() - mChartData.getMinValue())));
        return realY;
    }

    private boolean slideMoving = false;
    private float startMoveX = 0.0f;
    private float xStartSaved = 0.0f;
    private float xEndSaved = 0.f;

    private void drawSlider(Object canvas) {
        //left part
        drawRect(canvas, 0, H * sliderYfactor, xStart, H, Color.parseColor(mTheme.sliderBackground()), Color.alpha(Color.parseColor(mTheme.sliderBackground())));

        //right part
        drawRect(canvas, xEnd, H * sliderYfactor, W, H, Color.parseColor(mTheme.sliderBackground()), Color.alpha(Color.parseColor(mTheme.sliderBackground())));

        //slider window
        drawRect(canvas, xStart + 16, H * sliderYfactor + 4, xEnd - 16, H - 4, Color.parseColor(mTheme.sliderInner()), Color.alpha(Color.parseColor(mTheme.sliderInner())));
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
        mPath.quadTo(xk, H / 2, xk, H * chartYendsFactor);
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(false);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(3f);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setPathEffect(new DashPathEffect(new float[]{1, 1}, 0));

        drawPath(canvas, mPath, mPaint);

        for (int j = 1; j < series.size(); j++) {
            //if (!series.get(j).isChecked()) continue;
            fp.setColor(Color.parseColor(series.get(j).getColor()));
            fpc.setColor(Color.parseColor(series.get(j).getColor()));

            float normalizator = 1f;
            if (canvas instanceof Canvas)
                normalizator = 255;

            fp.setAlpha((int) (series.get(j).getAlpha() * normalizator));
            fpc.setAlpha((int) (series.get(j).getAlpha() * normalizator));

            for (int i = minmaxIndexes.min + 1; i < minmaxIndexes.max + 1; i++) {
                //float deltaX = ()
                int x1 = GetX(series.get(0).getValues().get(i - 1));
                int x2 = GetX(series.get(0).getValues().get(i));

                int y1 = (int) GetY(series.get(j).getValues().get(i - 1), series.get(j).getScale());
                int y2 = (int) GetY(series.get(j).getValues().get(i), series.get(j).getScale());

                drawLine(canvas, x1, y1, x2, y2, fp.getColor(), series.get(j).getAlpha());
                if (series.get(j).getAlpha() > 0.95)
                    drawCircle(canvas, x1, y1, 2.0f, fpc);
            }

            if (touchIndex >= 0 && touchIndex < series.get(j).getValues().size()) {
                timestamp = series.get(0).getValues().get(touchIndex);
                markerValues[j - 1] = series.get(j).getValues().get(touchIndex);
                markerColors[j - 1] = series.get(j).getColor();

                float yk = GetY(series.get(j).getValues().get(touchIndex), series.get(j).getScale());

                if (yk < yMin && yk > 50)
                    yMin = (int) yk;

                if (series.get(j).isChecked()) {
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

            if (mShowVerticalLines) {
                Path mPath = new Path();
                mPath.moveTo(xL, H * 0.2f);
                mPath.quadTo(xL, H / 2, xL, H * chartYendsFactor);
                Paint mPaint = new Paint();
                mPaint.setAntiAlias(false);
                mPaint.setColor(Color.BLACK);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setPathEffect(new DashPathEffect(new float[]{1, 1}, 0));
                drawPath(canvas, mPath, mPaint);
            }

            String str = Utils.unixtimeToString((long) xLine, "MMM dd");
            Paint p = new Paint();
            float textSize = H * textAxisSize;
            int xw = (int) p.measureText(str);
            p.setTextSize(textSize);
            p.setAntiAlias(true);
            p.setColor(Color.parseColor(mTheme.fontColor()));
            drawText(canvas, str, xL - xw, H * textYFactor, p);

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
        if (rightX > W) {
            rightX = W - 30;
            leftX = (int) (rightX - xw * Utils.FLOATING_WIDTH_RATIO);
        }

        //TODO hide on click legend
        paint.setColor(Color.parseColor(mTheme.legendBackgroundColor()));
        RectF rect = new RectF(
                leftX,
                lastY - H * 1f / 15f,
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
                if (mChartData.getSeries() != null)
                    mChartData.getSeries().get(k).setAlpha((float) animation.getAnimatedValue());
                view.invalidate();
            }
        });
        va.start();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (mChartData == null) return false;

        xTouched = event.getX();
        yTouched = event.getY();

        int startIndex = (int) (startNormalized * mChartData.getSeries().get(0).getValues().size());
        int endIndex = (int) (endNormalized * mChartData.getSeries().get(0).getValues().size());

        if (yTouched >= H * sliderYfactor && event.getAction() == MotionEvent.ACTION_DOWN) {

            int leftDist = (int) Math.abs(xStart - xTouched);
            int rightDist = (int) Math.abs(xEnd - xTouched);

            catchedLeft = leftDist < 150;
            catchedRight = rightDist < 150;

            if (catchedLeft) catchedRight = false;
            if (catchedRight) catchedLeft = false;

            if (catchedLeft)
                xStartTouched = xTouched;
            if (catchedRight)
                xEndTouched = xTouched;

            if (!catchedLeft && !catchedRight) {
                if (xTouched < xEnd && xTouched > xStart) {
                    movingSlider = true;
                    xMoveTouched = xTouched;
                    xStartSaved = xStart;
                    xEndSaved = xEnd;
                }
            }

            catchX = xTouched;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN && isLegend(xTouched, yTouched)) {

        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (catchedLeft) {
                if (Math.abs(xTouched - xEnd) > 100 && xTouched < xEnd)
                    xStart = xTouched;
            }
            if (catchedRight) {
                if (Math.abs(xTouched - xStart) > 100 && xTouched > xStart)
                    xEnd = xTouched;
            }
            if (movingSlider) {
                xStart = xStartSaved + (xTouched - xMoveTouched);
                xEnd = xEndSaved + (xTouched - xMoveTouched);
            }

            if (xStart < 0) xStart = 0;
            if (xEnd > W) xEnd = W;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            movingSlider = false;
            catchedLeft = false;
            catchedRight = false;
        }

        startNormalized = (xStart + 0.f) / W;
        endNormalized = (xEnd + 0.f) / W;

        if (yTouched >= H * sliderYfactor) return true;

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

    private boolean isLegend(float x, float y) {
        return false;
    }


    ///////////////////////////////////
    // Draw methods
    ///////////////////////////////////

    private void drawText(Object canvas1, String str, float x, float y, Paint p) {
        if (canvas1 instanceof Canvas)
            ((Canvas) canvas1).drawText(str, x, y, p);
        if (canvas1 instanceof GL10) {
        }
    }

    private void drawRoundRect(Object canvas, RectF rect, int x, int y, Paint paint) {
        if (canvas instanceof Canvas)
            ((Canvas) canvas).drawRoundRect(rect, x, y, paint);
    }

    private void drawPath(Object canvas, Path mPath, Paint mPaint) {
        if (canvas instanceof Canvas)
            ((Canvas) canvas).drawPath(mPath, mPaint);
    }

    private void drawCircle(Object canvas, float x1, float y1, float v, Paint fp) {
        if (canvas instanceof Canvas)
            ((Canvas) canvas).drawCircle(x1, y1, v, fp);
    }

    private void drawRect(Object canvas, float x1, float y1, float x2, float y2, int color, int alpha) {
        if (canvas instanceof Canvas) {
            Paint fp = new Paint();
            fp.setColor(color);
            fp.setStyle(Paint.Style.FILL_AND_STROKE);
            ((Canvas) canvas).drawRect(x1, y1, x2, y2, fp);
        }
        if (canvas instanceof GL10) {
            drawRectGL((GL10) canvas, x1, y1, x2, y2, color, alpha / 255f);
        }
    }

    private void drawLine(Object canvas, int x1, int y1, int x2, int y2, int color, float alpha) {
        if (canvas instanceof Canvas) {
            Paint fp = new Paint();
            fp.setColor(color);
            fp.setAlpha((int) (alpha * 255));
            fp.setAntiAlias(true);
            fp.setStyle(Paint.Style.FILL_AND_STROKE);
            fp.setStrokeWidth(5.0f);

            ((Canvas) canvas).drawLine(x1, y1, x2, y2, fp);
        }
        if (canvas instanceof GL10) {
            drawLineGL((GL10) canvas, x1, H - y1, x2, H - y2, 1f, color, alpha);
            //pixel((GL10)canvas, x1, H-y1, 1f, fp.getColor(), fp.getAlpha());
        }
    }

    //// OpenGL

    private void setBackground(Object canvas, String backgroundColor) {
        if (canvas instanceof GL10) {
            ((GL10) canvas).glEnable(GL10.GL_TEXTURE_2D);            //Enable Texture Mapping ( NEW )
            ((GL10) canvas).glShadeModel(GL10.GL_SMOOTH);            //Enable Smooth Shading
            //mTheme.backgroundColor()
            float r = ((Color.parseColor(backgroundColor) >> 16) & 0xff) / 255f;
            float g = ((Color.parseColor(backgroundColor) >> 8) & 0xff) / 255f;
            float b = ((Color.parseColor(backgroundColor) >> 0) & 0xff) / 255f;

            ((GL10) canvas).glClearColor(r, g, b, 1f);    //Background
            //gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
            ((GL10) canvas).glEnable(GL10.GL_DEPTH_TEST);            //Enables Depth Testing
            ((GL10) canvas).glDepthFunc(GL10.GL_LEQUAL);            //The Type Of Depth Testing To Do

            //Really Nice Perspective Calculations
            ((GL10) canvas).glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        }
    }

    private void pixel(GL10 gl, int x, int y, float w, float h, int color, int alpha) {
        x = x - W / 2;
        y = -(y - H / 2);
        gl.glLoadIdentity();
        gl.glTranslatef(x, y, 0);
        gl.glScalef(w, h, 1);
        new CubeColorSides().draw(gl, color, alpha);
        gl.glLoadIdentity();
    }

    private void drawRectGL(GL10 gl, float x1, float y1, float x2, float y2, int color, float alpha) {
        x1 = x1 - W / 2;
        y1 = (y1 - H / 2) - H * .9f;
        x2 = x2 - W / 2;
        y2 = (y2 - H / 2) - H * .9f;
        gl.glLoadIdentity();
        gl.glTranslatef(x1, y1, 0);
        gl.glScalef(x2 - x1, y2 - y1, 0);
        new CubeColorSides().draw(gl, color, alpha);
    }

    private void drawLineGL(GL10 gl, int x1, int y1, int x2, int y2, float w, int color, float alpha) {
        float vertices[] = {x1 - W / 2, y1 - H / 2, x2 - W / 2, y2 - H / 2};

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder()); // Use native byte order
        FloatBuffer vertexBuffer = vbb.asFloatBuffer(); // Convert from byte to float
        vertexBuffer.put(vertices);         // Copy data into buffer
        vertexBuffer.position(0);

        float r = ((color >> 16) & 0xff) / 255f;
        float g = ((color >> 8) & 0xff) / 255f;
        float b = ((color >> 0) & 0xff) / 255f;

        gl.glEnableClientState(GL_VERTEX_ARRAY);
        gl.glVertexPointer(2, GL_FLOAT, 0, vertexBuffer);
        gl.glColor4f(r, g, b, alpha);
        gl.glLineWidth(5f);
        gl.glDrawArrays(GL_LINES, 0, 2);
    }

    private void drawBresenhamsLine(GL10 g, int x1, int y1, int x2, int y2, float w, int color, int alpha) {
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
                pixel(g, x, y, w, w, color, alpha);
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
                pixel(g, x, y, w, w, color, alpha);
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
        float r = ((Color.parseColor(mTheme.backgroundColor()) >> 16) & 0xff) / 255f;
        float g = ((Color.parseColor(mTheme.backgroundColor()) >> 8) & 0xff) / 255f;
        float b = ((Color.parseColor(mTheme.backgroundColor()) >> 0) & 0xff) / 255f;

        gl.glClearColor(r, g, b, 1f);    //Background
        //gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
        gl.glEnable(GL10.GL_DEPTH_TEST);            //Enables Depth Testing
        gl.glDepthFunc(GL10.GL_LEQUAL);            //The Type Of Depth Testing To Do

        //Really Nice Perspective Calculations
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        W = width;
        H = height;
        Log.d(TAG, "onSurfaceChanged: " + W + "x" + H);

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