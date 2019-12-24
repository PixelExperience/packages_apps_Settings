package com.google.android.libraries.hats20.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;



import com.google.android.libraries.hats20.answer.QuestionResponse;
import com.google.android.libraries.hats20.model.Question;
import com.google.android.libraries.hats20.model.QuestionMultipleSelect;
import com.google.android.libraries.hats20.view.FragmentViewDelegate;
import java.util.ArrayList;

public class MultipleSelectFragment extends ScrollableAnswerFragment {
    private ArrayList<String> answers;
    private ViewGroup answersContainer;
    private FragmentViewDelegate fragmentViewDelegate = new FragmentViewDelegate();
    private boolean isNoneOfTheAboveChecked;
    private ArrayList<Integer> ordering;
    private QuestionMetrics questionMetrics;
    private String questionText;
    private boolean[] responses;

    public static MultipleSelectFragment newInstance(Question question) {
        MultipleSelectFragment multipleSelectFragment = new MultipleSelectFragment();
        QuestionMultipleSelect questionMultipleSelect = (QuestionMultipleSelect) question;
        Bundle bundle = new Bundle();
        bundle.putString("QuestionText", question.getQuestionText());
        bundle.putStringArrayList("AnswersAsArray", questionMultipleSelect.getAnswers());
        bundle.putIntegerArrayList("OrderingAsArray", questionMultipleSelect.getOrdering());
        multipleSelectFragment.setArguments(bundle);
        return multipleSelectFragment;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle arguments = getArguments();
        questionText = arguments.getString("QuestionText");
        answers = arguments.getStringArrayList("AnswersAsArray");
        ordering = arguments.getIntegerArrayList("OrderingAsArray");
        if (bundle != null) {
            isNoneOfTheAboveChecked = bundle.getBoolean("NoneOfTheAboveAsBoolean", false);
            questionMetrics = (QuestionMetrics) bundle.getParcelable("QuestionMetrics");
            responses = bundle.getBooleanArray("ResponsesAsArray");
        }
        if (questionMetrics == null) {
            questionMetrics = new QuestionMetrics();
        }
        boolean[] zArr = responses;
        if (zArr == null) {
            responses = new boolean[answers.size()];
        } else if (zArr.length != answers.size()) {
            int length = responses.length;
            StringBuilder sb = new StringBuilder(64);
            sb.append("Saved instance state responses had incorrect length: ");
            sb.append(length);
            Log.e("HatsLibMultiSelectFrag", sb.toString());
            responses = new boolean[answers.size()];
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("NoneOfTheAboveAsBoolean", isNoneOfTheAboveChecked);
        bundle.putParcelable("QuestionMetrics", questionMetrics);
        bundle.putBooleanArray("ResponsesAsArray", responses);
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

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        ((OnQuestionProgressableChangeListener) getActivity()).onQuestionProgressableChanged(isResponseSatisfactory(), this);
    }

    public void onDetach() {
        fragmentViewDelegate.cleanUp();
        super.onDetach();
    }

    String getQuestionText() {
        return questionText;
    }

    public View createScrollViewContents() {
        answersContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.hats_survey_scrollable_answer_content_container, (ViewGroup) null).findViewById(R.id.hats_lib_survey_answers_container);
        for (int i = 0; i < answers.size(); i++) {
            addCheckboxToAnswersContainer(answers.get(i), responses[i], i, (String) null);
        }
        addCheckboxToAnswersContainer(getResources().getString(R.string..hats_lib_none_of_the_above), isNoneOfTheAboveChecked, answers.size(), "NoneOfTheAbove");
        return answersContainer;
    }

    private void addCheckboxToAnswersContainer(String str, boolean z, int i, String str2) {
        LayoutInflater.from(getContext()).inflate(R.layout.hats_survey_question_multiple_select_item, answersContainer, true);
        FrameLayout frameLayout = (FrameLayout) answersContainer.getChildAt(i);
        final CheckBox checkBox = (CheckBox) frameLayout.findViewById(R.id.hats_lib_multiple_select_checkbox);
        checkBox.setText(str);
        checkBox.setContentDescription(str);
        checkBox.setChecked(z);
        checkBox.setOnCheckedChangeListener(new CheckboxChangeListener(i));
        frameLayout.setOnClickListener(new View.OnClickListener(this) {
            public void onClick(View view) {
                checkBox.performClick();
            }
        });
        if (str2 != null) {
            checkBox.setTag(str2);
        }
    }

    public QuestionResponse computeQuestionResponse() {
        QuestionResponse.Builder builder = QuestionResponse.builder();
        if (questionMetrics.isShown()) {
            ArrayList<Integer> arrayList = ordering;
            if (arrayList != null) {
                builder.setOrdering(arrayList);
            }
            if (!isNoneOfTheAboveChecked) {
                int i = 0;
                while (true) {
                    boolean[] zArr = responses;
                    if (i >= zArr.length) {
                        break;
                    }
                    if (zArr[i]) {
                        builder.addResponse(answers.get(i));
                        questionMetrics.markAsAnswered();
                    }
                    i++;
                }
            } else {
                questionMetrics.markAsAnswered();
            }
            builder.setDelayMs(questionMetrics.getDelayMs());
        }
        return builder.build();
    }

    private class CheckboxChangeListener implements CompoundButton.OnCheckedChangeListener {
        private final int index;

        CheckboxChangeListener(int i) {
            index = i;
        }

        public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
            if ("NoneOfTheAbove".equals(compoundButton.getTag())) {
                boolean unused = MultipleSelectFragment.isNoneOfTheAboveChecked = z;
                if (z) {
                    uncheckAllButNoneOfAbove();
                }
            } else {
                MultipleSelectFragment.responses[index] = z;
                if (z) {
                    ((CheckBox) MultipleSelectFragment.answersContainer.findViewWithTag("NoneOfTheAbove")).setChecked(false);
                }
            }
            OnQuestionProgressableChangeListener onQuestionProgressableChangeListener = (OnQuestionProgressableChangeListener) MultipleSelectFragment.getActivity();
            if (onQuestionProgressableChangeListener != null) {
                onQuestionProgressableChangeListener.onQuestionProgressableChanged(MultipleSelectFragment.isResponseSatisfactory(), MultipleSelectFragment.this);
            }
        }

        private void uncheckAllButNoneOfAbove() {
            if (MultipleSelectFragment.answersContainer.getChildCount() != MultipleSelectFragment.responses.length + 1) {
                Log.e("HatsLibMultiSelectFrag", "Number of children (checkboxes) contained in the answers container was not equal to the number of possible responses including \"None of the Above\". Note this is not expected to happen in prod.");
            }
            for (int i = 0; i < MultipleSelectFragment.answersContainer.getChildCount(); i++) {
                CheckBox checkBox = (CheckBox) MultipleSelectFragment.answersContainer.getChildAt(i).findViewById(R.id.hats_lib_multiple_select_checkbox);
                if (!"NoneOfTheAbove".equals(checkBox.getTag())) {
                    checkBox.setChecked(false);
                }
            }
        }
    }

    public boolean isResponseSatisfactory() {
        if (isNoneOfTheAboveChecked) {
            return true;
        }
        for (boolean z : responses) {
            if (z) {
                return true;
            }
        }
        return false;
    }
}
