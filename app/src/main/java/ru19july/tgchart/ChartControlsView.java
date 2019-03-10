package ru19july.tgchart;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru19july.tgchart.R;

public class ChartControlsView extends LinearLayout {

    private final ChartViewSlider mSlider;
    private View mValue;
    private ChartView mImage;
    private String TAG = ChartControlsView.class.getSimpleName();

    public ChartControlsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ChartControlsView, 0, 0);
        String titleText = a.getString(R.styleable.ChartControlsView_titleText);
        //@SuppressWarnings("ResourceAsColor")
        //int valueColor = a.getColor(R.styleable.ChartControlsView_valueColor,
        //        android.R.color.holo_blue_light);
        a.recycle();

        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.TOP);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.chart_view, this, true);

        TextView title = (TextView) getChildAt(0);
        title.setText(titleText);

        mImage = (ChartView) getChildAt(1);
        //mImage.setVisibility(GONE);

        mSlider = (ChartViewSlider) getChildAt(2);

        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.insert_point);
        for(int i=0; i<2; i++) {
            CheckBox checkBox = new CheckBox(context);
            checkBox.setText("your text " + (i+1));
            checkBox.setChecked(i % 2 == 0);

            insertPoint.addView(checkBox, i, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG, "onCheckedChanged: " + isChecked);
                }
            });
        }

    }

    public ChartControlsView(Context context) {
        this(context, null);
    }

    public void setValueColor(int color) {
        mValue.setBackgroundColor(color);
    }

    public void setImageVisible(boolean visible) {
        mImage.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

}