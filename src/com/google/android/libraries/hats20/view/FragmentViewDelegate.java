package com.google.android.libraries.hats20.view;

import android.graphics.Point;
import android.view.View;
import android.view.ViewTreeObserver;

public class FragmentViewDelegate implements ViewTreeObserver.OnGlobalLayoutListener {
    private View fragmentView;
    private MeasurementSurrogate measurementSurrogate;

    public interface MeasurementSurrogate {
        Point getMeasureSpecs();

        void onFragmentContentMeasurement(int i, int i2);
    }

    public void watch(MeasurementSurrogate measurementSurrogate2, View view) {
        measurementSurrogate = measurementSurrogate2;
        fragmentView = view;
        view.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public void cleanUp() {
        View view = fragmentView;
        if (view != null) {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
        fragmentView = null;
        measurementSurrogate = null;
    }

    public void onGlobalLayout() {
        Point measureSpecs = measurementSurrogate.getMeasureSpecs();
        fragmentView.measure(measureSpecs.x, measureSpecs.y);
        measurementSurrogate.onFragmentContentMeasurement(fragmentView.getMeasuredWidth(), fragmentView.getMeasuredHeight());
        cleanUp();
    }
}
