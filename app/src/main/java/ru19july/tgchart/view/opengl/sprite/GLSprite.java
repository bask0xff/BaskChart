package ru19july.tgchart.view.opengl.sprite;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11Ext;

/**
 * This is the OpenGL ES version of a sprite.  It is more complicated than the
 * CanvasSprite class because it can be used in more than one way.  This class
 * can draw using a grid of verts, a grid of verts stored in VBO objects, or
 * using the DrawTexture extension.
 */
public class GLSprite extends Renderable {
    // The OpenGL ES texture handle to draw.
    private int mTextureName;
    // The id of the original resource that mTextureName is based on.
    private int mResourceId;
    // If drawing with verts or VBO verts, the grid object defining those verts.
    private Grid mGrid;

    public GLSprite(int resourceId) {
        super();
        mResourceId = resourceId;
    }

    public void setTextureName(int name) {
        mTextureName = name;
    }

    public int getTextureName() {
        return mTextureName;
    }

    public void setResourceId(int id) {
        mResourceId = id;
    }

    public int getResourceId() {
        return mResourceId;
    }

    public void setGrid(Grid grid) {
        mGrid = grid;
    }

    public Grid getGrid() {
        return mGrid;
    }

    public void draw(GL10 gl) {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureName);

        if (mGrid == null) {
            // Draw using the DrawTexture extension.
            ((GL11Ext) gl).glDrawTexfOES(x, y, z, width, height);
        } else {
            // Draw using verts or VBO verts.
            gl.glPushMatrix();
            gl.glLoadIdentity();
            gl.glTranslatef(
                    x,
                    y,
                    z);

            mGrid.draw(gl, true);

            gl.glPopMatrix();
        }
    }
}
