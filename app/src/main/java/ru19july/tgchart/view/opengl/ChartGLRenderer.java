package ru19july.tgchart.view.opengl;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ru19july.tgchart.data.ChartData;

public class ChartGLRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = ChartGLRenderer.class.getSimpleName();
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

    private float[] lightDiffuseColor = {0.99f, 0.99f, 0.99f, 0};
    private float[] lightAmbientColor = {0.92f, 0.92f, 0.92f, 0};
    private float[] lightPosition = {30, 30, 30, 30};
    private float lightSpecular[] = new float[]{0.97f, 0.97f, 0.97f, 1};
    private float lightDirection[] = new float[]{0.0f, 0.0f, -1.0f};
    private float matAmbient[] = new float[]{0.93f, 0.93f, 0.93f, 1.0f};
    private float matDiffuse[] = new float[]{0.96f, 0.96f, 0.96f, 1.0f};

    private int Width;
    private int Height;

    public ChartGLRenderer(Context context) {
        mContext = context;
    }

    public void onDrawFrame(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glLoadIdentity();

        DrawPixels(gl);

        ticks++;

        gl.glLoadIdentity();
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glEnable(GL10.GL_TEXTURE_2D);            //Enable Texture Mapping ( NEW )
        gl.glShadeModel(GL10.GL_SMOOTH);            //Enable Smooth Shading
        //gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 	//Black Background
        //gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
        gl.glEnable(GL10.GL_DEPTH_TEST);            //Enables Depth Testing
        gl.glDepthFunc(GL10.GL_LEQUAL);            //The Type Of Depth Testing To Do

        //Really Nice Perspective Calculations
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
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

        for (int j = 0; j < 3; j++)
            for (int i = 0; i < 1000; i++) {
                int x = (int) (Math.cos(i / 1000f * (2 * Math.PI)) * (j + 1) * (100f + (i + ticks-500)/10f)) + Width / 2;
                int y = (int) (Math.sin(i / 1000f * (2 * Math.PI)) * (j + 1) * (100f)) + Height / 2;

                x = (int) (Width * (i/1000f));
                //pixel(gl, x, y, j < 1 ? Color.RED : (j < 2 ? Color.BLUE : Color.GREEN));
            }

        for(int j=1; j<mChartData.getSeries().size(); j++)
        {
            for(int i=0; i<mChartData.getSeries().get(0).getValues().size(); i++){
                int x = (int) (Width * (i + 0f)/mChartData.getSeries().get(0).getValues().size());
                int y = (int) (Height * (mChartData.getSeries().get(j).getValues().get(i) - mChartData.getSeries().get(j).getMinValue() - 0f)/ (mChartData.getSeries().get(j).getMaxValue() - mChartData.getSeries().get(j).getMinValue()));
                pixel(gl, x, y, j < 1 ? Color.BLUE : (j < 2 ? Color.RED : Color.GREEN));
                line(gl, x, y, x + 30, y + 20, j < 1 ? Color.BLUE : (j < 2 ? Color.RED : Color.GREEN));
            }
        }

    }

    private void line(GL10 gl, int x1, int y1, int x2, int y2, int color) {
        x1 = x1 - Width / 2;
        y1 = y1 - Height / 2;
        gl.glLoadIdentity();
        Random r = new Random();
        gl.glTranslatef(x1, y1, 0);
        gl.glScalef(1, 1, 1);
        //mLine.draw(gl, color);
    }

    private void pixel(GL10 gl, int x, int y, int color) {
        x = x - Width / 2;
        y = y - Height / 2;
        gl.glLoadIdentity();
        Random r = new Random();
        gl.glTranslatef(x, y, 0);
        gl.glScalef(r.nextFloat()*2f, r.nextFloat()*2f, 1);
        new CubeColorSides().draw(gl, color);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Width = width;
        Height = height;
        Log.d(TAG, "onSurfaceChanged: " + Width + "x" + Height);

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

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

    }

    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x - mPreviousX;
                float dy = y - mPreviousY;
                ticks = (int) (mAngleX + (int)(dy * TOUCH_SCALE_FACTOR));
 //               mAngleY = (mAngleY + (int)(dx * TOUCH_SCALE_FACTOR) + 360) % 360;
 //               mAngleX = (mAngleX + (int)(dy * TOUCH_SCALE_FACTOR) + 360) % 360;
                break;
        }
        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    public void slideFrame(int xStart, int xEnd) {
        //Log.d(TAG, "slideFrame: " + xStart + " / " + xEnd);
        //startX = (500f - xStart);
    }

    public void setData(ChartData chartData) {
        mChartData = chartData;
    }
}