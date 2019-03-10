package ru19july.tgchart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import ru19july.tgchart.utils.Logger;
import ru19july.tgchart.utils.NiceScale;
import ru19july.tgchart.utils.Utils;

public class ChartView extends View {

    Paint paint;

    private double minQuote = Double.MAX_VALUE;
    private double maxQuote = Double.MIN_VALUE;

    private int W, ChartLineWidth, H;
    private final String TAG = "TradingChart";

    private int fps;
    private float fpt;
    private int frames = 0;
    private long startTime = 0;

    private float lastX = 0;
    private float lastY = 0;

    private long lastDrawTime = 0;

    private boolean drawing = false;

    public ChartView(Context context) {
        super(context);
        initView(context, null);
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    public void initView(Context context, AttributeSet attrs) {
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
            } finally {
                a.recycle();
            }
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        frames++;

        //long timeLeft = BinaryStationClient.Instance().CurrentTime() - startTime;

        //if(timeLeft>1000)
        {
            fps = frames;
            frames = 0;
            //startTime = BinaryStationClient.Instance().CurrentTime();
        }
        //else
        //if(timeLeft>0)
        //    fpt = 1000 * frames / (float)timeLeft;

        W = canvas.getWidth();
        H = canvas.getHeight();
        ChartLineWidth = canvas.getWidth() * 4 / 5;//место, где заканчивается график

        PrepareCanvas(canvas);
    }

    private float GetY(double y) {
        float realY = 0;
        if ((maxQuote - minQuote) > 0)
            realY = (float) (H * (1 - 0.2 - 0.6 * (y - minQuote) / (maxQuote - minQuote)));
        return realY;
    }

    public Canvas PrepareCanvas(Canvas canvas) {
        if(drawing) return canvas;

        //long startDrawing = BinaryStationClient.Instance().CurrentTime();

        drawing = true;

        List<Quote> quotes = new ArrayList<>();
        for(int i = 0; i< 100; i++){
            Quote q = new Quote();
            q.unixtime = (int) (i + System.currentTimeMillis()/1000);
            q.value = Math.cos(i/100 * 3.14);
            q.datetime = new Date();

            quotes.add(q);
        }

        if (quotes == null) return null;

        /*HookTimeframe htf = BinaryStationClient.Instance().CurrentHookTimeframe();
        int optionKind = BinaryStationClient.Instance().OptionKind();
        Tool tool = BinaryStationClient.Instance().CurrentTool();
        int decimalCount = tool == null ? Utils.DEFAULT_DECIMAL_COUNT : tool.DecimalCount;
        //quotes = GroupBy(60, quotes);//M1:60; M5:300; H1:3600
*/
        //очищаем график
        Paint fp = new Paint();
        fp.setAntiAlias(false);
        fp.setStyle(Paint.Style.FILL_AND_STROKE);
        long ms = (new Date()).getTime();
        Log.d(TAG, "ms: " + ms);
        if(ms % 2 == 0)
            fp.setColor(Color.RED);
        else
            fp.setColor(Color.YELLOW);

        canvas.drawRect(0, 0, W, H, fp);

        //drawing graph quote

        if (quotes.size() > 0) {
            double quoteValue = 0.0;

            Quote q = quotes.get(quotes.size() - 1);
            quoteValue = q.value;

            //find minQuote, maxQuote by last period
            minQuote = Double.MAX_VALUE;
            maxQuote = Double.MIN_VALUE;

            //FindMinMaxByHookTimeframe(quotes, htf, optionKind);

            FindMinMax(quotes);

            NiceScale numScale = new NiceScale(minQuote, maxQuote);
            minQuote = numScale.niceMin;
            maxQuote = numScale.niceMax;

            if(Double.isNaN(minQuote))
                minQuote = quoteValue - 0.01;
            if(Double.isNaN(maxQuote))
                maxQuote = quoteValue + 0.01;

            Log.i("ChartView",  quoteValue + "; min:" + minQuote + ", max:" + maxQuote);

            numScale = new NiceScale(minQuote, maxQuote);

            DrawChartCurve(quotes, canvas);

            //DrawHorizontalLines(numScale, decimalCount, canvas);

            //пишем Profit
            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setStyle(Paint.Style.FILL_AND_STROKE);
            p.setFakeBoldText(true);
            String str = String.format("+%.0f%%", 77.0f);
            p.setTextSize(H / 2);
            int xw = (int) p.measureText(str);
            p.setColor(Utils.PROFIT_COLOR);
            canvas.drawText(str, (W - xw) * Utils.PROFIT_TEXT_X_POSITION_RATIO, H * Utils.PROFIT_TEXT_Y_POSITION_RATIO, p);

            //floating line
            //DrawFloatingLine(quoteValue, decimalCount, lastX, lastY, canvas);
        }

        drawing = false;
        //lastDrawTime = BinaryStationClient.Instance().CurrentTime();
        return canvas;
    }

