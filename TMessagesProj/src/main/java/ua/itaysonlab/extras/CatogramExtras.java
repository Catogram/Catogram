package ua.itaysonlab.extras;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;

import org.telegram.messenger.FileLoader;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import ua.itaysonlab.catogram.CatogramConfig;

public class CatogramExtras {
    public static String CG_VERSION = "3.7.4";

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale =  context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static void setSecureFlag(Window window) {
        if (!CatogramConfig.INSTANCE.getControversiveNoSecureFlag()) window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }

    public static void setSecureFlag(WindowManager.LayoutParams window) {
        if (!CatogramConfig.INSTANCE.getControversiveNoSecureFlag()) window.flags |= WindowManager.LayoutParams.FLAG_SECURE;
    }

    public static void clearSecureFlag(Window window) {
        if (!CatogramConfig.INSTANCE.getControversiveNoSecureFlag()) window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }

    public static void vibrate(Vibrator vibrator, int ms) {
        if (CatogramConfig.INSTANCE.getNoVibration()) return;
        vibrator.vibrate(ms);
    }

    public static void vibrate(Vibrator vibrator, long[] pattern, int repeat) {
        if (CatogramConfig.INSTANCE.getNoVibration()) return;
        vibrator.vibrate(pattern, repeat);
    }

    public static boolean performHapticFeedback(View view, int feedbackConstant, int flags) {
        if (CatogramConfig.INSTANCE.getNoVibration()) return false;
        return view.performHapticFeedback(feedbackConstant, flags);
    }

    public static boolean performHapticFeedback(View view, int feedbackConstant) {
        if (CatogramConfig.INSTANCE.getNoVibration()) return false;
        return view.performHapticFeedback(feedbackConstant);
    }

    public static Typeface getBold() {
        return CatogramFontLoader.getBold();
    }

    public static Typeface getBoldItalic() {
        return CatogramFontLoader.getBoldItalic();
    }

    public static Typeface getItalic() {
        return CatogramFontLoader.getItalic();
    }

    public static Typeface getMono() {
        return CatogramFontLoader.getMono();
    }

    @ColorInt public static int getLightStatusbarColor() {
        if (SharedConfig.noStatusBar) {
            return 0x00000000;
        } else {
            return 0x0f000000;
        }
    }

    @ColorInt public static int getDarkStatusbarColor() {
        if (SharedConfig.noStatusBar) {
            return 0x00000000;
        } else {
            return 0x33000000;
        }
    }

    public static void setAccountBitmap(TLRPC.User user) {
        if (user.photo != null) {
            try {
                final File photo = FileLoader.getPathToAttach(user.photo.photo_big, true);
                byte[] photoData = new byte[(int) photo.length()];
                
                FileInputStream photoIn;
                photoIn = new FileInputStream(photo);
                new DataInputStream(photoIn).readFully(photoData);
                photoIn.close();

                Bitmap bg = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
                if (CatogramConfig.INSTANCE.getDrawerBlur()) bg = Utilities.blurWallpaper(bg);

                currentAccountBitmap = new BitmapDrawable(Resources.getSystem(), bg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap darkenBitmap(Bitmap bm) {
        Canvas canvas = new Canvas(bm);
        Paint p = new Paint(Color.RED);
        ColorFilter filter = new LightingColorFilter(0xFF7F7F7F, 0x00000000);    // darken
        p.setColorFilter(filter);
        canvas.drawBitmap(bm, new Matrix(), p);
        return bm;
    }

    public static String wrapEmoticon(String base) {
        if (base == null) {
            return "\uD83D\uDCC1";
        } else if (base.length() == 0) {
            //Log.d("CG-Test", Arrays.toString(base.getBytes(StandardCharsets.UTF_16BE)));
            return "\uD83D\uDDC2";
        } else {
            //Log.d("CG-Test", Arrays.toString(base.getBytes(StandardCharsets.UTF_16BE)));
            return base;
        }
    }

    public static BitmapDrawable currentAccountBitmap;

    // 80 in official
    public static int LOAD_AVATAR_COUNT_HEADER = 100;
    public static int LOAD_AVATAR_COUNT = 100;
}
