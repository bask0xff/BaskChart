package ru19july.baskchart.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ru19july.baskchart.data.ChartData;
import ru19july.baskchart.data.MinMaxIndex;
import ru19july.baskchart.data.Series;
import ru19july.baskchart.interfaces.IChartTheme;
import ru19july.baskchart.utils.NiceDate;
import ru19july.baskchart.utils.NiceScale;
import ru19july.baskchart.utils.Utils;
import ru19july.baskchart.view.opengl.ColorRectangle;
import ru19july.baskchart.view.opengl.text.GLText;
import ru19july.baskchart.view.theme.DarkTheme;

import static javax.microedition.khronos.opengles.GL10.GL_FLOAT;
import static javax.microedition.khronos.opengles.GL10.GL_LINES;
import static javax.microedition.khronos.opengles.GL10.GL_VERTEX_ARRAY;

public class ChartEngine {

    private static final String TAG = ChartEngine.class.getSimpleName();
    private final Context mContext;
    private final View mView;
    private int W, H;

    Paint paint;
    Random r = new Random();

    private boolean drawing = false;
    private float xStart = 0.0f;
    private float xEnd = 0.0f;
    private IChartTheme mTheme = new DarkTheme();

    float startNormalized = 0.0f;
    float endNormalized = 1.0f;
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
    private RectF legendRect;
    private HashMap<Integer, GLText> glTexts = new HashMap<>();
    private long selectedTimestamp;
    private MinMaxIndex minmaxIndexes;

    public ChartEngine(Context ctx, View v) {
        mContext = ctx;
        mView = v;
    }

