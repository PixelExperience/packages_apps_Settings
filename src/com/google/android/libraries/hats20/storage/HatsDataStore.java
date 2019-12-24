package com.google.android.libraries.hats20.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class HatsDataStore {
    static final int MAX_DAYS_UNTIL_EXPIRATION = 7;
    private static final long MILLIS_TO_CACHE_FAILED_DOWNLOAD = TimeUnit.HOURS.toMillis(24);
    static final String SHARED_PREF_SET_COOKIE_URI = "SET_COOKIE_URI";
    static final String SHARED_PREF_SET_COOKIE_VALUE = "SET_COOKIE_VALUE";
    private final SharedPreferences sharedPreferences;

    private HatsDataStore(SharedPreferences sharedPreferences2) {
        sharedPreferences = sharedPreferences2;
    }

    public static HatsDataStore buildFromContext(Context context) {
        return new HatsDataStore(getSharedPreferences(context.getApplicationContext()));
    }

    public void saveSuccessfulDownload(int i, long j, String str, String str2) {
        if (!(i == 0 || i == 1 || i == 2 || i == 3)) {
            i = 5;
        }
        sharedPreferences.edit().putInt(getKeyForPrefSuffix(str2, "RESPONSE_CODE"), i).putLong(getKeyForPrefSuffix(str2, "EXPIRATION_DATE"), Math.min((System.currentTimeMillis() + 604800000) / 1000, j)).putString(getKeyForPrefSuffix(str2, "CONTENT"), str).apply();
    }

    public void saveFailedDownload(String str) {
        sharedPreferences.edit().putInt(getKeyForPrefSuffix(str, "RESPONSE_CODE"), 4).putLong(getKeyForPrefSuffix(str, "EXPIRATION_DATE"), (System.currentTimeMillis() + MILLIS_TO_CACHE_FAILED_DOWNLOAD) / 1000).putString(getKeyForPrefSuffix(str, "CONTENT"), "").apply();
    }

    private static String getKeyForPrefSuffix(String str, String str2) {
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 1 + String.valueOf(str2).length());
        sb.append(str);
        sb.append("_");
        sb.append(str2);
        return sb.toString();
    }

    public void removeSurveyIfExpired(String str) {
        long surveyExpirationDate = getSurveyExpirationDate(str);
        if (surveyExpirationDate == -1) {
            sharedPreferences.edit().remove(getKeyForPrefSuffix(str, "RESPONSE_CODE")).remove(getKeyForPrefSuffix(str, "CONTENT")).apply();
        } else if (surveyExpirationDate < System.currentTimeMillis() / 1000) {
            removeSurvey(str);
        }
    }

    public long getSurveyExpirationDate(String str, int i) {
        if (i == sharedPreferences.getInt(getKeyForPrefSuffix(str, "RESPONSE_CODE"), -1)) {
            return getSurveyExpirationDate(str);
        }
        return -1;
    }

    public void removeSurvey(String str) {
        sharedPreferences.edit().remove(getKeyForPrefSuffix(str, "EXPIRATION_DATE")).remove(getKeyForPrefSuffix(str, "RESPONSE_CODE")).remove(getKeyForPrefSuffix(str, "CONTENT")).apply();
    }

    public boolean validSurveyExists(String str) {
        int i = sharedPreferences.getInt(getKeyForPrefSuffix(str, "RESPONSE_CODE"), -1);
        if (i == -1) {
            Log.d("HatsLibDataStore", String.format("Checking for survey to show, Site ID %s was not in shared preferences.", new Object[]{str}));
        } else {
            Log.d("HatsLibDataStore", String.format("Checking for survey to show, Site ID %s has response code %d in shared preferences.", new Object[]{str, Integer.valueOf(i)}));
        }
        if (i == 0) {
            return true;
        }
        return false;
    }

    static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("com.google.android.libraries.hats20", 0);
    }

    public boolean surveyExists(String str) {
        int i = sharedPreferences.getInt(getKeyForPrefSuffix(str, "RESPONSE_CODE"), -1);
        if (i == -1) {
            Log.d("HatsLibDataStore", String.format("Checking if survey exists, Site ID %s was not in shared preferences.", new Object[]{str}));
        } else {
            Log.d("HatsLibDataStore", String.format("Checking if survey exists, Site ID %s has response code %d in shared preferences.", new Object[]{str, Integer.valueOf(i)}));
        }
        if (i != -1) {
            return true;
        }
        return false;
    }

    public String getSurveyJson(String str) {
        return sharedPreferences.getString(getKeyForPrefSuffix(str, "CONTENT"), (String) null);
    }

    public void forTestingInjectSurveyIntoStorage(String str, String str2, int i, long j) {
        sharedPreferences.edit().putInt(getKeyForPrefSuffix(str, "RESPONSE_CODE"), i).putLong(getKeyForPrefSuffix(str, "EXPIRATION_DATE"), j).putString(getKeyForPrefSuffix(str, "CONTENT"), str2).apply();
    }

    public void forTestingClearAllData() {
        sharedPreferences.edit().clear().apply();
        CookieHandler cookieHandler = CookieHandler.getDefault();
        if (cookieHandler instanceof CookieManager) {
            ((CookieManager) cookieHandler).getCookieStore().removeAll();
            return;
        }
        String valueOf = String.valueOf(cookieHandler.getClass().getName());
        Log.e("HatsLibDataStore", valueOf.length() != 0 ? "Unknown cookie manager type, could not clear cookies: ".concat(valueOf) : new String("Unknown cookie manager type, could not clear cookies: "));
    }

    public void storeSetCookieHeaders(Uri uri, Map<String, List<String>> map) {
        for (Map.Entry next : map.entrySet()) {
            if (isSetCookieHeader(next)) {
                storeSetCookieHeaderValueSet(uri, new HashSet((Collection) next.getValue()));
                return;
            }
        }
    }

    private void storeSetCookieHeaderValueSet(Uri uri, Set<String> set) {
        sharedPreferences.edit().putString("SET_COOKIE_URI", uri.toString()).putStringSet("SET_COOKIE_VALUE", set).apply();
    }

    private static boolean isSetCookieHeader(Map.Entry<String, ?> entry) {
        return "Set-Cookie".equalsIgnoreCase(entry.getKey());
    }

    public void restoreCookiesFromPersistence() {
        if (CookieHandler.getDefault() == null) {
            Log.e("HatsLibDataStore", "Cannot restore cookies: Application does not have a cookie jar installed.");
            return;
        }
        String string = sharedPreferences.getString("SET_COOKIE_URI", "");
        Set<String> stringSet = sharedPreferences.getStringSet("SET_COOKIE_VALUE", Collections.emptySet());
        if (R.string..isEmpty() || stringSet.isEmpty()) {
            Log.d("HatsLibDataStore", "No cookies found in persistence.");
            return;
        }
        try {
            CookieHandler.getDefault().put(new URI(string), Collections.singletonMap("Set-Cookie", new ArrayList(stringSet)));
        } catch (IOException | URISyntaxException e) {
            Log.e("HatsLibDataStore", "Failed to restore cookies from persistence.", e);
        }
    }

    private long getSurveyExpirationDate(String str) {
        return sharedPreferences.getLong(getKeyForPrefSuffix(str, "EXPIRATION_DATE"), -1);
    }
}
