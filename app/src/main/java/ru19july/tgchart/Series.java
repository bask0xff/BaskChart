package ru19july.tgchart;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Series implements Serializable {
    private String mName;
    private String mTitle;
    private String mType;
    private String mColor;
    private List<Long> mValues;
    private float mMinValue;
    private float mMaxValue;
    private boolean mChecked;

    public Series() {
    }

    public Series(String name, String title, String type, String color, List<Long> values){
        setName(name);
        setTitle(title);
        setType(type);
        setColor(color);
        setValues(values);
    }

    @Override
    public String toString(){
        return String.format("%s - %s; type:%s; color:%s => %s", mName, mTitle, mType, mColor, Arrays.toString(mValues.toArray()));
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getType() {
        return mType;
    }

    public void setType(String mType) {
        this.mType = mType;
    }

    public String getColor() {
        return mColor;
    }

    public void setColor(String mColor) {
        this.mColor = mColor;
    }

    public List<Long> getValues() {
        return mValues;
    }

    public void setValues(List<Long> mValues) {
        this.mValues = mValues;

        MinMax minMax = FindMinMax(mValues);
        mMinValue = minMax.min;
        mMaxValue = minMax.max;
    }

    private MinMax FindMinMax(List<Long> values) {
        MinMax result = new MinMax();
        result.min = Float.MAX_VALUE;
        result.max = Float.MIN_VALUE;

        for (int i = 0; i < values.size(); i++) {
            int k = values.size() - i - 1;
            if (k >= 0 && k < values.size()) {
                Long q = values.get(k);
                if (q > result.max) result.max = q;
                if (q < result.min) result.min = q;
            } else {
                return null;
            }
        }
        return result;
    }

    public void setChecked(boolean mChecked) {
        this.mChecked = mChecked;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public float getMinValue() {
        return mMinValue;
    }

    public float getMaxValue() {
        return mMaxValue;
    }
}
