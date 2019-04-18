package ru19july.baskchart.view.opengl;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class LineSides {
    private FloatBuffer vertexBuffer;  // Buffer for vertex-array
    private int numFaces = 1;

    private float[] vertices = {  // Vertices of the 6 faces
            0.0f, 0.0f, 0f,  // 0. left-bottom-front
            1.0f, 0.0f, 0f,  // 1. right-bottom-front
            0.0f, 1.0f, 0f,  // 2. left-top-front
            1.0f, 1.0f, 0f   // 3. right-top-front
    };


    // Constructor - Set up the buffers
    public LineSides() {
        // Setup vertex-array buffer. Vertices in float. An float has 4 bytes
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder()); // Use native byte order
        vertexBuffer = vbb.asFloatBuffer(); // Convert from byte to float
        vertexBuffer.put(vertices);         // Copy data into buffer
        vertexBuffer.position(0);           // Rewind
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void draw(GL10 gl, float x1, float y1, float x2, float y2, float w, int color) {

        float dx = x2 - x1;
        float dy = y2 - y1;
        float d = (float) Math.sqrt(dx * dx + dy * dy);
        float a = (float) Math.acos(dx / d);

        x2 = x2 - x1;
        y2 = y2 - y1;
        x1 = 0;
        y1 = 0;

        float xx2 = (float) (x1 - Math.cos(a) * w / 2f);
        float yy2 = (float) (y1 + Math.sin(a) * w / 2f);

        float xx0 = (float) (x1 + Math.cos(a) * w / 2f);
        float yy0 = (float) (y1 - Math.sin(a) * w / 2f);

        float xx3 = (float) (x2 - Math.cos(a) * w / 2f);
        float yy3 = (float) (y2 + Math.sin(a) * w / 2f);

        float xx1 = (float) (x2 + Math.cos(a) * w / 2f);
        float yy1 = (float) (y2 + Math.sin(a) * w / 2f);

        vertices = new float[]{  // Vertices of the 6 faces
                xx0, yy0, 0f,  // 0. left-bottom-front
                xx1, yy1, 0f,  // 1. right-bottom-front
                xx2, yy2, 0f,  // 2. left-top-front
                xx3, yy3, 0f   // 3. right-top-front
        };

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder()); // Use native byte order
        vertexBuffer = vbb.asFloatBuffer(); // Convert from byte to float
        vertexBuffer.put(vertices);         // Copy data into buffer
        vertexBuffer.position(0);


        gl.glFrontFace(GL10.GL_CCW);    // Front face in counter-clockwise orientation
        gl.glEnable(GL10.GL_CULL_FACE); // Enable cull face
        gl.glCullFace(GL10.GL_BACK);    // Cull the back face (don't display)

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

        for (int face = 0; face < numFaces; face++) {
            gl.glColor4f(Color.red(color), Color.green(color), Color.blue(color), 1f);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, face * 4, 4);
        }
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisable(GL10.GL_CULL_FACE);
    }

}