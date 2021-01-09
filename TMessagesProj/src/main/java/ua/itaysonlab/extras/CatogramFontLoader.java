package ua.itaysonlab.extras;

import android.graphics.Typeface;

import org.telegram.messenger.AndroidUtilities;

import ua.itaysonlab.catogram.CatogramConfig;

public class CatogramFontLoader {
    private static Typeface sysBold = Typeface.create(Typeface.DEFAULT, 700, false);
    private static Typeface sysBoldItalic = Typeface.create(Typeface.DEFAULT, 700, true);
    private static Typeface sysItalic = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC);
    private static Typeface sysMono = Typeface.MONOSPACE;

    public static boolean needRedirect(String path) {
        return (
                path.equals("fonts/rmedium.ttf")
                || path.equals("fonts/rmediumitalic.ttf")
                || path.equals("fonts/ritalic.ttf")
                || path.equals("fonts/rmono.ttf")
        );
    }

    public static Typeface redirect(String path) {
        switch (path) {
            case "fonts/rmediumitalic.ttf":
                return getBoldItalic();
            case "fonts/ritalic.ttf":
                return getItalic();
            case "fonts/rmono.ttf":
                return getMono();
            default:
                return getBold();
        }
    }

    public static Typeface getBold() {
        if (CatogramConfig.INSTANCE.getSystemFontsTT()) return AndroidUtilities.getTypeface("fonts/VKSans-DemiBold.ttf");
        return sysBold;
    }

    public static Typeface getBoldItalic() {
        return sysBoldItalic;
    }

    public static Typeface getItalic() {
        return sysItalic;
        
    }

    public static Typeface getMono() {
        return sysMono;
    }
}
