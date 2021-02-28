package ua.itaysonlab.catogram;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;

public class CGControversive {
    public static boolean isControversiveFeaturesEnabled() {
        return false;
    }

    public static boolean noReading() {
        return false;
    }

    public static boolean noPeekTimeout() {
        return false;
    }
}
