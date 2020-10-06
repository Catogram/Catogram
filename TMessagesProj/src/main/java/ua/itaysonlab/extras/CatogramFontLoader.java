package ua.itaysonlab.extras;

import android.graphics.Typeface;

import org.telegram.messenger.AndroidUtilities;

import ua.itaysonlab.catogram.CatogramConfig;

public class CatogramFontLoader {
    private static Typeface sysBold = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
    private static Typeface sysBoldItalic = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
    private static Typeface sysItalic = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC);
    private static Typeface sysMono = Typeface.MONOSPACE;

    public static boolean needRedirect(String path) {
        return CatogramConfig.INSTANCE.getSystemFonts() && (
                path.equals("fonts/TTCommons-Bold.ttf")
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
        if (CatogramConfig.INSTANCE.getSystemFonts()) {
            return sysBold;
        } else {
            return AndroidUtilities.getTypeface("fonts/TTCommons-Bold.ttf");
        }
    }

    public static Typeface getBoldItalic() {
        if (CatogramConfig.INSTANCE.getSystemFonts()) {
            return sysBoldItalic;
        } else {
            return AndroidUtilities.getTypeface("fonts/rmediumitalic.ttf");
        }
    }

    public static Typeface getItalic() {
        if (CatogramConfig.INSTANCE.getSystemFonts()) {
            return sysItalic;
        } else {
            return AndroidUtilities.getTypeface("fonts/ritalic.ttf");
        }
    }

    public static Typeface getMono() {
        if (CatogramConfig.INSTANCE.getSystemFonts()) {
            return sysMono;
        } else {
            return AndroidUtilities.getTypeface("fonts/rmono.ttf");
        }
    }
}
