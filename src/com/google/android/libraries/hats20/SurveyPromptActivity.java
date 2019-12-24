package com.google.android.libraries.hats20;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.libraries.hats20.adapter.SurveyViewPagerAdapter;
import com.google.android.libraries.hats20.answer.AnswerBeacon;
import com.google.android.libraries.hats20.answer.AnswerBeaconTransmitter;
import com.google.android.libraries.hats20.answer.QuestionResponse;
import com.google.android.libraries.hats20.model.Question;
import com.google.android.libraries.hats20.model.SurveyController;
import com.google.android.libraries.hats20.storage.HatsDataStore;
import com.google.android.libraries.hats20.util.LayoutDimensions;
import com.google.android.libraries.hats20.util.LayoutUtils;
import com.google.android.libraries.hats20.view.FragmentViewDelegate;
import com.google.android.libraries.hats20.view.OnQuestionProgressableChangeListener;
import com.google.android.libraries.hats20.view.OpenTextFragment;
import com.google.android.libraries.hats20.view.SurveyViewPager;

public class SurveyPromptActivity extends AppCompatActivity implements FragmentViewDelegate.MeasurementSurrogate, OnQuestionProgressableChangeListener {
    private final Handler activityFinishHandler = new Handler();
    private AnswerBeacon answerBeacon;
    private AnswerBeaconTransmitter answerBeaconTransmitter;
    private IdleResourceManager idleResourceManager;
    private boolean isFullWidth;
    private boolean isSubmitting;
    private int itemMeasureCount = 0;
    private LayoutDimensions layoutDimensions;
    private FrameLayout overallContainer;
    private String siteId;
    private LinearLayout surveyContainer;
    private SurveyController surveyController;
    private final Point surveyPreDrawMeasurements = new Point(0, 0);
    private SurveyViewPager surveyViewPager;
    private SurveyViewPagerAdapter surveyViewPagerAdapter;
    private TextView thankYouTextView;

    static void startSurveyActivity(Activity activity, String str, SurveyController surveyController2, AnswerBeacon answerBeacon2, Integer num, boolean z) {
        Intent intent = new Intent(activity, SurveyPromptActivity.class);
        intent.putExtra("SiteId", str);
        intent.putExtra("SurveyController", surveyController2);
        intent.putExtra("AnswerBeacon", answerBeacon2);
        intent.putExtra("IsFullWidth", z);
        Log.d("HatsLibSurveyActivity", String.format("Starting survey for client activity: %s", new Object[]{activity.getClass().getCanonicalName()}));
        if (num == null) {
            activity.startActivity(intent);
        } else {
            activity.startActivityForResult(intent, num.intValue());
        }
    }

    public IdleResourceManager getIdleResourceManager() {
        return idleResourceManager;
    }

    public void setIsMultipleChoiceSelectionAnimating(boolean z) {
        idleResourceManager.setIsMultipleChoiceSelectionAnimating(z);
    }

