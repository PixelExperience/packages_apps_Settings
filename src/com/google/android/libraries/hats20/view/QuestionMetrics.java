package com.google.android.libraries.hats20.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;

public class QuestionMetrics implements Parcelable {
    public static final Parcelable.Creator<QuestionMetrics> CREATOR = new Parcelable.Creator<QuestionMetrics>() {
        public QuestionMetrics createFromParcel(Parcel parcel) {
            return new QuestionMetrics(parcel);
        }

        public QuestionMetrics[] newArray(int i) {
            return new QuestionMetrics[i];
        }
    };
    private long delayEndMs;
    private long delayStartMs;

    public int describeContents() {
        return 0;
    }

    QuestionMetrics() {
        delayStartMs = -1;
        delayEndMs = -1;
    }

    private QuestionMetrics(Parcel parcel) {
        delayStartMs = parcel.readLong();
        delayEndMs = parcel.readLong();
    }

    void markAsShown() {
        if (!isShown()) {
            delayStartMs = SystemClock.elapsedRealtime();
        }
    }

    void markAsAnswered() {
        if (!isShown()) {
            Log.e("HatsLibQuestionMetrics", "Question was marked as answered but was never marked as shown.");
        } else if (isAnswered()) {
            Log.d("HatsLibQuestionMetrics", "Question was already marked as answered.");
        } else {
            delayEndMs = SystemClock.elapsedRealtime();
        }
    }

    boolean isShown() {
        return delayStartMs >= 0;
    }

    boolean isAnswered() {
        return delayEndMs >= 0;
    }

    long getDelayMs() {
        if (isAnswered()) {
            return delayEndMs - delayStartMs;
        }
        return -1;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(delayStartMs);
        parcel.writeLong(delayEndMs);
    }
}
