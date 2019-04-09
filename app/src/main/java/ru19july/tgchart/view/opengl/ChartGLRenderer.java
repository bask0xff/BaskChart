package ru19july.tgchart.view.opengl;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.interfaces.IChartTheme;
import ru19july.tgchart.interfaces.IChartView;
import ru19july.tgchart.view.ChartEngine;

public class ChartGLRenderer implements IChartView, GLSurfaceView.Renderer, View.OnTouchListener  {
    private static final String TAG = ChartGLRenderer.class.getSimpleName();
    private final View view;
    private Context mContext;
    private FloatBuffer mVertexBuffer = null;
    private ShortBuffer mTriangleBorderIndicesBuffer = null;
    private int mNumOfTriangleBorderIndices = 0;

    public float mAngleX = 0.0f;
    private float mPreviousX;
    private float mPreviousY;
    private final float TOUCH_SCALE_FACTOR = 0.6f;

    private int ticks = 0;

    private ChartData mChartData;
    private ChartEngine chartEngine = new ChartEngine();

    private int Width;
    private int Height;

    public ChartGLRenderer(View chartView, Context context) {
        mContext = context;
        this.view = chartView;
    }

    public void onDrawFrame(GL10 gl) {
        chartEngine.DrawChart(gl);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        chartEngine.onSurfaceCreated(gl, config);
    }

    private void DrawPixels(GL10 gl) {
        //chart
        if (mVertexBuffer != null) {
            gl.glLoadIdentity();
            gl.glTranslatef(0, 0, 0);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
            gl.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
            gl.glDrawElements(GL10.GL_LINES, mNumOfTriangleBorderIndices,
                    GL10.GL_UNSIGNED_SHORT, mTriangleBorderIndicesBuffer);
        }

        for (int j = 1; j < mChartData.getSeries().size(); j++) {
            for (int i = 0; i < mChartData.getSeries().get(0).getValues().size() - 1; i++) {
                int x1 = (int) (Width * (i + 0f) / mChartData.getSeries().get(0).getValues().size());
                int y1 = (int) (Height * (mChartData.getSeries().get(j).getValues().get(i) - mChartData.getSeries().get(j).getMinValue() - 0f) / (mChartData.getSeries().get(j).getMaxValue() - mChartData.getSeries().get(j).getMinValue()));

                int x2 = (int) (Width * (i + 1f) / mChartData.getSeries().get(0).getValues().size());
                int y2 = (int) (Height * (mChartData.getSeries().get(j).getValues().get(i + 1) - mChartData.getSeries().get(j).getMinValue() - 0f) / (mChartData.getSeries().get(j).getMaxValue() - mChartData.getSeries().get(j).getMinValue()));
                pixel(gl, x1, y1, 1f, j < 1 ? Color.BLUE : (j < 2 ? Color.RED : Color.GREEN));
                //line(gl, x, y, x + j*3, y + j*2, j < 1 ? Color.BLUE : (j < 2 ? Color.RED : Color.GREEN));

                //drawLine(gl, x1, y1, x2, y2, 3f, j < 2 ? Color.BLUE : (j < 3 ? Color.RED : Color.GREEN)/*Color.parseColor(mChartData.getSeries().get(j).getColor())*/);
            }
        }

    }

    private void pixel(GL10 gl, int x, int y, float w, int color) {
        x = x - Width / 2;
        y = y - Height / 2;
        gl.glLoadIdentity();
        Random r = new Random();
        gl.glTranslatef(x, y, 0);
        //gl.glScalef(r.nextFloat()*20f, r.nextFloat()*20f, 1);
        gl.glScalef(w, w, 1);
        new CubeColorSides().draw(gl, color);
    }



    private void line(GL10 gl, float x1, float y1, float x2, float y2, int color) {
        x1 = x1 - Width / 2;
        y1 = y1 - Height / 2;
        gl.glLoadIdentity();
        Random r = new Random();
        gl.glTranslatef(x1, y1, 0);
        gl.glScalef(1, 1, 1);
        new LineSides().draw(gl, x1, y1, x2, y2, 20f, color);
    }

    private void line(GL10 gl, float x1, float y1, float x2, float y2, float w, int color) {
        float xc = (x1 + x2) / 2;
        float yc = (y1 + y2) / 2;
        gl.glLoadIdentity();
        gl.glTranslatef(xc, yc, 0);
        gl.glRotatef(ticks * 5, 0.0f, 0.0f, 1.0f);
        gl.glScalef(x2-x1, w, 1);
        new CubeColorSides().draw(gl, color);

        gl.glLoadIdentity();
        gl.glTranslatef(x1, y1, 0);
        gl.glScalef(x2-x1, w, 1);
        //new CubeColorSides().draw(gl, color);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        chartEngine.onSurfaceChanged(gl, width, height);

    }

    public boolean onTouchEvent(MotionEvent e) {
        Log.d(TAG, "onTouchEvent: " + e);
        return chartEngine.onTouchEvent(e);
    }

    public void setData(ChartData chartData) {
        chartEngine.setData(chartData);
    }

    @Override
    public void updateSlideFrameWindow(int startX, int endX) {
        Log.d(TAG, "updateSlideFrameWindow: " + startX + " / " + endX);
        chartEngine.updateSlideFrameWindow(startX, endX);
    }

    @Override
    public void showChart(int position, float v, float v1) {
        chartEngine.showChart(view, position, v, v1);
    }

    @Override
    public void animateChanges(ChartData oldChartData, ChartData newChartData) {
        chartEngine.animateChanges(view, oldChartData, newChartData);
    }

    @Override
    public void invalidate() {
    }

    @Override
    public void setTheme(IChartTheme mTheme) {
        chartEngine.setTheme(mTheme);
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
}