    public void DrawChart(Object canvas) {
        ChartData chartData = mChartData;

        if (chartData == null) return;
        if(mChartData.getSeries() == null || mChartData.getSeries().size() < 1) return;

        minmaxIndexes = findIndexes(mChartData.getSeries().get(0), startNormalized, endNormalized);

        if (canvas instanceof Canvas) {
            ((Canvas) canvas).save();
            W = ((Canvas) canvas).getWidth();
            H = ((Canvas) canvas).getHeight();
        }
        if (xEnd < 1) xEnd = W;

        if (canvas instanceof GL10) {
            GL10 gl = (GL10) canvas;
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            //gl.glEnable(GL10.GL_TEXTURE_2D);              // Enable Texture Mapping
            //gl.glEnable(GL10.GL_BLEND);                   // Enable Alpha Blend
            //gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);  // Set Alpha Blend Function

            gl.glLoadIdentity();
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

        drawSliderGraph(canvas);
        //drawSlider(canvas);

        if (canvas instanceof Canvas)
            ((Canvas) canvas).restore();

        //drawing = false;
        return;//.getCanvas();
    }

    private void drawSliderGraph(Object canvas) {
        int h0 = (int) (H * sliderYfactor);
        int hh = H - h0;

        NiceScale numScale = new NiceScale(mChartData.getSeries());

        for (int i = 1; i < mChartData.getSeries().get(0).getValues().size(); i++) {
            for (int j = 1; j < mChartData.getSeries().size(); j++) {
                if (!mChartData.getSeries().get(j).isChecked()) continue;

                int x1 = (int) (W * ((i - 1.f) / (mChartData.getSeries().get(0).getValues().size() - 1)));
                int x2 = (int) (W * ((i - 0.f) / (mChartData.getSeries().get(0).getValues().size() - 1)));

                int y1 = h0 + (int) ((1 - mChartData.getSeries().get(j).getValues().get(i - 1) / numScale.niceMax) * hh);
                int y2 = h0 + (int) ((1 - mChartData.getSeries().get(j).getValues().get(i) / numScale.niceMax) * hh);

                drawLine(canvas, x1, y1, x2, y2, 1f, Color.parseColor(mChartData.getSeries().get(j).getColor()), 1f);
            }
        }

        //left part
        drawRect(canvas, 0, h0, xStart, H, Color.parseColor(mTheme.sliderBackground()), Color.alpha(Color.parseColor(mTheme.sliderBackground())));

        //right part
        drawRect(canvas, xEnd, h0, W, H, Color.parseColor(mTheme.sliderBackground()), Color.alpha(Color.parseColor(mTheme.sliderBackground())));

        //slider window
        drawRect(canvas, xStart + 16, h0 + 4, xEnd - 16, H - 4, Color.parseColor(mTheme.sliderInner()), Color.alpha(Color.parseColor(mTheme.sliderInner())));
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

        float[] markerValues = new float[series.size() - 1];
        String[] markerColors = new String[series.size() - 1];
        selectedTimestamp = 0L;

        float xk = 0;
        if (touchIndex >= 0)
            xk = GetX(series.get(0).getValues().get(touchIndex));

        int yMin = H;

        //touched vertical line
        drawLine(canvas, (int)xk, 0, (int)xk, (int)(H * chartYendsFactor), 2f, Color.BLACK, 1f);

        for (int j = 1; j < series.size(); j++) {
            //if (!series.get(j).isChecked()) continue;
            fp.setColor(Color.parseColor(series.get(j).getColor()));
            fpc.setColor(Color.parseColor(series.get(j).getColor()));

            float alpha = series.get(j).getAlpha();
            fp.setAlpha((int) (255 * alpha));
            fpc.setAlpha((int) (255 * alpha));

            //if (seriesY.getAlpha() > 0.95) drawCircle(canvas, x1, y1, 2.0f, fpc);
            if(mChartData.getChartType() == ChartData.CHART_TYPE.CHART_TYPE_LINE) {
                float vertices[] = new float[(minmaxIndexes.max  - minmaxIndexes.min)*4];

                for (int i = minmaxIndexes.min; i < minmaxIndexes.max - 1; i++) {
                    if (canvas instanceof Canvas) {
                        vertices[(i - minmaxIndexes.min) * 4] = GetX(series.get(0).getValues().get(i));
                        vertices[(i - minmaxIndexes.min) * 4 + 1] = GetY(series.get(j).getValues().get(i), series.get(j).getScale());
                        vertices[(i - minmaxIndexes.min) * 4 + 2] = GetX(series.get(0).getValues().get(i + 1));
                        vertices[(i - minmaxIndexes.min) * 4 + 3] = GetY(series.get(j).getValues().get(i + 1), series.get(j).getScale());
                    } else {
                        vertices[(i - minmaxIndexes.min) * 4] = GetX(series.get(0).getValues().get(i)) - W / 2;
                        vertices[(i - minmaxIndexes.min) * 4 + 1] = H / 2 - (int) GetY(series.get(j).getValues().get(i), series.get(j).getScale());
                        vertices[(i - minmaxIndexes.min) * 4 + 2] = GetX(series.get(0).getValues().get(i + 1)) - W / 2;
                        vertices[(i - minmaxIndexes.min) * 4 + 3] = H / 2 - (int) GetY(series.get(j).getValues().get(i + 1), series.get(j).getScale());
                    }
                }

                drawPoly(canvas, vertices, minmaxIndexes.min + 1, minmaxIndexes.max + 1, 5f, fp.getColor(), alpha);
            }

            if(mChartData.getChartType() == ChartData.CHART_TYPE.CHART_TYPE_FILLEDPOLY) {
                float vertices[] = new float[(minmaxIndexes.max  - minmaxIndexes.min)*8];

                Path polyPath = new Path();
                polyPath.moveTo(GetX(series.get(0).getValues().get(minmaxIndexes.min)), GetY(series.get(j).getValues().get(minmaxIndexes.min), series.get(j).getScale()));

                int indx = 0;
                for (int i = minmaxIndexes.min; i < minmaxIndexes.max - 1; i++) {
                    if (canvas instanceof Canvas) {
                        int x = GetX(series.get(0).getValues().get(i));
                        int y = (int) GetY(series.get(j).getValues().get(i), series.get(j).getScale());
                        polyPath.lineTo(x, y);
                    } else {
                        vertices[indx * 8] = GetX(series.get(0).getValues().get(i)) - W / 2;
                        vertices[indx * 8 + 1] = H / 2 - (int) GetY(series.get(j).getValues().get(i), series.get(j).getScale());

                        vertices[indx * 8 + 2] = GetX(series.get(0).getValues().get(i + 1)) - W / 2;
                        vertices[indx * 8 + 3] = H / 2 - (int) GetY(series.get(j).getValues().get(i + 1), series.get(j).getScale());

                        vertices[indx * 8 + 4] = GetX(series.get(0).getValues().get(i + 1)) - W / 2;
                        vertices[indx * 8 + 5] = H / 2 - (int) GetY(series.get(j-1).getValues().get(i + 1), series.get(j-1).getScale());

                        vertices[indx * 8 + 6] = GetX(series.get(0).getValues().get(i)) - W / 2;
                        vertices[indx * 8 + 7] = H / 2 - (int) GetY(series.get(j-1).getValues().get(i), series.get(j-1).getScale());

                        indx++;
                    }
                }

                if(j>1 ){
                    for (int i = minmaxIndexes.max-1; i > minmaxIndexes.min; i--) {
                        if (canvas instanceof Canvas) {
                            int x = GetX(series.get(0).getValues().get(i));
                            int y = (int) GetY(series.get(j-1).getValues().get(i), series.get(j-1).getScale());
                            polyPath.lineTo(x, y);

                        } else {
                        }
                    }
                }

                polyPath.lineTo(GetX(series.get(0).getValues().get(minmaxIndexes.min)), GetY(series.get(j).getValues().get(minmaxIndexes.min), series.get(j).getScale()));

                fp.setStyle(Paint.Style.FILL);
                drawPath(canvas, vertices, polyPath, fp);
            }

            if(mChartData.getChartType() == ChartData.CHART_TYPE.CHART_TYPE_BAR) {
                int barWidth = W / (minmaxIndexes.max - minmaxIndexes.min);
                for (int i = minmaxIndexes.min; i < minmaxIndexes.max; i++) {

                    if (canvas instanceof Canvas) {
                        float x  = GetX(series.get(0).getValues().get(i));
                        float offsetY = 0f;

                        float y = offsetY + GetY(series.get(j).getValues().get(i), series.get(1).getScale());

                        drawBar(canvas, x, y, barWidth, fp.getColor(), alpha);
                    } else {
                        float x = GetX(series.get(0).getValues().get(i));
                        float h = GetY(series.get(j).getValues().get(i), series.get(j).getScale());
                        float y = GetY(series.get(j).getValues().get(i), series.get(j).getScale());

                        pixel((GL10) canvas, x, H/2 + y/2, barWidth, 10, fp.getColor(), 1f);
                    }
                }
            }

            if (touchIndex >= 0 && touchIndex < series.get(j).getValues().size()) {
                selectedTimestamp = series.get(0).getValues().get(touchIndex);
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
            DrawMarker(canvas, selectedTimestamp, markerValues, markerColors, xk + 20, yMin);
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

            drawLine(canvas, 0, (int)yL, W, (int)yL, 2f, Color.BLACK, 1f);

            String strFmt = String.format("%%.%df", decimalCount);
            String str = String.format(strFmt, (float) yLine);

            Paint p = new Paint();
            float textSize = H * 0.033f;
            int xw = (int) p.measureText(str);
            p.setTextSize(textSize);
            p.setAntiAlias(true);
            p.setColor(Color.parseColor(mTheme.fontColor()));
            drawText(canvas, str, 40f, yL - textSize * 0.3f, 8f, p);

            yLine += numScale.tickSpacing;
        }
    }

    private void DrawVerticalLines(NiceDate numScale, Object canvas) {
        double xLine = numScale.niceMin;

        while (xLine <= numScale.niceMax) {
            float xL = GetX(xLine);

            if (mShowVerticalLines) {
                drawLine(canvas, (int)xL, (int)(H * chartYstartsFactor), (int)xL, (int)(H * chartYendsFactor), 2f, Color.BLACK, 1f);
            }

            String str = Utils.unixtimeToString((long) xLine, "MMM dd");
            Paint p = new Paint();
            float textSize = H * textAxisSize;
            int xw = (int) p.measureText(str);
            p.setTextSize(textSize);
            p.setAntiAlias(true);
            p.setColor(Color.parseColor(mTheme.fontColor()));
            drawText(canvas, str, xL - xw, H * textYFactor, 8f, p);

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
        int fullTextWidth = 0;
        int separatorWidth = 20;
        for (int i = 0; i < values.length; i++) {
            if (colors[i] == null) continue;
            int sz = (int) p.measureText(values[i] + "");
            if (xw < sz) xw = sz;
            fullTextWidth += sz + separatorWidth;
            activeCounter++;
        }

        int leftX = (int) (lastX + Utils.FLOATING_MARGIN_LEFT);
        int rightX = (int) (leftX + xw * Utils.FLOATING_WIDTH_RATIO);
        rightX = leftX + fullTextWidth;
        if (rightX > W) {
            rightX = W - 30;
            leftX = (int) (rightX - xw * Utils.FLOATING_WIDTH_RATIO);
        }

        //TODO hide on click legend
        paint.setColor(Color.parseColor(mTheme.legendBackgroundColor()));
        legendRect = new RectF(
                leftX,
                lastY - H * 1f / 15f,
                rightX,
                lastY + H * Utils.FLOATING_MARGIN_BOTTOM_RATIO + (1) * 105);

        drawRoundRect(canvas, legendRect, 8, 8, paint);

        //date
        p.setColor(Color.parseColor(mTheme.markerFontColor()));
        drawText(canvas, dat, leftX + 50, lastY + 16, 30f, p);

        int k = 0;
        int textOffset = 0;
        for (int i = 0; i < values.length; i++) {
            String strFmt = String.format("%%.%df", decimalCount);
            String str = String.format(strFmt, (float) values[i]);
            if (colors[i] == null) continue;
            p.setColor(Color.parseColor(colors[i]));
            int sz = (int) p.measureText(values[i] + "");
            drawText(canvas, str, leftX + 50 + textOffset, lastY + 16 + 80, 30f, p);
            textOffset += sz + separatorWidth;
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

    public boolean onTouch(View v, MotionEvent event) {
        return onTouchEvent(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (mChartData == null) return false;

        xTouched = event.getX();
        yTouched = event.getY();

        if(mChartData.getSeries() == null || mChartData.getSeries().size() < 1) return false;

        int startIndex = (int) (startNormalized * mChartData.getSeries().get(0).getValues().size());
        int endIndex = (int) (endNormalized * mChartData.getSeries().get(0).getValues().size());

        if (yTouched >= H * sliderYfactor && event.getAction() == MotionEvent.ACTION_DOWN) {

            int leftDist = (int) Math.abs(xStart - xTouched);
            int rightDist = (int) Math.abs(xEnd - xTouched);

            catchedLeft = leftDist < 50 && leftDist < rightDist;
            catchedRight = rightDist < 50 && rightDist < leftDist;

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
            //startTransformAnimation();
            ChartData monthData = mChartData.loadMonth(mContext, selectedTimestamp);
            //mChartData = monthData;
            return true;
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

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
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
        }

        return true;
    }

    private void startTransformAnimation() {
        final ChartData oldChartData = mChartData;
        //final ChartData newChartData = createNewForm(mChartData);

        ValueAnimator va = ValueAnimator.ofFloat(1, 2f);
        int mDuration = 1000;
        va.setDuration(mDuration);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {

                    for (int i = 0; i < mChartData.getSeries().get(0).getValues().size(); i++) {
                        long newValue = mChartData.getSeries().get(0).getValues().get(i)/10;
                        //mChartData.getSeries().get(0).getValues().set(i, newValue);
                        for (int j = 1; j < mChartData.getSeries().size(); j++) {
                            long newValue2 = (long) (mChartData.getSeries().get(j).getValues().get(i) * (float) animation.getAnimatedValue());

                            //Log.d(TAG, "onAnimationUpdate: " + i + "(" + j + ") " + mChartData.getSeries().get(j).getValues().get(i) + " =(" + (float) animation.getAnimatedValue()  + ")=> " + newValue);
                            mChartData.getSeries().get(j).getValues().set(i, newValue2);
                        }
                    }

                //for (int i = 1; i < mChartData.getSeries().size(); i++)
                //    mChartData.getSeries().get(i).updateMorphTransformation(oldChartData, newChartData, (float) animation.getAnimatedValue());
                mView.invalidate();
            }
        });

        va.start();
    }

    private ChartData createNewForm(ChartData chartData) {
        ChartData newChart = new ChartData();
        //new X coordinates
        for(int i=0; i< chartData.getSeries().get(0).getValues().size(); i++){
            long newValue = 0;
            chartData.getSeries().get(0).getValues().set(i, newValue);
        }
        //change Y coordinates
        for(int j=1; j<chartData.getSeries().size(); j++)
            for(int i=0; i< chartData.getSeries().get(0).getValues().size(); i++) {
                long newValue = 0;
                chartData.getSeries().get(j).getValues().set(i, newValue);
            }

        return newChart;
    }

    private boolean isLegend(float x, float y) {
        if(legendRect == null) return false;
        return (x >= legendRect.left && x <= legendRect.right && y >= legendRect.top && y <= legendRect.bottom);
    }


    ///////////////////////////////////
    // Draw methods
    ///////////////////////////////////

    private void drawText(Object canvas, String str, float x, float y, float size, Paint p) {
        if (canvas instanceof Canvas)
            ((Canvas) canvas).drawText(str, x, y, p);
        if (canvas instanceof GL10) {
            if(true) return;
            
            int glSize = (int) (size * 2);
            GLText glText = glTexts.get(glSize);
            if(glText == null) {
                glText = new GLText((GL10)canvas, mContext.getAssets());
                glText.load("Roboto-Regular.ttf", glSize, 2, 2);  // Create Font (Height: 14 Pixels / X+Y Padding 2 Pixels)
                glTexts.put(glSize, glText);
            }

            glText.begin(
                    (p.getColor() >> 16 & 0xff) / 255f,
                    (p.getColor() >> 8 & 0xff) / 255f,
                    (p.getColor() & 0xff) / 255f,
                    p.getAlpha() / 255);
            glText.draw(str, x - W / 2, -(y - H / 2));          // Draw Test String
            glText.end();                                   // End Text Rendering

        }
    }

    private void drawTextGl(GL10 gl, String text, int x, int y, float size, int color, int alpha) {
        for(int i=0; i<text.length(); i++) {
            pixel(gl, (int) (x + i * size), (int) y, size * 0.8f, size, color, alpha);
        }
    }

    private void drawBar(Object canvas, float x, float y, float barWidth, int color, float alpha) {
        Rect rect = new Rect((int)(x - barWidth/2), (int)y, (int)(x + barWidth/2), (int)(H * chartYendsFactor));
        if (canvas instanceof Canvas){
            Paint fp = new Paint();
            fp.setColor(color);
            fp.setAlpha((int) (alpha * 255));
            fp.setAntiAlias(false);
            fp.setStyle(Paint.Style.FILL_AND_STROKE);
            ((Canvas) canvas).drawRect(rect, fp);
        }
        if (canvas instanceof GL10) {
            pixel((GL10)canvas, rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top, color, 1);
        }
    }

    private void drawRoundRect(Object canvas, RectF rect, int x, int y, Paint paint) {
        if (canvas instanceof Canvas)
            ((Canvas) canvas).drawRoundRect(rect, x, y, paint);
        if (canvas instanceof GL10) {
            pixel((GL10)canvas, rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top, paint.getColor(), 1);
        }
    }

    private void drawPath(Object canvas, float[] vertices, Path mPath, Paint mPaint) {
        if (canvas instanceof Canvas)
            ((Canvas) canvas).drawPath(mPath, mPaint);
        if (canvas instanceof GL10) {
            if (canvas instanceof GL10) {
                GL10 gl = (GL10) canvas;

                FloatBuffer vertexBuffer;  // Buffer for vertex-array
                int numFaces = vertices.length / 8;

                int color = mPaint.getColor();
                float alpha = mPaint.getAlpha() / 255f;
                float width = 1f;

                ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
                vbb.order(ByteOrder.nativeOrder()); // Use native byte order
                vertexBuffer = vbb.asFloatBuffer(); // Convert from byte to float
                vertexBuffer.put(vertices);         // Copy data into buffer
                vertexBuffer.position(0);           // Rewind

                gl.glFrontFace(GL10.GL_CCW);    // Front face in counter-clockwise orientation
                gl.glEnable(GL10.GL_CULL_FACE); // Enable cull face
                gl.glCullFace(GL10.GL_BACK);    // Cull the back face (don't display)

                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);

                float r = ((color >> 16) & 0xff) / 255f;
                float g = ((color >> 8) & 0xff) / 255f;
                float b = ((color >> 0) & 0xff) / 255f;

                // Render all the faces
                for (int face = 0; face < numFaces; face++) {
                    // Set the color for each of the faces
                    gl.glColor4f(r, g, b, alpha);
                    // Draw the primitive from the vertex-array directly
                    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, face*4, 4);
                }
                gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glDisable(GL10.GL_CULL_FACE);


                gl.glFrontFace(GL10.GL_CW);    // Front face in counter-clockwise orientation
                gl.glEnable(GL10.GL_CULL_FACE); // Enable cull face
                gl.glCullFace(GL10.GL_BACK);    // Cull the back face (don't display)

                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);

                // Render all the faces
                for (int face = 0; face < numFaces; face++) {
                    // Set the color for each of the faces
                    gl.glColor4f(r, g, b, alpha);
                    // Draw the primitive from the vertex-array directly
                    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, face*4, 4);
                }
                gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glDisable(GL10.GL_CULL_FACE);
            }
        }
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

    private void drawLine(Object canvas, int x1, int y1, int x2, int y2, float width, int color, float alpha) {
        if (canvas instanceof Canvas) {
            Paint fp = new Paint();
            fp.setColor(color);
            fp.setAlpha((int) (alpha * 255));
            fp.setAntiAlias(true);
            fp.setStyle(Paint.Style.FILL_AND_STROKE);
            fp.setStrokeWidth(width);

            ((Canvas) canvas).drawLine(x1, y1, x2, y2, fp);
        }
        if (canvas instanceof GL10) {
            drawLineGL((GL10) canvas, x1, H - y1, x2, H - y2, 1f, color, alpha);
        }
    }

    private void drawPoly(Object canvas, final float[] vertices, int from, int to, float width, int color, float alpha) {
        if (canvas instanceof Canvas) {
            Paint fp = new Paint();
            fp.setColor(color);
            fp.setAlpha((int) (alpha * 255));
            fp.setAntiAlias(true);
            fp.setStyle(Paint.Style.STROKE);
            fp.setStrokeWidth(width);

            ((Canvas) canvas).drawLines(vertices, fp);
        }
        if (canvas instanceof GL10) {
            GL10 gl = (GL10) canvas;

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
            gl.glLineWidth(width);
            gl.glDrawArrays(GL_LINES, 0, vertices.length/2);
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

    private void pixel(GL10 gl, float x, float y, float w, float h, int color, float alpha) {
        x = x - W / 2;
        y = -(y - H / 2);
        gl.glLoadIdentity();
        gl.glTranslatef(x, y, 0);
        gl.glScalef(w, h, 1);
        new ColorRectangle().draw(gl, color, alpha);
        gl.glLoadIdentity();
    }

    private void drawRectGL(GL10 gl, float x1, float y1, float x2, float y2, int color, float alpha) {
        x1 = x1 - W / 2;
        y1 = (y1 - H / 2) - H * sliderYfactor;
        x2 = x2 - W / 2;
        y2 = (y2 - H / 2) - H * sliderYfactor;
        gl.glLoadIdentity();
        gl.glTranslatef(x1, y1, 0);
        gl.glScalef(x2 - x1, y2 - y1, 0);
        new ColorRectangle().draw(gl, color, alpha);
    }

    private void drawLineGL(GL10 gl, int x1, int y1, int x2, int y2, float width, int color, float alpha) {
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
        gl.glLineWidth(width);
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