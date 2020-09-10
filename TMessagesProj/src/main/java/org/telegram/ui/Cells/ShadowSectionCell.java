/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewGroup;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CombinedDrawable;

public class ShadowSectionCell extends View {
    private static final int LR_PADDING = AndroidUtilities.dp(16f);
    private static final int HEIGHT = AndroidUtilities.dp(1f);
    private static final int PAD = AndroidUtilities.dp(8f);
    protected ColorDrawable color = new ColorDrawable(Theme.getColor(Theme.key_divider));

    private int size;

    public ShadowSectionCell(Context context) {
        this(context, 12);
    }

    public ShadowSectionCell(Context context, int s) {
        super(context);
        setBackgroundInternal(getThemedDrawable());
        size = s;
    }

    public ShadowSectionCell(Context context, int s, int backgroundColor) {
        super(context);
        setBackgroundColor(backgroundColor);
        size = s;
    }

    public void setBackgroundInternal(Drawable background) {
        super.setBackground(background);
    }

    @Override
    public void setBackground(Drawable background) {

    }

    public Drawable getThemedDrawable() {
        LayerDrawable icon = (LayerDrawable) getResources().getDrawable(R.drawable.vkui_sep, null);
        icon.getDrawable(0).setTint(Theme.getColor(Theme.key_windowBackgroundWhite));
        icon.getDrawable(1).setTint(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        return icon;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(size), MeasureSpec.EXACTLY));
    }
}
