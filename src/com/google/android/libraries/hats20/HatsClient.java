package com.google.android.libraries.hats20;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.libraries.hats20.answer.AnswerBeacon;
import com.google.android.libraries.hats20.model.SurveyController;
import com.google.android.libraries.hats20.network.GcsRequest;
import com.google.android.libraries.hats20.network.GcsResponse;
import com.google.android.libraries.hats20.storage.HatsDataStore;
import com.google.android.libraries.hats20.util.LayoutDimensions;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;

public class HatsClient {
    private static final AtomicBoolean isSurveyRunning = new AtomicBoolean(false);

    public static void installCookieHandlerIfNeeded() {
        if (CookieHandler.getDefault() == null) {
            CookieHandler.setDefault(new CookieManager());
            Log.d("HatsLibClient", "Installed cookie handler.");
            return;
        }
        Log.d("HatsLibClient", "Attempted to install cookie handler but one was already installed; skipping the install.");
    }

    public static void downloadSurvey(final HatsDownloadRequest hatsDownloadRequest) {
        if ("-1".equals(hatsDownloadRequest.getSiteId())) {
            Log.d("HatsLibClient", "No Site ID set, ignoring download request.");
            return;
        }
        synchronized (isSurveyRunning) {
            if (!isSurveyRunning.get()) {
                final HatsDataStore buildFromContext = HatsDataStore.buildFromContext(hatsDownloadRequest.getContext());
                buildFromContext.removeSurveyIfExpired(hatsDownloadRequest.getSiteId());
                if (!buildFromContext.surveyExists(hatsDownloadRequest.getSiteId())) {
                    if (!hasInternetPermission(hatsDownloadRequest.getContext())) {
                        Log.e("HatsLibClient", "Application does not have internet permission. Cannot make network request.");
                    } else if (CookieHandler.getDefault() == null) {
                        Log.e("HatsLibClient", "Invalid configuration: Application does not have a cookie jar installed.");
                    } else {
                        buildFromContext.restoreCookiesFromPersistence();
                        final GcsRequest gcsRequest = new GcsRequest(new GcsRequest.ResponseListener() {
                            public void onSuccess(GcsResponse gcsResponse) {
                                Log.d("HatsLibClient", String.format("Site ID %s downloaded with response code: %s", new Object[]{HatsDownloadRequest.getSiteId(), Integer.valueOf(gcsResponse.getResponseCode())}));
                                buildFromContext.saveSuccessfulDownload(gcsResponse.getResponseCode(), gcsResponse.expirationDateUnix(), gcsResponse.getSurveyJson(), HatsDownloadRequest.getSiteId());
                                HatsClient.sendBroadcast(HatsDownloadRequest.getContext(), HatsDownloadRequest.getSiteId(), gcsResponse.getResponseCode());
                            }

                            public void onError(Exception exc) {
                                Log.w("HatsLibClient", String.format("Site ID %s failed to download with error: %s", new Object[]{HatsDownloadRequest.getSiteId(), exc.toString()}));
                                buildFromContext.saveFailedDownload(HatsDownloadRequest.getSiteId());
                            }
                        }, hatsDownloadRequest.computeDownloadUri(), buildFromContext);
                        NetworkExecutor.getNetworkExecutor().execute(new Runnable() {
                            public void run() {
                                GcsRequest.send();
                            }
                        });
                    }
                }
            }
        }
    }

    public static long getSurveyExpirationDate(String str, Context context) {
        HatsDataStore buildFromContext = HatsDataStore.buildFromContext(context);
        buildFromContext.removeSurveyIfExpired(str);
        return buildFromContext.getSurveyExpirationDate(str, 0);
    }

