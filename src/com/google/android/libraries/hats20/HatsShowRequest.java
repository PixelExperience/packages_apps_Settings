package com.google.android.libraries.hats20;

import android.app.Activity;
import android.util.Log;

public class HatsShowRequest {
    private final boolean bottomSheet;
    private final Activity clientActivity;
    private final Integer maxPromptWidth;
    private final int parentResId;
    private final Integer requestCode;
    private final String siteId;

    private HatsShowRequest(Builder builder) {
        clientActivity = builder.clientActivity;
        siteId = builder.siteId;
        requestCode = builder.requestCode;
        parentResId = builder.parentResId;
        maxPromptWidth = builder.maxPromptWidth;
        bottomSheet = builder.bottomSheet;
    }

    public Activity getClientActivity() {
        return clientActivity;
    }

    public String getSiteId() {
        return siteId;
    }

    public Integer getRequestCode() {
        return requestCode;
    }

    public int getParentResId() {
        return parentResId;
    }

    public Integer getMaxPromptWidth() {
        return maxPromptWidth;
    }

    public boolean isBottomSheet() {
        return bottomSheet;
    }

    public String toString() {
        String valueOf = String.valueOf(clientActivity.getLocalClassName());
        String str = siteId;
        String valueOf2 = String.valueOf(requestCode);
        int i = parentResId;
        String valueOf3 = String.valueOf(maxPromptWidth);
        boolean z = bottomSheet;
        StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 118 + String.valueOf(str).length() + String.valueOf(valueOf2).length() + String.valueOf(valueOf3).length());
        sb.append("HatsShowRequest{clientActivity=");
        sb.append(valueOf);
        sb.append(", siteId='");
        sb.append(str);
        sb.append("'");
        sb.append(", requestCode=");
        sb.append(valueOf2);
        sb.append(", parentResId=");
        sb.append(i);
        sb.append(", maxPromptWidth=");
        sb.append(valueOf3);
        sb.append(", bottomSheet=");
        sb.append(z);
        sb.append("}");
        return sb.toString();
    }

    public static Builder builder(Activity activity) {
        return new Builder(activity);
    }

    public static class Builder {
        private boolean bottomSheet;
        private final Activity clientActivity;
        private Integer maxPromptWidth;
        private int parentResId;
        private Integer requestCode;
        private String siteId;

        Builder(Activity activity) {
            if (activity != null) {
                clientActivity = activity;
                return;
            }
            throw new IllegalArgumentException("Client activity is missing.");
        }

        public Builder forSiteId(String str) {
            if (str == null) {
                throw new NullPointerException("Site ID cannot be set to null.");
            } else if (siteId == null) {
                siteId = str;
                return this;
            } else {
                throw new UnsupportedOperationException("Currently don't support multiple site IDs.");
            }
        }

        public HatsShowRequest build() {
            if (siteId == null) {
                Log.d("HatsLibShowRequest", "Site ID was not set, no survey will be shown.");
                siteId = "-1";
            }
            return new HatsShowRequest(this);
        }
    }
}
