package com.google.android.libraries.hats20.model;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.libraries.hats20.model.Question;
import com.google.android.libraries.hats20.model.QuestionRating;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SurveyController implements Parcelable {
    public static final Parcelable.Creator<SurveyController> CREATOR = new Parcelable.Creator<SurveyController>() {
        public SurveyController createFromParcel(Parcel parcel) {
            return new SurveyController(parcel);
        }

        public SurveyController[] newArray(int i) {
            return new SurveyController[i];
        }
    };
    private String answerUrl;
    private String promptMessage;
    private String promptParams;
    private Question[] questions;
    private boolean showInvitation;
    private String thankYouMessage;

    public int describeContents() {
        return 0;
    }

    private SurveyController() {
        showInvitation = true;
    }

    public Question[] getQuestions() {
        return questions;
    }

    public boolean showInvitation() {
        return showInvitation;
    }

    public boolean shouldIncludeSurveyControls() {
        Question[] questionArr = questions;
        if (questionArr.length != 1 || questionArr[0].getType() != 4) {
            return true;
        }
        if (((QuestionRating) questions[0]).getSprite() != QuestionRating.Sprite.SMILEYS) {
            return true;
        }
        return false;
    }

    public String getPromptMessage() {
        return promptMessage;
    }

    public String getThankYouMessage() {
        return thankYouMessage;
    }

    public String getPromptParams() {
        return promptParams;
    }

    public String getAnswerUrl() {
        return answerUrl;
    }

    public static SurveyController initWithSurveyFromJson(String str, Resources resources) throws JSONException, MalformedSurveyException {
        JSONObject jSONObject = new JSONObject(str).getJSONObject("params");
        SurveyController surveyController = new SurveyController();
        retrieveTagDataFromJson(surveyController, jSONObject.getJSONArray("tags"), resources);
        surveyController.questions = Question.getQuestionsFromSurveyDefinition(jSONObject);
        surveyController.promptParams = jSONObject.optString("promptParams");
        surveyController.answerUrl = jSONObject.optString("answerUrl");
        assertSurveyIsValid(surveyController);
        return surveyController;
    }

    private static void retrieveTagDataFromJson(SurveyController surveyController, JSONArray jSONArray, Resources resources) throws JSONException {
        for (int i = 0; i < jSONArray.length(); i++) {
            String[] split = jSONArray.getString(i).split("=");
            char c = 2;
            if (split.length == 2) {
                String str = split[0];
                String str2 = split[1];
                switch (str.hashCode()) {
                    case -1765207296:
                        if (str.equals("hatsNoRateLimiting")) {
                            c = 6;
                            break;
                        }
                    case -1505536394:
                        if (str.equals("showInvitation")) {
                            c = 0;
                            break;
                        }
                    case -1336354446:
                        break;
                    case -1268779017:
                        if (str.equals("format")) {
                            c = 3;
                            break;
                        }
                    case -1224386186:
                        if (str.equals("hats20")) {
                            c = 5;
                            break;
                        }
                    case -1179592925:
                        if (str.equals("hatsClient")) {
                            c = 4;
                            break;
                        }
                    case -453401085:
                        if (str.equals("promptMessage")) {
                            c = 1;
                            break;
                        }
                    default:
                        c = 65535;
                        break;
                }
                switch (c) {
                    case 0:
                        surveyController.showInvitation = Boolean.valueOf(str2).booleanValue();
                        break;
                    case 1:
                        surveyController.promptMessage = str2;
                        break;
                    case 2:
                        surveyController.thankYouMessage = str2;
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    default:
                        Log.w("HatsLibSurveyController", String.format("Skipping unknown tag '%s'", new Object[]{str}));
                        break;
                }
            } else {
                Log.e("HatsLibSurveyController", String.format("Tag couldn't be split: %s", new Object[]{jSONArray.getString(i)}));
            }
        }
        if (!surveyController.showInvitation && !TextUtils.isEmpty(surveyController.promptMessage)) {
            Log.w("HatsLibSurveyController", String.format("Survey is promptless but a prompt message was parsed: %s", new Object[]{surveyController.promptMessage}));
        }
        if (surveyController.showInvitation && TextUtils.isEmpty(surveyController.promptMessage)) {
            surveyController.promptMessage = resources.getString(R.string.hats_lib_default_prompt_title);
        }
        if (TextUtils.isEmpty(surveyController.thankYouMessage)) {
            surveyController.thankYouMessage = resources.getString(R.string.hats_lib_default_thank_you);
        }
    }

    private static void assertSurveyIsValid(SurveyController surveyController) throws MalformedSurveyException {
        if (surveyController.getQuestions().length == 0) {
            throw new MalformedSurveyException("Survey has no questions.");
        } else if (TextUtils.isEmpty(surveyController.getAnswerUrl())) {
            throw new MalformedSurveyException("Survey did not have an AnswerUrl, this is a GCS issue.");
        } else if (!TextUtils.isEmpty(surveyController.getPromptParams())) {
            int i = 0;
            while (i < surveyController.getQuestions().length) {
                Question question = surveyController.questions[i];
                if (!TextUtils.isEmpty(question.questionText)) {
                    if (question instanceof Question.QuestionWithSelectableAnswers) {
                        Question.QuestionWithSelectableAnswers questionWithSelectableAnswers = (Question.QuestionWithSelectableAnswers) question;
                        List<String> answers = questionWithSelectableAnswers.getAnswers();
                        List<Integer> ordering = questionWithSelectableAnswers.getOrdering();
                        if (answers.isEmpty()) {
                            StringBuilder sb = new StringBuilder(42);
                            sb.append("Question #");
                            sb.append(i + 1);
                            sb.append(" was missing answers.");
                            throw new MalformedSurveyException(sb.toString());
                        } else if (ordering.isEmpty()) {
                            StringBuilder sb2 = new StringBuilder(74);
                            sb2.append("Question #");
                            sb2.append(i + 1);
                            sb2.append(" was missing an ordering, this likely is a GCS issue.");
                            throw new MalformedSurveyException(sb2.toString());
                        }
                    }
                    if (question.getType() == 4) {
                        QuestionRating questionRating = (QuestionRating) question;
                        if (TextUtils.isEmpty(questionRating.getLowValueText()) || TextUtils.isEmpty(questionRating.getHighValueText())) {
                            throw new MalformedSurveyException("A rating question was missing its high/low text.");
                        } else if (questionRating.getSprite() != QuestionRating.Sprite.SMILEYS || questionRating.getNumIcons() == 5) {
                            QuestionRating.Sprite sprite = questionRating.getSprite();
                            if (!(sprite == QuestionRating.Sprite.STARS || sprite == QuestionRating.Sprite.SMILEYS)) {
                                String valueOf = String.valueOf(sprite);
                                StringBuilder sb3 = new StringBuilder(String.valueOf(valueOf).length() + 40);
                                sb3.append("Rating question has unsupported sprite: ");
                                sb3.append(valueOf);
                                throw new MalformedSurveyException(sb3.toString());
                            }
                        } else {
                            throw new MalformedSurveyException("Smiley surveys must have 5 options.");
                        }
                    }
                    i++;
                } else {
                    StringBuilder sb4 = new StringBuilder(43);
                    sb4.append("Question #");
                    sb4.append(i + 1);
                    sb4.append(" had no question text.");
                    throw new MalformedSurveyException(sb4.toString());
                }
            }
        } else {
            throw new MalformedSurveyException("Survey did not have prompt params, this is a GCS issue.");
        }
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte(showInvitation ? (byte) 1 : 0);
        parcel.writeInt(questions.length);
        for (Question writeParcelable : questions) {
            parcel.writeParcelable(writeParcelable, i);
        }
        parcel.writeString(promptMessage);
        parcel.writeString(thankYouMessage);
        parcel.writeString(promptParams);
        parcel.writeString(answerUrl);
    }

    private SurveyController(Parcel parcel) {
        boolean z = true;
        showInvitation = true;
        showInvitation = parcel.readByte() == 0 ? false : z;
        int readInt = parcel.readInt();
        questions = new Question[readInt];
        for (int i = 0; i < readInt; i++) {
            questions[i] = (Question) parcel.readParcelable(Question.class.getClassLoader());
        }
        promptMessage = parcel.readString();
        thankYouMessage = parcel.readString();
        promptParams = parcel.readString();
        answerUrl = parcel.readString();
    }

    public static class MalformedSurveyException extends Exception {
        public MalformedSurveyException(String str) {
            super(str);
        }
    }
}
