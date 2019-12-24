package com.google.android.libraries.hats20.network;

public class GcsResponse {
    private final long expirationDateUnix;
    private final int responseCode;
    private final String surveyJson;

    public GcsResponse(int i, long j, String str) {
        this.responseCode = i;
        this.expirationDateUnix = j;
        this.surveyJson = str;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public String getSurveyJson() {
        return this.surveyJson;
    }

    public long expirationDateUnix() {
        return this.expirationDateUnix;
    }
}
