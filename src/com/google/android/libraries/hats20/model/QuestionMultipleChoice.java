package com.google.android.libraries.hats20.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.collection.ArrayMap;
import com.google.android.libraries.hats20.drawable;
import com.google.android.libraries.hats20.model.Question;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionMultipleChoice extends Question implements Question.QuestionWithSelectableAnswers {
    public static final Parcelable.Creator<QuestionMultipleChoice> CREATOR = new Parcelable.Creator<QuestionMultipleChoice>() {
        public QuestionMultipleChoice createFromParcel(Parcel parcel) {
            return new QuestionMultipleChoice(parcel);
        }

        public QuestionMultipleChoice[] newArray(int i) {
            return new QuestionMultipleChoice[i];
        }
    };
    public static final Map<Integer, Integer> READONLY_SURVEY_RATING_ICON_RESOURCE_MAP;
    private ArrayList<String> answers;
    private ArrayList<Integer> ordering;
    private String spriteName;

    public int describeContents() {
        return 0;
    }

    public int getType() {
        return 1;
    }

    static {
        ArrayMap arrayMap = new ArrayMap();
        arrayMap.put(0, Integer.valueOf(R.drawable.hats_smiley_5));
        arrayMap.put(1, Integer.valueOf(R.drawable.hats_smiley_4));
        arrayMap.put(2, Integer.valueOf(R.drawable.hats_smiley_3));
        arrayMap.put(3, Integer.valueOf(R.drawable.hats_smiley_2));
        arrayMap.put(4, Integer.valueOf(R.drawable.hats_smiley_1));
        READONLY_SURVEY_RATING_ICON_RESOURCE_MAP = Collections.unmodifiableMap(arrayMap);
    }

    QuestionMultipleChoice(JSONObject jSONObject) throws JSONException {
        answers = new ArrayList<>();
        ordering = new ArrayList<>();
        questionText = jSONObject.optString("question");
        JSONArray emptyArrayIfNull = Question.toEmptyArrayIfNull(jSONObject.optJSONArray("ordering"));
        JSONArray emptyArrayIfNull2 = Question.toEmptyArrayIfNull(jSONObject.optJSONArray("answers"));
        for (int i = 0; i < emptyArrayIfNull2.length(); i++) {
            answers.add(emptyArrayIfNull2.getString(i));
        }
        for (int i2 = 0; i2 < emptyArrayIfNull.length(); i2++) {
            ordering.add(Integer.valueOf(emptyArrayIfNull.getInt(i2)));
        }
        spriteName = jSONObject.optString("sprite_name");
    }

    public ArrayList<String> getAnswers() {
        return answers;
    }

    public ArrayList<Integer> getOrdering() {
        return ordering;
    }

    public String getSpriteName() {
        return spriteName;
    }

    public String toString() {
        String str = questionText;
        String valueOf = String.valueOf(answers);
        String valueOf2 = String.valueOf(ordering);
        String str2 = spriteName;
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 71 + String.valueOf(valueOf).length() + String.valueOf(valueOf2).length() + String.valueOf(str2).length());
        sb.append("QuestionMultipleChoice{questionText=");
        sb.append(str);
        sb.append(", answers=");
        sb.append(valueOf);
        sb.append(", ordering=");
        sb.append(valueOf2);
        sb.append(", spriteName=");
        sb.append(str2);
        sb.append("}");
        return sb.toString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringList(answers);
        parcel.writeList(ordering);
        parcel.writeString(questionText);
        parcel.writeString(spriteName);
    }

    private QuestionMultipleChoice(Parcel parcel) {
        answers = new ArrayList<>();
        ordering = new ArrayList<>();
        parcel.readStringList(answers);
        parcel.readList(ordering, Integer.class.getClassLoader());
        questionText = parcel.readString();
        spriteName = parcel.readString();
    }
}
