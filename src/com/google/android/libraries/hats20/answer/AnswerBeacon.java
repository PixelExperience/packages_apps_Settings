package com.google.android.libraries.hats20.answer;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AnswerBeacon implements Parcelable {
    public static final Parcelable.Creator<AnswerBeacon> CREATOR = new Parcelable.Creator<AnswerBeacon>() {
        public AnswerBeacon createFromParcel(Parcel parcel) {
            return new AnswerBeacon(parcel);
        }

        public AnswerBeacon[] newArray(int i) {
            return new AnswerBeacon[i];
        }
    };
    private final Bundle parameterBundle;

    public int describeContents() {
        return 0;
    }

    public AnswerBeacon() {
        parameterBundle = new Bundle();
        parameterBundle.putString("m.v", "3");
    }

    private AnswerBeacon(Parcel parcel) {
        parameterBundle = parcel.readBundle(AnswerBeacon.class.getClassLoader());
        if (parameterBundle == null) {
            throw new NullPointerException("Parcel did not contain required Bundle while unparceling an AnswerBeacon.");
        }
    }

    public Uri exportAllParametersInQueryString() {
        Uri.Builder builder = new Uri.Builder();
        setLongParameter("m.lt", System.currentTimeMillis() / 1000);
        for (String str : parameterBundle.keySet()) {
            Object obj = parameterBundle.get(str);
            if (obj instanceof List) {
                for (Object valueOf : (List) obj) {
                    builder.appendQueryParameter(str, String.valueOf(valueOf));
                }
            } else if (obj != null) {
                builder.appendQueryParameter(str, String.valueOf(obj));
            }
        }
        if (hasBeaconTypeOtherAccess()) {
            builder.appendQueryParameter("m.sh", "close");
        }
        builder.appendQueryParameter("d", "1");
        return builder.build();
    }

    public String toString() {
        String valueOf = String.valueOf(exportAllParametersInQueryString().toString().replace("&", "\n"));
        StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 14);
        sb.append("AnswerBeacon{");
        sb.append(valueOf);
        sb.append("}");
        return sb.toString();
    }

    public AnswerBeacon setBeaconType(String str) {
        if (str != null) {
            setStringParameter("t", str);
            return this;
        }
        throw new NullPointerException("Beacon type cannot be null.");
    }

    public boolean hasBeaconType() {
        return parameterBundle.getString("t") != null;
    }

    public boolean hasBeaconTypeFullAnswer() {
        return "a".equals(parameterBundle.getString("t"));
    }

    public boolean hasBeaconTypeOtherAccess() {
        return "o".equals(parameterBundle.getString("t"));
    }

    public AnswerBeacon setPromptParams(String str) {
        setStringParameter("p", str);
        return this;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeBundle(parameterBundle);
    }

    public int hashCode() {
        return parameterBundle.keySet().hashCode();
    }

    public boolean equals(Object obj) {
        return (obj instanceof AnswerBeacon) && areBundlesEqual(parameterBundle, ((AnswerBeacon) obj).parameterBundle);
    }

    public AnswerBeacon setQuestionResponse(int i, QuestionResponse questionResponse) {
        setDelayMsForQuestion(i, questionResponse.getDelayMs());
        setOrdering(i, questionResponse.getOrdering());
        hasWriteIn(i, questionResponse.hasWriteIn());
        addResponses(i, questionResponse.getResponses());
        return this;
    }

    private void setDelayMsForQuestion(int i, long j) {
        Bundle bundle = parameterBundle;
        StringBuilder sb = new StringBuilder("m.sc-".length() + 11);
        sb.append("m.sc-");
        sb.append(i);
        bundle.remove(sb.toString());
        Bundle bundle2 = parameterBundle;
        StringBuilder sb2 = new StringBuilder("m.d-".length() + 11);
        sb2.append("m.d-");
        sb2.append(i);
        bundle2.remove(sb2.toString());
        if (i != 0 || j >= 1500) {
            StringBuilder sb3 = new StringBuilder("m.d-".length() + 11);
            sb3.append("m.d-");
            sb3.append(i);
            setLongParameter(sb3.toString(), j);
            return;
        }
        StringBuilder sb4 = new StringBuilder(63);
        sb4.append("First question delay ");
        sb4.append(j);
        sb4.append(" is considered spammy.");
        Log.d("HatsLibAnswerBeacon", sb4.toString());
        StringBuilder sb5 = new StringBuilder("m.sc-".length() + 11);
        sb5.append("m.sc-");
        sb5.append(i);
        setLongParameter(sb5.toString(), j);
    }

    private void addResponses(int i, List<String> list) {
        Bundle bundle = parameterBundle;
        StringBuilder sb = new StringBuilder("r.r-".length() + 11);
        sb.append("r.r-");
        sb.append(i);
        bundle.putStringArrayList(sb.toString(), new ArrayList(list));
    }

    private void setOrdering(int i, String str) {
        StringBuilder sb = new StringBuilder("r.o-".length() + 11);
        sb.append("r.o-");
        sb.append(i);
        setStringParameter(sb.toString(), str);
    }

    private void hasWriteIn(int i, boolean z) {
        StringBuilder sb = new StringBuilder("r.t-".length() + 11);
        sb.append("r.t-");
        sb.append(i);
        setBooleanParameter(sb.toString(), z);
    }

    public AnswerBeacon setShown(int i) {
        StringBuilder sb = new StringBuilder("r.s-".length() + 11);
        sb.append("r.s-");
        sb.append(i);
        setBooleanParameter(sb.toString(), true);
        return this;
    }

    private AnswerBeacon setStringParameter(String str, String str2) {
        if (str2 == null) {
            parameterBundle.remove(str);
        } else {
            parameterBundle.putString(str, str2);
        }
        return this;
    }

    private AnswerBeacon setLongParameter(String str, long j) {
        if (j < 0) {
            parameterBundle.remove(str);
        } else {
            parameterBundle.putLong(str, j);
        }
        return this;
    }

    private AnswerBeacon setBooleanParameter(String str, boolean z) {
        if (z) {
            parameterBundle.putString(str, "1");
        } else {
            parameterBundle.remove(str);
        }
        return this;
    }

    private static boolean areBundlesEqual(Bundle bundle, Bundle bundle2) {
        if (bundle.size() != bundle2.size()) {
            return false;
        }
        Set<String> keySet = bundle.keySet();
        if (!keySet.containsAll(bundle2.keySet())) {
            return false;
        }
        for (String str : keySet) {
            Object obj = bundle.get(str);
            Object obj2 = bundle2.get(str);
            if (obj == null) {
                if (obj2 != null) {
                    return false;
                }
            } else if (!obj.equals(obj2)) {
                return false;
            }
        }
        return true;
    }
}
