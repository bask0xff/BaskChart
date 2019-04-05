package ru19july.tgchart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public interface ICanvas {
    int getWidth();

    int getHeight();

    void drawRect(int x, int y, int w, int h, int p);

    void drawPath(Path mPath, Paint mPaint);

    void drawText(String str, float v, float v1, Paint p);

    void drawLine(int x1, int y1, int x2, int y2, Paint fp);

    void drawCircle(float x1, float y1, float v, Paint fpc);

    void drawRoundRect(RectF rect, int i, int i1, Paint paint);

    void save();

    void restore();

    Canvas getCanvas();
}
