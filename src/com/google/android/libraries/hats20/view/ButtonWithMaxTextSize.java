package com.google.android.libraries.hats20.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.libraries.hats20.styleable;

public class ButtonWithMaxTextSize extends AppCompatButton {
    public ButtonWithMaxTextSize(Context context) {
        super(context);
    }

    public ButtonWithMaxTextSize(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        capTextSize(context, attributeSet);
    }

    public ButtonWithMaxTextSize(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        capTextSize(context, attributeSet);
    }

    private void capTextSize(Context context, AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.ButtonWithMaxTextSize);
        setTextSize(0, Math.min(getTextSize(), (float) obtainStyledAttributes.getDimensionPixelSize(R.styleable.ButtonWithMaxTextSize_textSizeMaxDp, Integer.MAX_VALUE)));
        obtainStyledAttributes.recycle();
    }
}
