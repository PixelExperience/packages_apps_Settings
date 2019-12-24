package com.google.android.libraries.hats20.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.google.android.libraries.hats20.model.Question;
import com.google.android.libraries.hats20.model.QuestionRating;
import com.google.android.libraries.hats20.view.MultipleChoiceFragment;
import com.google.android.libraries.hats20.view.MultipleSelectFragment;
import com.google.android.libraries.hats20.view.OpenTextFragment;
import com.google.android.libraries.hats20.view.RatingFragment;

public class SurveyViewPagerAdapter extends FragmentPagerAdapter {
    private final Question[] questions;

    public SurveyViewPagerAdapter(FragmentManager fragmentManager, Question[] questionArr) {
        super(fragmentManager);
        if (questionArr != null) {
            questions = questionArr;
            return;
        }
        throw new NullPointerException("Questions were missing!");
    }

    public int getCount() {
        return questions.length;
    }

    public Fragment getItem(int i) {
        Fragment buildFragment = buildFragment(questions[i]);
        buildFragment.getArguments().putInt("QuestionIndex", i);
        return buildFragment;
    }

    private Fragment buildFragment(Question question) {
        int type = question.getType();
        if (type == 1) {
            return MultipleChoiceFragment.newInstance(question);
        }
        if (type == 2) {
            return MultipleSelectFragment.newInstance(question);
        }
        if (type == 3) {
            return OpenTextFragment.newInstance(question);
        }
        if (type == 4) {
            return RatingFragment.newInstance((QuestionRating) question);
        }
        throw new AssertionError(String.format("Attempted to build fragment for unsupported Question type %s.  Note this should never happen as it's invalid to create a Question type that does not have a matching fragment.", new Object[]{Integer.valueOf(type)}));
    }

    public static int getQuestionIndex(Fragment fragment) {
        return fragment.getArguments().getInt("QuestionIndex", -1);
    }
}
