package ru19july.tgchart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class ChartViewSlider extends View implements View.OnTouchListener {

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector detector;
    private float mScaleFactor = 1.f;
    Paint paint;

    int slLeft;
    int slRight;
    private int W, H;
    private final String TAG = "ChartViewSlider";

    ISliderListener mOnSliderListener;

    private boolean drawing = false;
    private float xStart = 50.0f;
    private float xEnd = 450.0f;

    public ChartViewSlider(Context context) {
        super(context);
        Log.d(TAG, "ChartView(Context context)");

        initView(context, null);
    }

    public ChartViewSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "ChartView(Context context, AttributeSet attrs) ");

        initView(context, attrs);
    }

    public ChartViewSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(TAG, "ChartView(Context context, AttributeSet attrs, int defStyleAttr)");

        initView(context, attrs);
    }

    public void initView(Context context, AttributeSet attrs) {
        setOnTouchListener(this);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        detector = new GestureDetector(ChartViewSlider.this.getContext(), new MyListener());

        slRight = W;
        slLeft = W * 3/4;

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);

        //startTime = BinaryStationClient.Instance().CurrentTime();
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.ChartView,
                    0, 0);

            try {
                boolean mShowText = a.getBoolean(R.styleable.ChartView_showLegend, false);
                int mTextPos = a.getInteger(R.styleable.ChartView_labelPosition, 0);

                Log.d(TAG, "initView: showLegend: " + mShowText);
                Log.d(TAG, "initView: textPos: " + mTextPos);
            } finally {
                a.recycle();
            }
        }
    }
/*
    @Override
    protected void onMeasure() {}
*/
    @Override
    protected void onDraw(final Canvas canvas) {

        canvas.save();
        canvas.scale(mScaleFactor, mScaleFactor);

        W = canvas.getWidth();
        H = canvas.getHeight();

        PrepareCanvas(canvas);

        canvas.restore();
    }

    public Canvas PrepareCanvas(Canvas canvas) {
        if(drawing) return canvas;

        drawing = true;

        Paint fp = new Paint();
        fp.setAntiAlias(false);
        fp.setStyle(Paint.Style.FILL_AND_STROKE);
        fp.setColor(Color.parseColor("#77222222"));

        canvas.drawRect(0, 0, W, H, fp);

        fp.setAntiAlias(false);
        fp.setStyle(Paint.Style.FILL_AND_STROKE);
        fp.setColor(Color.parseColor("#77555555"));

        canvas.drawRect(0, 0, xStart, H, fp);
        canvas.drawRect(xEnd, 0, W, H, fp);

//        canvas.drawRect(0, 0, W, H, fp);

        drawing = false;
        return canvas;
    }

    public void setSliderListener(ISliderListener sliderListener) {
        mOnSliderListener = sliderListener;
    }

    class MyListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            invalidate();
            return true;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_MOVE) {
            invalidate();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = detector.onTouchEvent(event);
        int x = (int) event.getX();
        Log.d(TAG, "onTouchEvent; ACTION: " + event.getAction() + ";  x=" + x + "; result: " + result);

        float xx = event.getX();
        float dx1 = Math.abs(xx - xStart);
        float dx2 = Math.abs(xx - xEnd);

        if(dx1 < 50 && dx1 < dx2) xStart = xx;
        if(dx2 < 50 && dx2 < dx1) xEnd = xx;

        Log.d(TAG, "onTouchEvent: dx1/dx2: " + (int)dx1 + " / " + (int)dx2);

        mOnSliderListener.onSlide((int) xStart, (int)xEnd);

        if (!result) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                //stopScrolling();
                result = true;
            }
        }
        
        return result;
    }

    interface ISliderListener{
        void onSlide(int xStart, int xEnd);
    }

}
