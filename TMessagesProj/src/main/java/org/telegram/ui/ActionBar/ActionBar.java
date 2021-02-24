/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.ActionBar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.Adapters.FiltersView;
import org.telegram.ui.Components.EllipsizeSpanAnimator;
import org.telegram.ui.Components.FireworksEffect;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SnowflakesEffect;

import java.util.ArrayList;

import ua.itaysonlab.catogram.CatogramConfig;

public class ActionBar extends FrameLayout {

    public static class ActionBarMenuOnItemClick {
        public void onItemClick(int id) {

        }

        public boolean canOpenMenu() {
            return true;
        }
    }

    private ImageView backButtonImageView;
    private SimpleTextView[] titleTextView = new SimpleTextView[2];
    private SimpleTextView subtitleTextView;
    private View actionModeTop;
    private int actionModeColor;
    private int actionBarColor;
    private ActionBarMenu menu;
    private ActionBarMenu actionMode;
    private String actionModeTag;
    private boolean ignoreLayoutRequest;
    private boolean occupyStatusBar = Build.VERSION.SDK_INT >= 21;
    private boolean actionModeVisible;
    private boolean addToContainer = true;
    private boolean clipContent;
    private boolean interceptTouches = true;
    private int extraHeight;
    private AnimatorSet actionModeAnimation;
    private View actionModeExtraView;
    private View actionModeTranslationView;
    private View actionModeShowingView;
    private View[] actionModeHidingViews;

    private boolean supportsHolidayImage;
    private SnowflakesEffect snowflakesEffect;
    private FireworksEffect fireworksEffect;
    private Paint.FontMetricsInt fontMetricsInt;
    private boolean manualStart;
    private Rect rect;

    private int titleRightMargin;

    private boolean allowOverlayTitle;
    private CharSequence lastTitle;
    private CharSequence lastOverlayTitle;
    private Object[] overlayTitleToSet = new Object[3];
    private Runnable lastRunnable;
    private boolean titleOverlayShown;
    private Runnable titleActionRunnable;
    private boolean castShadows = !CatogramConfig.INSTANCE.getFlatActionbar();

    protected boolean isSearchFieldVisible;
    protected int itemsBackgroundColor;
    protected int itemsActionModeBackgroundColor;
    protected int itemsColor;
    protected int itemsActionModeColor;
    private boolean isBackOverlayVisible;
    protected BaseFragment parentFragment;
    public ActionBarMenuOnItemClick actionBarMenuOnItemClick;
    private int titleColorToSet = 0;
    private boolean overlayTitleAnimation;
    private boolean titleAnimationRunning;
    private boolean fromBottom;

    EllipsizeSpanAnimator ellipsizeSpanAnimator = new EllipsizeSpanAnimator(this);

    public ActionBar(Context context) {
        super(context);
        setOnClickListener(v -> {
            if (isSearchFieldVisible()) {
                return;
            }
            if (titleActionRunnable != null) {
                titleActionRunnable.run();
            }
        });
    }

    private void createBackButtonImage() {
        if (backButtonImageView != null) {
            return;
        }
        backButtonImageView = new ImageView(getContext());
        backButtonImageView.setScaleType(ImageView.ScaleType.CENTER);
        backButtonImageView.setBackgroundDrawable(Theme.createSelectorDrawable(itemsBackgroundColor));
        if (itemsColor != 0) {
            backButtonImageView.setColorFilter(new PorterDuffColorFilter(itemsColor, PorterDuff.Mode.MULTIPLY));
        }
        backButtonImageView.setPadding(AndroidUtilities.dp(1), 0, 0, 0);
        addView(backButtonImageView, LayoutHelper.createFrame(54, 54, Gravity.LEFT | Gravity.TOP));

        backButtonImageView.setOnClickListener(v -> {
            if (!actionModeVisible && isSearchFieldVisible) {
                closeSearchField();
                return;
            }
            if (actionBarMenuOnItemClick != null) {
                actionBarMenuOnItemClick.onItemClick(-1);
            }
        });
        backButtonImageView.setContentDescription(LocaleController.getString("AccDescrGoBack", R.string.AccDescrGoBack));
    }

    public void setBackButtonDrawable(Drawable drawable) {
        if (backButtonImageView == null) {
            createBackButtonImage();
        }
        backButtonImageView.setVisibility(drawable == null ? GONE : VISIBLE);
        backButtonImageView.setImageDrawable(drawable);
        if (drawable instanceof BackDrawable) {
            BackDrawable backDrawable = (BackDrawable) drawable;
            backDrawable.setRotation(isActionModeShowed() ? 1 : 0, false);
            backDrawable.setRotatedColor(itemsActionModeColor);
            backDrawable.setColor(itemsColor);
        }
    }

    public void setBackButtonContentDescription(CharSequence description) {
        if (backButtonImageView != null) {
            backButtonImageView.setContentDescription(description);
        }
    }

    public void setSupportsHolidayImage(boolean value) {
        supportsHolidayImage = value;
        if (supportsHolidayImage) {
            fontMetricsInt = new Paint.FontMetricsInt();
            rect = new Rect();
        }
        invalidate();
    }

