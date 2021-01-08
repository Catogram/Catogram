package ua.itaysonlab.catogram;

import android.content.pm.PackageManager;

import org.telegram.messenger.ApplicationLoader;

public class CGControversive {
    public static boolean isControversiveFeaturesEnabled() {
        if ("com.android.vending".equals(ApplicationLoader.applicationContext.getPackageManager().getInstallerPackageName(BuildConfig.APPLICATION_ID)) {
            return false;
        }
        else return true;
    }

    public static boolean noReading() {
        return isControversiveFeaturesEnabled() && CatogramConfig.INSTANCE.getControversiveNoReading();
    }

    public static boolean noPeekTimeout() {
        return isControversiveFeaturesEnabled();
    }
}
