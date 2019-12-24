package com.google.android.libraries.hats20.p004ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

public final class StarRatingBar extends View {
    private AccessibilityManager accessibilityManager;
    private Bitmap emptyStarBitmap;
    private int numStars = 11;
    private OnRatingChangeListener onRatingChangeListener;
    private Paint paint;
    private int rating;
    private Bitmap starBitmap;

    public interface OnRatingChangeListener {
        void onRatingChanged(int i);
    }

    public StarRatingBar(Context context) {
        super(context);
        init(context);
    }

    public StarRatingBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public StarRatingBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    public StarRatingBar(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        init(context);
    }

    private void init(Context context) {
        accessibilityManager = (AccessibilityManager) context.getSystemService("accessibility");
        starBitmap = BitmapFactory.decodeResource(context.getResources(), drawable.quantum_ic_star_black_24);
        emptyStarBitmap = BitmapFactory.decodeResource(context.getResources(), drawable.quantum_ic_star_border_grey600_24);
        paint = new Paint(5);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setOnRatingChangeListener(OnRatingChangeListener onRatingChangeListener2) {
        onRatingChangeListener = onRatingChangeListener2;
    }

    public void setNumStars(int i) {
        if (i >= 3) {
            numStars = i;
            requestLayout();
            return;
        }
        throw new IllegalArgumentException("numStars must be at least 3");
    }

    protected void onMeasure(int i, int i2) {
        setMeasuredDimension(View.resolveSize((numStars * starBitmap.getWidth()) + getPaddingLeft() + getPaddingRight(), i), View.resolveSize(starBitmap.getHeight() + getPaddingTop() + getPaddingBottom(), i2));
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() != 0 && getHeight() != 0) {
            int i = 0;
            while (i < numStars) {
                canvas.drawBitmap(i < rating ? starBitmap : emptyStarBitmap, getStarXCoord(i), (float) getPaddingTop(), paint);
                i++;
            }
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction() & 255;
        if (action != 0 && action != 2) {
            return false;
        }
        setRating(getRatingAtTouchPoint(motionEvent.getX(), motionEvent.getY()));
        return true;
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 21) {
            setRating(rating - 1);
            return true;
        } else if (i != 22) {
            return super.onKeyDown(i, keyEvent);
        } else {
            setRating(rating + 1);
            return true;
        }
    }

    private void setRating(int i) {
        if (i > 0 && i <= numStars && i != rating) {
            rating = i;
            invalidate();
            OnRatingChangeListener onRatingChangeListener2 = onRatingChangeListener;
            if (onRatingChangeListener2 != null) {
                onRatingChangeListener2.onRatingChanged(rating);
            }
            if (accessibilityManager.isEnabled()) {
                sendAccessibilityEvent(4);
            }
        }
    }

    private float getStarXCoord(int i) {
        return ((float) getPaddingLeft()) + (((float) i) * getDistanceBetweenStars());
    }

    private float getDistanceBetweenStars() {
        return ((float) (((getWidth() - getPaddingLeft()) - getPaddingRight()) - starBitmap.getWidth())) / ((float) (numStars - 1));
    }

    private int getRatingAtTouchPoint(float f, float f2) {
        float distanceBetweenStars = getDistanceBetweenStars();
        int i = 1;
        for (float paddingLeft = ((float) getPaddingLeft()) + (((float) starBitmap.getWidth()) / 2.0f) + (distanceBetweenStars / 2.0f); paddingLeft < f && i < numStars; paddingLeft += distanceBetweenStars) {
            i++;
        }
        return i;
    }

    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.numStars = numStars;
        savedState.rating = rating;
        return savedState;
    }

    protected void onRestoreInstanceState(Parcelable parcelable) {
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        numStars = savedState.numStars;
        rating = savedState.rating;
    }

    private static final class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        int numStars;
        int rating;

        private SavedState(Parcel parcel) {
            super(parcel);
            numStars = parcel.readInt();
            rating = parcel.readInt();
        }

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(numStars);
            parcel.writeInt(rating);
        }
    }
}
