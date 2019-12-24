package com.google.android.libraries.hats20.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;
import com.google.android.libraries.material.autoresizetext.AutoResizeTextView;

public final class LayoutUtils {
    public static Point getNavigationBarDimensionPixelSize(Activity activity) {
        Point realScreenDimensions = getRealScreenDimensions(activity);
        Point usableContentDimensions = getUsableContentDimensions(activity);
        int i = usableContentDimensions.x;
        int i2 = realScreenDimensions.x;
        if (i < i2) {
            return new Point(i2 - i, usableContentDimensions.y);
        }
        int i3 = usableContentDimensions.y;
        int i4 = realScreenDimensions.y;
        if (i3 < i4) {
            return new Point(i, i4 - i3);
        }
        return new Point();
    }

    public static Point getUsableContentDimensions(Context context) {
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        return point;
    }

    public static Point getRealScreenDimensions(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        return new Point(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    public static boolean isNavigationBarOnRight(Activity activity) {
        return getUsableContentDimensions(activity).x < getRealScreenDimensions(activity).x;
    }

    public static void fitTextInTextViewWrapIfNeeded(float f, int i, int i2, String str, AutoResizeTextView autoResizeTextView) {
        Integer fontSizeGivenSpace = getFontSizeGivenSpace(f, i, i2, str, autoResizeTextView.getContext());
        if (fontSizeGivenSpace == null) {
            float f2 = (float) i2;
            autoResizeTextView.setMinTextSize(1, f2);
            autoResizeTextView.setTextSize(2, f2);
            autoResizeTextView.setLines(2);
            autoResizeTextView.setMaxLines(2);
            return;
        }
        autoResizeTextView.setTextSize(1, (float) fontSizeGivenSpace.intValue());
        autoResizeTextView.setLines(1);
        autoResizeTextView.setMaxLines(1);
    }

    private static Integer getFontSizeGivenSpace(float f, int i, int i2, String str, Context context) {
        int i3;
        TextView textView = new TextView(context);
        int round = Math.round(((float) i) * context.getResources().getConfiguration().fontScale);
        float measureTextAtFontSize = measureTextAtFontSize(round, str, textView);
        while (true) {
            i3 = (measureTextAtFontSize > f ? 1 : (measureTextAtFontSize == f ? 0 : -1));
            if (i3 > 0 && round > i2) {
                round--;
                measureTextAtFontSize = measureTextAtFontSize(round, str, textView);
            } else if (i3 <= 0) {
                return null;
            } else {
                return Integer.valueOf(round);
            }
        }
        if (i3 <= 0) {
        }
    }

    private static float measureTextAtFontSize(int i, String str, TextView textView) {
        textView.setTextSize(1, (float) i);
        return textView.getPaint().measureText(str);
    }
}
