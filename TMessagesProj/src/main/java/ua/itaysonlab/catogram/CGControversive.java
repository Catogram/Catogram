package ua.itaysonlab.catogram;

import android.content.pm.PackageManager;

import org.telegram.messenger.ApplicationLoader;

public class CGControversive {
    private static String UNLOCKER_PACKAGE = "ua.itaysonlab.vkx";

    public static boolean isControversiveFeaturesEnabled() {
        try {
            ApplicationLoader.applicationContext.getPackageManager().getApplicationInfo(UNLOCKER_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean noReading() {
        return isControversiveFeaturesEnabled() && CatogramConfig.controversiveNoReading;
    }
}
