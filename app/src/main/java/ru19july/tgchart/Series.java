package ru19july.tgchart;

import java.util.Arrays;
import java.util.List;

class Series {
    public String name;
    public String title;
    public String type;
    public String color;
    public List<Long> values;
    private boolean checked;

    @Override
    public String toString(){
        return String.format("%s - %s; type:%s; color:%s => %s", name, title, type, color, Arrays.toString(values.toArray()));
    }

    public void setChecked(boolean isChecked) {
        this.checked = isChecked;
    }

    public boolean isChecked(){
        return checked;
    }
}
