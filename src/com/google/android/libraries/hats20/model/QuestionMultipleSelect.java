package com.google.android.libraries.hats20.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.libraries.hats20.model.Question;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionMultipleSelect extends Question implements Question.QuestionWithSelectableAnswers {
    public static final Parcelable.Creator<QuestionMultipleSelect> CREATOR = new Parcelable.Creator<QuestionMultipleSelect>() {
        public QuestionMultipleSelect createFromParcel(Parcel parcel) {
            return new QuestionMultipleSelect(parcel);
        }

        public QuestionMultipleSelect[] newArray(int i) {
            return new QuestionMultipleSelect[i];
        }
    };
    private ArrayList<String> answers;
    private ArrayList<Integer> ordering;

    public int describeContents() {
        return 0;
    }

    public int getType() {
        return 2;
    }

    QuestionMultipleSelect(JSONObject jSONObject) throws JSONException {
        this.answers = new ArrayList<>();
        this.ordering = new ArrayList<>();
        this.questionText = jSONObject.optString("question");
        JSONArray emptyArrayIfNull = Question.toEmptyArrayIfNull(jSONObject.optJSONArray("ordering"));
        JSONArray emptyArrayIfNull2 = Question.toEmptyArrayIfNull(jSONObject.optJSONArray("answers"));
        for (int i = 0; i < emptyArrayIfNull2.length(); i++) {
            this.answers.add(emptyArrayIfNull2.getString(i));
        }
        for (int i2 = 0; i2 < emptyArrayIfNull.length(); i2++) {
            this.ordering.add(Integer.valueOf(emptyArrayIfNull.getInt(i2)));
        }
    }

    public ArrayList<String> getAnswers() {
        return this.answers;
    }

    public ArrayList<Integer> getOrdering() {
        return this.ordering;
    }

    public String toString() {
        String str = this.questionText;
        String valueOf = String.valueOf(this.answers);
        String valueOf2 = String.valueOf(this.ordering);
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 58 + String.valueOf(valueOf).length() + String.valueOf(valueOf2).length());
        sb.append("QuestionMultipleSelect{questionText=");
        sb.append(str);
        sb.append(", answers=");
        sb.append(valueOf);
        sb.append(", ordering=");
        sb.append(valueOf2);
        sb.append("}");
        return sb.toString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringList(this.answers);
        parcel.writeList(this.ordering);
        parcel.writeString(this.questionText);
    }

    private QuestionMultipleSelect(Parcel parcel) {
        this.answers = new ArrayList<>();
        this.ordering = new ArrayList<>();
        parcel.readStringList(this.answers);
        parcel.readList(this.ordering, Integer.class.getClassLoader());
        this.questionText = parcel.readString();
    }
}
