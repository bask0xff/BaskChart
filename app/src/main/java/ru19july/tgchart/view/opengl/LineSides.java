package ru19july.tgchart.view.opengl;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class LineSides {
    private FloatBuffer vertexBuffer;  // Buffer for vertex-array
    private int numFaces = 1;// 6;

    private float[][] colors = {  // Colors of the 6 faces
            {1.0f, 1.0f, 1.0f, 1.0f},  // 0. WHITE
            {1.0f, 0.5f, 0.0f, 1.0f},  // 0. orange
            {1.0f, 0.0f, 1.0f, 1.0f},  // 1. violet
            {0.0f, 1.0f, 0.0f, 1.0f},  // 2. green
            {0.0f, 0.0f, 1.0f, 1.0f},  // 3. blue
            {1.0f, 0.0f, 0.0f, 1.0f},  // 4. red
            {1.0f, 1.0f, 0.0f, 1.0f}   // 5. yellow
    };

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

    // Draw the shape
    public void draw(GL10 gl) {
        gl.glFrontFace(GL10.GL_CCW);    // Front face in counter-clockwise orientation
        gl.glEnable(GL10.GL_CULL_FACE); // Enable cull face
        gl.glCullFace(GL10.GL_BACK);    // Cull the back face (don't display)

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

        //gl.glScalef(030f, 100f, 30f);
        // Render all the faces
        for (int face = 0; face < numFaces; face++) {
            // Set the color for each of the faces
            gl.glColor4f(colors[0][0], colors[0][1], colors[0][2], colors[0][3]);
            // Draw the primitive from the vertex-array directly
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, face*4, 4);
        }
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisable(GL10.GL_CULL_FACE);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void draw(GL10 gl, int color) {
        gl.glFrontFace(GL10.GL_CCW);    // Front face in counter-clockwise orientation
        gl.glEnable(GL10.GL_CULL_FACE); // Enable cull face
        gl.glCullFace(GL10.GL_BACK);    // Cull the back face (don't display)

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

        //gl.glScalef(030f, 100f, 30f);
        // Render all the faces
        for (int face = 0; face < numFaces; face++) {
            // Set the color for each of the faces
            gl.glColor4f(Color.red(color), Color.green(color), Color.blue(color), 1f);
            // Draw the primitive from the vertex-array directly
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, face*4, 4);
        }
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisable(GL10.GL_CULL_FACE);
    }
}