    public void setOnTitleLongClickListner(View.OnLongClickListener listener) {
        titleTextView[0].setOnLongClickListener(listener);
        subtitleTextView.setOnLongClickListener(listener);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (supportsHolidayImage && !titleOverlayShown && !LocaleController.isRTL && ev.getAction() == MotionEvent.ACTION_DOWN) {
            Drawable drawable = Theme.getCurrentHolidayDrawable();
            if (drawable != null && drawable.getBounds().contains((int) ev.getX(), (int) ev.getY())) {
                manualStart = true;
                if (snowflakesEffect == null) {
                    fireworksEffect = null;
                    snowflakesEffect = new SnowflakesEffect();
                    titleTextView[0].invalidate();
                    invalidate();
                } else {
                    snowflakesEffect = null;
                    fireworksEffect = new FireworksEffect();
                    titleTextView[0].invalidate();
                    invalidate();
                }
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    protected boolean shouldClipChild(View child) {
        return clipContent && (child == titleTextView[0] || child == titleTextView[1] || child == subtitleTextView || child == menu || child == backButtonImageView);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean clip = shouldClipChild(child);
        if (clip) {
            canvas.save();
            canvas.clipRect(0, -getTranslationY() + (occupyStatusBar ? AndroidUtilities.statusBarHeight : 0), getMeasuredWidth(), getMeasuredHeight());
        }
        boolean result = super.drawChild(canvas, child, drawingTime);
        if (supportsHolidayImage && !titleOverlayShown && !LocaleController.isRTL && (child == titleTextView[0] || child == titleTextView[1])) {
            Drawable drawable = Theme.getCurrentHolidayDrawable();
            if (drawable != null) {

                SimpleTextView titleView = (SimpleTextView) child;
                if (titleView.getVisibility() == View.VISIBLE && titleView.getText() instanceof String) {
                    TextPaint textPaint = titleView.getTextPaint();
                    textPaint.getFontMetricsInt(fontMetricsInt);
                    textPaint.getTextBounds((String) titleView.getText(), 0, 1, rect);
                    int x = titleView.getTextStartX() + Theme.getCurrentHolidayDrawableXOffset() + (rect.width() - (drawable.getIntrinsicWidth() + Theme.getCurrentHolidayDrawableXOffset())) / 2;
                    int y = titleView.getTextStartY() + Theme.getCurrentHolidayDrawableYOffset() + (int) Math.ceil((titleView.getTextHeight() - rect.height()) / 2.0f);
                    drawable.setBounds(x, y - drawable.getIntrinsicHeight(), x + drawable.getIntrinsicWidth(), y);
                    drawable.setAlpha((int) (255 * titleView.getAlpha()));
                    drawable.draw(canvas);
                    if (overlayTitleAnimationInProgress) {
                        child.invalidate();
                        invalidate();
                    }
                }

                if (Theme.canStartHolidayAnimation()) {
                    snowflakesEffect = new SnowflakesEffect();
                }

                if (snowflakesEffect != null) {
                    snowflakesEffect.onDraw(this, canvas);
                } else if (fireworksEffect != null) {
                    fireworksEffect.onDraw(this, canvas);
                }
            }
        }
        if (clip) {
            canvas.restore();
        }
        return result;
    }

    @Override
    public void setTranslationY(float translationY) {
        super.setTranslationY(translationY);
        if (clipContent) {
            invalidate();
        }
    }

    public void setBackButtonImage(int resource) {
        if (backButtonImageView == null) {
            createBackButtonImage();
        }
        backButtonImageView.setVisibility(resource == 0 ? GONE : VISIBLE);
        backButtonImageView.setImageResource(resource);
    }

    private void createSubtitleTextView() {
        if (subtitleTextView != null) {
            return;
        }
        subtitleTextView = new SimpleTextView(getContext());
        subtitleTextView.setGravity(Gravity.LEFT);
        subtitleTextView.setVisibility(GONE);
        subtitleTextView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSubtitle));
        addView(subtitleTextView, 0, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));
    }

    public void setAddToContainer(boolean value) {
        addToContainer = value;
    }

    public boolean shouldAddToContainer() {
        return addToContainer;
    }

    public void setClipContent(boolean value) {
        clipContent = value;
    }

    public void setSubtitle(CharSequence value) {
        if (value != null && subtitleTextView == null) {
            createSubtitleTextView();
        }
        if (subtitleTextView != null) {
            subtitleTextView.setVisibility(!TextUtils.isEmpty(value) && !isSearchFieldVisible ? VISIBLE : GONE);
            subtitleTextView.setAlpha(1f);
            subtitleTextView.setText(value);
        }
    }

