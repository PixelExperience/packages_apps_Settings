package com.google.android.libraries.hats20.answer;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.libraries.hats20.network.GcsRequest;
import com.google.android.libraries.hats20.storage.HatsDataStore;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Executor;

public class AnswerBeaconTransmitter {
    /* access modifiers changed from: private */
    public final String answerUrl;
    private final Executor executor;
    /* access modifiers changed from: private */
    public final HatsDataStore hatsDataStore;

    public AnswerBeaconTransmitter(String str, HatsDataStore hatsDataStore2) {
        this(str, hatsDataStore2, AsyncTask.THREAD_POOL_EXECUTOR);
    }

    AnswerBeaconTransmitter(String str, HatsDataStore hatsDataStore2, Executor executor2) {
        if (str == null) {
            throw new NullPointerException("Answer URL was missing");
        } else if (hatsDataStore2 == null) {
            throw new NullPointerException("HaTS cookie store was missing");
        } else if (executor2 != null) {
            this.answerUrl = str;
            this.hatsDataStore = hatsDataStore2;
            this.executor = executor2;
        } else {
            throw new NullPointerException("Executor was missing");
        }
    }

    public void transmit(AnswerBeacon answerBeacon) {
        this.executor.execute(new TransmitTask(answerBeacon.exportAllParametersInQueryString()));
    }

    private class TransmitTask implements Runnable {
        private final Uri uri;

        TransmitTask(Uri uri2) {
            this.uri = uri2;
        }

        public void run() {
            try {
                if (AnswerBeaconTransmitter.this.answerUrl.equals("/")) {
                    Log.d("HatsLibTransmitter", "Skipping transmission of beacon since answerUrl was \"/\", this should only happen during debugging.");
                } else {
                    transmit();
                }
            } catch (Exception e) {
                Log.e("HatsLibTransmitter", "Transmission of answer beacon failed.", e);
            }
        }

        private void transmit() throws IOException, URISyntaxException {
            AnswerBeaconTransmitter.this.hatsDataStore.restoreCookiesFromPersistence();
            String queryParameter = this.uri.getQueryParameter("t");
            URL url = new URL(AnswerBeaconTransmitter.this.answerUrl);
            byte[] bytes = this.uri.getEncodedQuery().getBytes("UTF-8");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            try {
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setInstanceFollowRedirects(false);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpURLConnection.setRequestProperty("Content-Length", Integer.toString(bytes.length));
                httpURLConnection.setRequestProperty("charset", "utf-8");
                httpURLConnection.setRequestProperty("Connection", "close");
                httpURLConnection.setRequestProperty("User-Agent", GcsRequest.USER_AGENT);
                httpURLConnection.setUseCaches(false);
                new DataOutputStream(httpURLConnection.getOutputStream()).write(bytes);
                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == 200) {
                    String valueOf = String.valueOf(queryParameter);
                    Log.d("HatsLibTransmitter", valueOf.length() != 0 ? "Successfully transmitted answer beacon of type: ".concat(valueOf) : new String("Successfully transmitted answer beacon of type: "));
                    AnswerBeaconTransmitter.this.hatsDataStore.storeSetCookieHeaders(Uri.parse(AnswerBeaconTransmitter.this.answerUrl), httpURLConnection.getHeaderFields());
                } else {
                    StringBuilder sb = new StringBuilder(String.valueOf(queryParameter).length() + 74);
                    sb.append("Failed to transmit answer beacon of type: ");
                    sb.append(queryParameter);
                    sb.append("; response code was: ");
                    sb.append(responseCode);
                    Log.e("HatsLibTransmitter", sb.toString());
                }
            } finally {
                httpURLConnection.disconnect();
            }
        }
    }
}
