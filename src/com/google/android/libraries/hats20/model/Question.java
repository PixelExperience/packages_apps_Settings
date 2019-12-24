package com.google.android.libraries.hats20.model;

import android.os.Parcelable;
import android.util.Log;
import androidx.collection.ArrayMap;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Question implements Parcelable {
    private static final Map<String, Integer> READONLY_JSON_KEY_TO_QUESTION_TYPE;
    protected String questionText;

    public interface QuestionWithSelectableAnswers {
        List<String> getAnswers();

        List<Integer> getOrdering();
    }

    public abstract int getType();

    static {
        ArrayMap arrayMap = new ArrayMap();
        arrayMap.put("multi", 1);
        arrayMap.put("multi-select", 2);
        arrayMap.put("open-text", 3);
        arrayMap.put("rating", 4);
        READONLY_JSON_KEY_TO_QUESTION_TYPE = Collections.unmodifiableMap(arrayMap);
    }

    private static int getQuestionTypeFromString(String str) {
        Integer num = READONLY_JSON_KEY_TO_QUESTION_TYPE.get(str);
        if (num != null) {
            return num.intValue();
        }
        throw new IllegalArgumentException(String.format("Question string %s was not found in the json to QuestionType map", new Object[]{str}));
    }

    public String getQuestionText() {
        return questionText;
    }

    public static JSONArray toEmptyArrayIfNull(JSONArray jSONArray) {
        return jSONArray == null ? new JSONArray() : jSONArray;
    }

    public static Question[] getQuestionsFromSurveyDefinition(JSONObject jSONObject) throws JSONException {
        Question question;
        JSONArray jSONArray = jSONObject.getJSONObject("payload").getJSONArray("longform_questions");
        Question[] questionArr = new Question[jSONArray.length()];
        int i = 0;
        while (i < jSONArray.length()) {
            JSONObject jSONObject2 = jSONArray.getJSONObject(i);
            String string = jSONObject2.getString("type");
            int questionTypeFromString = getQuestionTypeFromString(string);
            if (questionTypeFromString == 1) {
                question = new QuestionMultipleChoice(jSONObject2);
            } else if (questionTypeFromString == 2) {
                question = new QuestionMultipleSelect(jSONObject2);
            } else if (questionTypeFromString == 3) {
                question = new QuestionOpenText(jSONObject2);
            } else if (questionTypeFromString == 4) {
                question = new QuestionRating(jSONObject2);
            } else {
                throw new UnsupportedOperationException(String.format("Attempted to deserialize an unsupported question type.  Unsupported type was: %s", new Object[]{string}));
            }
            questionArr[i] = question;
            i++;
            Log.d("HatsLibQuestionClass", String.format(Locale.US, "Parsed question %d of %d with content %s", new Object[]{Integer.valueOf(i), Integer.valueOf(jSONArray.length()), question.toString()}));
        }
        return questionArr;
    }
}
