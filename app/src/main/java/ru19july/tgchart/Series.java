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
    private boolean mChecked;

    public Series() {
    }

    public Series(String name, String title, String type, String color, List<Long> values){
        mName = name;
        mTitle = title;
        mType = type;
        mColor = color;
        mValues = values;
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
    }

    public void setChecked(boolean mChecked) {
        this.mChecked = mChecked;
    }

    public boolean isChecked() {
        return mChecked;
    }
}
