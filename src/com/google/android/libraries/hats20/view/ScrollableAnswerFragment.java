package com.google.android.libraries.hats20.view;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;



import com.google.android.libraries.hats20.SurveyPromptActivity;
import com.google.android.libraries.hats20.util.TextFormatUtil;
import com.google.android.libraries.hats20.view.ScrollViewWithSizeCallback;

public abstract class ScrollableAnswerFragment extends BaseFragment {
    private boolean isOnScrollChangedListenerAttached = false;
    private TextView questionTextView;
    private ScrollShadowHandler scrollShadowHandler = new ScrollShadowHandler();
    private ScrollViewWithSizeCallback scrollView;
    private View scrollViewContents;
    private View surveyControlsContainer;

    abstract View createScrollViewContents();

    abstract String getQuestionText();

    public void onDetach() {
        stopRespondingToScrollChanges();
        super.onDetach();
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.hats_survey_question_with_scrollable_content, viewGroup, false);
        questionTextView = (TextView) inflate.findViewById(R.id.hats_lib_survey_question_text);
        questionTextView.setText(TextFormatUtil.format(getQuestionText()));
        questionTextView.setContentDescription(getQuestionText());
        scrollViewContents = createScrollViewContents();
        scrollView = (ScrollViewWithSizeCallback) inflate.findViewById(R.id.hats_survey_question_scroll_view);
        scrollView.addView(scrollViewContents);
        scrollView.setOnHeightChangedListener(scrollShadowHandler);
        startRespondingToScrollChanges();
        surveyControlsContainer = ((SurveyPromptActivity) viewGroup.getContext()).getSurveyContainer().findViewById(R.id.hats_lib_survey_controls_container);
        return inflate;
    }

    private void startRespondingToScrollChanges() {
        ScrollViewWithSizeCallback scrollViewWithSizeCallback;
        if (!isOnScrollChangedListenerAttached && (scrollViewWithSizeCallback = scrollView) != null) {
            scrollViewWithSizeCallback.getViewTreeObserver().addOnScrollChangedListener(scrollShadowHandler);
            isOnScrollChangedListenerAttached = true;
        }
    }

    private void stopRespondingToScrollChanges() {
        ScrollViewWithSizeCallback scrollViewWithSizeCallback;
        if (isOnScrollChangedListenerAttached && (scrollViewWithSizeCallback = scrollView) != null) {
            scrollViewWithSizeCallback.getViewTreeObserver().removeOnScrollChangedListener(scrollShadowHandler);
            isOnScrollChangedListenerAttached = false;
        }
    }

    private class ScrollShadowHandler implements ViewTreeObserver.OnScrollChangedListener, ScrollViewWithSizeCallback.OnHeightChangedListener {
        private ScrollShadowHandler() {
        }

        public void onScrollChanged() {
            updateShadowVisibility(ScrollableAnswerFragment.scrollView.getHeight());
        }

        public void onHeightChanged(int i) {
            if (i != 0) {
                updateShadowVisibility(i);
            }
        }

        private void updateShadowVisibility(int i) {
            if (ScrollableAnswerFragment.getUserVisibleHint()) {
                boolean z = true;
                boolean z2 = ScrollableAnswerFragment.scrollView.getScrollY() == 0;
                boolean z3 = ScrollableAnswerFragment.scrollViewContents.getBottom() == ScrollableAnswerFragment.scrollView.getScrollY() + i;
                if (ScrollableAnswerFragment.scrollViewContents.getBottom() <= i) {
                    z = false;
                }
                if (!z || z2) {
                    hideTopShadow();
                } else {
                    showTopShadow();
                }
                if (!z || z3) {
                    hideBottomShadow();
                } else {
                    showBottomShadow();
                }
            }
        }

        private void hideBottomShadow() {
            setElevation(ScrollableAnswerFragment.surveyControlsContainer, 0.0f);
        }

        private void showTopShadow() {
            setElevation(ScrollableAnswerFragment.questionTextView, (float) ScrollableAnswerFragment.getResources().getDimensionPixelSize(R.dimen.hats_lib_question_view_elevation));
        }

        private void hideTopShadow() {
            setElevation(ScrollableAnswerFragment.questionTextView, 0.0f);
        }

        private void showBottomShadow() {
            setElevation(ScrollableAnswerFragment.surveyControlsContainer, (float) ScrollableAnswerFragment.getResources().getDimensionPixelSize(R.dimen.hats_lib_survey_controls_view_elevation));
        }

        private void setElevation(View view, float f) {
            if (Build.VERSION.SDK_INT >= 21) {
                view.setElevation(f);
            }
        }
    }
}
