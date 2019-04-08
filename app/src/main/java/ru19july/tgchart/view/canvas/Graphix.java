package ru19july.tgchart.view.canvas;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import ru19july.tgchart.ICanvas;
import ru19july.tgchart.data.ChartData;
import ru19july.tgchart.view.ChartEngine;

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

    @Override
    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public void onDraw(Canvas canvas, ChartEngine chartEngine, ChartData mChartData) {
        Paint fp = new Paint();
        fp.setAntiAlias(false);
        fp.setStyle(Paint.Style.FILL_AND_STROKE);
        fp.setColor(Color.RED);
        canvas.drawRect( 0, 0, 511, 333, fp);


        setCanvas(canvas);
        //canvas = chartEngine.DrawChart(this, mChartData);

    }
}
