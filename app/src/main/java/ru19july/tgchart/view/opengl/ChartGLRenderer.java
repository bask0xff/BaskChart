package ru19july.tgchart.view.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static javax.microedition.khronos.opengles.GL10.*;

public class ChartGLRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = ChartGLRenderer.class.getSimpleName();
    private Context mContext;
    private FloatBuffer mVertexBuffer = null;
    private ShortBuffer mTriangleBorderIndicesBuffer = null;
    private int mNumOfTriangleBorderIndices = 0;

    public float mAngleX = 0.0f;
    public float mAngleY = 0.0f;
    public float mAngleZ = 0.0f;
    private float mPreviousX;
    private float mPreviousY;
    private final float TOUCH_SCALE_FACTOR = 0.6f;

    private int ticks = 0;

    private float startX = -0f;


    public ChartGLRenderer(Context context) {
        mContext = context;
    }

    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glTranslatef(0.0f, 0.0f, -1.00001f);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);

        // Set line color to green
        gl.glColor4f(0.0f, 1.0f, 0.0f, 1.0f - ticks * 0.1f);

        // Draw all lines
        gl.glDrawElements(GL10.GL_LINES, mNumOfTriangleBorderIndices,
                GL10.GL_UNSIGNED_SHORT, mTriangleBorderIndicesBuffer);

        setAllBuffers();

        //ticks++;

    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        init1(gl, config);
        //init2(gl, config);
        //init3(gl, config);

        // Get all the buffers ready
        setAllBuffers();
    }

    private void init3(GL10 gl, EGLConfig config) {
        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        gl.glMatrixMode(GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrthof( 0,1000,500,0,0.0f,100.0f);

        gl.glEnableClientState(GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL_COLOR_ARRAY);
        line ( 10,100,100,300,  //coordinates
                1.2,                //thickness in px
                0.5, 0.0, 1.0, 1.0, //line color RGBA
                0,0,                //not used
                true);              //enable alphablend

        //more line() or glDrawArrays() calls
        gl.glDisableClientState(GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL_COLOR_ARRAY);

        //other drawing code...
        gl.glPopMatrix();
        gl.glDisable(GL_BLEND); //restore blending options
    }

    private void line(int i, int i1, int i2, int i3, double v, double v1, double v2, double v3, double v4, int i4, int i5, boolean b) {

    }

    private void init1(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        gl.glEnable(GL10.GL_DEPTH_TEST);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
    }

    private void init2(GL10 gl, EGLConfig config) {
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

        //gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        gl.glShadeModel(GL10.GL_FLAT);
        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        /*
         * By default, OpenGL enables features that improve quality but reduce
         * performance. One might want to tweak that especially on software
         * renderer.
         */
        gl.glDisable(GL10.GL_DITHER);
        gl.glDisable(GL10.GL_LIGHTING);

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        //gl.glEnable(GL10.GL_DEPTH_TEST);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        float aspect = (float)width / height;
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-aspect, aspect, -1.0f, 1.0f, 1f, 10.0f);
    }

    private void setAllBuffers(){
        // Set vertex buffer
        float vertexlist[] = {
                startX + 0.0f, 0.0f + ticks*0.0085f, 0.0f,
                startX + 0.2f, 0.0f + ticks*0.02f, 0.0f,
                startX + 0.4f, 0.3f + -ticks*0.015f, 0.0f,
                startX + 0.6f, 0.2f + ticks*0.011f, 0.0f,
                startX + 0.8f, 0.7f + -ticks*0.007f, 0.0f,
                startX + 1.0f, 0.8f + ticks*0.017f, 0.0f,
        };

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexlist.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertexlist);
        mVertexBuffer.position(0);

        short[] trigborderindexlist = new short[vertexlist.length * 2 / 3];
        for(short i=0; i< trigborderindexlist.length/2-1; i++){
            trigborderindexlist[i*2] = i;
            trigborderindexlist[i*2+1] = (short)(i+1);
        }

        mNumOfTriangleBorderIndices = trigborderindexlist.length;
        ByteBuffer tbibb = ByteBuffer.allocateDirect(trigborderindexlist.length * 2);
        tbibb.order(ByteOrder.nativeOrder());
        mTriangleBorderIndicesBuffer = tbibb.asShortBuffer();
        mTriangleBorderIndicesBuffer.put(trigborderindexlist);
        mTriangleBorderIndicesBuffer.position(0);

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
        Log.d(TAG, "slideFrame: " + xStart + " / " + xEnd);
        startX = (500f - xStart) / 500f;
    }
}