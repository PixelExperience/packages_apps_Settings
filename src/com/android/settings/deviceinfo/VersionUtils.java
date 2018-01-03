
package com.android.settings.deviceinfo;

import android.os.SystemProperties;

public class VersionUtils {
    public static String getPixelExperienceVersion(){
        return SystemProperties.get("org.pixelexperience.version.display","");
    }
}