    public static boolean showSurveyIfAvailable(HatsShowRequest hatsShowRequest) {
        if ("-1".equals(hatsShowRequest.getSiteId())) {
            Log.d("HatsLibClient", "No Site ID set, ignoring show request.");
            return false;
        }
        synchronized (isSurveyRunning) {
            if (isSurveyRunning.get()) {
                Log.d("HatsLibClient", "Attempted to show a survey while another one was already running, bailing out.");
                return false;
            }
            Activity clientActivity = hatsShowRequest.getClientActivity();
            boolean isDestroyed = Build.VERSION.SDK_INT >= 17 ? clientActivity.isDestroyed() : false;
            if (clientActivity != null && !clientActivity.isFinishing()) {
                if (!isDestroyed) {
                    String siteId = hatsShowRequest.getSiteId();
                    Integer requestCode = hatsShowRequest.getRequestCode();
                    HatsDataStore buildFromContext = HatsDataStore.buildFromContext(hatsShowRequest.getClientActivity());
                    buildFromContext.removeSurveyIfExpired(siteId);
                    if (!buildFromContext.validSurveyExists(siteId)) {
                        return false;
                    }
                    String surveyJson = buildFromContext.getSurveyJson(siteId);
                    if (surveyJson == null || surveyJson.isEmpty()) {
                        Log.e("HatsLibClient", String.format("Attempted to start survey with site ID %s, but the json in the shared preferences was not found or was empty.", new Object[]{siteId}));
                        return false;
                    }
                    try {
                        SurveyController initWithSurveyFromJson = SurveyController.initWithSurveyFromJson(surveyJson, clientActivity.getResources());
                        markSurveyRunning();
                        buildFromContext.removeSurvey(siteId);
                        AnswerBeacon promptParams = new AnswerBeacon().setPromptParams(initWithSurveyFromJson.getPromptParams());
                        if (initWithSurveyFromJson.showInvitation()) {
                            if (new LayoutDimensions(clientActivity.getResources()).shouldDisplayPrompt()) {
                                if (clientActivity instanceof FragmentActivity) {
                                    FragmentManager supportFragmentManager = ((FragmentActivity) clientActivity).getSupportFragmentManager();
                                    if (supportFragmentManager.findFragmentByTag("com.google.android.libraries.hats20.PromptDialogFragment") == null) {
                                        PromptDialogFragment newInstance = PromptDialogFragment.newInstance(siteId, initWithSurveyFromJson, promptParams, requestCode, hatsShowRequest.getMaxPromptWidth(), hatsShowRequest.isBottomSheet());
                                        FragmentTransaction beginTransaction = supportFragmentManager.beginTransaction();
                                        beginTransaction.add(hatsShowRequest.getParentResId(), newInstance, "com.google.android.libraries.hats20.PromptDialogFragment");
                                        beginTransaction.commitAllowingStateLoss();
                                    } else {
                                        Log.w("HatsLibClient", "PromptDialog was already open, bailing out.");
                                    }
                                } else if (clientActivity instanceof Activity) {
                                    android.app.FragmentManager fragmentManager = clientActivity.getFragmentManager();
                                    if (fragmentManager.findFragmentByTag("com.google.android.libraries.hats20.PromptDialogFragment") == null) {
                                        fragmentManager.beginTransaction().add(hatsShowRequest.getParentResId(), PlatformPromptDialogFragment.newInstance(siteId, initWithSurveyFromJson, promptParams, requestCode, hatsShowRequest.getMaxPromptWidth(), hatsShowRequest.isBottomSheet()), "com.google.android.libraries.hats20.PromptDialogFragment").commitAllowingStateLoss();
                                    } else {
                                        Log.w("HatsLibClient", "PromptDialog was already open, bailing out.");
                                    }
                                }
                            }
                        }
                        SurveyPromptActivity.startSurveyActivity(clientActivity, siteId, initWithSurveyFromJson, promptParams, requestCode, hatsShowRequest.isBottomSheet());
                        return true;
                    } catch (SurveyController.MalformedSurveyException e) {
                        Log.e("HatsLibClient", e.getMessage());
                        return false;
                    } catch (JSONException e2) {
                        StringBuilder sb = new StringBuilder(String.valueOf(siteId).length() + 46);
                        sb.append("Failed to parse JSON for survey with site ID ");
                        sb.append(siteId);
                        sb.append(".");
                        Log.e("HatsLibClient", sb.toString(), e2);
                        return false;
                    }
                }
            }
            Log.w("HatsLibClient", "Cancelling show request, activity was null, destroyed or finishing.");
            return false;
        }
    }

    static void markSurveyFinished() {
        synchronized (isSurveyRunning) {
            if (!isSurveyRunning.get()) {
                Log.e("HatsLibClient", "Notified that survey was destroyed when it wasn't marked as running.");
            }
            isSurveyRunning.set(false);
        }
    }

    static void sendBroadcast(Context context, String str, int i) {
        if (Log.isLoggable("HatsLibClient", 3)) {
            Log.d("HatsLibClient", "Hats survey is downloaded. Sending broadcast with action ACTION_BROADCAST_SURVEY_DOWNLOADED");
        }
        Intent intent = new Intent("com.google.android.libraries.hats20.SURVEY_DOWNLOADED");
        intent.putExtra("SiteId", str);
        intent.putExtra("ResponseCode", i);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    static void markSurveyRunning() {
        synchronized (isSurveyRunning) {
            isSurveyRunning.set(true);
        }
    }

    private static boolean hasInternetPermission(Context context) {
        return context.checkCallingOrSelfPermission("android.permission.INTERNET") == 0;
    }

    public static void forTestingInjectSurveyIntoStorage(Context context, String str, String str2, int i, long j) {
        HatsDataStore.buildFromContext(context).forTestingInjectSurveyIntoStorage(str, str2, i, j);
    }

    public static void forTestingClearAllData(Context context) {
        HatsDataStore.buildFromContext(context).forTestingClearAllData();
    }
}
