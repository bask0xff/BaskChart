package ru19july.tgchart.view.canvas;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import ru19july.tgchart.ICanvas;

public class Graphix implements ICanvas {

    private Canvas canvas;

    public Graphix(){
    }

    @Override
    public int getWidth() {
        return canvas.getWidth();
    }

    @Override
    public int getHeight() {
        return canvas.getHeight();
    }

    @Override
    public void drawRect(int x, int y, int w, int h, int color ) {
        Paint fp = new Paint();
        fp.setAntiAlias(false);
        fp.setStyle(Paint.Style.FILL_AND_STROKE);
        fp.setColor(color);
        canvas.drawRect(x, y, w, h, fp);
    }

    @Override
    public void drawPath(Path mPath, Paint mPaint) {

    }

    @Override
    public void drawText(String str, float v, float v1, Paint p) {
        canvas.drawText(str, v,v1, p);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, Paint fp) {
        canvas.drawLine(x1, y1, x2, y2, fp);
    }

    @Override
    public void drawCircle(float x1, float y1, float v, Paint fpc) {
        canvas.drawCircle(x1, y1, v, fpc);
    }

    @Override
    public void drawRoundRect(RectF rect, int i, int i1, Paint paint) {
        canvas.drawRoundRect(rect, i, i1, paint);
    }

    @Override
    public void save() {
        canvas.save();
    }

    @Override
    public void restore() {
        canvas.restore();
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }
}
