package com.google.android.libraries.hats20;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class HatsDownloadRequest {
    private final String advertisingId;
    private final String baseDownloadUrl;
    private final Context context;
    private final String siteContext;
    private final String siteId;

    private HatsDownloadRequest(Builder builder) {
        context = builder.context;
        siteContext = builder.siteContext;
        siteId = builder.siteId;
        advertisingId = builder.advertisingId;
        baseDownloadUrl = builder.baseDownloadUrl;
    }

    Context getContext() {
        return context;
    }

    String getSiteId() {
        return siteId;
    }

    Uri computeDownloadUri() {
        Uri.Builder appendQueryParameter = Uri.parse(baseDownloadUrl).buildUpon().appendQueryParameter("lang", "EN").appendQueryParameter("site", siteId).appendQueryParameter("adid", advertisingId);
        String str = siteContext;
        if (str != null) {
            appendQueryParameter.appendQueryParameter("sc", str);
        }
        return appendQueryParameter.build();
    }

    public static Builder builder(Context context2) {
        return new Builder(context2);
    }

    public static class Builder {
        private String advertisingId;
        private boolean alreadyBuilt;
        private String baseDownloadUrl;
        private final Context context;
        private String siteContext;
        private String siteId;
        private Builder(Context context2) {
            baseDownloadUrl = "https://clients4.google.com/insights/consumersurveys/gk/prompt";
            alreadyBuilt = false;
            if (context2 != null) {
                context = context2;
                return;
            }
            throw new NullPointerException("Context was missing.");
        }

        public Builder forSiteId(String str) {
            if (siteId != null) {
                throw new UnsupportedOperationException("Currently don't support multiple site IDs.");
            } else if (str != null) {
                siteId = str;
                return this;
            } else {
                throw new NullPointerException("Site ID cannot be set to null.");
            }
        }

        public Builder withAdvertisingId(String str) {
            if (str != null) {
                advertisingId = str;
                return this;
            }
            throw new NullPointerException("Advertising ID was missing.");
        }

        public Builder withSiteContext(String str) {
            if (str != null) {
                if (str.length() > 1000) {
                    Log.w("HatsLibDownloadRequest", "Site context was longer than 1000 chars, please trim it down.");
                }
                siteContext = str;
                return this;
            }
            throw new NullPointerException("Site context was missing.");
        }

        public Builder setBaseDownloadUrlForTesting(String str) {
            if (str != null) {
                baseDownloadUrl = str;
                return this;
            }
            throw new NullPointerException("Base download URL was missing.");
        }

        public HatsDownloadRequest build() {
            if (!alreadyBuilt) {
                alreadyBuilt = true;
                if (siteId == null) {
                    Log.d("HatsLibDownloadRequest", "Site ID was not set, no survey will be downloaded.");
                    siteId = "-1";
                }
                if (advertisingId != null) {
                    return new HatsDownloadRequest(this);
                }
                throw new NullPointerException("Advertising ID was missing.");
            }
            throw new IllegalStateException("Cannot reuse Builder instance once instantiated");
        }
    }
}
