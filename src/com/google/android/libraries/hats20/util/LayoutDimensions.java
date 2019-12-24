package com.google.android.libraries.hats20.util;

import android.content.res.Resources;

public final class LayoutDimensions {
    private final Resources resources;

    public boolean shouldSurveyDisplayScrim() {
        return true;
    }

    public LayoutDimensions(Resources resources2) {
        this.resources = resources2;
    }

    public boolean shouldDisplayPrompt() {
        return this.resources.getBoolean(R.bool.hats_lib_prompt_should_display);
    }

    public int getPromptBannerHeight(boolean z) {
        if (z) {
            return this.resources.getDimensionPixelSize(R.dimen.hats_lib_prompt_banner_tall_height);
        }
        return this.resources.getDimensionPixelSize(R.dimen.hats_lib_prompt_banner_height);
    }

    public int getPromptMaxWidth() {
        return this.resources.getDimensionPixelSize(R.dimen.hats_lib_prompt_max_width);
    }

    public int getSurveyMaxWidth() {
        return this.resources.getDimensionPixelSize(R.dimen.hats_lib_survey_max_width);
    }

    public int getSurveyMaxHeight() {
        return this.resources.getDimensionPixelSize(R.dimen.hats_lib_survey_max_height);
    }

    public boolean isSurveyFullBleed() {
        return this.resources.getBoolean(R.bool.hats_lib_survey_is_full_bleed);
    }

    public boolean shouldSurveyDisplayCloseButton() {
        return this.resources.getBoolean(R.bool.hats_lib_survey_should_display_close_button);
    }
}