    protected void onCreate(Bundle bundle) {
        AnswerBeacon answerBeacon2;
        boolean z;
        super.onCreate(bundle);
        setTitle("");
        layoutDimensions = new LayoutDimensions(getResources());
        siteId = getIntent().getStringExtra("SiteId");
        surveyController = (SurveyController) getIntent().getParcelableExtra("SurveyController");
        if (bundle == null) {
            answerBeacon2 = (AnswerBeacon) getIntent().getParcelableExtra("AnswerBeacon");
        } else {
            answerBeacon2 = (AnswerBeacon) bundle.getParcelable("AnswerBeacon");
        }
        answerBeacon = answerBeacon2;
        if (bundle == null) {
            z = false;
        } else {
            z = bundle.getBoolean("IsSubmitting");
        }
        isSubmitting = z;
        isFullWidth = getIntent().getBooleanExtra("IsFullWidth", false);
        if (siteId == null || surveyController == null || answerBeacon == null) {
            Log.e("HatsLibSurveyActivity", "Required EXTRAS not found in the intent, bailing out.");
            finish();
            return;
        }
        HatsClient.markSurveyRunning();
        Object[] objArr = new Object[2];
        objArr[0] = bundle != null ? "created with savedInstanceState" : "created anew";
        objArr[1] = siteId;
        Log.d("HatsLibSurveyActivity", String.format("Activity %s with site ID: %s", objArr));
        answerBeaconTransmitter = new AnswerBeaconTransmitter(surveyController.getAnswerUrl(), HatsDataStore.buildFromContext(this));
        setContentView(R.layout.hats_container);
        surveyContainer = (LinearLayout) findViewById(R.id.hats_lib_survey_container);
        overallContainer = (FrameLayout) findViewById(R.id.hats_lib_overall_container);
        wireUpCloseButton();
        thankYouTextView = (TextView) overallContainer.findViewById(R.id.hats_lib_thank_you);
        thankYouTextView.setText(surveyController.getThankYouMessage());
        thankYouTextView.setContentDescription(surveyController.getThankYouMessage());
        if (surveyController.shouldIncludeSurveyControls()) {
            getLayoutInflater().inflate(R.layout.hats_survey_controls, surveyContainer);
        }
        setUpSurveyPager(surveyController.getQuestions(), bundle);
        signalSurveyBegun();
        if (surveyController.shouldIncludeSurveyControls()) {
            wireUpSurveyControls();
        }
        idleResourceManager = new IdleResourceManager();
    }

    protected void onPostResume() {
        super.onPostResume();
        if (isSubmitting) {
            finish();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            HatsClient.markSurveyFinished();
        }
        activityFinishHandler.removeCallbacks((Runnable) null);
    }

    private void configureSurveyWindowParameters() {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        Point point = new Point(0, 0);
        getWindowManager().getDefaultDisplay().getSize(point);
        attributes.gravity = 85;
        attributes.width = getFinalizedSurveyDimensions().x;
        attributes.height = point.y;
        if (LayoutUtils.isNavigationBarOnRight(this)) {
            attributes.x = LayoutUtils.getNavigationBarDimensionPixelSize(this).x;
        } else {
            attributes.y = LayoutUtils.getNavigationBarDimensionPixelSize(this).y;
        }
        if (layoutDimensions.shouldSurveyDisplayScrim()) {
            showWindowScrim();
        }
        window.setAttributes(attributes);
    }

