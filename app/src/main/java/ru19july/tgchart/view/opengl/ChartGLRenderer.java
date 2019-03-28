package ru19july.tgchart.view.opengl;

import android.content.Context;
import android.graphics.BitmapFactory;
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

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ru19july.tgchart.data.ChartData;

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
    private ChartData mChartData;


    private float[] lightDiffuseColor = {0.99f, 0.99f, 0.99f, 0};
    private float[] lightAmbientColor = {0.92f, 0.92f, 0.92f, 0};
    private float[] lightPosition = {30, 30, 30, 30};
    private float lightSpecular[] = new float[]{0.97f, 0.97f, 0.97f, 1};
    private float lightDirection[] = new float[]{0.0f, 0.0f, -1.0f};
    private float matAmbient[] = new float[]{0.93f, 0.93f, 0.93f, 1.0f};
    private float matDiffuse[] = new float[]{0.96f, 0.96f, 0.96f, 1.0f};

    private final int ISOMETRIC_MODE = 0;
    private final int PERSPECTIVE_MODE = 1;  //with background
    private final int AngleStep = 5;

    private int MODE = 0;

    // Specifies the format our textures should be converted to upon load.
    private static BitmapFactory.Options sBitmapOptions
            = new BitmapFactory.Options();
    // An array of things to draw every frame.
    // Pre-allocated arrays to use at runtime so that allocation during the
    // test can be avoided.
    private int[] mTextureNameWorkspace;
    private int[] mCropWorkspace;
    // Determines the use of vertex arrays.
    private boolean mUseVerts;
    // Determines the use of vertex buffer objects.
    private boolean mUseHardwareBuffers;

    private int Width;
    private int Height;
    private CubeColorSides mCube = new CubeColorSides();
    private CubeColorSides mPixel = new CubeColorSides();


    private float mCubeRotation;

    //private Cube cube;
    private static float angleCube = 0;
    private static float speedCube = -1.5f;   // Rotational speed for cube (NEW)
    //private Square      square;     // the square
    private FloatBuffer textureBuffer;    // buffer holding the texture coordinates
    private float texture[] = {
            // Mapping coordinates for the vertices
            0.0f, 1.0f,        // top left		(V2)
            0.0f, 0.0f,        // bottom left	(V1)
            1.0f, 1.0f,        // top right	(V4)
            1.0f, 0.0f        // bottom right	(V3)
    };


    public ChartGLRenderer(Context context) {
        mContext = context;
    }

    public void onDrawFrame(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        //mCubeRotation = Scene.Instance().GetAngle();

        gl.glLoadIdentity();
        //DrawSprites(gl);
        //DrawRotatingAxes(gl);

        DrawPixels(gl);
        //setAllBuffers(mChartData);

        ticks++;

        gl.glLoadIdentity();

        /*

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

        setAllBuffers(mChartData);

        //ticks++;
*/
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //init1(gl, config);
        //init2(gl, config);
        //init3(gl, config);

        // Get all the buffers ready
        //setAllBuffers(mChartData);


        // Load the texture for the square
        //square.loadGLTexture(gl, mContext, R.drawable.level03);

        gl.glEnable(GL10.GL_TEXTURE_2D);            //Enable Texture Mapping ( NEW )
        gl.glShadeModel(GL10.GL_SMOOTH);            //Enable Smooth Shading
        //gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 	//Black Background
        //gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
        gl.glEnable(GL10.GL_DEPTH_TEST);            //Enables Depth Testing
        gl.glDepthFunc(GL10.GL_LEQUAL);            //The Type Of Depth Testing To Do

        //Really Nice Perspective Calculations
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    	/*
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);

        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);

        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                  GL10.GL_NICEST);*/
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
                int x = (int) (Math.cos(i / 1000f * (2 * Math.PI)) * (j + 1) * (100f + (i + ticks-500)/10f));
                int y = (int) (Math.sin(i / 1000f * (2 * Math.PI)) * (j + 1) * (100f));
                pixel(gl, x, y, j < 1 ? Color.RED : (j < 2 ? Color.BLUE : Color.GREEN));
            }

        /*
        //line 45 degrees
        gl.glLoadIdentity();
        gl.glTranslatef(10, 0, 0);
        gl.glRotatef(45f, 0f, 0f, -1f);
        gl.glScalef(1, 100, 1);
        mCube.draw(gl);

        gl.glLoadIdentity();
        gl.glTranslatef(10, 0, 0);
        gl.glRotatef(90f, 0f, 0f, -1f);
        gl.glScalef(1, 100, 1);
        mCube.draw(gl);*/
    }

    private void pixel(GL10 gl, int x, int y, int color) {
        gl.glLoadIdentity();
        Random r = new Random();
        gl.glTranslatef(x, y, 0);
        gl.glScalef(r.nextFloat()*2f, r.nextFloat()*2f, 1);
        mPixel.draw(gl, color);
    }

    public int[] getConfigSpec() {
        // We don't need a depth buffer, and don't care about our
        // color depth.
        //int[] configSpec = { EGL10.EGL_DEPTH_SIZE, 0, EGL10.EGL_NONE };
        int[] configSpec = {EGL10.EGL_DEPTH_SIZE, 1, EGL10.EGL_NONE};
        /*
    	int[] configSpec = {
                EGL10.EGL_RED_SIZE, 5,
                EGL10.EGL_GREEN_SIZE, 6,
                EGL10.EGL_BLUE_SIZE, 5,
                EGL10.EGL_DEPTH_SIZE, 16,
                // Requires that setEGLContextClientVersion(2) is called on the view.
                EGL10.EGL_RENDERABLE_TYPE, 4, // EGL_OPENGL_ES2_BIT
                EGL10.EGL_SAMPLE_BUFFERS, 1, // true
                EGL10.EGL_SAMPLES, 2,
                EGL10.EGL_NONE
        };
        */
        return configSpec;
    }

    /**
     * Changes the vertex mode used for drawing.
     *
     * @param useVerts           Specifies whether to use a vertex array.  If false, the
     *                           DrawTexture extension is used.
     * @param useHardwareBuffers Specifies whether to store vertex arrays in
     *                           main memory or on the graphics card.  Ignored if useVerts is false.
     */
    public void setVertMode(boolean useVerts, boolean useHardwareBuffers) {
        mUseVerts = useVerts;
        mUseHardwareBuffers = useVerts ? useHardwareBuffers : false;
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


    /* Called when the size of the window changes. */
    public void sizeChanged(GL10 gl, int width, int height) {
        //http://androidcookbook.com/Recipe.seam;jsessionid=C0EC047F8349134EE99E8F376C828E3F?recipeId=1529
        Width = width;
        Height = height;

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        //3D
        if (MODE == PERSPECTIVE_MODE) {
            GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);
            gl.glViewport(0, 0, width, height);
        } else {
            gl.glOrthof(-width / 2, width / 2, -height / 2, height / 2, -1000.0f, 1000.0f);
            gl.glShadeModel(GL10.GL_SMOOTH);
            if (true) {
                gl.glEnable(GL10.GL_BLEND);
                gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
                gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);
                gl.glEnable(GL10.GL_TEXTURE_2D);
                gl.glEnable(GL10.GL_DEPTH_TEST);
            }

            gl.glEnable(GL10.GL_LIGHTING);
            gl.glEnable(GL10.GL_LIGHT0);
            gl.glEnable(GL10.GL_COLOR_MATERIAL);
            gl.glEnable(GL10.GL_BLEND);

            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, matAmbient, 0);
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, matDiffuse, 0);

            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientColor, 0);
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseColor, 0);
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);

            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, lightSpecular, 0);
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPOT_DIRECTION, lightDirection, 0);
            gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_CUTOFF, 180f);
            gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_EXPONENT, 100f);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glDepthFunc(GL10.GL_LESS);
            gl.glDisable(GL10.GL_DITHER);
        }

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        //GLU.gluLookAt(gl, 0.0f, 0.0f, -1000.0f, 0.0f, 0.0f, 0.0f, 0, 0, 1);
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
        /*
        gl.glViewport(0, 0, width, height);
        float aspect = (float)width / height;
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-aspect, aspect, -1.0f, 1.0f, 1f, 10.0f);*/

        //http://androidcookbook.com/Recipe.seam;jsessionid=C0EC047F8349134EE99E8F376C828E3F?recipeId=1529
        Width = width;
        Height = height;

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        //3D
        if (MODE == PERSPECTIVE_MODE) {
            GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);
            gl.glViewport(0, 0, width, height);
        } else {
            gl.glOrthof(-width / 2, width / 2, -height / 2, height / 2, -1000.0f, 1000.0f);
            gl.glShadeModel(GL10.GL_SMOOTH);
            if (true) {
                gl.glEnable(GL10.GL_BLEND);
                gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
                gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);
                gl.glEnable(GL10.GL_TEXTURE_2D);
                gl.glEnable(GL10.GL_DEPTH_TEST);
            }

            gl.glEnable(GL10.GL_LIGHTING);
            gl.glEnable(GL10.GL_LIGHT0);
            gl.glEnable(GL10.GL_COLOR_MATERIAL);
            gl.glEnable(GL10.GL_BLEND);

            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, matAmbient, 0);
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, matDiffuse, 0);

            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientColor, 0);
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseColor, 0);
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);

            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, lightSpecular, 0);
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPOT_DIRECTION, lightDirection, 0);
            gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_CUTOFF, 180f);
            gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_EXPONENT, 100f);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glDepthFunc(GL10.GL_LESS);
            gl.glDisable(GL10.GL_DITHER);
        }

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        //GLU.gluLookAt(gl, 0.0f, 0.0f, -1000.0f, 0.0f, 0.0f, 0.0f, 0, 0, 1);


    }

    private void setAllBuffers(ChartData chartData){
        float[] vertexlist = new float[chartData.getSeries().get(0).getValues().size() * 3];
        float minX =  chartData.getSeries().get(0).getMinValue();
        float maxX =  chartData.getSeries().get(0).getMaxValue();

        for(int i=0; i<chartData.getSeries().get(1).getValues().size(); i++) {
            vertexlist[i * 3] = startX + ((chartData.getSeries().get(0).getValues().get(i) - minX) / (maxX - minX)) * 5.0f - 2.5f;
            vertexlist[i * 3 + 1] = ((chartData.getSeries().get(1).getValues().get(i))) / 300.0f;
            vertexlist[i * 3 + 2] = 0.0f;
        }


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
        //Log.d(TAG, "slideFrame: " + xStart + " / " + xEnd);
        startX = (500f - xStart) / 500f;
    }

    public void setData(ChartData chartData) {
        mChartData = chartData;
    }
}