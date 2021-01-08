package ua.itaysonlab.catogram;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;

public class CGControversive {
    public static boolean isControversiveFeaturesEnabled() {
        return "com.android.vending".equals(ApplicationLoader.applicationContext.getPackageManager().getInstallerPackageName(BuildConfig.APPLICATION_ID));
    }

    public static boolean noReading() {
        return isControversiveFeaturesEnabled() && CatogramConfig.INSTANCE.getControversiveNoReading();
    }

    public static boolean noPeekTimeout() {
        return isControversiveFeaturesEnabled();
    }
}
