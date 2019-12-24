package com.google.android.libraries.hats20.view;

import androidx.fragment.app.Fragment;
import com.google.android.libraries.hats20.answer.QuestionResponse;

public abstract class BaseFragment extends Fragment {
    public abstract QuestionResponse computeQuestionResponse();

    public abstract void onPageScrolledIntoView();
}