    private void FindMinMax(List<Quote> quotes) {
        for (int i = 0; i < quotes.size() && i < Utils.CHART_POINTS; i++) {
            int k = quotes.size() - i - 1;
            if (k>=0 && k<quotes.size()) {
                Quote q = quotes.get(k);
                if (q.value > maxQuote) maxQuote = q.value;
                if (q.value < minQuote) minQuote = q.value;
            }
            else {
                Logger.e(TAG, "quote is null, k=" + k + "; quotes=" + quotes.size());
                return;
            }
        }
    }

    private void DrawChartCurve(List<Quote> quotes, Canvas canvas) {
        Paint lp = new Paint();
        lp.setAntiAlias(false);
        lp.setStrokeWidth(1);
        lp.setPathEffect(new DashPathEffect(new float[]{1, 1}, 0));

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setColor(Color.BLUE);

        //drawing chart curve
        //two-pass
        for (int j = 0; j < 2; j++) {
            List<Point> points = new ArrayList<Point>();
            int k = 0;
            float x0 = -1;
            float y0 = -1;
            int startTime = 0;
            if(quotes.size()>1)
                startTime = quotes.get(quotes.size()-2).unixtime;

            while (k < Utils.CHART_POINTS && k < quotes.size() && quotes.size() > 0 && quotes.size() > k) {
                p.setStrokeWidth(1);
                int indx = quotes.size() - k - 1;
                if (indx >= 0 && indx < quotes.size()) {
                    Quote q = quotes.get(indx);

                    //меняем k на timeIndex
                    long currentTime = System.currentTimeMillis()/1000;
                    int timeIndex = (int) (currentTime - q.unixtime);
                    //timeIndex = k;
                    if(timeIndex<0) timeIndex = 0;

                    float x = (float) (ChartLineWidth - (ChartLineWidth * timeIndex / (60 * Utils.CHART_POINTS)));
                    float y = (float) (H * q.value / (maxQuote - minQuote));

                    points.add(new Point((int) x, (int) y));

                    y = GetY(q.value);

                    //линия графика
                    if (j == 0) {
                        if (k > 0) {
                            //chart's background polygon
                            Point[] pts = new Point[4];
                            pts[0] = new Point((int) x0, (int) y0);
                            pts[1] = new Point((int) x, (int) y);
                            pts[2] = new Point((int) x, H);
                            pts[3] = new Point((int) x0, H);
                            p.setColor(Utils.POLYGON_BG_COLOR);
                            DrawPoly(pts, canvas, p);
                        }
                    }
                    if (j == 1) {
                        p.setColor(Utils.CHART_LINE_COLOR);
                        p.setStrokeWidth(4);
                        if (k > 0)
                            canvas.drawLine(x0, y0, x, y, p);
                        p.setStrokeWidth(1);
                        canvas.drawCircle(x, y, 2, p);

                        //вертикальные линии для шкалы времени
                        //вычисляем время текущей точки
                        Date date = new Date(q.unixtime * 1000L);
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        String formattedTime = sdf.format(date);
                        if (date.getMinutes() % 10 == 0) {

                            Path mPath = new Path();
                            mPath.moveTo(x, 0);
                            mPath.quadTo(x, H/2, x, H);
                            Paint mPaint = new Paint();
                            mPaint.setAntiAlias(false);
                            mPaint.setColor(Utils.MARKER_BG_COLOR);
                            mPaint.setStyle(Paint.Style.STROKE);
                            mPaint.setPathEffect(new DashPathEffect(new float[]{1, 1}, 0));
                            canvas.drawPath(mPath, mPaint);

                            p.setColor(Utils.TIME_COLOR);
                            p.setStrokeWidth(1);
                            int tw = (int) p.measureText(formattedTime);
                            p.setTextSize( ChartLineWidth/50 );
                            canvas.drawText(formattedTime, x + 5, H * 0.95f, p);
                        }
                    }
                    if (k == 0) {
                        lastY = y;
                        lastX = x;
                    }

                    x0 = x;
                    y0 = y;
                }
                k++;
            }
        }
    }
/*
    private void DrawHorizontalLines(NiceScale numScale, int decimalCount, Canvas canvas) {
        //drawing horizontal lines
        double yLine = numScale.niceMin;
        while (yLine <= numScale.niceMax)
        {
            float yL = GetY(yLine);

            Path mPath = new Path();
            mPath.moveTo(0, yL);
            mPath.quadTo(W/2, yL, W, yL);
            Paint mPaint = new Paint();
            mPaint.setAntiAlias(false);
            mPaint.setColor(Utils.MARKER_BG_COLOR);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setPathEffect(new DashPathEffect(new float[]{1, 1}, 0));
            canvas.drawPath(mPath, mPaint);

            String strFmt = String.format("%%.%df", decimalCount);
            String str = String.format(strFmt, (float) yLine);

            Paint p = new Paint();
            float textSize = H * 0.033f;
            int xw = (int) p.measureText(str);
            p.setTextSize(textSize);
            p.setAntiAlias(true);
            p.setColor(Utils.NICESCALE_TEXT_COLOR);
            canvas.drawText(str, W * 0.90f, yL - textSize*0.3f, p);

            yLine += numScale.tickSpacing;
        }
    }

    private void FindMinMaxByHookTimeframe(List<Quote> quotes, HookTimeframe htf, int optionKind) {
        if (quotes.size() > 0) {
            Quote q = quotes.get(quotes.size() - 1);
            double qValue = q.value;
            if (htf != null) {

                if (optionKind == 2) {
                    float up_delta = ((HookTimeframeTouch) htf).UpDelta;
                    double ud = qValue + up_delta;
                    float down_delta = ((HookTimeframeTouch) htf).DownDelta;
                    double dd = qValue - down_delta;

                    minQuote = dd;
                    maxQuote = ud;
                }

                if (optionKind == 3) {
                    float delta_top_ext = ((HookTimeframeRange) htf).DeltaTopExternal;
                    float delta_top_int = ((HookTimeframeRange) htf).DeltaTopInternal;
                    float delta_bot_ext = ((HookTimeframeRange) htf).DeltaBottomExternal;
                    float delta_bot_int = ((HookTimeframeRange) htf).DeltaBottomInternal;

                    double dte = qValue + delta_top_ext;
                    double dti = qValue + delta_top_int;
                    double dbe = qValue - delta_bot_ext;
                    double dbi = qValue - delta_bot_int;

                    minQuote = Math.min(dbe, dbi);
                    maxQuote = Math.max(dte, dti);
                }
            }
        }
    }

    private void DrawTouch(HookTimeframe htf, double qValue, int decimalCount, Canvas canvas) {
        if (htf != null) {
            float up_delta = ((HookTimeframeTouch) htf).UpDelta;
            double ud = qValue + up_delta;
            float udY = GetY(ud);
            float down_delta = ((HookTimeframeTouch) htf).DownDelta;
            double dd = qValue - down_delta;
            float ddY = GetY(dd);

            Paint p2 = new Paint();
            p2.setAntiAlias(false);
            p2.setStyle(Paint.Style.FILL_AND_STROKE);
            p2.setColor(Utils.LINE_COLOR);
            p2.setStrokeWidth(2);
            //отрисовываем линию UpDelta
            canvas.drawLine(0, udY, ChartLineWidth, udY, p2);
            //отрисовываем линию DownDelta
            canvas.drawLine(0, ddY, ChartLineWidth, ddY, p2);

            DrawMarker2(canvas, 1, udY, ud, decimalCount);
            DrawMarker2(canvas, 1, ddY, dd, decimalCount);
        }

    }

    private void DrawRange(HookTimeframe htf, double qValue, int decimalCount, Canvas canvas) {
        if (htf != null) {
            float delta_top_ext = ((HookTimeframeRange) htf).DeltaTopExternal;
            float delta_top_int = ((HookTimeframeRange) htf).DeltaTopInternal;
            float delta_bot_ext = ((HookTimeframeRange) htf).DeltaBottomExternal;
            float delta_bot_int = ((HookTimeframeRange) htf).DeltaBottomInternal;

            double dte = qValue + delta_top_ext;
            double dti = qValue + delta_top_int;
            double dbe = qValue - delta_bot_ext;
            double dbi = qValue - delta_bot_int;

            float dteY = GetY(dte);
            float dtiY = GetY(dti);
            float dbeY = GetY(dbe);
            float dbiY = GetY(dbi);

            Paint p2 = new Paint();
            p2.setAntiAlias(false);
            p2.setStyle(Paint.Style.FILL_AND_STROKE);

            //internal-блоки
            if (BinaryStationClient.Instance().Direction() == Utils.DIRECTION_PUT)
                p2.setColor(Utils.RED_BLOCK_COLOR);
            else
                p2.setColor(Utils.GREEN_BLOCK_COLOR);
            canvas.drawRect(0, dteY, ChartLineWidth, dtiY, p2);
            canvas.drawRect(0, dbiY, ChartLineWidth, dbeY, p2);

            //external-блоки
            if (BinaryStationClient.Instance().Direction() == Utils.DIRECTION_PUT) {
                p2.setColor(Utils.GREEN_BLOCK_COLOR);

                Paint p3 = new Paint();
                p3.setAntiAlias(false);
                p3.setStyle(Paint.Style.FILL_AND_STROKE);
                p3.setColor(Utils.RED_BLOCK_COLOR);
                canvas.drawRect(0, dtiY, ChartLineWidth, dbiY, p3);
            }
            else {
                p2.setColor(Utils.RED_BLOCK_COLOR);
                canvas.drawRect(0, dtiY, ChartLineWidth, dbiY, p2);
            }

            canvas.drawRect(0, 0, ChartLineWidth, dteY, p2);
            canvas.drawRect(0, dbeY, ChartLineWidth, H, p2);

            //отрисовываем линии
            p2.setColor(Utils.LINE_COLOR);
            p2.setStrokeWidth(2);
            canvas.drawLine(0, dteY, ChartLineWidth, dteY, p2);
            canvas.drawLine(0, dtiY, ChartLineWidth, dtiY, p2);
            canvas.drawLine(0, dbiY, ChartLineWidth, dbiY, p2);
            canvas.drawLine(0, dbeY, ChartLineWidth, dbeY, p2);

            DrawMarker2(canvas, 1, dteY, dte, decimalCount);
            DrawMarker2(canvas, 2, dtiY, dti, decimalCount);
            DrawMarker2(canvas, 2, dbiY, dbi, decimalCount);
            DrawMarker2(canvas, 1, dbeY, dbe, decimalCount);

        }
    }

    private void DrawFloatingLine(double quoteValue, int decimalCount, float lastX, float lastY, Canvas canvas) {
        Path mPath = new Path();
        mPath.moveTo(0, lastY);
        mPath.quadTo(ChartLineWidth/2, lastY, ChartLineWidth, lastY);

        Paint mPaint = new Paint();
        mPaint.setAntiAlias(false);
        mPaint.setColor(Utils.MARKER_BG_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        mPaint.setPathEffect(new DashPathEffect(new float[]{20, 10}, 0));

        canvas.drawPath(mPath, mPaint);

        DrawMarker(canvas, lastX, lastY, quoteValue, decimalCount);

        //кружок в конце линии графика
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setFakeBoldText(true);
        p.setColor(Utils.CHART_LINE_COLOR);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(lastX, lastY, 10, p);
    }
*/
    private void DrawMarker(Canvas canvas, float lastX, float lastY, double quoteValue, int decimalCount) {
        //треугольник
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setStrokeWidth(2);
        paint.setColor(Utils.MARKER_BG_COLOR);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);

