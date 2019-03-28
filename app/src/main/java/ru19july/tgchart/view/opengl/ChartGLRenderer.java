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
        x = x - Width / 2;
        y = y - Height / 2;
        gl.glLoadIdentity();
        Random r = new Random();
        gl.glTranslatef(x, y, 0);
        gl.glScalef(r.nextFloat()*2f, r.nextFloat()*2f, 1);
        mPixel.draw(gl, color);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //http://androidcookbook.com/Recipe.seam;jsessionid=C0EC047F8349134EE99E8F376C828E3F?recipeId=1529
        Width = width;
        Height = height;
        Log.d(TAG, "onSurfaceChanged: " + Width + "x" + Height);

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
        startX = (500f - xStart);
    }

    public void setData(ChartData chartData) {
        mChartData = chartData;
    }
}