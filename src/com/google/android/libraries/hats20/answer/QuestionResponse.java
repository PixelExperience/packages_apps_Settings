package com.google.android.libraries.hats20.answer;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class QuestionResponse implements Parcelable {
    public static final Parcelable.Creator<QuestionResponse> CREATOR = new Parcelable.Creator<QuestionResponse>() {
        public QuestionResponse createFromParcel(Parcel parcel) {
            return new QuestionResponse(parcel);
        }

        public QuestionResponse[] newArray(int i) {
            return new QuestionResponse[i];
        }
    };
    private final long delayMs;
    private final boolean hasWriteIn;
    private final String ordering;
    private final List<String> responses;

    public int describeContents() {
        return 0;
    }

    private QuestionResponse(Builder builder) {
        this.delayMs = builder.delayMs;
        this.responses = Collections.unmodifiableList(builder.responses);
        this.ordering = builder.ordering;
        this.hasWriteIn = builder.hasWriteIn;
    }

    private QuestionResponse(Parcel parcel) {
        this.delayMs = parcel.readLong();
        this.responses = Collections.unmodifiableList(parcel.createStringArrayList());
        this.ordering = parcel.readString();
        this.hasWriteIn = parcel.readByte() != 0;
    }

    long getDelayMs() {
        return this.delayMs;
    }

    List<String> getResponses() {
        return this.responses;
    }

    String getOrdering() {
        return this.ordering;
    }

    boolean hasWriteIn() {
        return this.hasWriteIn;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.delayMs);
        parcel.writeStringList(this.responses);
        parcel.writeString(this.ordering);
        parcel.writeByte(this.hasWriteIn ? (byte) 1 : 0);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || QuestionResponse.class != obj.getClass()) {
            return false;
        }
        QuestionResponse questionResponse = (QuestionResponse) obj;
        if (this.delayMs == questionResponse.delayMs && this.hasWriteIn == questionResponse.hasWriteIn && this.responses.equals(questionResponse.responses)) {
            return this.ordering.equals(questionResponse.ordering);
        }
        return false;
    }

    public int hashCode() {
        long j = this.delayMs;
        return (((((((int) (j ^ (j >>> 32))) * 31) + this.responses.hashCode()) * 31) + this.ordering.hashCode()) * 31) + (this.hasWriteIn ? 1 : 0);
    }

    public String toString() {
        long j = this.delayMs;
        String valueOf = String.valueOf(this.responses);
        String str = this.ordering;
        boolean z = this.hasWriteIn;
        StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 89 + String.valueOf(str).length());
        sb.append("QuestionResponse{delayMs=");
        sb.append(j);
        sb.append(", responses=");
        sb.append(valueOf);
        sb.append(", ordering='");
        sb.append(str);
        sb.append("'");
        sb.append(", hasWriteIn=");
        sb.append(z);
        sb.append("}");
        return sb.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        /* access modifiers changed from: private */
        public long delayMs;
        /* access modifiers changed from: private */
        public boolean hasWriteIn;
        /* access modifiers changed from: private */
        public String ordering;
        /* access modifiers changed from: private */
        public ArrayList<String> responses;

        private Builder() {
            this.delayMs = -1;
            this.responses = new ArrayList<>(1);
            this.ordering = null;
            this.hasWriteIn = false;
        }

        public Builder setDelayMs(long j) {
            this.delayMs = j;
            return this;
        }

        public Builder addResponse(String str) {
            this.responses.add(str);
            return this;
        }

        public Builder setOrdering(List<Integer> list) {
            if (!list.isEmpty()) {
                Iterator<Integer> it = list.iterator();
                this.ordering = it.next().toString();
                while (it.hasNext()) {
                    String valueOf = String.valueOf(this.ordering);
                    String valueOf2 = String.valueOf(it.next());
                    StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 1 + String.valueOf(valueOf2).length());
                    sb.append(valueOf);
                    sb.append(".");
                    sb.append(valueOf2);
                    this.ordering = sb.toString();
                }
                return this;
            }
            throw new IllegalArgumentException("Must specify at least one ordering entry.");
        }

        public QuestionResponse build() {
            return new QuestionResponse(this);
        }
    }
}
