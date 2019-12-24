package com.google.android.libraries.hats20.view;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.libraries.hats20.dimen;
import com.google.android.libraries.hats20.id;
import com.google.android.libraries.hats20.layout;
import com.google.android.libraries.hats20.SurveyPromptActivity;
import com.google.android.libraries.hats20.answer.QuestionResponse;
import com.google.android.libraries.hats20.model.QuestionRating;
import com.google.android.libraries.hats20.p004ui.StarRatingBar;
import com.google.android.libraries.hats20.util.LayoutUtils;
import com.google.android.libraries.hats20.util.TextFormatUtil;
import com.google.android.libraries.hats20.view.FragmentViewDelegate;
import com.google.android.libraries.material.autoresizetext.AutoResizeTextView;

public class RatingFragment extends BaseFragment {
    private FragmentViewDelegate fragmentViewDelegate = new FragmentViewDelegate();
    private QuestionRating question;
    private QuestionMetrics questionMetrics;
    private String selectedResponse;

    public static RatingFragment newInstance(QuestionRating questionRating) {
        RatingFragment ratingFragment = new RatingFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("Question", questionRating);
        ratingFragment.setArguments(bundle);
        return ratingFragment;
    }

    public void updateRatingQuestionTextSize(AutoResizeTextView autoResizeTextView) {
        Resources resources = getResources();
        int size = View.MeasureSpec.getSize(((FragmentViewDelegate.MeasurementSurrogate) getActivity()).getMeasureSpecs().x);
        LayoutUtils.fitTextInTextViewWrapIfNeeded(((float) size) - ((((float) (resources.getDimensionPixelSize(dimen.hats_lib_rating_container_padding) * 2)) + TypedValue.applyDimension(1, 24.0f, resources.getDisplayMetrics())) + TypedValue.applyDimension(1, 40.0f, resources.getDisplayMetrics())), 20, 16, question.getQuestionText(), autoResizeTextView);
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        question = (QuestionRating) getArguments().getParcelable("Question");
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

    public void onDetach() {
        fragmentViewDelegate.cleanUp();
        super.onDetach();
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(layout.hats_survey_question_rating, viewGroup, false);
        inflate.setContentDescription(question.getQuestionText());
        TextView textView = (TextView) inflate.findViewById(id.hats_lib_survey_question_text);
        textView.setText(TextFormatUtil.format(question.getQuestionText()));
        textView.setContentDescription(question.getQuestionText());
        setTextAndContentDescription((TextView) inflate.findViewById(id.hats_lib_survey_rating_low_value_text), question.getLowValueText());
        setTextAndContentDescription((TextView) inflate.findViewById(id.hats_lib_survey_rating_high_value_text), question.getHighValueText());
        final ViewGroup viewGroup2 = (ViewGroup) inflate.findViewById(id.hats_lib_survey_rating_images_container);
        final StarRatingBar starRatingBar = (StarRatingBar) inflate.findViewById(id.hats_lib_star_rating_bar);
        int i = C14783.f65xffbb3005[question.getSprite().ordinal()];
        if (i == 1) {
            viewGroup2.setVisibility(0);
            int i2 = 0;
            while (i2 < 5) {
                View inflate2 = layoutInflater.inflate(layout.hats_survey_question_rating_item, viewGroup2, false);
                ((ImageView) inflate2.findViewById(id.hats_lib_survey_rating_icon)).setImageResource(QuestionRating.READONLY_SURVEY_RATING_ICON_RESOURCE_MAP.get(Integer.valueOf(i2)).intValue());
                final int i3 = i2 + 1;
                inflate2.setTag(Integer.valueOf(i3));
                setDescriptionForTalkBack(inflate2, i3, 5);
                inflate2.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        RatingFragment.removeOnClickListenersAndDisableClickEvents(viewGroup2);
                        int i = i3;
                        StringBuilder sb = new StringBuilder(35);
                        sb.append("Rating selected, value: ");
                        sb.append(i);
                        Log.d("HatsLibRatingFragment", sb.toString());
                        RatingFragment.questionMetrics.markAsAnswered();
                        String unused = RatingFragment.selectedResponse = Integer.toString(i3);
                        ((SurveyPromptActivity) RatingFragment.getActivity()).nextPageOrSubmit();
                    }
                });
                removeMarginIfNeeded(inflate2, i2, 5);
                viewGroup2.addView(inflate2);
                i2 = i3;
            }
        } else if (i == 2) {
            starRatingBar.setVisibility(0);
            starRatingBar.setNumStars(question.getNumIcons());
            starRatingBar.setOnRatingChangeListener(new StarRatingBar.OnRatingChangeListener() {
                public void onRatingChanged(int i) {
                    RatingFragment ratingFragment = RatingFragment.this;
                    ratingFragment.setDescriptionForTalkBack(starRatingBar, i, ratingFragment.question.getNumIcons());
                    RatingFragment.questionMetrics.markAsAnswered();
                    String unused = RatingFragment.selectedResponse = Integer.toString(i);
                    ((OnQuestionProgressableChangeListener) RatingFragment.getActivity()).onQuestionProgressableChanged(RatingFragment.isResponseSatisfactory(), RatingFragment.this);
                }
            });
        } else {
            String valueOf = String.valueOf(question.getSprite());
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 15);
            sb.append("Unknown sprite ");
            sb.append(valueOf);
            throw new IllegalStateException(sb.toString());
        }
        updateRatingQuestionTextSize((AutoResizeTextView) inflate.findViewById(id.hats_lib_survey_question_text));
        if (!isDetached()) {
            fragmentViewDelegate.watch((FragmentViewDelegate.MeasurementSurrogate) getActivity(), inflate);
        }
        return inflate;
    }