        Point point1_draw = new Point((int) lastX + 15, (int) lastY);
        Point point2_draw = new Point((int) lastX + 30, (int) lastY - 10);
        Point point3_draw = new Point((int) lastX + 30, (int) lastY + 10);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(point1_draw.x, point1_draw.y);
        path.lineTo(point2_draw.x, point2_draw.y);
        path.lineTo(point3_draw.x, point3_draw.y);
        path.lineTo(point1_draw.x, point1_draw.y);
        path.close();

        canvas.drawPath(path, paint);

        //floating quote
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setFakeBoldText(true);
        p.setStrokeWidth(1);
        String strFmt = String.format("%%.%df", decimalCount);
        String str = String.format(strFmt, (float) quoteValue);

        //TODO:сделать функцию авторесайза текста, чтобы текст вписывался в определённый регион
        p.setTextSize(H * Utils.FLOATING_QUOTE_TEXT_SIZE_RATIO);
        int xw = (int) p.measureText(str);

        //чёрный фон для текста плавающей текущей котировки
        p.setColor(Utils.MARKER_BG_COLOR);
        RectF rect = new RectF(
                lastX + Utils.FLOATING_QUOTE_MARGIN_LEFT,
                lastY - H * Utils.FLOATING_QUOTE_MARGIN_TOP_RATIO,
                lastX + Utils.FLOATING_QUOTE_MARGIN_LEFT + xw * Utils.FLOATING_QUOTE_WIDTH_RATIO,
                lastY + H * Utils.FLOATING_QUOTE_MARGIN_BOTTOM_RATIO);
        canvas.drawRoundRect(rect, 8, 8, p);

