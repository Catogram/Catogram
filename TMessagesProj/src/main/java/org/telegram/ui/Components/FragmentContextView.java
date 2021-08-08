/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Keep;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LocationController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.voip.VoIPService;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.GroupCallActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.LocationActivity;

import java.util.ArrayList;

public class FragmentContextView extends FrameLayout implements NotificationCenter.NotificationCenterDelegate, VoIPService.StateListener {

    private ImageView playButton;
    private PlayPauseDrawable playPauseDrawable;
    private AudioPlayerAlert.ClippingTextViewSwitcher titleTextView;
    private AudioPlayerAlert.ClippingTextViewSwitcher subtitleTextView;
    private AnimatorSet animatorSet;
    private BaseFragment fragment;
    private View applyingView;
    private FrameLayout frameLayout;
    private View shadow;
    private View selector;
    private RLottieImageView importingImageView;
    private RLottieImageView muteButton;
    private RLottieDrawable muteDrawable;
    private ImageView closeButton;
    private ActionBarMenuItem playbackSpeedButton;
    private ActionBarMenuSubItem[] speedItems = new ActionBarMenuSubItem[4];
    private FragmentContextView additionalContextView;
    private TextView joinButton;

    private boolean isMuted;

    private int currentProgress = -1;

    private MessageObject lastMessageObject;
    private float topPadding;
    private boolean visible;
    private int currentStyle = -1;
    private String lastString;
    private boolean isMusic;
    private boolean supportsCalls = true;
    private AvatarsImageView avatars;

