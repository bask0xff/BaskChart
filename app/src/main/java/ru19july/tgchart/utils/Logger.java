package ru19july.tgchart.utils;

import android.util.Log;

public class Logger {

    private static boolean Enabled = true;
    private static boolean saved = Enabled;

    public static void i(String tag, String message) {
        if (Enabled) Log.i(tag, message);
    }

    //forced logging
    public static void f(String tag, String message) {
        Log.i(tag, message);
    }

    public static void e(String tag, String message) {
        if(Enabled) Log.e(tag, message);
    }

    public static void w(String tag, String message) {
        if(Enabled) Log.w(tag, message);
    }

    public static void d(String tag, String message) {
        if(Enabled) Log.d(tag, message);
    }

    public static void v(String tag, String message) {
        if(Enabled) Log.v(tag, message);
    }

    public static void On(){
        saved = Enabled;
        Enabled = true;
    }

    public static void Off(){
        saved = Enabled;
        Enabled = false;
    }

    public static void Restore() {
        Enabled = saved;
    }
}