        p.setColor(Utils.MARKER_TEXT_COLOR);
        canvas.drawText(str, lastX + 30 + 3, lastY + 5, p);
    }

    private void DrawMarker2(Canvas canvas, int xPosition, float y, double value, int decimalCount) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setFakeBoldText(true);
        p.setStrokeWidth(1);
        String strFmt = String.format("%%.%df", decimalCount);
        String str = String.format(strFmt, (float) value);

        p.setTextSize(H * 0.05f);
        int xw = (int) p.measureText(str);

        float x = (xPosition - 1) * xw * 1.2f + W / 100.0f;

        p.setColor(Utils.MARKER_SECOND_BG_COLOR);
        RectF rect = new RectF(x + 25, y - H * 0.05f, x + 25 + xw * 1.25f, y + H * 0.04545f);
        canvas.drawRoundRect(rect, 8.0f, 8.0f, p);

        p.setColor(Utils.MARKER_SECOND_TEXT_COLOR);
        canvas.drawText(str, x + 30 + 3, y + 5, p);
    }

    private void DrawPoly(Point[] point, Canvas canvas, Paint paint) {
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(point[0].x, point[0].y);
        path.lineTo(point[1].x, point[1].y);
        path.lineTo(point[2].x, point[2].y);
        path.lineTo(point[3].x, point[3].y);
        path.close();
        canvas.drawPath(path, paint);
    }

}
