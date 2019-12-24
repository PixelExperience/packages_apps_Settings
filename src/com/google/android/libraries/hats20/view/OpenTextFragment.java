package com.google.android.libraries.hats20.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;




import com.google.android.libraries.hats20.answer.QuestionResponse;
import com.google.android.libraries.hats20.model.Question;
import com.google.android.libraries.hats20.model.QuestionOpenText;
import com.google.android.libraries.hats20.view.FragmentViewDelegate;

public class OpenTextFragment extends ScrollableAnswerFragment {
    private EditText editTextField;
    private FragmentViewDelegate fragmentViewDelegate = new FragmentViewDelegate();
    private boolean isSingleLine;
    private QuestionMetrics questionMetrics;
    private String questionText;

    public boolean isResponseSatisfactory() {
        return true;
    }

    public static OpenTextFragment newInstance(Question question) {
        OpenTextFragment openTextFragment = new OpenTextFragment();
        QuestionOpenText questionOpenText = (QuestionOpenText) question;
        Bundle bundle = new Bundle();
        bundle.putString("QuestionText", questionOpenText.getQuestionText());
        bundle.putBoolean("IsSingleLine", questionOpenText.isSingleLine());
        openTextFragment.setArguments(bundle);
        return openTextFragment;
    }

    public void closeKeyboard() {
        ((InputMethodManager) getActivity().getSystemService("input_method")).hideSoftInputFromWindow(editTextField.getWindowToken(), 0);
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle arguments = getArguments();
        questionText = arguments.getString("QuestionText");
        isSingleLine = arguments.getBoolean("IsSingleLine");
        if (bundle == null) {
            questionMetrics = new QuestionMetrics();
        } else {
            questionMetrics = (QuestionMetrics) bundle.getParcelable("QuestionMetrics");
        }
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

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelable("QuestionMetrics", questionMetrics);
    }

    public void onPageScrolledIntoView() {
        questionMetrics.markAsShown();
        ((OnQuestionProgressableChangeListener) getActivity()).onQuestionProgressableChanged(isResponseSatisfactory(), this);
    }

    String getQuestionText() {
        return questionText;
    }

    View createScrollViewContents() {
        LayoutInflater from = LayoutInflater.from(getContext());
        View inflate = from.inflate(R.layout.hats_survey_scrollable_answer_content_container, (ViewGroup) null);
        inflate.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.hats_lib_open_text_question_min_height));
        LinearLayout linearLayout = (LinearLayout) inflate.findViewById(R.id.hats_lib_survey_answers_container);
        from.inflate(R.layout.hats_survey_question_open_text_item, linearLayout, true);
        editTextField = (EditText) linearLayout.findViewById(R.id.hats_lib_survey_open_text);
        editTextField.setSingleLine(isSingleLine);
        editTextField.setHint(getResources().getString(R.string.hats_lib_open_text_hint));
        return linearLayout;
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        ((OnQuestionProgressableChangeListener) getActivity()).onQuestionProgressableChanged(isResponseSatisfactory(), this);
    }

    public QuestionResponse computeQuestionResponse() {
        QuestionResponse.Builder builder = QuestionResponse.builder();
        if (questionMetrics.isShown()) {
            questionMetrics.markAsAnswered();
            builder.setDelayMs(questionMetrics.getDelayMs());
            String obj = editTextField.getText().toString();
            if (obj.trim().isEmpty()) {
                builder.addResponse("skipped");
            } else {
                builder.addResponse(obj);
            }
        }
        return builder.build();
    }
}
