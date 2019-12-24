package com.google.android.libraries.hats20;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.cardview.widget.CardView;
import com.google.android.libraries.hats20.util.LayoutDimensions;
import com.google.android.libraries.hats20.util.LayoutUtils;

final class DimensionConfigurationHelper {
    private final boolean bottomSheet;
    private final int containerPadding = getResources().getDimensionPixelSize(dimen.hats_lib_container_padding);
    private final Dialog dialog;
    private final LayoutDimensions layoutDimensions;
    private final int maxPromptWidth;
    private final CardView promptCard;
    private final boolean twoLinePrompt;

    DimensionConfigurationHelper(CardView cardView, Dialog dialog2, LayoutDimensions layoutDimensions2, boolean z, boolean z2) {
        promptCard = cardView;
        dialog = dialog2;
        layoutDimensions = layoutDimensions2;
        bottomSheet = z;
        twoLinePrompt = z2;
        maxPromptWidth = layoutDimensions2.getPromptMaxWidth();
    }

    void configureDimensions() {
        int i;
        float f;
        boolean z = dialog != null;
        if (bottomSheet) {
            i = -1;
        } else {
            i = getPromptWidthPx(getContext(), maxPromptWidth);
        }
        int promptBannerHeight = layoutDimensions.getPromptBannerHeight(twoLinePrompt);
        CardView cardView = promptCard;
        if (bottomSheet) {
            f = getResources().getDimension(dimen.hats_lib_prompt_banner_elevation_sheet);
        } else {
            f = getResources().getDimension(dimen.hats_lib_prompt_banner_elevation_card);
        }
        cardView.setCardElevation(f);
        float maxCardElevation = promptCard.getMaxCardElevation() * 1.5f;
        float maxCardElevation2 = promptCard.getMaxCardElevation();
        RectF bannerPadding = getBannerPadding(maxCardElevation);
        if (z) {
            Window window = dialog.getWindow();
            window.setBackgroundDrawable(new ColorDrawable(0));
            window.addFlags(32);
            window.clearFlags(2);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.x = 0;
            attributes.y = 0;
            attributes.width = i;
            attributes.height = Math.round(((float) promptBannerHeight) + bannerPadding.top + bannerPadding.bottom);
            attributes.gravity = 85;
            window.setAttributes(attributes);
        }
        try {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) promptCard.getLayoutParams();
            marginLayoutParams.height = Math.round(((float) promptBannerHeight) + (2.0f * maxCardElevation));
            marginLayoutParams.setMargins(Math.round(bannerPadding.left - maxCardElevation2), Math.round(bannerPadding.top - maxCardElevation), Math.round(bannerPadding.right - maxCardElevation2), Math.round(bannerPadding.bottom - maxCardElevation));
            promptCard.setLayoutParams(marginLayoutParams);
        } catch (ClassCastException e) {
            throw new RuntimeException("HatsShowRequest.insertIntoParent can only be called with a ViewGroup whose LayoutParams extend MarginLayoutParams", e);
        }
    }

    private RectF getBannerPadding(float f) {
        float f2;
        float f3;
        float f4;
        float f5 = 0.0f;
        if (dialog != null) {
            boolean z = true;
            if (bottomSheet) {
                if (Build.VERSION.SDK_INT >= 21) {
                    z = false;
                }
                if (!z) {
                    f = (float) containerPadding;
                }
                f2 = 0.0f;
                f3 = 0.0f;
            } else {
                if (getPromptWidthPx(getContext(), maxPromptWidth) != LayoutUtils.getUsableContentDimensions(getContext()).x) {
                    z = false;
                }
                if (z) {
                    f4 = getResources().getDimension(dimen.hats_lib_container_padding);
                } else {
                    f4 = getResources().getDimension(dimen.hats_lib_container_padding_left);
                }
                f5 = f4;
                int i = containerPadding;
                f = (float) i;
                f3 = (float) i;
                f2 = (float) i;
            }
        } else {
            f2 = 0.0f;
            f = 0.0f;
            f3 = 0.0f;
        }
        return new RectF(f5, f, f3, f2);
    }

    private Context getContext() {
        return promptCard.getContext();
    }

    private Resources getResources() {
        return getContext().getResources();
    }

    static int getPromptWidthPx(Context context, int i) {
        return Math.min(LayoutUtils.getUsableContentDimensions(context).x, i);
    }
}
