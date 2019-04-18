package ru19july.baskchart.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utils{

    public static final int DEFAULT_DECIMAL_COUNT = 0;
    public static final int FLOATING_MARGIN_LEFT = 25;
    public static final float FLOATING_WIDTH_RATIO = 1.3f;
    public static final float FLOATING_MARGIN_BOTTOM_RATIO = 1.0f/20f;
    public static final float FLOATING_TEXT_SIZE_RATIO = 1.0f / 15f;

    public static String convertTime(long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("MMM dd HH:mm", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(date);
    }

    public static String convertTime(long time, String fmt) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat(fmt, Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(date);
    }

    public static String unixtimeToString(long unixtime, String datetimeFormat) {
        Date date = new Date(unixtime);
        SimpleDateFormat sdfDate = new SimpleDateFormat(datetimeFormat, Locale.ENGLISH);
        sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdfDate.format(date);
    }

}
