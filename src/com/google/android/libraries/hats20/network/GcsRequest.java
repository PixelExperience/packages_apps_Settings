package com.google.android.libraries.hats20.network;

import android.net.Uri;
import android.os.Build;
import android.util.Log;
import com.google.android.libraries.hats20.storage.HatsDataStore;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public class GcsRequest {
    public static final String USER_AGENT = String.format(Locale.US, "Mozilla/5.0; Hats App/v%d (Android %s; SDK %d; %s; %s; %s)", new Object[]{2, Build.VERSION.RELEASE, Integer.valueOf(Build.VERSION.SDK_INT), Build.ID, Build.MODEL, Build.TAGS});
    private final HatsDataStore hatsDataStore;
    private final String postData;
    private final Uri requestUriWithNoParams;
    private final ResponseListener responseListener;

    public interface ResponseListener {
        void onError(Exception exc);

        void onSuccess(GcsResponse gcsResponse);
    }

    public GcsRequest(ResponseListener responseListener2, Uri uri, HatsDataStore hatsDataStore2) {
        responseListener = responseListener2;
        postData = uri.getEncodedQuery();
        requestUriWithNoParams = uri.buildUpon().clearQuery().build();
        hatsDataStore = hatsDataStore2;
    }

    public void send() {
        HttpURLConnection httpURLConnection = null;
        try {
            long currentTimeMillis = System.currentTimeMillis();
            HttpURLConnection httpURLConnection2 = (HttpURLConnection) new URL(requestUriWithNoParams.toString()).openConnection();
            try {
                httpURLConnection2.setDoOutput(true);
                httpURLConnection2.setInstanceFollowRedirects(false);
                httpURLConnection2.setRequestMethod("POST");
                httpURLConnection2.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                byte[] bytes = postData.getBytes("utf-8");
                httpURLConnection2.setRequestProperty("Content-Length", Integer.toString(bytes.length));
                httpURLConnection2.setRequestProperty("charset", "utf-8");
                httpURLConnection2.setRequestProperty("Connection", "close");
                httpURLConnection2.setRequestProperty("User-Agent", USER_AGENT);
                httpURLConnection2.setUseCaches(false);
                new DataOutputStream(httpURLConnection2.getOutputStream()).write(bytes);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection2.getInputStream()));
                StringBuffer stringBuffer = new StringBuffer();
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    stringBuffer.append(readLine);
                }
                bufferedReader.close();
                long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
                String stringBuffer2 = stringBuffer.toString();
                int length = stringBuffer2.length();
                StringBuilder sb = new StringBuilder(55);
                sb.append("Downloaded ");
                sb.append(length);
                sb.append(" bytes in ");
                sb.append(currentTimeMillis2);
                sb.append(" ms");
                Log.d("HatsLibGcsRequest", sb.toString());
                if (stringBuffer2.isEmpty()) {
                    responseListener.onError(new IOException("GCS responded with no data. The site's publishing state may not be Enabled. Check Site > Advanced settings > Publishing state. For more info, see go/get-hats"));
                }
                hatsDataStore.storeSetCookieHeaders(requestUriWithNoParams, httpURLConnection2.getHeaderFields());
                JSONObject jSONObject = new JSONObject(stringBuffer2).getJSONObject("params");
                int i = jSONObject.getInt("responseCode");
                long j = jSONObject.getLong("expirationDate");
                if (i != 0) {
                    stringBuffer2 = "";
                }
                responseListener.onSuccess(new GcsResponse(i, j, stringBuffer2));
                if (httpURLConnection2 != null) {
                    httpURLConnection2.disconnect();
                }
            } catch (IOException | JSONException e) {
                e = e;
                httpURLConnection = httpURLConnection2;
                try {
                    responseListener.onError(e);
                    if (httpURLConnection == null) {
                        httpURLConnection.disconnect();
                    }
                } catch (Throwable th) {
                    th = th;
                    httpURLConnection2 = httpURLConnection;
                    if (httpURLConnection2 != null) {
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                if (httpURLConnection2 != null) {
                    httpURLConnection2.disconnect();
                }
                throw th;
            }
        } catch (IOException | JSONException e2) {
            e = e2;
            responseListener.onError(e);
            if (httpURLConnection == null) {
            }
        }
    }
}
