package ru19july.tgchart.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils{

    public static final int DEFAULT_DECIMAL_COUNT = 0;
    public static final int FLOATING_QUOTE_MARGIN_LEFT = 25;
    public static final float FLOATING_QUOTE_MARGIN_TOP_RATIO = 1.0f/15f;
    public static final float FLOATING_QUOTE_WIDTH_RATIO = 1.3f;
    public static final float FLOATING_QUOTE_MARGIN_BOTTOM_RATIO = 1.0f/20f;
    public static final float FLOATING_QUOTE_TEXT_SIZE_RATIO = 1.0f / 15f;

    public static String unixtimeToString(int unixtime, String datetimeFormat) {
        Date date = new Date(unixtime * 1000L);
        SimpleDateFormat sdfDate = new SimpleDateFormat(datetimeFormat);
        sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdfDate.format(date);
    }

}