    private void showWindowScrim() {
        Window window = getWindow();
        window.addFlags(2);
        window.clearFlags(32);
        window.addFlags(262144);
        window.setDimAmount(0.4f);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            Rect rect = new Rect();
            overallContainer.getGlobalVisibleRect(rect);
            if (!rect.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
                Log.d("HatsLibSurveyActivity", "User clicked outside of survey root container. Closing.");
                if (!answerBeacon.hasBeaconTypeFullAnswer()) {
                    setBeaconTypeAndTransmit("o");
                }
                finish();
                return true;
            }
        }
        return super.onTouchEvent(motionEvent);
    }

    private void wireUpSurveyControls() {
        ((Button) findViewById(R.id.hats_lib_next)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                SurveyPromptActivity.nextPageOrSubmit();
            }
        });
    }

    private void wireUpCloseButton() {
        findViewById(R.id.hats_lib_close_button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                SurveyPromptActivity.setBeaconTypeAndTransmit("o");
                SurveyPromptActivity.finish();
            }
        });
    }

    public void onBackPressed() {
        setBeaconTypeAndTransmit("o");
        super.onBackPressed();
    }

    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("CurrentQuestionIndex", surveyViewPager.getCurrentItem());
        bundle.putBoolean("IsSubmitting", isSubmitting);
        bundle.putParcelable("AnswerBeacon", answerBeacon);
    }

    private void setUpSurveyPager(Question[] questionArr, Bundle bundle) {
        surveyViewPagerAdapter = new SurveyViewPagerAdapter(getSupportFragmentManager(), questionArr);
        surveyViewPager = (SurveyViewPager) findViewById(R.id.hats_lib_survey_viewpager);
        surveyViewPager.setAdapter(surveyViewPagerAdapter);
        surveyViewPager.setImportantForAccessibility(2);
        if (bundle != null) {
            surveyViewPager.setCurrentItem(bundle.getInt("CurrentQuestionIndex"));
        }
        if (surveyController.shouldIncludeSurveyControls()) {
            switchNextTextToSubmitIfNeeded();
        }
    }

    public Point getMeasureSpecs() {
        Point usableContentDimensions = LayoutUtils.getUsableContentDimensions(this);
        usableContentDimensions.y = (int) Math.min(((float) usableContentDimensions.y) * 0.8f, (float) layoutDimensions.getSurveyMaxHeight());
        usableContentDimensions.x = Math.min(usableContentDimensions.x, layoutDimensions.getSurveyMaxWidth());
        return new Point(View.MeasureSpec.makeMeasureSpec(usableContentDimensions.x, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(usableContentDimensions.y, Integer.MIN_VALUE));
    }

    public void onFragmentContentMeasurement(int i, int i2) {
        itemMeasureCount++;
        Point point = surveyPreDrawMeasurements;
        point.x = Math.max(point.x, i);
        Point point2 = surveyPreDrawMeasurements;
        point2.y = Math.max(point2.y, i2);
        if (itemMeasureCount == surveyViewPagerAdapter.getCount()) {
            itemMeasureCount = 0;
            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.hats_lib_survey_controls_container);
            if (frameLayout != null) {
                surveyPreDrawMeasurements.y += frameLayout.getMeasuredHeight();
            }
            transitionToSurveyMode();
        }
    }

    private void transitionToSurveyMode() {
        surveyViewPager.fireOnPageScrolledIntoViewListener();
        if (!answerBeacon.hasBeaconType()) {
            setBeaconTypeAndTransmit("sv");
        }
        configureSurveyWindowParameters();
        surveyContainer.setAlpha(1.0f);
        updateSurveyLayoutParameters();
        updateSurveyFullBleed();
        if (layoutDimensions.shouldSurveyDisplayCloseButton()) {
            findViewById(R.id.hats_lib_close_button).setVisibility(0);
        }
        sendWindowStateChangeAccessibilityEvent();
    }

    private void sendWindowStateChangeAccessibilityEvent() {
        surveyViewPager.getCurrentItemFragment().getView().sendAccessibilityEvent(32);
    }

    private void updateSurveyLayoutParameters() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) overallContainer.getLayoutParams();
        Point finalizedSurveyDimensions = getFinalizedSurveyDimensions();
        layoutParams.width = finalizedSurveyDimensions.x;
        layoutParams.height = finalizedSurveyDimensions.y;
        overallContainer.setLayoutParams(layoutParams);
    }

    public void updateSurveyFullBleed() {
        if (layoutDimensions.isSurveyFullBleed()) {
            overallContainer.setPadding(0, 0, 0, 0);
            return;
        }
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.hats_lib_container_padding);
        overallContainer.setPadding(dimensionPixelSize, dimensionPixelSize, dimensionPixelSize, dimensionPixelSize);
    }

    private void signalSurveyBegun() {
        answerBeacon.setShown(surveyViewPager.getCurrentItem());
        surveyContainer.setVisibility(0);
        surveyContainer.forceLayout();
    }

    public void onQuestionProgressableChanged(boolean z, Fragment fragment) {
        if (SurveyViewPagerAdapter.getQuestionIndex(fragment) == surveyViewPager.getCurrentItem()) {
            setNextButtonEnabled(z);
        }
    }

    private void setNextButtonEnabled(boolean z) {
        Button button = (Button) findViewById(R.id.hats_lib_next);
        if (button != null && button.isEnabled() != z) {
            button.setAlpha(z ? 1.0f : 0.3f);
            button.setEnabled(z);
        }
    }

    private void switchNextTextToSubmitIfNeeded() {
        Button button = (Button) findViewById(R.id.hats_lib_next);
        if (button != null && surveyViewPager.isLastQuestion()) {
            button.setText(R.stringhats_lib_submit);
        }
    }

    public void nextPageOrSubmit() {
        if (surveyViewPager.getCurrentItemFragment() instanceof OpenTextFragment) {
            ((OpenTextFragment) surveyViewPager.getCurrentItemFragment()).closeKeyboard();
        }
        addCurrentItemResponseToAnswerBeacon();
        if (surveyViewPager.isLastQuestion()) {
            Log.d("HatsLibSurveyActivity", "Survey completed, submitting.");
            setBeaconTypeAndTransmit("a");
            submit();
            return;
        }
        setBeaconTypeAndTransmit("pa");
        surveyViewPager.navigateToNextPage();
        answerBeacon.setShown(surveyViewPager.getCurrentItem());
        switchNextTextToSubmitIfNeeded();
        sendWindowStateChangeAccessibilityEvent();
        Log.d("HatsLibSurveyActivity", String.format("Showing question: %d", new Object[]{Integer.valueOf(surveyViewPager.getCurrentItem() + 1)}));
    }

    private void addCurrentItemResponseToAnswerBeacon() {
        QuestionResponse currentItemQuestionResponse = surveyViewPager.getCurrentItemQuestionResponse();
        if (currentItemQuestionResponse != null) {
            answerBeacon.setQuestionResponse(surveyViewPager.getCurrentItem(), currentItemQuestionResponse);
        }
    }

    private void setBeaconTypeAndTransmit(String str) {
        answerBeacon.setBeaconType(str);
        answerBeaconTransmitter.transmit(answerBeacon);
    }

    private void submit() {
        isSubmitting = true;
        idleResourceManager.setIsThankYouAnimating(true);
        findViewById(R.id.hats_lib_close_button).setVisibility(8);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator duration = ObjectAnimator.ofFloat(surveyContainer, "alpha", new float[]{0.0f}).setDuration(350);
        duration.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                SurveyPromptActivity.surveyContainer.setVisibility(8);
            }
        });
        ValueAnimator duration2 = ValueAnimator.ofInt(new int[]{overallContainer.getHeight(), getResources().getDimensionPixelSize(R.dimen.hats_lib_thank_you_height)}).setDuration(350);
        duration2.setStartDelay(350);
        duration2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                SurveyPromptActivity.overallContainer.getLayoutParams().height = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                SurveyPromptActivity.overallContainer.requestLayout();
            }
        });
        ObjectAnimator duration3 = ObjectAnimator.ofFloat(thankYouTextView, "alpha", new float[]{1.0f}).setDuration(350);
        duration3.setStartDelay(700);
        thankYouTextView.setVisibility(0);
        TextView textView = thankYouTextView;
        textView.announceForAccessibility(textView.getContentDescription());
        activityFinishHandler.postDelayed(new Runnable() {
            public void run() {
                SurveyPromptActivity.idleResourceManager.setIsThankYouAnimating(false);
                SurveyPromptActivity.finish();
            }
        }, 2400);
        animatorSet.playTogether(new Animator[]{duration, duration2, duration3});
        animatorSet.start();
    }

    public ViewGroup getSurveyContainer() {
        return surveyContainer;
    }

    public Point getFinalizedSurveyDimensions() {
        int i = LayoutUtils.getUsableContentDimensions(this).x;
        if (!isFullWidth) {
            i = Math.min(i, layoutDimensions.getSurveyMaxWidth());
        }
        return new Point(i, Math.min(layoutDimensions.getSurveyMaxHeight(), surveyPreDrawMeasurements.y));
    }
}
