package com.utils;

import android.graphics.Color;

public class ColorUtilities {

    public static int darkerBy(int color, float per) {
        per = per % 100;
        if(per == 100){
            per = 99;
        }
        float factor = 1 - (per / 100);
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a, Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }

    public static int lighterBy(int color, float per) {
        per = per % 100;
        if(per == 100){
            per = 99;
        }
        float factor = (per / 100);
        int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
        int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
        int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
        return Color.argb(Color.alpha(color), red, green, blue);
    }

}
