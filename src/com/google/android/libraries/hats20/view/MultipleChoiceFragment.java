package com.google.android.libraries.hats20.view;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.libraries.hats20.id;
import com.google.android.libraries.hats20.layout;
import com.google.android.libraries.hats20.SurveyPromptActivity;
import com.google.android.libraries.hats20.answer.QuestionResponse;
import com.google.android.libraries.hats20.model.Question;
import com.google.android.libraries.hats20.model.QuestionMultipleChoice;
import com.google.android.libraries.hats20.view.FragmentViewDelegate;
import java.util.ArrayList;

public class MultipleChoiceFragment extends ScrollableAnswerFragment {
    private ArrayList<String> answers;
    private FragmentViewDelegate fragmentViewDelegate = new FragmentViewDelegate();
    private boolean hasSmileys = false;
    private ArrayList<Integer> ordering;
    private QuestionMetrics questionMetrics;
    private String questionText;
    private String selectedResponse;

    public boolean isResponseSatisfactory() {
        return false;
    }

    public static MultipleChoiceFragment newInstance(Question question) {
        MultipleChoiceFragment multipleChoiceFragment = new MultipleChoiceFragment();
        QuestionMultipleChoice questionMultipleChoice = (QuestionMultipleChoice) question;
        String spriteName = questionMultipleChoice.getSpriteName();
        boolean z = spriteName != null && spriteName.equals("smileys");
        if (z && questionMultipleChoice.getAnswers().size() != 5) {
            Log.e("HatsLibMultiChoiceFrag", "Multiple choice with smileys survey must have exactly five answers.");
            z = false;
        }
        Bundle bundle = new Bundle();
        bundle.putString("QuestionText", question.getQuestionText());
        bundle.putStringArrayList("AnswersAsArray", questionMultipleChoice.getAnswers());
        bundle.putIntegerArrayList("OrderingAsArray", questionMultipleChoice.getOrdering());
        bundle.putBoolean("Smileys", z);
        multipleChoiceFragment.setArguments(bundle);
        return multipleChoiceFragment;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle arguments = getArguments();
        questionText = arguments.getString("QuestionText");
        answers = arguments.getStringArrayList("AnswersAsArray");
        ordering = arguments.getIntegerArrayList("OrderingAsArray");
        hasSmileys = arguments.getBoolean("Smileys");
        if (bundle != null) {
            selectedResponse = bundle.getString("SelectedResponse", (String) null);
            questionMetrics = (QuestionMetrics) bundle.getParcelable("QuestionMetrics");
        }
        if (questionMetrics == null) {
            questionMetrics = new QuestionMetrics();
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString("SelectedResponse", selectedResponse);
        bundle.putParcelable("QuestionMetrics", questionMetrics);
    }

    public void onPageScrolledIntoView() {
        questionMetrics.markAsShown();
        ((OnQuestionProgressableChangeListener) getActivity()).onQuestionProgressableChanged(isResponseSatisfactory(), this);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View onCreateView = super.onCreateView(layoutInflater, viewGroup, bundle);
        onCreateView.setContentDescription(questionText);
        if (!isDetached()) {
            fragmentViewDelegate.watch((FragmentViewDelegate.MeasurementSurrogate) getActivity(), onCreateView);
        }
        return onCreateView;
    }

    public void onDetach() {
        fragmentViewDelegate.cleanUp();
        super.onDetach();
    }

    String getQuestionText() {
        return questionText;
    }

    public View createScrollViewContents() {
        LayoutInflater from = LayoutInflater.from(getContext());
        View inflate = from.inflate(layout.hats_survey_scrollable_answer_content_container, (ViewGroup) null);
        LinearLayout linearLayout = (LinearLayout) inflate.findViewById(id.hats_lib_survey_answers_container);
        final View[] viewArr = new View[answers.size()];
        for (final int i = 0; i < answers.size(); i++) {
            if (hasSmileys) {
                from.inflate(layout.hats_survey_question_multiple_choice_with_smileys_item, linearLayout, true);
                viewArr[i] = linearLayout.getChildAt(linearLayout.getChildCount() - 1);
                TextView textView = (TextView) viewArr[i].findViewById(id.hats_lib_survey_multiple_choice_text);
                textView.setText(answers.get(i));
                textView.setContentDescription(answers.get(i));
                ((ImageView) viewArr[i].findViewById(id.hats_lib_survey_multiple_choice_icon)).setImageResource(QuestionMultipleChoice.READONLY_SURVEY_RATING_ICON_RESOURCE_MAP.get(Integer.valueOf(i)).intValue());
            } else {
                from.inflate(layout.hats_survey_question_multiple_choice_item, linearLayout, true);
                viewArr[i] = linearLayout.getChildAt(linearLayout.getChildCount() - 1);
                ((Button) viewArr[i]).setText(answers.get(i));
                ((Button) viewArr[i]).setContentDescription(answers.get(i));
            }
            viewArr[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    MultipleChoiceFragment.removeOnClickListenersAndDisableClickEvents(viewArr);
                    ((SurveyPromptActivity) MultipleChoiceFragment.getActivity()).setIsMultipleChoiceSelectionAnimating(true);
                    view.postOnAnimationDelayed(new Runnable() {
                        public void run() {
                            SurveyPromptActivity surveyPromptActivity = (SurveyPromptActivity) MultipleChoiceFragment.getActivity();
                            boolean isDestroyed = Build.VERSION.SDK_INT >= 17 ? surveyPromptActivity.isDestroyed() : false;
                            if (surveyPromptActivity == null || surveyPromptActivity.isFinishing() || isDestroyed) {
                                Log.w("HatsLibMultiChoiceFrag", "Activity was null, finishing or destroyed while attempting to navigate to the next next page. Likely the user rotated the device before the Runnable executed.");
                                return;
                            }
                            MultipleChoiceFragment multipleChoiceFragment = MultipleChoiceFragment.this;
                            String unused = multipleChoiceFragment.selectedResponse = (String) multipleChoiceFragment.answers.get(i);
                            MultipleChoiceFragment.questionMetrics.markAsAnswered();
                            String valueOf = String.valueOf((String) MultipleChoiceFragment.answers.get(i));
                            Log.d("HatsLibMultiChoiceFrag", valueOf.length() != 0 ? "User selected response: ".concat(valueOf) : new String("User selected response: "));
                            surveyPromptActivity.nextPageOrSubmit();
                            surveyPromptActivity.setIsMultipleChoiceSelectionAnimating(false);
                        }
                    }, 200);
                }
            });
        }
        return inflate;
    }

    public QuestionResponse computeQuestionResponse() {
        QuestionResponse.Builder builder = QuestionResponse.builder();
        if (questionMetrics.isShown()) {
            String str = selectedResponse;
            if (str != null) {
                builder.addResponse(str);
            }
            builder.setDelayMs(questionMetrics.getDelayMs());
            ArrayList<Integer> arrayList = ordering;
            if (arrayList != null) {
                builder.setOrdering(arrayList);
            }
        }
        return builder.build();
    }

    private void removeOnClickListenersAndDisableClickEvents(View[] viewArr) {
        for (View view : viewArr) {
            view.setOnClickListener((View.OnClickListener) null);
            view.setClickable(false);
        }
    }
}
