package ru19july.tgchart.view.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ru19july.tgchart.view.opengl.sprite.SimpleGLRenderer;

import static javax.microedition.khronos.opengles.GL10.*;

class MyRenderer implements GLSurfaceView.Renderer {
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

    private final String VertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +

                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String FragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    protected int GlProgram;
    protected int PositionHandle;
    protected int ColorHandle;
    protected int MVPMatrixHandle;

    static final int COORDS_PER_VERTEX = 3;
    static float LineCoords[] = {
            0.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f
    };

    private final int VertexCount = LineCoords.length / COORDS_PER_VERTEX;
    private final int VertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    float color[] = { 0.0f, 0.0f, 0.0f, 1.0f };
    private int ticks = 0;


    public MyRenderer(Context context) {
        mContext = context;
    }

    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glTranslatef(0.0f, 0.0f, -3.0f);
        gl.glRotatef(mAngleX, 1, 0, 0);
        gl.glRotatef(mAngleY, 0, 1, 0);
        gl.glRotatef(mAngleZ, 0, 0, 1);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);

        // Set line color to green
        gl.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);


        // Add program to OpenGL ES environment
        GLES20.glUseProgram(GlProgram);

        // get handle to vertex shader's vPosition member
        PositionHandle = GLES20.glGetAttribLocation(GlProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(PositionHandle);

        // Prepare the triangle coordinate data
        //GLES20.glVertexAttribPointer(PositionHandle, COORDS_PER_VERTEX,
        //        GLES20.GL_FLOAT, false,
        //        VertexStride, VertexBuffer);

        // get handle to fragment shader's vColor member
        ColorHandle = GLES20.glGetUniformLocation(GlProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(ColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        MVPMatrixHandle = GLES20.glGetUniformLocation(GlProgram, "uMVPMatrix");
        //ArRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        //GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, mvpMatrix, 0);



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
                0.0f, 0.0f + ticks*0.0085f, 0.0f,
                0.2f, 0.0f + ticks*0.02f, 0.0f,
                0.4f, 0.3f + -ticks*0.015f, 0.0f,
                0.6f, 0.2f + ticks*0.011f, 0.0f,
                0.8f, 0.7f + -ticks*0.007f, 0.0f,
        };
        float vertexlist0[] = {
                -1.0f, 0.0f, -1.0f,
                1.0f, 0.0f, -1.0f,
                -1.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 1.0f,
                0.0f, 2.0f, 0.0f,
        };
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexlist.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertexlist);
        mVertexBuffer.position(0);

        // Set triangle border buffer with vertex indices
        short trigborderindexlist0[] = {
                4, 0,  4, 1,  4, 2,  4, 3,  0, 1,  1, 3,  3, 2,  2, 0,  0, 3
        };

        short trigborderindexlist[] = {
                0, 1, 1, 2, 2, 3
        };

        mNumOfTriangleBorderIndices = trigborderindexlist.length;
        ByteBuffer tbibb = ByteBuffer.allocateDirect(trigborderindexlist.length * 2);
        tbibb.order(ByteOrder.nativeOrder());
        mTriangleBorderIndicesBuffer = tbibb.asShortBuffer();
        mTriangleBorderIndicesBuffer.put(trigborderindexlist);
        mTriangleBorderIndicesBuffer.position(0);

        int vertexShader = SimpleGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, VertexShaderCode);
        int fragmentShader = SimpleGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, FragmentShaderCode);

        GlProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(GlProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(GlProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(GlProgram);                  // creates OpenGL ES program executables
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
}