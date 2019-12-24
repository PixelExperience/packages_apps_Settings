package com.google.android.libraries.hats20.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.collection.ArrayMap;
import com.google.android.libraries.hats20.drawable;
import java.util.Collections;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionRating extends Question {
    public static final Parcelable.Creator<QuestionRating> CREATOR = new Parcelable.Creator<QuestionRating>() {
        public QuestionRating createFromParcel(Parcel parcel) {
            return new QuestionRating(parcel);
        }

        public QuestionRating[] newArray(int i) {
            return new QuestionRating[i];
        }
    };
    public static final Map<Integer, Integer> READONLY_SURVEY_RATING_ICON_RESOURCE_MAP;
    private final String highValueText;
    private final String lowValueText;
    private final int numIcons;
    private final Sprite sprite;

    public enum Sprite {
        STARS,
        SMILEYS
    }

    public int describeContents() {
        return 0;
    }

    public int getType() {
        return 4;
    }

    static {
        ArrayMap arrayMap = new ArrayMap();
        arrayMap.put(0, Integer.valueOf(drawable.hats_smiley_1));
        arrayMap.put(1, Integer.valueOf(drawable.hats_smiley_2));
        arrayMap.put(2, Integer.valueOf(drawable.hats_smiley_3));
        arrayMap.put(3, Integer.valueOf(drawable.hats_smiley_4));
        arrayMap.put(4, Integer.valueOf(drawable.hats_smiley_5));
        READONLY_SURVEY_RATING_ICON_RESOURCE_MAP = Collections.unmodifiableMap(arrayMap);
    }

    QuestionRating(JSONObject jSONObject) throws JSONException {
        questionText = jSONObject.optString("question");
        lowValueText = jSONObject.optString("low_value");
        highValueText = jSONObject.optString("high_value");
        numIcons = jSONObject.getInt("num_stars");
        sprite = numIcons == 5 ? Sprite.SMILEYS : Sprite.STARS;
    }

    public String getLowValueText() {
        return lowValueText;
    }

    public String getHighValueText() {
        return highValueText;
    }

    public int getNumIcons() {
        return numIcons;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public String toString() {
        String str = questionText;
        String str2 = lowValueText;
        String str3 = highValueText;
        int i = numIcons;
        String valueOf = String.valueOf(sprite);
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 97 + String.valueOf(str2).length() + String.valueOf(str3).length() + String.valueOf(valueOf).length());
        sb.append("QuestionRating{questionText='");
        sb.append(str);
        sb.append("'");
        sb.append(", lowValueText='");
        sb.append(str2);
        sb.append("'");
        sb.append(", highValueText='");
        sb.append(str3);
        sb.append("'");
        sb.append(", numIcons=");
        sb.append(i);
        sb.append(", sprite=");
        sb.append(valueOf);
        sb.append("}");
        return sb.toString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(lowValueText);
        parcel.writeString(highValueText);
        parcel.writeInt(numIcons);
        parcel.writeString(questionText);
        parcel.writeSerializable(sprite);
    }

    private QuestionRating(Parcel parcel) {
        lowValueText = parcel.readString();
        highValueText = parcel.readString();
        numIcons = parcel.readInt();
        questionText = parcel.readString();
        sprite = (Sprite) parcel.readSerializable();
    }
}