/*
//CheckLater
    public void QuestionRating {
        static final int[] f65xffbb3005 = new int[QuestionRating.Sprite.values().length];
        try {
            f65xffbb3005[QuestionRating.Sprite.SMILEYS.ordinal()] = 1;
            f65xffbb3005[QuestionRating.Sprite.STARS.ordinal()] = 2;
        }
    }
*/

    private void removeOnClickListenersAndDisableClickEvents(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            viewGroup.getChildAt(i).setOnClickListener((View.OnClickListener) null);
            viewGroup.getChildAt(i).setClickable(false);
        }
    }

    private void setTextAndContentDescription(TextView textView, String str) {
        textView.setText(str);
        textView.setContentDescription(str);
    }

    private void setDescriptionForTalkBack(View view, int i, int i2) {
        String format = String.format("%d of %d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
        if (i == 1) {
            String valueOf = String.valueOf(format);
            String valueOf2 = String.valueOf(question.getLowValueText());
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 1 + String.valueOf(valueOf2).length());
            sb.append(valueOf);
            sb.append(" ");
            sb.append(valueOf2);
            format = sb.toString();
        } else if (i == i2) {
            String valueOf3 = String.valueOf(format);
            String valueOf4 = String.valueOf(question.getHighValueText());
            StringBuilder sb2 = new StringBuilder(String.valueOf(valueOf3).length() + 1 + String.valueOf(valueOf4).length());
            sb2.append(valueOf3);
            sb2.append(" ");
            sb2.append(valueOf4);
            format = sb2.toString();
        }
        view.setContentDescription(format);
    }

    private void removeMarginIfNeeded(View view, int i, int i2) {
        if (i == 0 || i == i2 - 1) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
            if (i == 0) {
                layoutParams.setMargins(0, layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin);
            } else if (i == i2 - 1) {
                layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, 0, layoutParams.bottomMargin);
            }
            view.setLayoutParams(layoutParams);
        }
    }

    public QuestionResponse computeQuestionResponse() {
        QuestionResponse.Builder builder = QuestionResponse.builder();
        if (questionMetrics.isShown()) {
            builder.setDelayMs(questionMetrics.getDelayMs());
            String str = selectedResponse;
            if (str != null) {
                builder.addResponse(str);
                String valueOf = String.valueOf(selectedResponse);
                Log.d("HatsLibRatingFragment", valueOf.length() != 0 ? "Selected response: ".concat(valueOf) : new String("Selected response: "));
            }
        }
        return builder.build();
    }

    public boolean isResponseSatisfactory() {
        return selectedResponse != null;
    }
}