    private void createTitleTextView(int i) {
        if (titleTextView[i] != null) {
            return;
        }
        titleTextView[i] = new SimpleTextView(getContext());
        titleTextView[i].setGravity(Gravity.LEFT);
        if (titleColorToSet != 0) {
            titleTextView[i].setTextColor(titleColorToSet);
        } else {
            titleTextView[i].setTextColor(Theme.getColor(Theme.key_actionBarDefaultTitle));
        }
        titleTextView[i].setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        addView(titleTextView[i], 0, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));
    }

    public void setTitleRightMargin(int value) {
        titleRightMargin = value;
    }

    public void setTitle(CharSequence value) {
        if (value != null && titleTextView[0] == null) {
            createTitleTextView(0);
        }
        if (titleTextView[0] != null) {
            lastTitle = value;
            titleTextView[0].setVisibility(value != null && !isSearchFieldVisible ? VISIBLE : INVISIBLE);
            titleTextView[0].setText(value);
        }
        fromBottom = false;
    }

    public void setTitleColor(int color) {
        if (titleTextView[0] == null) {
            createTitleTextView(0);
        }
        titleColorToSet = color;
        titleTextView[0].setTextColor(color);
        if (titleTextView[1] != null) {
            titleTextView[1].setTextColor(color);
        }
    }

    public void setSubtitleColor(int color) {
        if (subtitleTextView == null) {
            createSubtitleTextView();
        }
        subtitleTextView.setTextColor(color);
    }

    public void setTitleScrollNonFitText(boolean b) {
        titleTextView[0].setScrollNonFitText(b);
    }

    public void setPopupItemsColor(int color, boolean icon, boolean forActionMode) {
        if (forActionMode && actionMode != null) {
            actionMode.setPopupItemsColor(color, icon);
        } else if (!forActionMode && menu != null) {
            menu.setPopupItemsColor(color, icon);
        }
    }

    public void setPopupItemsSelectorColor(int color, boolean forActionMode) {
        if (forActionMode && actionMode != null) {
            actionMode.setPopupItemsSelectorColor(color);
        } else if (!forActionMode && menu != null) {
            menu.setPopupItemsSelectorColor(color);
        }
    }

    public void setPopupBackgroundColor(int color, boolean forActionMode) {
        if (forActionMode && actionMode != null) {
            actionMode.redrawPopup(color);
        } else if (!forActionMode && menu != null) {
            menu.redrawPopup(color);
        }
    }

    public SimpleTextView getSubtitleTextView() {
        return subtitleTextView;
    }

    public SimpleTextView getTitleTextView() {
        return titleTextView[0];
    }

    public String getTitle() {
        if (titleTextView[0] == null) {
            return null;
        }
        return titleTextView[0].getText().toString();
    }

    public String getSubtitle() {
        if (subtitleTextView == null) {
            return null;
        }
        return subtitleTextView.getText().toString();
    }

    public ActionBarMenu createMenu() {
        if (menu != null) {
            return menu;
        }
        menu = new ActionBarMenu(getContext(), this);
        addView(menu, 0, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.RIGHT));
        return menu;
    }

    public void setActionBarMenuOnItemClick(ActionBarMenuOnItemClick listener) {
        actionBarMenuOnItemClick = listener;
    }

    public ActionBarMenuOnItemClick getActionBarMenuOnItemClick() {
        return actionBarMenuOnItemClick;
    }

    public ImageView getBackButton() {
        return backButtonImageView;
    }

    public ActionBarMenu createActionMode() {
        return createActionMode(true, null);
    }

    public boolean actionModeIsExist(String tag) {
        if (actionMode != null && ((actionModeTag == null && tag == null) || (actionModeTag != null && actionModeTag.equals(tag)))) {
            return true;
        }
        return false;
    }

    public ActionBarMenu createActionMode(boolean needTop, String tag) {
        if (actionModeIsExist(tag)) {
            return actionMode;
        }
        if (actionMode != null) {
            removeView(actionMode);
            actionMode = null;
        }
        actionModeTag = tag;
        actionMode = new ActionBarMenu(getContext(), this) {
            @Override
            public void setBackgroundColor(int color) {
                super.setBackgroundColor(actionModeColor = color);
            }
        };
        actionMode.isActionMode = true;
        actionMode.setClickable(true);
        actionMode.setBackgroundColor(Theme.getColor(Theme.key_actionBarActionModeDefault));
        addView(actionMode, indexOfChild(backButtonImageView));
        actionMode.setPadding(0, occupyStatusBar ? AndroidUtilities.statusBarHeight : 0, 0, 0);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) actionMode.getLayoutParams();
        layoutParams.height = LayoutHelper.MATCH_PARENT;
        layoutParams.width = LayoutHelper.MATCH_PARENT;
        layoutParams.bottomMargin = extraHeight;
        layoutParams.gravity = Gravity.RIGHT;
        actionMode.setLayoutParams(layoutParams);
        actionMode.setVisibility(INVISIBLE);

        if (occupyStatusBar && needTop && actionModeTop == null) {
            actionModeTop = new View(getContext());
            actionModeTop.setBackgroundColor(Theme.getColor(Theme.key_actionBarActionModeDefaultTop));
            addView(actionModeTop);
            layoutParams = (FrameLayout.LayoutParams) actionModeTop.getLayoutParams();
            layoutParams.height = AndroidUtilities.statusBarHeight;
            layoutParams.width = LayoutHelper.MATCH_PARENT;
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            actionModeTop.setLayoutParams(layoutParams);
            actionModeTop.setVisibility(INVISIBLE);
        }

        return actionMode;
    }

    public void showActionMode() {
        showActionMode(true, null, null, null, null, null, 0);
    }

    public void showActionMode(boolean animated) {
        showActionMode(animated, null, null, null, null, null, 0);
    }

    public void showActionMode(boolean animated, View extraView, View showingView, View[] hidingViews, boolean[] hideView, View translationView, int translation) {
        if (actionMode == null || actionModeVisible) {
            return;
        }
        actionModeVisible = true;
        if (animated) {
            ArrayList<Animator> animators = new ArrayList<>();
            animators.add(ObjectAnimator.ofFloat(actionMode, View.ALPHA, 0.0f, 1.0f));
            if (hidingViews != null) {
                for (int a = 0; a < hidingViews.length; a++) {
                    if (hidingViews[a] != null) {
                        animators.add(ObjectAnimator.ofFloat(hidingViews[a], View.ALPHA, 1.0f, 0.0f));
                    }
                }
            }
            if (showingView != null) {
                animators.add(ObjectAnimator.ofFloat(showingView, View.ALPHA, 0.0f, 1.0f));
            }
            if (translationView != null) {
                animators.add(ObjectAnimator.ofFloat(translationView, View.TRANSLATION_Y, translation));
                actionModeTranslationView = translationView;
            }
            actionModeExtraView = extraView;
            actionModeShowingView = showingView;
            actionModeHidingViews = hidingViews;
            if (occupyStatusBar && actionModeTop != null && !SharedConfig.noStatusBar) {
                animators.add(ObjectAnimator.ofFloat(actionModeTop, View.ALPHA, 0.0f, 1.0f));
            }
            if (SharedConfig.noStatusBar) {
                if (AndroidUtilities.computePerceivedBrightness(actionModeColor) < 0.721f) {
                    AndroidUtilities.setLightStatusBar(((Activity) getContext()).getWindow(), false);
                } else {
                    AndroidUtilities.setLightStatusBar(((Activity) getContext()).getWindow(), true);
                }
            }
            if (actionModeAnimation != null) {
                actionModeAnimation.cancel();
            }
            actionModeAnimation = new AnimatorSet();
            actionModeAnimation.playTogether(animators);
            actionModeAnimation.setDuration(200);
            actionModeAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    actionMode.setVisibility(VISIBLE);
                    if (occupyStatusBar && actionModeTop != null && !SharedConfig.noStatusBar) {
                        actionModeTop.setVisibility(VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (actionModeAnimation != null && actionModeAnimation.equals(animation)) {
                        actionModeAnimation = null;
                        if (titleTextView[0] != null) {
                            titleTextView[0].setVisibility(INVISIBLE);
                        }
                        if (subtitleTextView != null && !TextUtils.isEmpty(subtitleTextView.getText())) {
                            subtitleTextView.setVisibility(INVISIBLE);
                        }
                        if (menu != null) {
                            menu.setVisibility(INVISIBLE);
                        }
                        if (actionModeHidingViews != null) {
                            for (int a = 0; a < actionModeHidingViews.length; a++) {
                                if (actionModeHidingViews[a] != null) {
                                    if (hideView == null || a >= hideView.length || hideView[a]) {
                                        actionModeHidingViews[a].setVisibility(INVISIBLE);
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (actionModeAnimation != null && actionModeAnimation.equals(animation)) {
                        actionModeAnimation = null;
                    }
                }
            });
            actionModeAnimation.start();
            if (backButtonImageView != null) {
                Drawable drawable = backButtonImageView.getDrawable();
                if (drawable instanceof BackDrawable) {
                    ((BackDrawable) drawable).setRotation(1, true);
                }
                backButtonImageView.setBackgroundDrawable(Theme.createSelectorDrawable(itemsActionModeBackgroundColor));
            }
        } else {
            actionMode.setAlpha(1.0f);
            if (hidingViews != null) {
                for (int a = 0; a < hidingViews.length; a++) {
                    if (hidingViews[a] != null) {
                        hidingViews[a].setAlpha(0.0f);
                    }
                }
            }
            if (showingView != null) {
                showingView.setAlpha(1.0f);
            }
            if (translationView != null) {
                translationView.setTranslationY(translation);
                actionModeTranslationView = translationView;
            }
            actionModeExtraView = extraView;
            actionModeShowingView = showingView;
            actionModeHidingViews = hidingViews;
            if (occupyStatusBar && actionModeTop != null && !SharedConfig.noStatusBar) {
                actionModeTop.setAlpha(1.0f);
            }
            if (SharedConfig.noStatusBar) {
                if (AndroidUtilities.computePerceivedBrightness(actionModeColor) < 0.721f) {
                    AndroidUtilities.setLightStatusBar(((Activity) getContext()).getWindow(), false);
                } else {
                    AndroidUtilities.setLightStatusBar(((Activity) getContext()).getWindow(), true);
                }
            }
            actionMode.setVisibility(VISIBLE);
            if (occupyStatusBar && actionModeTop != null && !SharedConfig.noStatusBar) {
                actionModeTop.setVisibility(VISIBLE);
            }
            if (titleTextView[0] != null) {
                titleTextView[0].setVisibility(INVISIBLE);
            }
            if (subtitleTextView != null && !TextUtils.isEmpty(subtitleTextView.getText())) {
                subtitleTextView.setVisibility(INVISIBLE);
            }
            if (menu != null) {
                menu.setVisibility(INVISIBLE);
            }
            if (actionModeHidingViews != null) {
                for (int a = 0; a < actionModeHidingViews.length; a++) {
                    if (actionModeHidingViews[a] != null) {
                        if (hideView == null || a >= hideView.length || hideView[a]) {
                            actionModeHidingViews[a].setVisibility(INVISIBLE);
                        }
                    }
                }
            }
            if (backButtonImageView != null) {
                Drawable drawable = backButtonImageView.getDrawable();
                if (drawable instanceof BackDrawable) {
                    ((BackDrawable) drawable).setRotation(1, false);
                }
                backButtonImageView.setBackgroundDrawable(Theme.createSelectorDrawable(itemsActionModeBackgroundColor));
            }
        }
    }

    public void hideActionMode() {
        if (actionMode == null || !actionModeVisible) {
            return;
        }
        actionMode.hideAllPopupMenus();
        actionModeVisible = false;
        ArrayList<Animator> animators = new ArrayList<>();
        animators.add(ObjectAnimator.ofFloat(actionMode, View.ALPHA, 0.0f));
        if (actionModeHidingViews != null) {
            for (int a = 0; a < actionModeHidingViews.length; a++) {
                if (actionModeHidingViews[a] != null) {
                    actionModeHidingViews[a].setVisibility(VISIBLE);
                    animators.add(ObjectAnimator.ofFloat(actionModeHidingViews[a], View.ALPHA, 1.0f));
                }
            }
        }
        if (actionModeTranslationView != null) {
            animators.add(ObjectAnimator.ofFloat(actionModeTranslationView, View.TRANSLATION_Y, 0.0f));
            actionModeTranslationView = null;
        }
        if (actionModeShowingView != null) {
            animators.add(ObjectAnimator.ofFloat(actionModeShowingView, View.ALPHA, 0.0f));
        }
        if (occupyStatusBar && actionModeTop != null && !SharedConfig.noStatusBar) {
            animators.add(ObjectAnimator.ofFloat(actionModeTop, View.ALPHA, 0.0f));
        }
        if (SharedConfig.noStatusBar) {
            if (AndroidUtilities.computePerceivedBrightness(actionBarColor) < 0.721f) {
                AndroidUtilities.setLightStatusBar(((Activity) getContext()).getWindow(), false);
            } else {
                AndroidUtilities.setLightStatusBar(((Activity) getContext()).getWindow(), true);
            }
        }
        if (actionModeAnimation != null) {
            actionModeAnimation.cancel();
        }
        actionModeAnimation = new AnimatorSet();
        actionModeAnimation.playTogether(animators);
        actionModeAnimation.setDuration(200);
        actionModeAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (actionModeAnimation != null && actionModeAnimation.equals(animation)) {
                    actionModeAnimation = null;
                    actionMode.setVisibility(INVISIBLE);
                    if (occupyStatusBar && actionModeTop != null && !SharedConfig.noStatusBar) {
                        actionModeTop.setVisibility(INVISIBLE);
                    }
                    if (actionModeExtraView != null) {
                        actionModeExtraView.setVisibility(INVISIBLE);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (actionModeAnimation != null && actionModeAnimation.equals(animation)) {
                    actionModeAnimation = null;
                }
            }
        });
        actionModeAnimation.start();
        if (!isSearchFieldVisible) {
            if (titleTextView[0] != null) {
                titleTextView[0].setVisibility(VISIBLE);
            }
            if (subtitleTextView != null && !TextUtils.isEmpty(subtitleTextView.getText())) {
                subtitleTextView.setVisibility(VISIBLE);
            }
        }
        if (menu != null) {
            menu.setVisibility(VISIBLE);
        }
        if (backButtonImageView != null) {
            Drawable drawable = backButtonImageView.getDrawable();
            if (drawable instanceof BackDrawable) {
                ((BackDrawable) drawable).setRotation(0, true);
            }
            backButtonImageView.setBackgroundDrawable(Theme.createSelectorDrawable(itemsBackgroundColor));
        }
    }

    public void showActionModeTop() {
        if (occupyStatusBar && actionModeTop == null) {
            actionModeTop = new View(getContext());
            actionModeTop.setBackgroundColor(Theme.getColor(Theme.key_actionBarActionModeDefaultTop));
            addView(actionModeTop);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) actionModeTop.getLayoutParams();
            layoutParams.height = AndroidUtilities.statusBarHeight;
            layoutParams.width = LayoutHelper.MATCH_PARENT;
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            actionModeTop.setLayoutParams(layoutParams);
        }
    }

    public void setActionModeTopColor(int color) {
        if (actionModeTop != null) {
            actionModeTop.setBackgroundColor(color);
        }
    }

    public void setSearchTextColor(int color, boolean placeholder) {
        if (menu != null) {
            menu.setSearchTextColor(color, placeholder);
        }
    }

    public void setActionModeColor(int color) {
        if (actionMode != null) {
            actionMode.setBackgroundColor(color);
        }
    }

    public void setActionModeOverrideColor(int color) {
        actionModeColor = color;
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(actionBarColor = color);
    }

    public boolean isActionModeShowed() {
        return actionMode != null && actionModeVisible;
    }

    public void onSearchFieldVisibilityChanged(boolean visible) {
        isSearchFieldVisible = visible;
        if (titleTextView[0] != null) {
            titleTextView[0].setVisibility(visible ? INVISIBLE : VISIBLE);
        }
        if (subtitleTextView != null && !TextUtils.isEmpty(subtitleTextView.getText())) {
            subtitleTextView.setVisibility(visible ? INVISIBLE : VISIBLE);
        }
        Drawable drawable = backButtonImageView.getDrawable();
        if (drawable instanceof MenuDrawable) {
            MenuDrawable menuDrawable = (MenuDrawable) drawable;
            menuDrawable.setRotateToBack(true);
            menuDrawable.setRotation(visible ? 1 : 0, true);
        }
    }

    public void setInterceptTouches(boolean value) {
        interceptTouches = value;
    }

    public void setExtraHeight(int value) {
        extraHeight = value;
        if (actionMode != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) actionMode.getLayoutParams();
            layoutParams.bottomMargin = extraHeight;
            actionMode.setLayoutParams(layoutParams);
        }
    }

    public void closeSearchField() {
        closeSearchField(true);
    }

    public void closeSearchField(boolean closeKeyboard) {
        if (!isSearchFieldVisible || menu == null) {
            return;
        }
        menu.closeSearchField(closeKeyboard);
    }

    public void openSearchField(String text, boolean animated) {
        if (menu == null || text == null) {
            return;
        }
        menu.openSearchField(!isSearchFieldVisible, text, animated);
    }

    public void setSearchFilter(FiltersView.MediaFilterData filter) {
        if (menu != null) {
            menu.setFilter(filter);
        }
    }

    public void setSearchFieldText(String text) {
        menu.setSearchFieldText(text);
    }

    public void onSearchPressed() {
        menu.onSearchPressed();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (backButtonImageView != null) {
            backButtonImageView.setEnabled(enabled);
        }
        if (menu != null) {
            menu.setEnabled(enabled);
        }
        if (actionMode != null) {
            actionMode.setEnabled(enabled);
        }
    }

    @Override
    public void requestLayout() {
        if (ignoreLayoutRequest) {
            return;
        }
        super.requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int actionBarHeight = getCurrentActionBarHeight();
        int actionBarHeightSpec = MeasureSpec.makeMeasureSpec(actionBarHeight, MeasureSpec.EXACTLY);

        ignoreLayoutRequest = true;
        if (actionModeTop != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) actionModeTop.getLayoutParams();
            layoutParams.height = AndroidUtilities.statusBarHeight;
        }
        if (actionMode != null) {
            actionMode.setPadding(0, occupyStatusBar ? AndroidUtilities.statusBarHeight : 0, 0, 0);
        }
        ignoreLayoutRequest = false;

        setMeasuredDimension(width, actionBarHeight + (occupyStatusBar ? AndroidUtilities.statusBarHeight : 0) + extraHeight);

        int textLeft;
        if (backButtonImageView != null && backButtonImageView.getVisibility() != GONE) {
            backButtonImageView.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(54), MeasureSpec.EXACTLY), actionBarHeightSpec);
            textLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 80 : 72);
        } else {
            textLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 26 : 18);
        }

        if (menu != null && menu.getVisibility() != GONE) {
            int menuWidth;
            if (isSearchFieldVisible) {
                menuWidth = MeasureSpec.makeMeasureSpec(width - AndroidUtilities.dp(AndroidUtilities.isTablet() ? 74 : 66), MeasureSpec.EXACTLY);
            } else {
                menuWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
            }
            menu.measure(menuWidth, actionBarHeightSpec);
        }

        for (int i = 0; i < 2; i++) {
            if (titleTextView[0] != null && titleTextView[0].getVisibility() != GONE || subtitleTextView != null && subtitleTextView.getVisibility() != GONE) {
                int availableWidth = width - (menu != null ? menu.getMeasuredWidth() : 0) - AndroidUtilities.dp(16) - textLeft - titleRightMargin;

                if (((fromBottom && i == 0) || (!fromBottom && i == 1)) && overlayTitleAnimation && titleAnimationRunning) {
                    titleTextView[i].setTextSize(!AndroidUtilities.isTablet() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 18 : 20);
                } else {
                    if (titleTextView[0] != null && titleTextView[0].getVisibility() != GONE && subtitleTextView != null && subtitleTextView.getVisibility() != GONE) {
                        if (titleTextView[i] != null) {
                            titleTextView[i].setTextSize(AndroidUtilities.isTablet() ? 20 : 18);
                        }
                        subtitleTextView.setTextSize(AndroidUtilities.isTablet() ? 16 : 14);
                    } else {
                        if (titleTextView[i] != null && titleTextView[i].getVisibility() != GONE) {
                            titleTextView[i].setTextSize(!AndroidUtilities.isTablet() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 18 : 20);
                        }
                        if (subtitleTextView != null && subtitleTextView.getVisibility() != GONE) {
                            subtitleTextView.setTextSize(!AndroidUtilities.isTablet() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 14 : 16);
                        }
                    }
                }

                if (titleTextView[i] != null && titleTextView[i].getVisibility() != GONE) {
                    CharSequence text = titleTextView[i].getText();
                    titleTextView[i].setPivotX(titleTextView[i].getTextPaint().measureText(text, 0, text.length()) / 2f);
                    titleTextView[i].setPivotY((AndroidUtilities.dp(24) >> 1));
                    titleTextView[i].measure(MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(24), MeasureSpec.AT_MOST));
                }
                if (subtitleTextView != null && subtitleTextView.getVisibility() != GONE) {
                    subtitleTextView.measure(MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(20), MeasureSpec.AT_MOST));
                }
            }
        }

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE || child == titleTextView[0] || child == titleTextView[1] || child == subtitleTextView || child == menu || child == backButtonImageView) {
                continue;
            }
            measureChildWithMargins(child, widthMeasureSpec, 0, MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY), 0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int additionalTop = occupyStatusBar ? AndroidUtilities.statusBarHeight : 0;

        int textLeft;
        if (backButtonImageView != null && backButtonImageView.getVisibility() != GONE) {
            backButtonImageView.layout(0, additionalTop, backButtonImageView.getMeasuredWidth(), additionalTop + backButtonImageView.getMeasuredHeight());
            textLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 80 : 72);
        } else {
            textLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 26 : 18);
        }

        if (menu != null && menu.getVisibility() != GONE) {
            int menuLeft = isSearchFieldVisible ? AndroidUtilities.dp(AndroidUtilities.isTablet() ? 74 : 66) : (right - left) - menu.getMeasuredWidth();
            menu.layout(menuLeft, additionalTop, menuLeft + menu.getMeasuredWidth(), additionalTop + menu.getMeasuredHeight());
        }

        for (int i = 0; i < 2; i++) {
            if (titleTextView[i] != null && titleTextView[i].getVisibility() != GONE) {
                int textTop;
                if (((fromBottom && i == 0) || (!fromBottom && i == 1)) && overlayTitleAnimation && titleAnimationRunning) {
                    textTop = (getCurrentActionBarHeight() - titleTextView[i].getTextHeight()) / 2;
                } else {
                    if ((subtitleTextView != null && subtitleTextView.getVisibility() != GONE)) {
                        textTop = (getCurrentActionBarHeight() / 2 - titleTextView[i].getTextHeight()) / 2 + AndroidUtilities.dp(!AndroidUtilities.isTablet() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 2 : 3);
                    } else {
                        textTop = (getCurrentActionBarHeight() - titleTextView[i].getTextHeight()) / 2;
                    }
                }
                titleTextView[i].layout(textLeft, additionalTop + textTop, textLeft + titleTextView[i].getMeasuredWidth(), additionalTop + textTop + titleTextView[i].getTextHeight());
            }
        }
        if (subtitleTextView != null && subtitleTextView.getVisibility() != GONE) {
            int textTop = getCurrentActionBarHeight() / 2 + (getCurrentActionBarHeight() / 2 - subtitleTextView.getTextHeight()) / 2 - AndroidUtilities.dp(!AndroidUtilities.isTablet() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 1 : 1);
            subtitleTextView.layout(textLeft, additionalTop + textTop, textLeft + subtitleTextView.getMeasuredWidth(), additionalTop + textTop + subtitleTextView.getTextHeight());
        }

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE || child == titleTextView[0] || child == titleTextView[1] || child == subtitleTextView || child == menu || child == backButtonImageView) {
                continue;
            }

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            int childLeft;
            int childTop;

            int gravity = lp.gravity;
            if (gravity == -1) {
                gravity = Gravity.TOP | Gravity.LEFT;
            }

            final int absoluteGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

            switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.CENTER_HORIZONTAL:
                    childLeft = (right - left - width) / 2 + lp.leftMargin - lp.rightMargin;
                    break;
                case Gravity.RIGHT:
                    childLeft = right - width - lp.rightMargin;
                    break;
                case Gravity.LEFT:
                default:
                    childLeft = lp.leftMargin;
            }

            switch (verticalGravity) {
                case Gravity.CENTER_VERTICAL:
                    childTop = (bottom - top - height) / 2 + lp.topMargin - lp.bottomMargin;
                    break;
                case Gravity.BOTTOM:
                    childTop = (bottom - top) - height - lp.bottomMargin;
                    break;
                default:
                    childTop = lp.topMargin;
            }
            child.layout(childLeft, childTop, childLeft + width, childTop + height);
        }
    }

    public void onMenuButtonPressed() {
        if (isActionModeShowed()) {
            return;
        }
        if (menu != null) {
            menu.onMenuButtonPressed();
        }
    }

    protected void onPause() {
        if (menu != null) {
            menu.hideAllPopupMenus();
        }
    }

    public void setAllowOverlayTitle(boolean value) {
        allowOverlayTitle = value;
    }

    public void setTitleActionRunnable(Runnable action) {
        lastRunnable = titleActionRunnable = action;
    }

    boolean overlayTitleAnimationInProgress;

    public void setTitleOverlayText(String title, int titleId, Runnable action) {
        if (!allowOverlayTitle || parentFragment.parentLayout == null) {
            return;
        }
        overlayTitleToSet[0] = title;
        overlayTitleToSet[1] = titleId;
        overlayTitleToSet[2] = action;
        if (overlayTitleAnimationInProgress) {
            return;
        }
        if (lastOverlayTitle == null && title == null || (lastOverlayTitle != null && lastOverlayTitle.equals(title))) {
            return;
        }
        lastOverlayTitle = title;

        CharSequence textToSet = title != null ? LocaleController.getString(title, titleId) : lastTitle;
        boolean ellipsize = false;
        if (title != null) {

            int index = TextUtils.indexOf(textToSet, "...");
            if (index >= 0) {
                SpannableString spannableString = SpannableString.valueOf(textToSet);
                ellipsizeSpanAnimator.wrap(spannableString, index);
                textToSet = spannableString;
                ellipsize = true;
            }
        }
        titleOverlayShown = title != null;
        if ((textToSet != null && titleTextView[0] == null) || getMeasuredWidth() == 0 || (titleTextView[0] != null && titleTextView[0].getVisibility() != View.VISIBLE)) {
            createTitleTextView(0);
            if (supportsHolidayImage) {
                titleTextView[0].invalidate();
                invalidate();
            }
            titleTextView[0].setText(textToSet);
            if (ellipsize) {
                ellipsizeSpanAnimator.addView(titleTextView[0]);
            } else {
                ellipsizeSpanAnimator.removeView(titleTextView[0]);
            }
        } else if (titleTextView[0] != null) {
            titleTextView[0].animate().cancel();
            if (titleTextView[1] != null) {
                titleTextView[1].animate().cancel();
            }
            if (titleTextView[1] == null) {
                createTitleTextView(1);
            }
            titleTextView[1].setText(textToSet);
            if (ellipsize) {
                ellipsizeSpanAnimator.addView(titleTextView[1]);
            }
            overlayTitleAnimationInProgress = true;
            SimpleTextView tmp = titleTextView[1];
            titleTextView[1] = titleTextView[0];
            titleTextView[0] = tmp;
            titleTextView[0].setAlpha(0);
            titleTextView[0].setTranslationY(-AndroidUtilities.dp(20));
            titleTextView[0].animate()
                    .alpha(1f)
                    .translationY(0)
                    .setDuration(220).start();
            ViewPropertyAnimator animator = titleTextView[1].animate()
                    .alpha(0);
            if (subtitleTextView == null) {
                animator.translationY(AndroidUtilities.dp(20));
            } else {
                animator.scaleY(0.7f).scaleX(0.7f);
            }
            animator.setDuration(220).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (titleTextView[1] != null && titleTextView[1].getParent() != null) {
                        ViewGroup viewGroup = (ViewGroup) titleTextView[1].getParent();
                        viewGroup.removeView(titleTextView[1]);
                    }
                    ellipsizeSpanAnimator.removeView(titleTextView[1]);
                    titleTextView[1] = null;
                    overlayTitleAnimationInProgress = false;
                    setTitleOverlayText((String) overlayTitleToSet[0], (int) overlayTitleToSet[1], (Runnable) overlayTitleToSet[2]);
                }
            }).start();
        }
        titleActionRunnable = action != null ? action : lastRunnable;
    }

    public boolean isSearchFieldVisible() {
        return isSearchFieldVisible;
    }

    public void setOccupyStatusBar(boolean value) {
        occupyStatusBar = value;
        if (actionMode != null) {
            actionMode.setPadding(0, occupyStatusBar ? AndroidUtilities.statusBarHeight : 0, 0, 0);
        }
    }

    public boolean getOccupyStatusBar() {
        return occupyStatusBar;
    }

    public void setItemsBackgroundColor(int color, boolean isActionMode) {
        if (isActionMode) {
            itemsActionModeBackgroundColor = color;
            if (actionModeVisible) {
                if (backButtonImageView != null) {
                    backButtonImageView.setBackgroundDrawable(Theme.createSelectorDrawable(itemsActionModeBackgroundColor));
                }
            }
            if (actionMode != null) {
                actionMode.updateItemsBackgroundColor();
            }
        } else {
            itemsBackgroundColor = color;
            if (backButtonImageView != null) {
                backButtonImageView.setBackgroundDrawable(Theme.createSelectorDrawable(itemsBackgroundColor));
            }
            if (menu != null) {
                menu.updateItemsBackgroundColor();
            }
        }
    }

    public void setItemsColor(int color, boolean isActionMode) {
        if (isActionMode) {
            itemsActionModeColor = color;
            if (actionMode != null) {
                actionMode.updateItemsColor();
            }
            if (backButtonImageView != null) {
                Drawable drawable = backButtonImageView.getDrawable();
                if (drawable instanceof BackDrawable) {
                    ((BackDrawable) drawable).setRotatedColor(color);
                }
            }
        } else {
            itemsColor = color;
            if (backButtonImageView != null) {
                if (itemsColor != 0) {
                    backButtonImageView.setColorFilter(new PorterDuffColorFilter(itemsColor, PorterDuff.Mode.MULTIPLY));
                    Drawable drawable = backButtonImageView.getDrawable();
                    if (drawable instanceof BackDrawable) {
                        ((BackDrawable) drawable).setColor(color);
                    } else if (drawable instanceof  MenuDrawable) {
                        ((MenuDrawable) drawable).setIconColor(color);
                    }
                }
            }
            if (menu != null) {
                menu.updateItemsColor();
            }
        }
    }

    public void setCastShadows(boolean value) {
        if (CatogramConfig.INSTANCE.getFlatActionbar()) return;
        castShadows = value;
    }

    public boolean getCastShadows() {
        return !CatogramConfig.INSTANCE.getFlatActionbar();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event) || interceptTouches;
    }

    public static int getCurrentActionBarHeight() {
        if (AndroidUtilities.isTablet()) {
            return AndroidUtilities.dp(64);
        } else if (AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y) {
            return AndroidUtilities.dp(48);
        } else {
            return AndroidUtilities.dp(56);
        }
    }

    public void setTitleAnimated(CharSequence title, boolean fromBottom, long duration) {
        if (titleTextView[0] == null || title == null) {
            setTitle(title);
            return;
        }
        boolean crossfade =  overlayTitleAnimation && !TextUtils.isEmpty(subtitleTextView.getText());
        if (crossfade) {
            if (subtitleTextView.getVisibility() != View.VISIBLE) {
                subtitleTextView.setVisibility(View.VISIBLE);
                subtitleTextView.setAlpha(0);
            }
            subtitleTextView.animate().alpha(fromBottom ? 0 : 1f).setDuration(220).start();
        }
        if (titleTextView[1] != null) {
            if (titleTextView[1].getParent() != null) {
                ViewGroup viewGroup = (ViewGroup) titleTextView[1].getParent();
                viewGroup.removeView(titleTextView[1]);
            }
            titleTextView[1] = null;
        }
        titleTextView[1] = titleTextView[0];
        titleTextView[0] = null;
        setTitle(title);
        this.fromBottom = fromBottom;
        titleTextView[0].setAlpha(0);
        if (!crossfade) {
            titleTextView[0].setTranslationY(fromBottom ? AndroidUtilities.dp(20) : -AndroidUtilities.dp(20));
        }
        titleTextView[0].animate().alpha(1f).translationY(0).setDuration(duration).start();

        titleAnimationRunning = true;
        ViewPropertyAnimator a = titleTextView[1].animate().alpha(0);
        if (!crossfade) {
            a.translationY(fromBottom ? -AndroidUtilities.dp(20) : AndroidUtilities.dp(20));
        }
        a.setDuration(duration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (titleTextView[1] != null && titleTextView[1].getParent() != null) {
                    ViewGroup viewGroup = (ViewGroup) titleTextView[1].getParent();
                    viewGroup.removeView(titleTextView[1]);
                }
                titleTextView[1] = null;
                titleAnimationRunning = false;

                if (crossfade && fromBottom) {
                    subtitleTextView.setVisibility(View.GONE);
                }

                requestLayout();
            }
        }).start();
        requestLayout();
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ellipsizeSpanAnimator.onAttachedToWindow();
        if (SharedConfig.noStatusBar && actionModeVisible) {
            if (AndroidUtilities.computePerceivedBrightness(actionModeColor) < 0.721f) {
                AndroidUtilities.setLightStatusBar(((Activity) getContext()).getWindow(), false);
            } else {
                AndroidUtilities.setLightStatusBar(((Activity) getContext()).getWindow(), true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ellipsizeSpanAnimator.onDetachedFromWindow();
        if (SharedConfig.noStatusBar && actionModeVisible) {
            if (AndroidUtilities.computePerceivedBrightness(actionBarColor) < 0.721f) {
                AndroidUtilities.setLightStatusBar(((Activity) getContext()).getWindow(), false);
            } else {
                AndroidUtilities.setLightStatusBar(((Activity) getContext()).getWindow(), true);
            }
        }
    }

    public ActionBarMenu getActionMode() {
        return actionMode;
    }

    public void setOverlayTitleAnimation(boolean ovelayTitleAnimation) {
        this.overlayTitleAnimation = ovelayTitleAnimation;
    }
}