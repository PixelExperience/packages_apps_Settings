package com.google.android.libraries.hats20.model;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionOpenText extends Question {
    public static final Parcelable.Creator<QuestionOpenText> CREATOR = new Parcelable.Creator<QuestionOpenText>() {
        public QuestionOpenText createFromParcel(Parcel parcel) {
            return new QuestionOpenText(parcel);
        }

        public QuestionOpenText[] newArray(int i) {
            return new QuestionOpenText[i];
        }
    };
    private boolean singleLine;

    public int describeContents() {
        return 0;
    }

    public int getType() {
        return 3;
    }

    QuestionOpenText(JSONObject jSONObject) throws JSONException {
        questionText = jSONObject.optString("question");
        singleLine = jSONObject.optBoolean("single_line");
    }

    public boolean isSingleLine() {
        return singleLine;
    }

    public String toString() {
        String str = questionText;
        boolean z = singleLine;
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 49);
        sb.append("QuestionOpenText{questionText=");
        sb.append(str);
        sb.append(", singleLine=");
        sb.append(z);
        sb.append("}");
        return sb.toString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte(singleLine ? (byte) 1 : 0);
        parcel.writeString(questionText);
    }

    private QuestionOpenText(Parcel parcel) {
        singleLine = parcel.readByte() != 0;
        questionText = parcel.readString();
    }
}