    private Paint gradientPaint;
    private LinearGradient linearGradient;
    private Matrix matrix;
    private int gradientWidth;
    private TextPaint gradientTextPaint;
    private StaticLayout timeLayout;
    private RectF rect = new RectF();
    private boolean scheduleRunnableScheduled;
    private Runnable updateScheduleTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (gradientTextPaint == null || !(fragment instanceof ChatActivity)) {
                scheduleRunnableScheduled = false;
                return;
            }
            ChatObject.Call call = ((ChatActivity) fragment).getGroupCall();
            if (call == null || !call.isScheduled()) {
                timeLayout = null;
                scheduleRunnableScheduled = false;
                return;
            }
            int currentTime = fragment.getConnectionsManager().getCurrentTime();
            int diff = call.call.schedule_date - currentTime;
            String str;
            if (diff >= 24 * 60 * 60) {
                str = LocaleController.formatPluralString("Days", Math.round(diff / (24 * 60 * 60.0f)));
            } else {
                str = AndroidUtilities.formatFullDuration(call.call.schedule_date - currentTime);
            }
            int width = (int) Math.ceil(gradientTextPaint.measureText(str));
            timeLayout = new StaticLayout(str, gradientTextPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            AndroidUtilities.runOnUIThread(updateScheduleTimeRunnable, 1000);
            frameLayout.invalidate();
        }
    };

    private final int account = UserConfig.selectedAccount;

    private boolean isLocation;

    private FragmentContextViewDelegate delegate;

    private boolean firstLocationsLoaded;
    private int lastLocationSharingCount = -1;
    private Runnable checkLocationRunnable = new Runnable() {
        @Override
        public void run() {
            checkLocationString();
            AndroidUtilities.runOnUIThread(checkLocationRunnable, 1000);
        }
    };
    private int animationIndex = -1;

    private boolean checkCallAfterAnimation;
    private boolean checkPlayerAfterAnimation;
    private boolean checkImportAfterAnimation;

    private final static int menu_speed_slow = 1;
    private final static int menu_speed_normal = 2;
    private final static int menu_speed_fast = 3;
    private final static int menu_speed_veryfast = 4;

    @Override
    public void onAudioSettingsChanged() {
        boolean newMuted = VoIPService.getSharedInstance() != null && VoIPService.getSharedInstance().isMicMute();
        if (isMuted != newMuted) {
            isMuted = newMuted;
            muteDrawable.setCustomEndFrame(isMuted ? 15 : 29);
            muteDrawable.setCurrentFrame(muteDrawable.getCustomEndFrame() - 1, false, true);
            muteButton.invalidate();
            Theme.getFragmentContextViewWavesDrawable().updateState(visible);
        }
        if (isMuted) {
            micAmplitude = 0;
            Theme.getFragmentContextViewWavesDrawable().setAmplitude(0);
        }
    }

    public boolean drawOverlayed() {
        return currentStyle == 3;
    }

    public interface FragmentContextViewDelegate {
        void onAnimation(boolean start, boolean show);
    }

    public FragmentContextView(Context context, BaseFragment parentFragment, boolean location) {
        this(context, parentFragment, null, location);
    }

    public FragmentContextView(Context context, BaseFragment parentFragment, View paddingView, boolean location) {
        super(context);

        fragment = parentFragment;
        applyingView = paddingView;
        visible = true;
        isLocation = location;
        if (applyingView == null) {
            ((ViewGroup) fragment.getFragmentView()).setClipToPadding(false);
        }

        setTag(1);
        frameLayout = new FrameLayout(context) {
            @Override
            public void invalidate() {
                super.invalidate();
                if (avatars != null && avatars.getVisibility() == VISIBLE) {
                    avatars.invalidate();
                }
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (currentStyle == 4 && timeLayout != null) {
                    int width = (int) Math.ceil(timeLayout.getLineWidth(0)) + AndroidUtilities.dp(24);
                    if (width != gradientWidth) {
                        linearGradient = new LinearGradient(0, 0, width * 1.7f, 0, new int[]{0xff648CF4, 0xff8C69CF, 0xffD45979, 0xffD45979}, new float[]{0.0f, 0.294f, 0.588f, 1.0f}, Shader.TileMode.CLAMP);
                        gradientPaint.setShader(linearGradient);
                        gradientWidth = width;
                    }
                    ChatObject.Call call = ((ChatActivity) fragment).getGroupCall();
                    float moveProgress = 0.0f;
                    if (fragment != null && call != null && call.isScheduled()) {
                        long diff = ((long) call.call.schedule_date) * 1000 - fragment.getConnectionsManager().getCurrentTimeMillis();
                        if (diff < 0) {
                            moveProgress = 1.0f;
                        } else if (diff < 5000) {
                            moveProgress = 1.0f - diff / 5000.0f;
                        }
                        if (diff < 6000) {
                            invalidate();
                        }
                    }
                    matrix.reset();
                    matrix.postTranslate(-gradientWidth * 0.7f * moveProgress, 0);
                    linearGradient.setLocalMatrix(matrix);
                    int x = getMeasuredWidth() - width - AndroidUtilities.dp(10);
                    int y = AndroidUtilities.dp(12);
                    rect.set(0, 0, width, AndroidUtilities.dp(24));
                    canvas.save();
                    canvas.translate(x, y);
                    canvas.drawRoundRect(rect, AndroidUtilities.dp(12), AndroidUtilities.dp(12), gradientPaint);
                    canvas.translate(AndroidUtilities.dp(12), AndroidUtilities.dp(4));
                    timeLayout.draw(canvas);
                    canvas.restore();
                }
            }
        };
        addView(frameLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 36, Gravity.TOP | Gravity.LEFT, 0, 0, 0, 0));

        selector = new View(context);
        frameLayout.addView(selector, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        shadow = new View(context);
        shadow.setBackgroundResource(R.drawable.blockpanel_shadow);
        addView(shadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 2, Gravity.LEFT | Gravity.TOP, 0, 36, 0, 0));

        playButton = new ImageView(context);
        playButton.setScaleType(ImageView.ScaleType.CENTER);
        playButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_inappPlayerPlayPause), PorterDuff.Mode.MULTIPLY));
        playButton.setImageDrawable(playPauseDrawable = new PlayPauseDrawable(14));
        if (Build.VERSION.SDK_INT >= 21) {
            playButton.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(Theme.key_inappPlayerPlayPause) & 0x19ffffff, 1, AndroidUtilities.dp(14)));
        }
        addView(playButton, LayoutHelper.createFrame(36, 36, Gravity.TOP | Gravity.LEFT));
        playButton.setOnClickListener(v -> {
            if (currentStyle == 0) {
                if (MediaController.getInstance().isMessagePaused()) {
                    MediaController.getInstance().playMessage(MediaController.getInstance().getPlayingMessageObject());
                } else {
                    MediaController.getInstance().pauseMessage(MediaController.getInstance().getPlayingMessageObject());
                }
            }
        });

        importingImageView = new RLottieImageView(context);
        importingImageView.setScaleType(ImageView.ScaleType.CENTER);
        importingImageView.setAutoRepeat(true);
        importingImageView.setAnimation(R.raw.import_progress, 30, 30);
        importingImageView.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(22), Theme.getColor(Theme.key_inappPlayerPlayPause)));
        addView(importingImageView, LayoutHelper.createFrame(22, 22, Gravity.TOP | Gravity.LEFT, 7, 7, 0, 0));

        titleTextView = new AudioPlayerAlert.ClippingTextViewSwitcher(context) {
            @Override
            protected TextView createTextView() {
                TextView textView = new TextView(context);
                textView.setMaxLines(1);
                textView.setLines(1);
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

                if (currentStyle == 0 || currentStyle == 2) {
                    textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                    textView.setTextColor(Theme.getColor(Theme.key_inappPlayerTitle));
                    textView.setTypeface(Typeface.DEFAULT);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                } else if (currentStyle == 4) {
                    textView.setGravity(Gravity.TOP | Gravity.LEFT);
                    textView.setTextColor(Theme.getColor(Theme.key_inappPlayerPerformer));
                    textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                } else if (currentStyle == 1 || currentStyle == 3) {
                    textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                    textView.setTextColor(Theme.getColor(Theme.key_returnToCallText));
                    textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                }
                return textView;
            }
        };
        addView(titleTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 36, Gravity.LEFT | Gravity.TOP, 35, 0, 36, 0));

        subtitleTextView = new AudioPlayerAlert.ClippingTextViewSwitcher(context) {
            @Override
            protected TextView createTextView() {
                TextView textView = new TextView(context);
                textView.setMaxLines(1);
                textView.setLines(1);
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setGravity(Gravity.LEFT);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
                textView.setTextColor(Theme.getColor(Theme.key_inappPlayerClose));
                return textView;
            }
        };
        addView(subtitleTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 36, Gravity.LEFT | Gravity.TOP, 35, 10, 36, 0));

        joinButton = new TextView(context);
        joinButton.setText(LocaleController.getString("VoipChatJoin", R.string.VoipChatJoin));
        joinButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        joinButton.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        joinButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        joinButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        joinButton.setGravity(Gravity.CENTER);
        joinButton.setPadding(AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14), 0);
        addView(joinButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 28, Gravity.TOP | Gravity.RIGHT, 0, 10, 14, 0));
        joinButton.setOnClickListener(v -> FragmentContextView.this.callOnClick());

        if (!location) {
            playbackSpeedButton = new ActionBarMenuItem(context, null, 0, Theme.getColor(Theme.key_dialogTextBlack));
            playbackSpeedButton.setLongClickEnabled(false);
            playbackSpeedButton.setShowSubmenuByMove(false);
            playbackSpeedButton.setContentDescription(LocaleController.getString("AccDescrPlayerSpeed", R.string.AccDescrPlayerSpeed));
            playbackSpeedButton.setDelegate(id -> {
                float oldSpeed = MediaController.getInstance().getPlaybackSpeed(isMusic);
                if (id == menu_speed_slow) {
                    MediaController.getInstance().setPlaybackSpeed(isMusic, 0.5f);
                } else if (id == menu_speed_normal) {
                    MediaController.getInstance().setPlaybackSpeed(isMusic, 1.0f);
                } else if (id == menu_speed_fast) {
                    MediaController.getInstance().setPlaybackSpeed(isMusic, 1.5f);
                } else {
                    MediaController.getInstance().setPlaybackSpeed(isMusic, 1.8f);
                }
                float newSpeed = MediaController.getInstance().getPlaybackSpeed(isMusic);
                if (oldSpeed != newSpeed) {
                    playbackSpeedChanged(newSpeed);
                }
                updatePlaybackButton();
            });
            speedItems[0] = playbackSpeedButton.addSubItem(menu_speed_slow, R.drawable.msg_speed_0_5, LocaleController.getString("SpeedSlow", R.string.SpeedSlow));
            speedItems[1] = playbackSpeedButton.addSubItem(menu_speed_normal, R.drawable.msg_speed_1, LocaleController.getString("SpeedNormal", R.string.SpeedNormal));
            speedItems[2] = playbackSpeedButton.addSubItem(menu_speed_fast, R.drawable.msg_speed_1_5, LocaleController.getString("SpeedFast", R.string.SpeedFast));
            speedItems[3] = playbackSpeedButton.addSubItem(menu_speed_veryfast, R.drawable.msg_speed_2, LocaleController.getString("SpeedVeryFast", R.string.SpeedVeryFast));
            if (AndroidUtilities.density >= 3.0f) {
                playbackSpeedButton.setPadding(0, 1, 0, 0);
            }
            playbackSpeedButton.setAdditionalXOffset(AndroidUtilities.dp(8));
            addView(playbackSpeedButton, LayoutHelper.createFrame(36, 36, Gravity.TOP | Gravity.RIGHT, 0, 0, 36, 0));
            playbackSpeedButton.setOnClickListener(v -> {
                float currentPlaybackSpeed = MediaController.getInstance().getPlaybackSpeed(isMusic);
                float newSpeed;
                if (Math.abs(currentPlaybackSpeed - 1.0f) > 0.001f) {
                    MediaController.getInstance().setPlaybackSpeed(isMusic, newSpeed = 1.0f);
                } else {
                    MediaController.getInstance().setPlaybackSpeed(isMusic, newSpeed = MediaController.getInstance().getFastPlaybackSpeed(isMusic));
                }
                playbackSpeedChanged(newSpeed);
            });
            playbackSpeedButton.setOnLongClickListener(view -> {
                playbackSpeedButton.toggleSubMenu();
                return true;
            });
            updatePlaybackButton();
        }

        avatars = new AvatarsImageView(context, false);
        avatars.setDelegate(() -> updateAvatars(true));
        avatars.setVisibility(GONE);
        addView(avatars, LayoutHelper.createFrame(108, 36, Gravity.LEFT | Gravity.TOP));

        muteDrawable = new RLottieDrawable(R.raw.voice_muted, "" + R.raw.voice_muted, AndroidUtilities.dp(16), AndroidUtilities.dp(20), true, null);

        muteButton = new RLottieImageView(context) {
            boolean scheduled;
            boolean pressed;

            private final Runnable toggleMicRunnable = () -> {
                if (VoIPService.getSharedInstance() == null) {
                    return;
                }
                VoIPService.getSharedInstance().setMicMute(false, true, false);
                if (muteDrawable.setCustomEndFrame(isMuted ? 15 : 29)) {
                    if (isMuted) {
                        muteDrawable.setCurrentFrame(0);
                    } else {
                        muteDrawable.setCurrentFrame(14);
                    }
                }
                muteButton.playAnimation();

                Theme.getFragmentContextViewWavesDrawable().updateState(true);
            };


            private final Runnable pressRunnable = () -> {
                if (!scheduled || VoIPService.getSharedInstance() == null) {
                    return;
                }
                scheduled = false;
                pressed = true;
                isMuted = false;

                AndroidUtilities.runOnUIThread(toggleMicRunnable, 90);
                muteButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            };


            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (currentStyle == 3 || currentStyle == 1) {
                    VoIPService service = VoIPService.getSharedInstance();
                    if (service == null) {
                        AndroidUtilities.cancelRunOnUIThread(pressRunnable);
                        AndroidUtilities.cancelRunOnUIThread(toggleMicRunnable);
                        scheduled = false;
                        pressed = false;
                        return true;
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN && service.isMicMute()) {
                        AndroidUtilities.runOnUIThread(pressRunnable, 300);
                        scheduled = true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        AndroidUtilities.cancelRunOnUIThread(toggleMicRunnable);
                        if (scheduled) {
                            AndroidUtilities.cancelRunOnUIThread(pressRunnable);
                            scheduled = false;
                        } else if (pressed) {
                            isMuted = true;
                            if (muteDrawable.setCustomEndFrame(15)) {
                                if (isMuted) {
                                    muteDrawable.setCurrentFrame(0);
                                } else {
                                    muteDrawable.setCurrentFrame(14);
                                }
                            }
                            muteButton.playAnimation();
                            if (VoIPService.getSharedInstance() != null) {
                                VoIPService.getSharedInstance().setMicMute(true, true, false);
                                muteButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                            }
                            pressed = false;
                            Theme.getFragmentContextViewWavesDrawable().updateState(true);
                            MotionEvent cancel = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
                            super.onTouchEvent(cancel);
                            cancel.recycle();
                            return true;
                        }
                    }
                    return super.onTouchEvent(event);
                } else {
                    return super.onTouchEvent(event);
                }
            }

            @Override
            public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(info);
                info.setClassName(Button.class.getName());
                info.setText(isMuted ? LocaleController.getString("VoipUnmute", R.string.VoipUnmute) : LocaleController.getString("VoipMute", R.string.VoipMute));
            }
        };
        muteButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_returnToCallText), PorterDuff.Mode.MULTIPLY));
        if (Build.VERSION.SDK_INT >= 21) {
            muteButton.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(Theme.key_inappPlayerClose) & 0x19ffffff, 1, AndroidUtilities.dp(14)));
        }
        muteButton.setAnimation(muteDrawable);
        muteButton.setScaleType(ImageView.ScaleType.CENTER);
        muteButton.setVisibility(GONE);
        addView(muteButton, LayoutHelper.createFrame(36, 36, Gravity.RIGHT | Gravity.TOP, 0, 0, 2, 0));
        muteButton.setOnClickListener(v -> {
            VoIPService voIPService = VoIPService.getSharedInstance();
            if (voIPService == null) {
                return;
            }
            if (voIPService.groupCall != null) {
                AccountInstance accountInstance = AccountInstance.getInstance(voIPService.getAccount());
                ChatObject.Call call = voIPService.groupCall;
                TLRPC.Chat chat = voIPService.getChat();
                TLRPC.TL_groupCallParticipant participant = call.participants.get(voIPService.getSelfId());
                if (participant != null && !participant.can_self_unmute && participant.muted && !ChatObject.canManageCalls(chat)) {
                    return;
                }
            }

            isMuted = !voIPService.isMicMute();
            voIPService.setMicMute(isMuted, false, true);
            if (muteDrawable.setCustomEndFrame(isMuted ? 15 : 29)) {
                if (isMuted) {
                    muteDrawable.setCurrentFrame(0);
                } else {
                    muteDrawable.setCurrentFrame(14);
                }
            }
            muteButton.playAnimation();
            Theme.getFragmentContextViewWavesDrawable().updateState(true);
            muteButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        });

        closeButton = new ImageView(context);
        closeButton.setImageResource(R.drawable.miniplayer_close);
        closeButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_inappPlayerClose), PorterDuff.Mode.MULTIPLY));
        if (Build.VERSION.SDK_INT >= 21) {
            closeButton.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(Theme.key_inappPlayerClose) & 0x19ffffff, 1, AndroidUtilities.dp(14)));
        }
        closeButton.setScaleType(ImageView.ScaleType.CENTER);
        addView(closeButton, LayoutHelper.createFrame(36, 36, Gravity.RIGHT | Gravity.TOP, 0, 0, 2, 0));
        closeButton.setOnClickListener(v -> {
            if (currentStyle == 2) {
                AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getParentActivity());
                builder.setTitle(LocaleController.getString("StopLiveLocationAlertToTitle", R.string.StopLiveLocationAlertToTitle));
                if (fragment instanceof DialogsActivity) {
                    builder.setMessage(LocaleController.getString("StopLiveLocationAlertAllText", R.string.StopLiveLocationAlertAllText));
                } else {
                    ChatActivity activity = (ChatActivity) fragment;
                    TLRPC.Chat chat = activity.getCurrentChat();
                    TLRPC.User user = activity.getCurrentUser();
                    if (chat != null) {
                        builder.setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("StopLiveLocationAlertToGroupText", R.string.StopLiveLocationAlertToGroupText, chat.title)));
                    } else if (user != null) {
                        builder.setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("StopLiveLocationAlertToUserText", R.string.StopLiveLocationAlertToUserText, UserObject.getFirstName(user))));
                    } else {
                        builder.setMessage(LocaleController.getString("AreYouSure", R.string.AreYouSure));
                    }
                }
                builder.setPositiveButton(LocaleController.getString("Stop", R.string.Stop), (dialogInterface, i) -> {
                    if (fragment instanceof DialogsActivity) {
                        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                            LocationController.getInstance(a).removeAllLocationSharings();
                        }
                    } else {
                        LocationController.getInstance(fragment.getCurrentAccount()).removeSharingLocation(((ChatActivity) fragment).getDialogId());
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                AlertDialog alertDialog = builder.create();
                builder.show();
                TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (button != null) {
                    button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
                }
            } else {
                MediaController.getInstance().cleanupPlayer(true, true);
            }
        });

        setOnClickListener(v -> {
            if (currentStyle == 0) {
                MessageObject messageObject = MediaController.getInstance().getPlayingMessageObject();
                if (fragment != null && messageObject != null) {
                    if (messageObject.isMusic() || messageObject.isVoice()) {
                        fragment.showDialog(new AudioPlayerAlert(getContext()));
                    } else {
                        long dialog_id = 0;
                        if (fragment instanceof ChatActivity) {
                            dialog_id = ((ChatActivity) fragment).getDialogId();
                        }
                        if (messageObject.getDialogId() == dialog_id) {
                            ((ChatActivity) fragment).scrollToMessageId(messageObject.getId(), 0, false, 0, true, 0);
                        } else {
                            dialog_id = messageObject.getDialogId();
                            Bundle args = new Bundle();
                            int lower_part = (int) dialog_id;
                            int high_id = (int) (dialog_id >> 32);
                            if (lower_part != 0) {
                                if (lower_part > 0) {
                                    args.putInt("user_id", lower_part);
                                } else {
                                    args.putInt("chat_id", -lower_part);
                                }
                            } else {
                                args.putInt("enc_id", high_id);
                            }
                            args.putInt("message_id", messageObject.getId());
                            fragment.presentFragment(new ChatActivity(args), fragment instanceof ChatActivity);
                        }
                    }
                }
            } else if (currentStyle == 1) {
                Intent intent = new Intent(getContext(), LaunchActivity.class).setAction("voip");
                getContext().startActivity(intent);
            } else if (currentStyle == 2) {
                long did = 0;
                int account = UserConfig.selectedAccount;
                if (fragment instanceof ChatActivity) {
                    did = ((ChatActivity) fragment).getDialogId();
                    account = fragment.getCurrentAccount();
                } else if (LocationController.getLocationsCount() == 1) {
                    for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                        ArrayList<LocationController.SharingLocationInfo> arrayList = LocationController.getInstance(a).sharingLocationsUI;
                        if (!arrayList.isEmpty()) {
                            LocationController.SharingLocationInfo info = LocationController.getInstance(a).sharingLocationsUI.get(0);
                            did = info.did;
                            account = info.messageObject.currentAccount;
                            break;
                        }
                    }
                }
                if (did != 0) {
                    openSharingLocation(LocationController.getInstance(account).getSharingLocationInfo(did));
                } else {
                    fragment.showDialog(new SharingLocationsAlert(getContext(), this::openSharingLocation));
                }
            } else if (currentStyle == 3) {
                if (VoIPService.getSharedInstance() != null && getContext() instanceof LaunchActivity) {
                    GroupCallActivity.create((LaunchActivity) getContext(), AccountInstance.getInstance(VoIPService.getSharedInstance().getAccount()), null, null, false, null);
                }
            } else if (currentStyle == 4) {
                if (fragment.getParentActivity() == null) {
                    return;
                }
                ChatActivity chatActivity = (ChatActivity) fragment;
                ChatObject.Call call = chatActivity.getGroupCall();
                if (call == null) {
                    return;
                }
                VoIPHelper.startCall(chatActivity.getMessagesController().getChat(call.chatId), null, null, false, fragment.getParentActivity(), fragment, fragment.getAccountInstance());
            } else if (currentStyle == 5) {
                SendMessagesHelper.ImportingHistory importingHistory = parentFragment.getSendMessagesHelper().getImportingHistory(((ChatActivity) parentFragment).getDialogId());
                if (importingHistory == null) {
                    return;
                }
                ImportingAlert importingAlert = new ImportingAlert(getContext(), null, (ChatActivity) fragment);
                importingAlert.setOnHideListener(dialog -> checkImport(false));
                fragment.showDialog(importingAlert);
                checkImport(false);
            }
        });
    }

    public void setSupportsCalls(boolean value) {
        supportsCalls = value;
    }

    public void setDelegate(FragmentContextViewDelegate fragmentContextViewDelegate) {
        delegate = fragmentContextViewDelegate;
    }

    private void updatePlaybackButton() {
        if (playbackSpeedButton == null) {
            return;
        }
        float currentPlaybackSpeed = MediaController.getInstance().getPlaybackSpeed(isMusic);
        String key;
        if (Math.abs(currentPlaybackSpeed - 1.0f) > 0.001f) {
            key = Theme.key_inappPlayerPlayPause;
        } else {
            key = Theme.key_inappPlayerClose;
        }
        float speed = MediaController.getInstance().getFastPlaybackSpeed(isMusic);
        if (Math.abs(speed - 1.8f) < 0.001f) {
            playbackSpeedButton.setIcon(R.drawable.voice_mini_2_0);
        } else if (Math.abs(speed - 1.5f) < 0.001f) {
            playbackSpeedButton.setIcon(R.drawable.voice_mini_1_5);
        } else {
            playbackSpeedButton.setIcon(R.drawable.voice_mini_0_5);
        }
        playbackSpeedButton.setIconColor(Theme.getColor(key));
        if (Build.VERSION.SDK_INT >= 21) {
            playbackSpeedButton.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(key) & 0x19ffffff, 1, AndroidUtilities.dp(14)));
        }
        for (int a = 0; a < speedItems.length; a++) {
            if (a == 0 && Math.abs(currentPlaybackSpeed - 0.5f) < 0.001f ||
                    a == 1 && Math.abs(currentPlaybackSpeed - 1.0f) < 0.001f ||
                    a == 2 && Math.abs(currentPlaybackSpeed - 1.5f) < 0.001f ||
                    a == 3 && Math.abs(currentPlaybackSpeed - 1.8f) < 0.001f) {
                speedItems[a].setColors(Theme.getColor(Theme.key_inappPlayerPlayPause), Theme.getColor(Theme.key_inappPlayerPlayPause));
            } else {
                speedItems[a].setColors(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem), Theme.getColor(Theme.key_actionBarDefaultSubmenuItemIcon));
            }
        }
    }

    public void setAdditionalContextView(FragmentContextView contextView) {
        additionalContextView = contextView;
    }

    private void openSharingLocation(final LocationController.SharingLocationInfo info) {
        if (info == null || !(fragment.getParentActivity() instanceof LaunchActivity)) {
            return;
        }
        LaunchActivity launchActivity = ((LaunchActivity) fragment.getParentActivity());
        launchActivity.switchToAccount(info.messageObject.currentAccount, true);

        LocationActivity locationActivity = new LocationActivity(2);
        locationActivity.setMessageObject(info.messageObject);
        final long dialog_id = info.messageObject.getDialogId();
        locationActivity.setDelegate((location, live, notify, scheduleDate) -> SendMessagesHelper.getInstance(info.messageObject.currentAccount).sendMessage(location, dialog_id, null, null, null, null, notify, scheduleDate));
        launchActivity.presentFragment(locationActivity);
    }

    @Keep
    public float getTopPadding() {
        return topPadding;
    }

    private void checkVisibility() {
        boolean show = false;
        if (isLocation) {
            if (fragment instanceof DialogsActivity) {
                show = LocationController.getLocationsCount() != 0;
            } else {
                show = LocationController.getInstance(fragment.getCurrentAccount()).isSharingLocation(((ChatActivity) fragment).getDialogId());
            }
        } else {
            if (VoIPService.getSharedInstance() != null && !VoIPService.getSharedInstance().isHangingUp() && VoIPService.getSharedInstance().getCallState() != VoIPService.STATE_WAITING_INCOMING) {
                show = true;
            } else if (fragment instanceof ChatActivity && fragment.getSendMessagesHelper().getImportingHistory(((ChatActivity) fragment).getDialogId()) != null && !isPlayingVoice()) {
                show = true;
            } else if (fragment instanceof ChatActivity && ((ChatActivity) fragment).getGroupCall() != null && ((ChatActivity) fragment).getGroupCall().shouldShowPanel() && !GroupCallPip.isShowing() && !isPlayingVoice()) {
                show = true;
            } else {
                MessageObject messageObject = MediaController.getInstance().getPlayingMessageObject();
                if (messageObject != null && messageObject.getId() != 0) {
                    show = true;
                }
            }
        }
        setVisibility(show ? VISIBLE : GONE);
    }

    @Keep
    public void setTopPadding(float value) {
        topPadding = value;
        if (fragment != null && getParent() != null) {
            View view = applyingView != null ? applyingView : fragment.getFragmentView();
            int additionalPadding = 0;
            if (additionalContextView != null && additionalContextView.getVisibility() == VISIBLE && additionalContextView.getParent() != null) {
                additionalPadding = AndroidUtilities.dp(additionalContextView.getStyleHeight());
            }
            if (view != null && getParent() != null) {
                view.setPadding(0, (int) (getVisibility() == View.VISIBLE ? topPadding : 0) + additionalPadding, 0, 0);
            }
        }
    }

    protected void playbackSpeedChanged(float value) {

    }

    private void updateStyle(int style) {
        if (currentStyle == style) {
            return;
        }
        if (currentStyle == 3 || currentStyle == 1) {
            Theme.getFragmentContextViewWavesDrawable().removeParent(this);
            if (VoIPService.getSharedInstance() != null) {
                VoIPService.getSharedInstance().unregisterStateListener(this);
            }
        }
        currentStyle = style;
        frameLayout.setWillNotDraw(currentStyle != 4);
        if (style != 4) {
            timeLayout = null;
        }

        if (avatars != null) {
            avatars.setStyle(currentStyle);
            avatars.setLayoutParams(LayoutHelper.createFrame(108, getStyleHeight(), Gravity.LEFT | Gravity.TOP));
        }
        frameLayout.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, getStyleHeight(), Gravity.TOP | Gravity.LEFT, 0, 0, 0, 0));
        shadow.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 2, Gravity.LEFT | Gravity.TOP, 0, getStyleHeight(), 0, 0));

        if (topPadding > 0 && topPadding != AndroidUtilities.dp2(getStyleHeight())) {
            updatePaddings();
            setTopPadding(AndroidUtilities.dp2(getStyleHeight()));
        }
        if (style == 5) {
            selector.setBackground(Theme.getSelectorDrawable(false));
            frameLayout.setBackgroundColor(Theme.getColor(Theme.key_inappPlayerBackground));
            frameLayout.setTag(Theme.key_inappPlayerBackground);

            for (int i = 0; i < 2; i++) {
                TextView textView = i == 0 ? titleTextView.getTextView() : titleTextView.getNextTextView();
                if (textView == null) {
                    continue;
                }
                textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                textView.setTextColor(Theme.getColor(Theme.key_inappPlayerTitle));
                textView.setTypeface(Typeface.DEFAULT);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            }
            titleTextView.setTag(Theme.key_inappPlayerTitle);
            subtitleTextView.setVisibility(GONE);
            joinButton.setVisibility(GONE);
            closeButton.setVisibility(GONE);
            playButton.setVisibility(GONE);
            muteButton.setVisibility(GONE);
            avatars.setVisibility(GONE);
            importingImageView.setVisibility(VISIBLE);
            importingImageView.playAnimation();
            closeButton.setContentDescription(LocaleController.getString("AccDescrClosePlayer", R.string.AccDescrClosePlayer));
            if (playbackSpeedButton != null) {
                playbackSpeedButton.setVisibility(GONE);
            }
            titleTextView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 36, Gravity.LEFT | Gravity.TOP, 35, 0, 36, 0));
        } else if (style == 0 || style == 2) {
            selector.setBackground(Theme.getSelectorDrawable(false));
            frameLayout.setBackgroundColor(Theme.getColor(Theme.key_inappPlayerBackground));
            frameLayout.setTag(Theme.key_inappPlayerBackground);

            subtitleTextView.setVisibility(GONE);
            joinButton.setVisibility(GONE);
            closeButton.setVisibility(VISIBLE);
            playButton.setVisibility(VISIBLE);
            muteButton.setVisibility(GONE);
            importingImageView.setVisibility(GONE);
            importingImageView.stopAnimation();
            avatars.setVisibility(GONE);
            for (int i = 0; i < 2; i++) {
                TextView textView = i == 0 ? titleTextView.getTextView() : titleTextView.getNextTextView();
                if (textView == null) {
                    continue;
                }
                textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                textView.setTextColor(Theme.getColor(Theme.key_inappPlayerTitle));
                textView.setTypeface(Typeface.DEFAULT);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            }
            titleTextView.setTag(Theme.key_inappPlayerTitle);
            if (style == 0) {
                playButton.setLayoutParams(LayoutHelper.createFrame(36, 36, Gravity.TOP | Gravity.LEFT, 0, 0, 0, 0));
                titleTextView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 36, Gravity.LEFT | Gravity.TOP, 35, 0, 36, 0));
                if (playbackSpeedButton != null) {
                    playbackSpeedButton.setVisibility(VISIBLE);
                }
                closeButton.setContentDescription(LocaleController.getString("AccDescrClosePlayer", R.string.AccDescrClosePlayer));
            } else {
                playButton.setLayoutParams(LayoutHelper.createFrame(36, 36, Gravity.TOP | Gravity.LEFT, 8, 0, 0, 0));
                titleTextView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 36, Gravity.LEFT | Gravity.TOP, 35 + 16, 0, 36, 0));
                closeButton.setContentDescription(LocaleController.getString("AccDescrStopLiveLocation", R.string.AccDescrStopLiveLocation));
            }
        } else if (style == 4) {
            selector.setBackground(Theme.getSelectorDrawable(false));
            frameLayout.setBackgroundColor(Theme.getColor(Theme.key_inappPlayerBackground));
            frameLayout.setTag(Theme.key_inappPlayerBackground);
            muteButton.setVisibility(GONE);
            subtitleTextView.setVisibility(VISIBLE);

            for (int i = 0; i < 2; i++) {
                TextView textView = i == 0 ? titleTextView.getTextView() : titleTextView.getNextTextView();
                if (textView == null) {
                    continue;
                }
                textView.setGravity(Gravity.TOP | Gravity.LEFT);
                textView.setTextColor(Theme.getColor(Theme.key_inappPlayerPerformer));
                textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            }
            titleTextView.setTag(Theme.key_inappPlayerPerformer);
            titleTextView.setPadding(0, 0, 0, 0);

            importingImageView.setVisibility(GONE);
            importingImageView.stopAnimation();

            avatars.setVisibility(VISIBLE);
            updateAvatars(false);

            closeButton.setVisibility(GONE);
            playButton.setVisibility(GONE);
            if (playbackSpeedButton != null) {
                playbackSpeedButton.setVisibility(GONE);
            }
        } else if (style == 1 || style == 3) {
            selector.setBackground(null);
            updateCallTitle();
            avatars.setVisibility(VISIBLE);
            if (style == 3) {
                if (VoIPService.getSharedInstance() != null) {
                    VoIPService.getSharedInstance().registerStateListener(this);
                }
            }
            updateAvatars(false);
            muteButton.setVisibility(VISIBLE);
            isMuted = VoIPService.getSharedInstance() != null && VoIPService.getSharedInstance().isMicMute();
            muteDrawable.setCustomEndFrame(isMuted ? 15 : 29);
            muteDrawable.setCurrentFrame(muteDrawable.getCustomEndFrame() - 1, false, true);
            muteButton.invalidate();
            frameLayout.setBackground(null);
            importingImageView.setVisibility(GONE);
            importingImageView.stopAnimation();
            Theme.getFragmentContextViewWavesDrawable().addParent(this);
            invalidate();

            for (int i = 0; i < 2; i++) {
                TextView textView = i == 0 ? titleTextView.getTextView() : titleTextView.getNextTextView();
                if (textView == null) {
                    continue;
                }
                textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                textView.setTextColor(Theme.getColor(Theme.key_returnToCallText));
                textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            }

            titleTextView.setTag(Theme.key_returnToCallText);
            closeButton.setVisibility(GONE);
            playButton.setVisibility(GONE);
            subtitleTextView.setVisibility(GONE);
            joinButton.setVisibility(GONE);

            titleTextView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 0, 0, 2));
            titleTextView.setPadding(AndroidUtilities.dp(112), 0, AndroidUtilities.dp(112), 0);
            if (playbackSpeedButton != null) {
                playbackSpeedButton.setVisibility(GONE);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animatorSet != null) {
            animatorSet.cancel();
            animatorSet = null;
        }
        if (scheduleRunnableScheduled) {
            AndroidUtilities.cancelRunOnUIThread(updateScheduleTimeRunnable);
            scheduleRunnableScheduled = false;
        }
        visible = false;
        NotificationCenter.getInstance(account).onAnimationFinish(animationIndex);
        topPadding = 0;
        if (isLocation) {
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.liveLocationsChanged);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.liveLocationsCacheChanged);
        } else {
            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                NotificationCenter.getInstance(a).removeObserver(this, NotificationCenter.messagePlayingDidReset);
                NotificationCenter.getInstance(a).removeObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
                NotificationCenter.getInstance(a).removeObserver(this, NotificationCenter.messagePlayingDidStart);
                NotificationCenter.getInstance(a).removeObserver(this, NotificationCenter.groupCallUpdated);
                NotificationCenter.getInstance(a).removeObserver(this, NotificationCenter.groupCallTypingsUpdated);
                NotificationCenter.getInstance(a).removeObserver(this, NotificationCenter.historyImportProgressChanged);
            }
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.messagePlayingSpeedChanged);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didStartedCall);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didEndCall);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.webRtcSpeakerAmplitudeEvent);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.webRtcMicAmplitudeEvent);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.groupCallVisibilityChanged);
        }

        if (currentStyle == 3 || currentStyle == 1) {
            Theme.getFragmentContextViewWavesDrawable().removeParent(this);
        }
        if (VoIPService.getSharedInstance() != null) {
            VoIPService.getSharedInstance().unregisterStateListener(this);
        }
        wasDraw = false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isLocation) {
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.liveLocationsChanged);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.liveLocationsCacheChanged);
            if (additionalContextView != null) {
                additionalContextView.checkVisibility();
            }
            checkLiveLocation(true);
        } else {
            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                NotificationCenter.getInstance(a).addObserver(this, NotificationCenter.messagePlayingDidReset);
                NotificationCenter.getInstance(a).addObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
                NotificationCenter.getInstance(a).addObserver(this, NotificationCenter.messagePlayingDidStart);
                NotificationCenter.getInstance(a).addObserver(this, NotificationCenter.groupCallUpdated);
                NotificationCenter.getInstance(a).addObserver(this, NotificationCenter.groupCallTypingsUpdated);
                NotificationCenter.getInstance(a).addObserver(this, NotificationCenter.historyImportProgressChanged);
            }
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.messagePlayingSpeedChanged);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didStartedCall);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didEndCall);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.webRtcSpeakerAmplitudeEvent);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.webRtcMicAmplitudeEvent);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.groupCallVisibilityChanged);
            if (additionalContextView != null) {
                additionalContextView.checkVisibility();
            }

            if (VoIPService.getSharedInstance() != null && !VoIPService.getSharedInstance().isHangingUp() && VoIPService.getSharedInstance().getCallState() != VoIPService.STATE_WAITING_INCOMING && !GroupCallPip.isShowing()) {
                checkCall(true);
            } else if (fragment instanceof ChatActivity && fragment.getSendMessagesHelper().getImportingHistory(((ChatActivity) fragment).getDialogId()) != null && !isPlayingVoice()) {
                checkImport(true);
            } else if (fragment instanceof ChatActivity && ((ChatActivity) fragment).getGroupCall() != null && ((ChatActivity) fragment).getGroupCall().shouldShowPanel() && !GroupCallPip.isShowing() && !isPlayingVoice()) {
                checkCall(true);
            } else {
                checkPlayer(true);
                updatePlaybackButton();
            }
        }

        if (currentStyle == 3 || currentStyle == 1) {
            Theme.getFragmentContextViewWavesDrawable().addParent(this);
            if (VoIPService.getSharedInstance() != null) {
                VoIPService.getSharedInstance().registerStateListener(this);
            }
            boolean newMuted = VoIPService.getSharedInstance() != null && VoIPService.getSharedInstance().isMicMute();
            if (isMuted != newMuted) {
                isMuted = newMuted;
                muteDrawable.setCustomEndFrame(isMuted ? 15 : 29);
                muteDrawable.setCurrentFrame(muteDrawable.getCustomEndFrame() - 1, false, true);
                muteButton.invalidate();
            }
        } else if (currentStyle == 4) {
            if (!scheduleRunnableScheduled) {
                scheduleRunnableScheduled = true;
                updateScheduleTimeRunnable.run();
            }
        }

        if (visible && topPadding == 0) {
            updatePaddings();
            setTopPadding(AndroidUtilities.dp2(getStyleHeight()));
        }

         speakerAmplitude = 0;
         micAmplitude = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, AndroidUtilities.dp2(getStyleHeight() + 2));
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.liveLocationsChanged) {
            checkLiveLocation(false);
        } else if (id == NotificationCenter.liveLocationsCacheChanged) {
            if (fragment instanceof ChatActivity) {
                long did = (Long) args[0];
                if (((ChatActivity) fragment).getDialogId() == did) {
                    checkLocationString();
                }
            }
        } else if (id == NotificationCenter.messagePlayingDidStart || id == NotificationCenter.messagePlayingPlayStateChanged || id == NotificationCenter.messagePlayingDidReset || id == NotificationCenter.didEndCall) {
            if (currentStyle == 1 || currentStyle == 3 || currentStyle == 4) {
                checkCall(false);
            }
            checkPlayer(false);
        } else if (id == NotificationCenter.didStartedCall || id == NotificationCenter.groupCallUpdated || id == NotificationCenter.groupCallVisibilityChanged) {
            checkCall(false);
            if (currentStyle == 3) {
                VoIPService sharedInstance = VoIPService.getSharedInstance();
                if (sharedInstance != null && sharedInstance.groupCall != null) {
                    if (id == NotificationCenter.didStartedCall) {
                        sharedInstance.registerStateListener(this);
                    }
                    int currentCallState = sharedInstance.getCallState();
                    if (currentCallState == VoIPService.STATE_WAIT_INIT || currentCallState == VoIPService.STATE_WAIT_INIT_ACK || currentCallState == VoIPService.STATE_CREATING || currentCallState == VoIPService.STATE_RECONNECTING) {

                    } else {
                        TLRPC.TL_groupCallParticipant participant = sharedInstance.groupCall.participants.get(sharedInstance.getSelfId());
                        if (participant != null && !participant.can_self_unmute && participant.muted && !ChatObject.canManageCalls(sharedInstance.getChat())) {
                            sharedInstance.setMicMute(true, false, false);
                            final long now = SystemClock.uptimeMillis();
                            final MotionEvent e = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0, 0, 0);
                            muteButton.dispatchTouchEvent(e);
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.groupCallTypingsUpdated) {
            if (visible && currentStyle == 4) {
                ChatObject.Call call = ((ChatActivity) fragment).getGroupCall();
                if (call != null) {
                    if (call.isScheduled()) {
                        subtitleTextView.setText(LocaleController.formatStartsTime(call.call.schedule_date, 4), false);
                    } else if (call.call.participants_count == 0) {
                        subtitleTextView.setText(LocaleController.getString("MembersTalkingNobody", R.string.MembersTalkingNobody), false);
                    } else {
                        subtitleTextView.setText(LocaleController.formatPluralString("Participants", call.call.participants_count), false);
                    }
                }
                updateAvatars(true);
            }
        } else if (id == NotificationCenter.historyImportProgressChanged) {
            if (currentStyle == 1 || currentStyle == 3 || currentStyle == 4) {
                checkCall(false);
            }
            checkImport(false);
        } else if (id == NotificationCenter.messagePlayingSpeedChanged) {
            updatePlaybackButton();
        } else if (id == NotificationCenter.webRtcMicAmplitudeEvent) {
            if (VoIPService.getSharedInstance() == null || VoIPService.getSharedInstance().isMicMute()) {
                micAmplitude = 0;
            } else {
                micAmplitude = (Math.min(GroupCallActivity.MAX_AMPLITUDE, ((float) args[0]) * 4000) / GroupCallActivity.MAX_AMPLITUDE);
            }
            if (VoIPService.getSharedInstance() != null) {
                Theme.getFragmentContextViewWavesDrawable().setAmplitude(Math.max(speakerAmplitude, micAmplitude));
            }
        } else if (id == NotificationCenter.webRtcSpeakerAmplitudeEvent) {
            float a = (float) args[0] * 15f / 80f;
            speakerAmplitude = Math.max(0, Math.min(a, 1));
            if (VoIPService.getSharedInstance() == null || VoIPService.getSharedInstance().isMicMute()) {
                micAmplitude = 0;
            }
            if (VoIPService.getSharedInstance() != null) {
                Theme.getFragmentContextViewWavesDrawable().setAmplitude(Math.max(speakerAmplitude, micAmplitude));
            }
            avatars.invalidate();
        }
    }

    float speakerAmplitude;
    float micAmplitude;

    public int getStyleHeight() {
        return currentStyle == 4 ? 48 : 36;
    }

    public boolean isCallTypeVisible() {
        return (currentStyle == 1 || currentStyle == 3) && visible;
    }

    private void checkLiveLocation(boolean create) {
        View fragmentView = fragment.getFragmentView();
        if (!create && fragmentView != null) {
            if (fragmentView.getParent() == null || ((View) fragmentView.getParent()).getVisibility() != VISIBLE) {
                create = true;
            }
        }
        boolean show;
        if (fragment instanceof DialogsActivity) {
            show = LocationController.getLocationsCount() != 0;
        } else {
            show = LocationController.getInstance(fragment.getCurrentAccount()).isSharingLocation(((ChatActivity) fragment).getDialogId());
        }
        if (!show) {
            lastLocationSharingCount = -1;
            AndroidUtilities.cancelRunOnUIThread(checkLocationRunnable);
            if (visible) {
                visible = false;
                if (create) {
                    if (getVisibility() != GONE) {
                        setVisibility(GONE);
                    }
                    setTopPadding(0);
                } else {
                    if (animatorSet != null) {
                        animatorSet.cancel();
                        animatorSet = null;
                    }
                    animatorSet = new AnimatorSet();
                    animatorSet.playTogether(ObjectAnimator.ofFloat(this, "topPadding", 0));
                    animatorSet.setDuration(200);
                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (animatorSet != null && animatorSet.equals(animation)) {
                                setVisibility(GONE);
                                animatorSet = null;
                            }
                        }
                    });
                    animatorSet.start();
                }
            }
        } else {
            updateStyle(2);
            playButton.setImageDrawable(new ShareLocationDrawable(getContext(), 1));
            if (create && topPadding == 0) {
                setTopPadding(AndroidUtilities.dp2(getStyleHeight()));
            }
            if (!visible) {
                if (!create) {
                    if (animatorSet != null) {
                        animatorSet.cancel();
                        animatorSet = null;
                    }
                    animatorSet = new AnimatorSet();
                    animatorSet.playTogether(ObjectAnimator.ofFloat(this, "topPadding", AndroidUtilities.dp2(getStyleHeight())));
                    animatorSet.setDuration(200);
                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (animatorSet != null && animatorSet.equals(animation)) {
                                animatorSet = null;
                            }
                        }
                    });
                    animatorSet.start();
                }
                visible = true;
                setVisibility(VISIBLE);
            }

            if (fragment instanceof DialogsActivity) {
                String liveLocation = LocaleController.getString("LiveLocationContext", R.string.LiveLocationContext);
                String param;
                String str;
                ArrayList<LocationController.SharingLocationInfo> infos = new ArrayList<>();
                for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                    infos.addAll(LocationController.getInstance(a).sharingLocationsUI);
                }
                if (infos.size() == 1) {
                    LocationController.SharingLocationInfo info = infos.get(0);
                    int lower_id = (int) info.messageObject.getDialogId();
                    if (lower_id > 0) {
                        TLRPC.User user = MessagesController.getInstance(info.messageObject.currentAccount).getUser(lower_id);
                        param = UserObject.getFirstName(user);
                        str = LocaleController.getString("AttachLiveLocationIsSharing", R.string.AttachLiveLocationIsSharing);
                    } else {
                        TLRPC.Chat chat = MessagesController.getInstance(info.messageObject.currentAccount).getChat(-lower_id);
                        if (chat != null) {
                            param = chat.title;
                        } else {
                            param = "";
                        }
                        str = LocaleController.getString("AttachLiveLocationIsSharingChat", R.string.AttachLiveLocationIsSharingChat);
                    }
                } else {
                    param = LocaleController.formatPluralString("Chats", infos.size());
                    str = LocaleController.getString("AttachLiveLocationIsSharingChats", R.string.AttachLiveLocationIsSharingChats);
                }
                String fullString = String.format(str, liveLocation, param);
                int start = fullString.indexOf(liveLocation);
                SpannableStringBuilder stringBuilder = new SpannableStringBuilder(fullString);
                for (int i = 0; i < 2; i++) {
                    TextView textView = i == 0 ? titleTextView.getTextView() : titleTextView.getNextTextView();
                    if (textView == null) {
                        continue;
                    }
                    textView.setEllipsize(TextUtils.TruncateAt.END);
                }

                TypefaceSpan span = new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), 0, Theme.getColor(Theme.key_inappPlayerPerformer));
                stringBuilder.setSpan(span, start, start + liveLocation.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                titleTextView.setText(stringBuilder, false);
            } else {
                checkLocationRunnable.run();
                checkLocationString();
            }
        }
    }

    private void checkLocationString() {
        if (!(fragment instanceof ChatActivity) || titleTextView == null) {
            return;
        }
        ChatActivity chatActivity = (ChatActivity) fragment;
        long dialogId = chatActivity.getDialogId();
        int currentAccount = chatActivity.getCurrentAccount();
        ArrayList<TLRPC.Message> messages = LocationController.getInstance(currentAccount).locationsCache.get(dialogId);
        if (!firstLocationsLoaded) {
            LocationController.getInstance(currentAccount).loadLiveLocations(dialogId);
            firstLocationsLoaded = true;
        }

        int locationSharingCount = 0;
        TLRPC.User notYouUser = null;
        if (messages != null) {
            int currentUserId = UserConfig.getInstance(currentAccount).getClientUserId();
            int date = ConnectionsManager.getInstance(currentAccount).getCurrentTime();
            for (int a = 0; a < messages.size(); a++) {
                TLRPC.Message message = messages.get(a);
                if (message.media == null) {
                    continue;
                }
                if (message.date + message.media.period > date) {
                    int fromId = MessageObject.getFromChatId(message);
                    if (notYouUser == null && fromId != currentUserId) {
                        notYouUser = MessagesController.getInstance(currentAccount).getUser(fromId);
                    }
                    locationSharingCount++;
                }
            }
        }
        if (lastLocationSharingCount == locationSharingCount) {
            return;
        }
        lastLocationSharingCount = locationSharingCount;

        String liveLocation = LocaleController.getString("LiveLocationContext", R.string.LiveLocationContext);
        String fullString;
        if (locationSharingCount == 0) {
            fullString = liveLocation;
        } else {
            int otherSharingCount = locationSharingCount - 1;
            if (LocationController.getInstance(currentAccount).isSharingLocation(dialogId)) {
                if (otherSharingCount != 0) {
                    if (otherSharingCount == 1 && notYouUser != null) {
                        fullString = String.format("%1$s - %2$s", liveLocation, LocaleController.formatString("SharingYouAndOtherName", R.string.SharingYouAndOtherName, UserObject.getFirstName(notYouUser)));
                    } else {
                        fullString = String.format("%1$s - %2$s %3$s", liveLocation, LocaleController.getString("ChatYourSelfName", R.string.ChatYourSelfName), LocaleController.formatPluralString("AndOther", otherSharingCount));
                    }
                } else {
                    fullString = String.format("%1$s - %2$s", liveLocation, LocaleController.getString("ChatYourSelfName", R.string.ChatYourSelfName));
                }
            } else {
                if (otherSharingCount != 0) {
                    fullString = String.format("%1$s - %2$s %3$s", liveLocation, UserObject.getFirstName(notYouUser), LocaleController.formatPluralString("AndOther", otherSharingCount));
                } else {
                    fullString = String.format("%1$s - %2$s", liveLocation, UserObject.getFirstName(notYouUser));
                }
            }
        }
        if (fullString.equals(lastString)) {
            return;
        }
        lastString = fullString;
        int start = fullString.indexOf(liveLocation);
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(fullString);
        for (int i = 0; i < 2; i++) {
            TextView textView = i == 0 ? titleTextView.getTextView() : titleTextView.getNextTextView();
            if (textView == null) {
                continue;
            }
            textView.setEllipsize(TextUtils.TruncateAt.END);
        }
        if (start >= 0) {
            TypefaceSpan span = new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), 0, Theme.getColor(Theme.key_inappPlayerPerformer));
            stringBuilder.setSpan(span, start, start + liveLocation.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        titleTextView.setText(stringBuilder, false);
    }

    private void checkPlayer(boolean create) {
        if (visible && (currentStyle == 1 || currentStyle == 3 || (currentStyle == 4 || currentStyle == 5) && !isPlayingVoice())) {
            return;
        }
        MessageObject messageObject = MediaController.getInstance().getPlayingMessageObject();
        View fragmentView = fragment.getFragmentView();
        if (!create && fragmentView != null) {
            if (fragmentView.getParent() == null || ((View) fragmentView.getParent()).getVisibility() != VISIBLE) {
                create = true;
            }
        }
        boolean wasVisible = visible;
        if (messageObject == null || messageObject.getId() == 0 || messageObject.isVideo()) {
            lastMessageObject = null;
            boolean callAvailable = supportsCalls && VoIPService.getSharedInstance() != null && !VoIPService.getSharedInstance().isHangingUp() && VoIPService.getSharedInstance().getCallState() != VoIPService.STATE_WAITING_INCOMING && !GroupCallPip.isShowing();
            if (!isPlayingVoice() && !callAvailable && fragment instanceof ChatActivity && !GroupCallPip.isShowing()) {
                ChatObject.Call call = ((ChatActivity) fragment).getGroupCall();
                callAvailable = call != null && call.shouldShowPanel();
            }
            if (callAvailable) {
                checkCall(false);
                return;
            }
            if (visible) {
                visible = false;
                if (create) {
                    if (getVisibility() != GONE) {
                        setVisibility(GONE);
                    }
                    setTopPadding(0);
                } else {
                    if (animatorSet != null) {
                        animatorSet.cancel();
                        animatorSet = null;
                    }
                    animationIndex = NotificationCenter.getInstance(account).setAnimationInProgress(animationIndex, null);
                    animatorSet = new AnimatorSet();
                    animatorSet.playTogether(ObjectAnimator.ofFloat(this, "topPadding", 0));
                    animatorSet.setDuration(200);
                    if (delegate != null) {
                        delegate.onAnimation(true, false);
                    }
                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            NotificationCenter.getInstance(account).onAnimationFinish(animationIndex);
                            if (animatorSet != null && animatorSet.equals(animation)) {
                                setVisibility(GONE);
                                if (delegate != null) {
                                    delegate.onAnimation(false, false);
                                }
                                animatorSet = null;
                                if (checkCallAfterAnimation) {
                                    checkCall(false);
                                } else if (checkPlayerAfterAnimation) {
                                    checkPlayer(false);
                                } else if (checkImportAfterAnimation) {
                                    checkImport(false);
                                }
                                checkCallAfterAnimation = false;
                                checkPlayerAfterAnimation = false;
                                checkImportAfterAnimation = false;
                            }
                        }
                    });
                    animatorSet.start();
                }
            } else {
                setVisibility(View.GONE);
            }
        } else {
            if (currentStyle != 0 && animatorSet != null && !create) {
                checkPlayerAfterAnimation = true;
                return;
            }
            int prevStyle = currentStyle;
            updateStyle(0);
            if (create && topPadding == 0) {
                updatePaddings();
                setTopPadding(AndroidUtilities.dp2(getStyleHeight()));
                if (delegate != null) {
                    delegate.onAnimation(true, true);
                    delegate.onAnimation(false, true);
                }
            }
            if (!visible) {
                if (!create) {
                    if (animatorSet != null) {
                        animatorSet.cancel();
                        animatorSet = null;
                    }
                    animationIndex = NotificationCenter.getInstance(account).setAnimationInProgress(animationIndex, null);
                    animatorSet = new AnimatorSet();
                    if (additionalContextView != null && additionalContextView.getVisibility() == VISIBLE) {
                        ((LayoutParams) getLayoutParams()).topMargin = -AndroidUtilities.dp(getStyleHeight() + additionalContextView.getStyleHeight());
                    } else {
                        ((LayoutParams) getLayoutParams()).topMargin = -AndroidUtilities.dp(getStyleHeight());
                    }
                    if (delegate != null) {
                        delegate.onAnimation(true, true);
                    }
                    animatorSet.playTogether(ObjectAnimator.ofFloat(this, "topPadding", AndroidUtilities.dp2(getStyleHeight())));
                    animatorSet.setDuration(200);
                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            NotificationCenter.getInstance(account).onAnimationFinish(animationIndex);
                            if (animatorSet != null && animatorSet.equals(animation)) {
                                if (delegate != null) {
                                    delegate.onAnimation(false, true);
                                }
                                animatorSet = null;
                                if (checkCallAfterAnimation) {
                                    checkCall(false);
                                } else if (checkPlayerAfterAnimation) {
                                    checkPlayer(false);
                                } else if (checkImportAfterAnimation) {
                                    checkImport(false);
                                }
                                checkCallAfterAnimation = false;
                                checkPlayerAfterAnimation = false;
                                checkImportAfterAnimation = false;
                            }
                        }
                    });
                    animatorSet.start();
                }
                visible = true;
                setVisibility(VISIBLE);
            }
            if (MediaController.getInstance().isMessagePaused()) {
                playPauseDrawable.setPause(false, !create);
                playButton.setContentDescription(LocaleController.getString("AccActionPlay", R.string.AccActionPlay));
            } else {
                playPauseDrawable.setPause(true, !create);
                playButton.setContentDescription(LocaleController.getString("AccActionPause", R.string.AccActionPause));
            }
            if (lastMessageObject != messageObject || prevStyle != 0) {
                lastMessageObject = messageObject;
                SpannableStringBuilder stringBuilder;
                if (lastMessageObject.isVoice() || lastMessageObject.isRoundVideo()) {
                    isMusic = false;
                    if (playbackSpeedButton != null) {
                        playbackSpeedButton.setAlpha(1.0f);
                        playbackSpeedButton.setEnabled(true);
                    }
                    titleTextView.setPadding(0, 0, AndroidUtilities.dp(44), 0);
                    stringBuilder = new SpannableStringBuilder(String.format("%s %s", messageObject.getMusicAuthor(), messageObject.getMusicTitle()));

                    for (int i = 0; i < 2; i++) {
                        TextView textView = i == 0 ? titleTextView.getTextView() : titleTextView.getNextTextView();
                        if (textView == null) {
                            continue;
                        }
                        textView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                    }

                    updatePlaybackButton();
                } else {
                    isMusic = true;
                    if (playbackSpeedButton != null) {
                        if (messageObject.getDuration() >= 10 * 60) {
                            playbackSpeedButton.setAlpha(1.0f);
                            playbackSpeedButton.setEnabled(true);
                            titleTextView.setPadding(0, 0, AndroidUtilities.dp(44), 0);
                            updatePlaybackButton();
                        } else {
                            playbackSpeedButton.setAlpha(0.0f);
                            playbackSpeedButton.setEnabled(false);
                            titleTextView.setPadding(0, 0, 0, 0);
                        }
                    } else {
                        titleTextView.setPadding(0, 0, 0, 0);
                    }
                    stringBuilder = new SpannableStringBuilder(String.format("%s - %s", messageObject.getMusicAuthor(), messageObject.getMusicTitle()));
                    for (int i = 0; i < 2; i++) {
                        TextView textView = i == 0 ? titleTextView.getTextView() : titleTextView.getNextTextView();
                        if (textView == null) {
                            continue;
                        }
                        textView.setEllipsize(TextUtils.TruncateAt.END);
                    }
                }
                TypefaceSpan span = new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), 0, Theme.getColor(Theme.key_inappPlayerPerformer));
                stringBuilder.setSpan(span, 0, messageObject.getMusicAuthor().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                titleTextView.setText(stringBuilder, !create && wasVisible && isMusic);
            }
        }
    }

    public void checkImport(boolean create) {
        if (!(fragment instanceof ChatActivity) || visible && (currentStyle == 1 || currentStyle == 3)) {
            return;
        }
        ChatActivity chatActivity = (ChatActivity) fragment;
        SendMessagesHelper.ImportingHistory importingHistory = chatActivity.getSendMessagesHelper().getImportingHistory(chatActivity.getDialogId());
        View fragmentView = fragment.getFragmentView();
        if (!create && fragmentView != null) {
            if (fragmentView.getParent() == null || ((View) fragmentView.getParent()).getVisibility() != VISIBLE) {
                create = true;
            }
        }

        Dialog dialog = chatActivity.getVisibleDialog();
        if ((isPlayingVoice() || chatActivity.shouldShowImport() || dialog instanceof ImportingAlert && !((ImportingAlert) dialog).isDismissed()) && importingHistory != null) {
            importingHistory = null;
        }

        if (importingHistory == null) {
            if (visible && (create && currentStyle == -1 || currentStyle == 5)) {
                visible = false;
                if (create) {
                    if (getVisibility() != GONE) {
                        setVisibility(GONE);
                    }
                    setTopPadding(0);
                } else {
                    if (animatorSet != null) {
                        animatorSet.cancel();
                        animatorSet = null;
                    }
                    final int currentAccount = account;
                    animationIndex = NotificationCenter.getInstance(currentAccount).setAnimationInProgress(animationIndex, null);
                    animatorSet = new AnimatorSet();
                    animatorSet.playTogether(ObjectAnimator.ofFloat(this, "topPadding", 0));
                    animatorSet.setDuration(220);
                    animatorSet.setInterpolator(CubicBezierInterpolator.DEFAULT);
                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            NotificationCenter.getInstance(currentAccount).onAnimationFinish(animationIndex);
                            if (animatorSet != null && animatorSet.equals(animation)) {
                                setVisibility(GONE);
                                animatorSet = null;
                                if (checkCallAfterAnimation) {
                                    checkCall(false);
                                } else if (checkPlayerAfterAnimation) {
                                    checkPlayer(false);
                                } else if (checkImportAfterAnimation) {
                                    checkImport(false);
                                }
                                checkCallAfterAnimation = false;
                                checkPlayerAfterAnimation = false;
                                checkImportAfterAnimation = false;
                            }
                        }
                    });
                    animatorSet.start();
                }
            } else if (currentStyle == -1 || currentStyle == 5) {
                visible = false;
                setVisibility(GONE);
            }
        } else {
            if (currentStyle != 5 && animatorSet != null && !create) {
                checkImportAfterAnimation = true;
                return;
            }
            int prevStyle = currentStyle;
            updateStyle(5);
            if (create && topPadding == 0) {
                updatePaddings();
                setTopPadding(AndroidUtilities.dp2(getStyleHeight()));
                if (delegate != null) {
                    delegate.onAnimation(true, true);
                    delegate.onAnimation(false, true);
                }
            }
            if (!visible) {
                if (!create) {
                    if (animatorSet != null) {
                        animatorSet.cancel();
                        animatorSet = null;
                    }
                    animationIndex = NotificationCenter.getInstance(account).setAnimationInProgress(animationIndex, null);
                    animatorSet = new AnimatorSet();
                    if (additionalContextView != null && additionalContextView.getVisibility() == VISIBLE) {
                        ((LayoutParams) getLayoutParams()).topMargin = -AndroidUtilities.dp(getStyleHeight() + additionalContextView.getStyleHeight());
                    } else {
                        ((LayoutParams) getLayoutParams()).topMargin = -AndroidUtilities.dp(getStyleHeight());
                    }
                    if (delegate != null) {
                        delegate.onAnimation(true, true);
                    }
                    animatorSet.playTogether(ObjectAnimator.ofFloat(this, "topPadding", AndroidUtilities.dp2(getStyleHeight())));
                    animatorSet.setDuration(200);
                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            NotificationCenter.getInstance(account).onAnimationFinish(animationIndex);
                            if (animatorSet != null && animatorSet.equals(animation)) {
                                if (delegate != null) {
                                    delegate.onAnimation(false, true);
                                }
                                animatorSet = null;
                                if (checkCallAfterAnimation) {
                                    checkCall(false);
                                } else if (checkPlayerAfterAnimation) {
                                    checkPlayer(false);
                                } else if (checkImportAfterAnimation) {
                                    checkImport(false);
                                }
                                checkCallAfterAnimation = false;
                                checkPlayerAfterAnimation = false;
                                checkImportAfterAnimation = false;
                            }
                        }
                    });
                    animatorSet.start();
                }
                visible = true;
                setVisibility(VISIBLE);
            }
            if (currentProgress != importingHistory.uploadProgress) {
                currentProgress = importingHistory.uploadProgress;
                titleTextView.setText(AndroidUtilities.replaceTags(LocaleController.formatString("ImportUploading", R.string.ImportUploading, importingHistory.uploadProgress)), false);
            }
        }
    }

    private boolean isPlayingVoice() {
        MessageObject messageObject = MediaController.getInstance().getPlayingMessageObject();
        return messageObject != null && messageObject.isVoice();
    }

    public void checkCall(boolean create) {
        VoIPService voIPService = VoIPService.getSharedInstance();
        if (visible && currentStyle == 5 && (voIPService == null || voIPService.isHangingUp())) {
            return;
        }
        View fragmentView = fragment.getFragmentView();
        if (!create && fragmentView != null) {
            if (fragmentView.getParent() == null || ((View) fragmentView.getParent()).getVisibility() != VISIBLE) {
                create = true;
            }
        }
        boolean callAvailable;
        boolean groupActive;
        if (GroupCallPip.isShowing()) {
            callAvailable = false;
            groupActive = false;
        } else {
            callAvailable = !GroupCallActivity.groupCallUiVisible && supportsCalls && voIPService != null && !voIPService.isHangingUp();
            if (voIPService != null && voIPService.groupCall != null && voIPService.groupCall.call instanceof TLRPC.TL_groupCallDiscarded) {
                callAvailable = false;
            }
            groupActive = false;
            if (!isPlayingVoice() && !GroupCallActivity.groupCallUiVisible && supportsCalls && !callAvailable && fragment instanceof ChatActivity) {
                ChatObject.Call call = ((ChatActivity) fragment).getGroupCall();
                if (call != null && call.shouldShowPanel()) {
                    callAvailable = true;
                    groupActive = true;
                }
            }
        }

        if (!callAvailable) {
            if (visible && (create && currentStyle == -1 || currentStyle == 4 || currentStyle == 3 || currentStyle == 1)) {
                visible = false;
                if (create) {
                    if (getVisibility() != GONE) {
                        setVisibility(GONE);
                    }
                    setTopPadding(0);
                } else {
                    if (animatorSet != null) {
                        animatorSet.cancel();
                        animatorSet = null;
                    }
                    final int currentAccount = account;
                    animationIndex = NotificationCenter.getInstance(currentAccount).setAnimationInProgress(animationIndex, null);
                    animatorSet = new AnimatorSet();
                    animatorSet.playTogether(ObjectAnimator.ofFloat(this, "topPadding", 0));
                    animatorSet.setDuration(220);
                    animatorSet.setInterpolator(CubicBezierInterpolator.DEFAULT);
                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            NotificationCenter.getInstance(currentAccount).onAnimationFinish(animationIndex);
                            if (animatorSet != null && animatorSet.equals(animation)) {
                                setVisibility(GONE);
                                animatorSet = null;
                                if (checkCallAfterAnimation) {
                                    checkCall(false);
                                } else if (checkPlayerAfterAnimation) {
                                    checkPlayer(false);
                                } else if (checkImportAfterAnimation) {
                                    checkImport(false);
                                }
                                checkCallAfterAnimation = false;
                                checkPlayerAfterAnimation = false;
                                checkImportAfterAnimation = false;
                            }
                        }
                    });
                    animatorSet.start();
                }
            } else if (visible && (currentStyle == -1 || currentStyle == 4 || currentStyle == 3 || currentStyle == 1)) {
                visible = false;
                setVisibility(GONE);
            }
        } else {
            int newStyle;
            if (groupActive) {
                newStyle = 4;
            } else if (voIPService.groupCall != null) {
                newStyle = 3;
            } else {
                newStyle = 1;
            }
            if (newStyle != currentStyle && animatorSet != null && !create) {
                checkCallAfterAnimation = true;
                return;
            }
            if (newStyle != currentStyle && visible && !create) {
                if (animatorSet != null) {
                    animatorSet.cancel();
                    animatorSet = null;
                }
                final int currentAccount = account;
                animationIndex = NotificationCenter.getInstance(currentAccount).setAnimationInProgress(animationIndex, null);
                animatorSet = new AnimatorSet();
                animatorSet.playTogether(ObjectAnimator.ofFloat(this, "topPadding", 0));
                animatorSet.setDuration(220);
                animatorSet.setInterpolator(CubicBezierInterpolator.DEFAULT);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        NotificationCenter.getInstance(currentAccount).onAnimationFinish(animationIndex);
                        if (animatorSet != null && animatorSet.equals(animation)) {
                            visible = false;
                            animatorSet = null;
                            checkCall(false);
                        }
                    }
                });
                animatorSet.start();
                return;
            }
            if (groupActive) {
                boolean updateAnimated = currentStyle == 4 && visible;
                updateStyle(4);

                ChatObject.Call call = ((ChatActivity) fragment).getGroupCall();
                if (call.isScheduled()) {
                    if (gradientPaint == null) {
                        gradientTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                        gradientTextPaint.setColor(0xffffffff);
                        gradientTextPaint.setTextSize(AndroidUtilities.dp(14));
                        gradientTextPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

                        gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                        gradientPaint.setColor(0xffffffff);

                        matrix = new Matrix();
                    }
                    joinButton.setVisibility(GONE);
                    if (!TextUtils.isEmpty(call.call.title)) {
                        titleTextView.setText(call.call.title, false);
                    } else {
                        titleTextView.setText(LocaleController.getString("VoipGroupScheduledVoiceChat", R.string.VoipGroupScheduledVoiceChat), false);
                    }
                    subtitleTextView.setText(LocaleController.formatStartsTime(call.call.schedule_date, 4), false);
                    if (!scheduleRunnableScheduled) {
                        scheduleRunnableScheduled = true;
                        updateScheduleTimeRunnable.run();
                    }
                } else {
                    timeLayout = null;
                    joinButton.setVisibility(VISIBLE);
                    titleTextView.setText(LocaleController.getString("VoipGroupVoiceChat", R.string.VoipGroupVoiceChat), false);
                    if (call.call.participants_count == 0) {
                        subtitleTextView.setText(LocaleController.getString("MembersTalkingNobody", R.string.MembersTalkingNobody), false);
                    } else {
                        subtitleTextView.setText(LocaleController.formatPluralString("Participants", call.call.participants_count), false);
                    }
                    frameLayout.invalidate();
                }

                updateAvatars(avatars.wasDraw && updateAnimated);
            } else {
                if (voIPService != null && voIPService.groupCall != null) {
                    updateAvatars(currentStyle == 3);
                    updateStyle(3);
                } else {
                    updateAvatars(currentStyle == 1);
                    updateStyle(1);
                }
            }
            if (!visible) {
                if (!create) {
                    if (animatorSet != null) {
                        animatorSet.cancel();
                        animatorSet = null;
                    }
                    animatorSet = new AnimatorSet();
                    if (additionalContextView != null && additionalContextView.getVisibility() == VISIBLE) {
                        ((LayoutParams) getLayoutParams()).topMargin = -AndroidUtilities.dp(getStyleHeight() + additionalContextView.getStyleHeight());
                    } else {
                        ((LayoutParams) getLayoutParams()).topMargin = -AndroidUtilities.dp(getStyleHeight());
                    }
                    final int currentAccount = account;
                    animationIndex = NotificationCenter.getInstance(currentAccount).setAnimationInProgress(animationIndex, new int[]{NotificationCenter.messagesDidLoad});
                    animatorSet.playTogether(ObjectAnimator.ofFloat(this, "topPadding", AndroidUtilities.dp2(getStyleHeight())));
                    animatorSet.setDuration(220);
                    animatorSet.setInterpolator(CubicBezierInterpolator.DEFAULT);
                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            NotificationCenter.getInstance(currentAccount).onAnimationFinish(animationIndex);
                            if (animatorSet != null && animatorSet.equals(animation)) {
                                animatorSet = null;
                            }
                            if (checkCallAfterAnimation) {
                                checkCall(false);
                            } else if (checkPlayerAfterAnimation) {
                                checkPlayer(false);
                            } else if (checkImportAfterAnimation) {
                                checkImport(false);
                            }
                            checkCallAfterAnimation = false;
                            checkPlayerAfterAnimation = false;
                            checkImportAfterAnimation = false;
                        }
                    });
                    animatorSet.start();
                } else {
                    updatePaddings();
                    setTopPadding(AndroidUtilities.dp2(getStyleHeight()));
                }
                visible = true;
                setVisibility(VISIBLE);
            }
        }
    }

    private void updateAvatars(boolean animated) {
        if (!animated) {
            if (avatars.transitionProgressAnimator != null) {
                avatars.transitionProgressAnimator.cancel();
                avatars.transitionProgressAnimator = null;
            }
        }
        ChatObject.Call call;
        TLRPC.User userCall;
        if (avatars.transitionProgressAnimator == null) {
            int currentAccount;
            if (currentStyle == 4) {
                if (fragment instanceof ChatActivity) {
                    ChatActivity chatActivity = (ChatActivity) fragment;
                    call = chatActivity.getGroupCall();
                    currentAccount = chatActivity.getCurrentAccount();
                } else {
                    call = null;
                    currentAccount = account;
                }
                userCall = null;
            } else {
                if (VoIPService.getSharedInstance() != null) {
                    call = VoIPService.getSharedInstance().groupCall;
                    userCall = fragment instanceof ChatActivity ? null : VoIPService.getSharedInstance().getUser();
                    currentAccount = VoIPService.getSharedInstance().getAccount();
                } else {
                    call = null;
                    userCall = null;
                    currentAccount = account;
                }
            }
            if (call != null) {
                for (int a = 0, N = call.sortedParticipants.size(); a < 3; a++) {
                    if (a < N) {
                        avatars.setObject(a, currentAccount, call.sortedParticipants.get(a));
                    } else {
                        avatars.setObject(a, currentAccount, null);
                    }
                }
            } else if (userCall != null) {
                avatars.setObject(0, currentAccount, userCall);
                for (int a = 1; a < 3; a++) {
                    avatars.setObject(a, currentAccount, null);
                }
            } else {
                for (int a = 0; a < 3; a++) {
                    avatars.setObject(a, currentAccount, null);
                }
            }
            avatars.commitTransition(animated);

            if (currentStyle == 4 && call != null) {
                int N = Math.min(3, call.sortedParticipants.size());
                int x = N == 0 ? 10 : (10 + 24 * (N - 1) + 32 + 10);
                if (animated) {
                    int leftMargin = ((LayoutParams) titleTextView.getLayoutParams()).leftMargin;
                    if (AndroidUtilities.dp(x) != leftMargin) {
                        float dx = titleTextView.getTranslationX() + leftMargin - AndroidUtilities.dp(x);
                        titleTextView.setTranslationX(dx);
                        subtitleTextView.setTranslationX(dx);
                        titleTextView.animate().translationX(0).setDuration(220).setInterpolator(CubicBezierInterpolator.DEFAULT);
                        subtitleTextView.animate().translationX(0).setDuration(220).setInterpolator(CubicBezierInterpolator.DEFAULT);
                    }
                } else {
                    titleTextView.animate().cancel();
                    subtitleTextView.animate().cancel();
                    titleTextView.setTranslationX(0);
                    subtitleTextView.setTranslationX(0);
                }
                titleTextView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, Gravity.LEFT | Gravity.TOP, x, 5, call.isScheduled() ? 90 : 36, 0));
                subtitleTextView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, Gravity.LEFT | Gravity.TOP, x, 25, call.isScheduled() ? 90 : 36, 0));
            }
        } else {
            avatars.updateAfterTransitionEnd();
        }
    }


    boolean collapseTransition;
    float extraHeight;
    float collapseProgress;
    boolean wasDraw;

    public void setCollapseTransition(boolean show, float extraHeight, float progress) {
        collapseTransition = show;
        this.extraHeight = extraHeight;
        this.collapseProgress = progress;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (drawOverlay && getVisibility() != View.VISIBLE) {
            return;
        }
        boolean clipped = false;
        if ((currentStyle == 3 || currentStyle == 1) && drawOverlay) {
            boolean mutedByAdmin = GroupCallActivity.groupCallInstance == null && Theme.getFragmentContextViewWavesDrawable().getState() == FragmentContextViewWavesDrawable.MUTE_BUTTON_STATE_MUTED_BY_ADMIN;
            Theme.getFragmentContextViewWavesDrawable().updateState(wasDraw);

            float progress = topPadding / AndroidUtilities.dp((getStyleHeight()));

            if (collapseTransition) {
                Theme.getFragmentContextViewWavesDrawable().draw(0, AndroidUtilities.dp((getStyleHeight())) - topPadding + extraHeight, getMeasuredWidth(), getMeasuredHeight() - AndroidUtilities.dp(2), canvas, null, Math.min(progress, (1f - collapseProgress)));
            } else {
                Theme.getFragmentContextViewWavesDrawable().draw(0, AndroidUtilities.dp((getStyleHeight())) - topPadding, getMeasuredWidth(), getMeasuredHeight() - AndroidUtilities.dp(2), canvas, this, progress);
            }
            float clipTop = AndroidUtilities.dp((getStyleHeight())) - topPadding;
            if (collapseTransition) {
                clipTop += extraHeight;
            }
            if (clipTop > getMeasuredHeight()) {
                return;
            }
            clipped = true;
            canvas.save();
            canvas.clipRect(0, clipTop, getMeasuredWidth(), getMeasuredHeight());
            invalidate();
        }
        super.dispatchDraw(canvas);
        if (clipped) {
            canvas.restore();
        }
        wasDraw = true;
    }

    boolean drawOverlay;

    public void setDrawOverlay(boolean drawOverlay) {
        this.drawOverlay = drawOverlay;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (currentStyle == 3 || currentStyle == 1) {
            if (getParent() != null) {
                ((View) getParent()).invalidate();
            }
        }
    }

    public boolean isCallStyle() {
        return currentStyle == 3 || currentStyle == 1;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        updatePaddings();
        setTopPadding(topPadding);
        if (visibility == View.GONE) {
            wasDraw = false;
        }
    }

    private void updatePaddings() {
        int margin = 0;
        if (getVisibility() == VISIBLE) {
            margin -= AndroidUtilities.dp(getStyleHeight());
        }
        if (additionalContextView != null && additionalContextView.getVisibility() == VISIBLE) {
            margin -= AndroidUtilities.dp(additionalContextView.getStyleHeight());
            ((LayoutParams) getLayoutParams()).topMargin = margin;
            ((LayoutParams) additionalContextView.getLayoutParams()).topMargin = margin;
        } else {
            ((LayoutParams) getLayoutParams()).topMargin = margin;
        }
    }

    @Override
    public void onStateChanged(int state) {
        updateCallTitle();
    }

    private void updateCallTitle() {
        VoIPService service = VoIPService.getSharedInstance();
        if (service != null && (currentStyle == 1 || currentStyle == 3)) {
            int currentCallState = service.getCallState();
            if (!service.isSwitchingStream() && (currentCallState == VoIPService.STATE_WAIT_INIT || currentCallState == VoIPService.STATE_WAIT_INIT_ACK || currentCallState == VoIPService.STATE_CREATING || currentCallState == VoIPService.STATE_RECONNECTING)) {
                titleTextView.setText(LocaleController.getString("VoipGroupConnecting", R.string. VoipGroupConnecting), false);
            } else if (service.getChat() != null) {
                if (!TextUtils.isEmpty(service.groupCall.call.title)) {
                    titleTextView.setText(service.groupCall.call.title, false);
                } else {
                    if (fragment instanceof ChatActivity && ((ChatActivity) fragment).getCurrentChat() != null && ((ChatActivity) fragment).getCurrentChat().id == service.getChat().id) {
                        titleTextView.setText(LocaleController.getString("VoipGroupViewVoiceChat", R.string.VoipGroupViewVoiceChat), false);
                    } else {
                        titleTextView.setText(service.getChat().title, false);
                    }
                }
            } else if (service.getUser() != null) {
                TLRPC.User user = service.getUser();
                if (fragment instanceof ChatActivity && ((ChatActivity) fragment).getCurrentUser() != null && ((ChatActivity) fragment).getCurrentUser().id == user.id) {
                    titleTextView.setText(LocaleController.getString("ReturnToCall", R.string.ReturnToCall));
                } else {
                    titleTextView.setText(ContactsController.formatName(user.first_name, user.last_name));
                }
            }
        }
    }
}
