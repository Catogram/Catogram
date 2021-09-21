/*
 * This is the source code of Telegram for Android v. 7.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2020.
 */

package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

public class BuildVars {

    public static boolean DEBUG_VERSION = false;
    public static boolean DEBUG_PRIVATE_VERSION = false;
    public static boolean LOGS_ENABLED = false;
    public static boolean USE_CLOUD_STRINGS = true;
    public static boolean CHECK_UPDATES = false;
    public static int APP_ID = BuildConfig.APP_ID;
    public static String APP_HASH = BuildConfig.API_HASH;
    public static int BUILD_VERSION = 2431;
    public static String BUILD_VERSION_STRING = "8.1.1";
    public static boolean NO_SCOPED_STORAGE = true/* || Build.VERSION.SDK_INT <= 28*/;
    //
    public static String SMS_HASH = "w0lkcmTZkKh";
    public static String PLAYSTORE_APP_URL = "https://t.me/catogram";

    static {
        if (ApplicationLoader.applicationContext != null) {
            SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", Context.MODE_PRIVATE);
            LOGS_ENABLED = DEBUG_VERSION || sharedPreferences.getBoolean("logsEnabled", false);
        }
    }

    public static boolean isStandaloneApp() {
        return true;
    }

    public static boolean isBetaApp() {
        return false;
    }
}
