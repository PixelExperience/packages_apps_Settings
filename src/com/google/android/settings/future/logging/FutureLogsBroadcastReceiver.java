package com.google.android.settings.future.logging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.settings.overlay.FeatureFactory;

public final class FutureLogsBroadcastReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if ("com.google.android.settings.future.logging.LOG_SGF".equals(intent.getAction())) {
            handleIntent(context, intent);
            Log.v("FutureLogsBR", "Received logs from SettingsGoogleFuture: " + intent);
            return;
        }
        Log.w("FutureLogsBR", "Unknown action for FutureLogsBroadcastReceiver: " + intent.getAction());
    }

    private void handleIntent(Context context, Intent intent) {
        char c;
        String stringExtra = intent.getStringExtra("eventType");
        int hashCode = stringExtra.hashCode();
        if (hashCode != -1422950858) {
            if (hashCode != -699664669) {
                if (hashCode == 961643801 && stringExtra.equals("pageHidden")) {
                    c = 1;
                    if (c != 0) {
                        logPageVisibleEvent(context, intent);
                        return;
                    } else if (c == 1) {
                        logPageHiddenEvent(context, intent);
                        return;
                    } else if (c != 2) {
                        Log.w("FutureLogsBR", "Log type is not defined or not supported: " + stringExtra);
                        return;
                    } else {
                        logAction(context, intent);
                        return;
                    }
                }
            } else if (stringExtra.equals("pageVisible")) {
                c = 0;
                if (c != 0) {
                }
            }
        } else if (stringExtra.equals("action")) {
            c = 2;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }

    private void logPageVisibleEvent(Context context, Intent intent) {
        int intExtra = intent.getIntExtra("sourcePage", -1);
        int intExtra2 = intent.getIntExtra("page", -1);
        if (intExtra == -1 || intExtra2 == -1) {
            Log.w("FutureLogsBR", "Log data is not defined or not supported: {sourcePage=" + intExtra + " openedPageId=" + intExtra2 + "}");
            return;
        }
        FeatureFactory.getFactory(context).getMetricsFeatureProvider().visible(context, intExtra, intExtra2);
        Log.v("FutureLogsBR", "Logged page visible event: {sourcePage=" + intExtra + " openedPageId=" + intExtra2 + "}");
    }

    private void logPageHiddenEvent(Context context, Intent intent) {
        int intExtra = intent.getIntExtra("page", -1);
        if (intExtra == -1) {
            Log.w("FutureLogsBR", "Log data is not defined or not supported, hiddenPageId=" + intExtra);
            return;
        }
        FeatureFactory.getFactory(context).getMetricsFeatureProvider().hidden(context, intExtra);
        Log.v("FutureLogsBR", "Logged page hidden event, hiddenPageId=" + intExtra);
    }

    private void logAction(Context context, Intent intent) {
        int intExtra = intent.getIntExtra("page", -1);
        int intExtra2 = intent.getIntExtra("action", -1);
        if (intExtra == -1 || intExtra2 == -1) {
            Log.w("FutureLogsBR", "Log data is not defined or not supported: {actionId=" + intExtra2 + " pageId=" + intExtra + "}");
            return;
        }
        FeatureFactory.getFactory(context).getMetricsFeatureProvider().action(0, intExtra2, intExtra, "", 0);
        Log.v("FutureLogsBR", "Logged action: {actionId=" + intExtra2 + " pageId=" + intExtra + "}");
    }
}
