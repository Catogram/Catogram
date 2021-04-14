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
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Property;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.ProfileActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class PollVotesAlert extends BottomSheet {

    private RecyclerListView listView;
    private Adapter listAdapter;
    private Drawable shadowDrawable;
    private View actionBarShadow;
    private ActionBar actionBar;
    private AnimatorSet actionBarAnimation;

    private ChatActivity chatActivity;
    private MessageObject messageObject;
    private TLRPC.Poll poll;
    private TLRPC.InputPeer peer;
    private HashSet<VotesList> loadingMore = new HashSet<>();
    private HashMap<VotesList, Button> votesPercents = new HashMap<>();

    private ArrayList<VotesList> voters = new ArrayList<>();

    private TextView titleTextView;

    private int scrollOffsetY;
    private int topBeforeSwitch;

    private ArrayList<Integer> queries = new ArrayList<>();

    private Paint placeholderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private LinearGradient placeholderGradient;
    private Matrix placeholderMatrix;
    private float totalTranslation;
    private float gradientWidth;
    private boolean loadingResults = true;
    private RectF rect = new RectF();

    private static class VotesList {

        public int count;
        public ArrayList<TLRPC.MessageUserVote> votes;
        public ArrayList<TLRPC.User> users;
        public String next_offset;
        public byte[] option;
        public boolean collapsed;
        public int collapsedCount = 10;

        public VotesList(TLRPC.TL_messages_votesList votesList, byte[] o) {
            count = votesList.count;
            votes = votesList.votes;
            users = votesList.users;
            next_offset = votesList.next_offset;
            option = o;
        }

        public int getCount() {
            if (collapsed) {
                return Math.min(collapsedCount, votes.size());
            }
            return votes.size();
        }

        public int getCollapsed() {
            if (votes.size() <= 15) {
                return 0;
            } else if (collapsed) {
                return 1;
            } else {
                return 2;
            }
        }
    }

    public class SectionCell extends FrameLayout {

        private TextView textView;
        private TextView middleTextView;
        private TextView righTextView;

        public SectionCell(Context context) {
            super(context);

            setBackgroundColor(Theme.getColor(Theme.key_graySection));

            textView = new TextView(getContext());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            textView.setTextColor(Theme.getColor(Theme.key_graySectionText));
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);

            middleTextView = new TextView(getContext());
            middleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            middleTextView.setTextColor(Theme.getColor(Theme.key_graySectionText));
            middleTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);

            righTextView = new TextView(getContext()) {
                @Override
                public boolean post(Runnable action) {
                    return containerView.post(action);
                }

                @Override
                public boolean postDelayed(Runnable action, long delayMillis) {
                    return containerView.postDelayed(action, delayMillis);
                }
            };
            righTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            righTextView.setTextColor(Theme.getColor(Theme.key_graySectionText));
            righTextView.setGravity((LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL);
            righTextView.setOnClickListener(v -> onCollapseClick());

            addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 0 : 16), 0, (LocaleController.isRTL ? 16 : 0), 0));
            addView(middleTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 0, 0, 0, 0));
            addView(righTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, 16, 0, 16, 0));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(32), MeasureSpec.EXACTLY);
            measureChildWithMargins(middleTextView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            measureChildWithMargins(righTextView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            measureChildWithMargins(textView, widthMeasureSpec, middleTextView.getMeasuredWidth() + righTextView.getMeasuredWidth() + AndroidUtilities.dp(32), heightMeasureSpec, 0);

            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(32));
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            if (LocaleController.isRTL) {
                int l = textView.getLeft() - middleTextView.getMeasuredWidth();
                middleTextView.layout(l, middleTextView.getTop(), l + middleTextView.getMeasuredWidth(), middleTextView.getBottom());
            } else {
                int l = textView.getRight();
                middleTextView.layout(l, middleTextView.getTop(), l + middleTextView.getMeasuredWidth(), middleTextView.getBottom());
            }
        }

        protected void onCollapseClick() {

        }

        public void setText(String left, int percent, int votesCount, int collapsed) {
            textView.setText(Emoji.replaceEmoji(left, textView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(14), false));
            String p = String.format("%d", percent);
            SpannableStringBuilder builder;
            if (LocaleController.isRTL) {
                builder = new SpannableStringBuilder(String.format("%s%% – ", percent));
            } else {
                builder = new SpannableStringBuilder(String.format(" – %s%%", percent));
            }
            builder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf")), 3, 3 + p.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            middleTextView.setText(builder);
            if (collapsed == 0) {
                if (poll.quiz) {
                    righTextView.setText(LocaleController.formatPluralString("Answer", votesCount));
                } else {
                    righTextView.setText(LocaleController.formatPluralString("Vote", votesCount));
                }
            } else if (collapsed == 1) {
                righTextView.setText(LocaleController.getString("PollExpand", R.string.PollExpand));
            } else {
                righTextView.setText(LocaleController.getString("PollCollapse", R.string.PollCollapse));
            }
        }
    }

    public static final Property<UserCell, Float> USER_CELL_PROPERTY = new AnimationProperties.FloatProperty<UserCell>("placeholderAlpha") {
        @Override
        public void setValue(UserCell object, float value) {
            object.setPlaceholderAlpha(value);
        }

        @Override
        public Float get(UserCell object) {
            return object.getPlaceholderAlpha();
        }
    };

    public class UserCell extends FrameLayout {

        private BackupImageView avatarImageView;
        private SimpleTextView nameTextView;

        private AvatarDrawable avatarDrawable;
        private TLRPC.User currentUser;

        private String lastName;
        private int lastStatus;
        private TLRPC.FileLocation lastAvatar;

        private int currentAccount = UserConfig.selectedAccount;

        private boolean needDivider;
        private int placeholderNum;
        private boolean drawPlaceholder;
        private float placeholderAlpha = 1.0f;

        private ArrayList<Animator> animators;

        public UserCell(Context context) {
            super(context);

            setWillNotDraw(false);

            avatarDrawable = new AvatarDrawable();

            avatarImageView = new BackupImageView(context);
            avatarImageView.setRoundRadius(AndroidUtilities.dp(18));
            addView(avatarImageView, LayoutHelper.createFrame(36, 36, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 0 : 14, 6, LocaleController.isRTL ? 14 : 0, 0));

            nameTextView = new SimpleTextView(context);
            nameTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            nameTextView.setTextSize(16);
            nameTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
            addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 28 : 65, 14, LocaleController.isRTL ? 65 : 28, 0));
        }

        public void setData(TLRPC.User user, int num, boolean divider) {
            currentUser = user;
            needDivider = divider;
            drawPlaceholder = user == null;
            placeholderNum = num;
            if (user == null) {
                nameTextView.setText("");
                avatarImageView.setImageDrawable(null);
            } else {
                update(0);
            }
            if (animators != null) {
                animators.add(ObjectAnimator.ofFloat(avatarImageView, View.ALPHA, 0.0f, 1.0f));
                animators.add(ObjectAnimator.ofFloat(nameTextView, View.ALPHA, 0.0f, 1.0f));
                animators.add(ObjectAnimator.ofFloat(this, USER_CELL_PROPERTY, 1.0f, 0.0f));
            } else if (!drawPlaceholder) {
                placeholderAlpha = 0.0f;
            }
        }

        @Keep
        public void setPlaceholderAlpha(float value) {
            placeholderAlpha = value;
            invalidate();
        }

        @Keep
        public float getPlaceholderAlpha() {
            return placeholderAlpha;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY));
        }

        public void update(int mask) {
            TLRPC.FileLocation photo = null;
            String newName = null;
            if (currentUser != null && currentUser.photo != null) {
                photo = currentUser.photo.photo_small;
            }

            if (mask != 0) {
                boolean continueUpdate = false;
                if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0) {
                    if (lastAvatar != null && photo == null || lastAvatar == null && photo != null || lastAvatar != null && photo != null && (lastAvatar.volume_id != photo.volume_id || lastAvatar.local_id != photo.local_id)) {
                        continueUpdate = true;
                    }
                }
                if (currentUser != null && !continueUpdate && (mask & MessagesController.UPDATE_MASK_STATUS) != 0) {
                    int newStatus = 0;
                    if (currentUser.status != null) {
                        newStatus = currentUser.status.expires;
                    }
                    if (newStatus != lastStatus) {
                        continueUpdate = true;
                    }
                }
                if (!continueUpdate && lastName != null && (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
                    if (currentUser != null) {
                        newName = UserObject.getUserName(currentUser);
                    }
                    if (!newName.equals(lastName)) {
                        continueUpdate = true;
                    }
                }
                if (!continueUpdate) {
                    return;
                }
            }

            avatarDrawable.setInfo(currentUser);
            if (currentUser.status != null) {
                lastStatus = currentUser.status.expires;
            } else {
                lastStatus = 0;
            }

            if (currentUser != null) {
                lastName = newName == null ? UserObject.getUserName(currentUser) : newName;
            } else {
                lastName = "";
            }
            nameTextView.setText(lastName);

            lastAvatar = photo;
            if (currentUser != null) {
                avatarImageView.setImage(ImageLocation.getForUserOrChat(currentUser, ImageLocation.TYPE_SMALL), "50_50", ImageLocation.getForUserOrChat(currentUser, ImageLocation.TYPE_STRIPPED), "50_50", avatarDrawable, currentUser);
            } else {
                avatarImageView.setImageDrawable(avatarDrawable);
            }
        }

        @Override
        public boolean hasOverlappingRendering() {
            return false;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (drawPlaceholder || placeholderAlpha != 0) {
                placeholderPaint.setAlpha((int) (255 * placeholderAlpha));
                int cx = avatarImageView.getLeft() + avatarImageView.getMeasuredWidth() / 2;
                int cy = avatarImageView.getTop() + avatarImageView.getMeasuredHeight() / 2;
                canvas.drawCircle(cx, cy, avatarImageView.getMeasuredWidth() / 2, placeholderPaint);

                int w;

                if (placeholderNum % 2 == 0) {
                    cx = AndroidUtilities.dp(65);
                    w = AndroidUtilities.dp(48);
                } else {
                    cx = AndroidUtilities.dp(65);
                    w = AndroidUtilities.dp(60);
                }
                if (LocaleController.isRTL) {
                    cx = getMeasuredWidth() - cx - w;
                }
                rect.set(cx, cy - AndroidUtilities.dp(4), cx + w, cy + AndroidUtilities.dp(4));
                canvas.drawRoundRect(rect, AndroidUtilities.dp(4), AndroidUtilities.dp(4), placeholderPaint);

                if (placeholderNum % 2 == 0) {
                    cx = AndroidUtilities.dp(119);
                    w = AndroidUtilities.dp(60);
                } else {
                    cx = AndroidUtilities.dp(131);
                    w = AndroidUtilities.dp(80);
                }
                if (LocaleController.isRTL) {
                    cx = getMeasuredWidth() - cx - w;
                }
                rect.set(cx, cy - AndroidUtilities.dp(4), cx + w, cy + AndroidUtilities.dp(4));
                canvas.drawRoundRect(rect, AndroidUtilities.dp(4), AndroidUtilities.dp(4), placeholderPaint);
            }
            if (needDivider) {
                canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(64), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(64) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
            }
        }
    }

    public static void showForPoll(ChatActivity parentFragment, MessageObject messageObject) {
        if (parentFragment == null || parentFragment.getParentActivity() == null) {
            return;
        }
        PollVotesAlert alert = new PollVotesAlert(parentFragment, messageObject);
        parentFragment.showDialog(alert);
    }

    private static class Button {
        private float decimal;
        private int percent;
        private int votesCount;
    }

    public PollVotesAlert(ChatActivity parentFragment, MessageObject message) {
        super(parentFragment.getParentActivity(), true);
        messageObject = message;
        chatActivity = parentFragment;
        TLRPC.TL_messageMediaPoll mediaPoll = (TLRPC.TL_messageMediaPoll) messageObject.messageOwner.media;
        poll = mediaPoll.poll;
        Context context = parentFragment.getParentActivity();

        TLRPC.Chat chat = parentFragment.getCurrentChat();
        TLRPC.User user = parentFragment.getCurrentUser();
        if (ChatObject.isChannel(chat)) {
            peer = new TLRPC.TL_inputPeerChannel();
            peer.channel_id = chat.id;
            peer.access_hash = chat.access_hash;
        } else if (chat != null) {
            peer = new TLRPC.TL_inputPeerChat();
            peer.chat_id = chat.id;
        } else {
            peer = new TLRPC.TL_inputPeerUser();
            peer.user_id = user.id;
            peer.access_hash = user.access_hash;
        }

        ArrayList<VotesList> loadedVoters = new ArrayList<>();
        int count = mediaPoll.results.results.size();
        Integer[] reqIds = new Integer[count];

        for (int a = 0; a < count; a++) {
            TLRPC.TL_pollAnswerVoters answerVoters = mediaPoll.results.results.get(a);
            if (answerVoters.voters == 0) {
                continue;
            }
            TLRPC.TL_messages_votesList votesList = new TLRPC.TL_messages_votesList();
            int N = answerVoters.voters <= 15 ? answerVoters.voters : 10;
            for (int b = 0; b < N; b++) {
                votesList.votes.add(new TLRPC.TL_messageUserVoteInputOption());
            }
            votesList.next_offset = N < answerVoters.voters ? "empty" : null;
            votesList.count = answerVoters.voters;
            VotesList list = new VotesList(votesList, answerVoters.option);
            voters.add(list);

            TLRPC.TL_messages_getPollVotes req = new TLRPC.TL_messages_getPollVotes();
            req.peer = peer;
            req.id = messageObject.getId();
            req.limit = answerVoters.voters <= 15 ? 15 : 10;
            req.flags |= 1;
            req.option = answerVoters.option;
            int num = a;
            reqIds[a] = parentFragment.getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                queries.remove(reqIds[num]);
                if (response != null) {
                    TLRPC.TL_messages_votesList res = (TLRPC.TL_messages_votesList) response;
                    parentFragment.getMessagesController().putUsers(res.users, false);
                    if (!res.votes.isEmpty()) {
                        loadedVoters.add(new VotesList(res, answerVoters.option));
                    }
                    if (queries.isEmpty()) {
                        boolean countChanged = false;
                        for (int b = 0, N2 = loadedVoters.size(); b < N2; b++) {
                            VotesList votesList1 = loadedVoters.get(b);
                            for (int c = 0, N3 = voters.size(); c < N3; c++) {
                                VotesList votesList2 = voters.get(c);
                                if (Arrays.equals(votesList1.option, votesList2.option)) {
                                    votesList2.next_offset = votesList1.next_offset;
                                    if (votesList2.count != votesList1.count || votesList2.votes.size() != votesList1.votes.size()) {
                                        countChanged = true;
                                    }
                                    votesList2.count = votesList1.count;
                                    votesList2.users = votesList1.users;
                                    votesList2.votes = votesList1.votes;
                                    break;
                                }
                            }
                        }
                        loadingResults = false;
                        if (listView != null) {
                            if (currentSheetAnimationType != 0 || startAnimationRunnable != null || countChanged) {
                                if (countChanged) {
                                    updateButtons();
                                }
                                listAdapter.notifyDataSetChanged();
                            } else {
                                int c = listView.getChildCount();
                                ArrayList<Animator> animators = new ArrayList<>();
                                for (int b = 0; b < c; b++) {
                                    View child = listView.getChildAt(b);
                                    if (!(child instanceof UserCell)) {
                                        continue;
                                    }
                                    RecyclerView.ViewHolder holder = listView.findContainingViewHolder(child);
                                    if (holder == null) {
                                        continue;
                                    }
                                    UserCell cell = (UserCell) child;
                                    cell.animators = animators;
                                    cell.setEnabled(true);
                                    listAdapter.onViewAttachedToWindow(holder);
                                    cell.animators = null;
                                }
                                if (!animators.isEmpty()) {
                                    AnimatorSet animatorSet = new AnimatorSet();
                                    animatorSet.playTogether(animators);
                                    animatorSet.setDuration(180);
                                    animatorSet.start();
                                }
                                loadingResults = false;
                            }
                        }
                    }
                } else {
                    dismiss();
                }
            }));
            queries.add(reqIds[a]);
        }
        updateButtons();

        Collections.sort(voters, new Comparator<VotesList>() {
            private int getIndex(VotesList votesList) {
                for (int a = 0, N = poll.answers.size(); a < N; a++) {
                    TLRPC.TL_pollAnswer answer = poll.answers.get(a);
                    if (Arrays.equals(answer.option, votesList.option)) {
                        return a;
                    }
                }
                return 0;
            }

            @Override
            public int compare(VotesList o1, VotesList o2) {
                int i1 = getIndex(o1);
                int i2 = getIndex(o2);
                if (i1 > i2) {
                    return 1;
                } else if (i1 < i2) {
                    return -1;
                }
                return 0;
            }
        });

        updatePlaceholder();

        shadowDrawable = context.getResources().getDrawable(R.drawable.sheet_shadow_round).mutate();
        shadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogBackground), PorterDuff.Mode.MULTIPLY));

        containerView = new FrameLayout(context) {

            private boolean ignoreLayout = false;
            private RectF rect = new RectF();

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int totalHeight = MeasureSpec.getSize(heightMeasureSpec);
                if (Build.VERSION.SDK_INT >= 21 && !isFullscreen) {
                    ignoreLayout = true;
                    setPadding(backgroundPaddingLeft, AndroidUtilities.statusBarHeight, backgroundPaddingLeft, 0);
                    ignoreLayout = false;
                }
                int availableHeight = totalHeight - getPaddingTop();

                LayoutParams layoutParams = (LayoutParams) listView.getLayoutParams();
                layoutParams.topMargin = ActionBar.getCurrentActionBarHeight();

                layoutParams = (LayoutParams) actionBarShadow.getLayoutParams();
                layoutParams.topMargin = ActionBar.getCurrentActionBarHeight();

                int contentSize = backgroundPaddingTop + AndroidUtilities.dp(15) + AndroidUtilities.statusBarHeight;
                int sectionCount = listAdapter.getSectionCount();
                for (int a = 0; a < sectionCount; a++) {
                    if (a == 0) {
                        titleTextView.measure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec - backgroundPaddingLeft * 2), MeasureSpec.EXACTLY), heightMeasureSpec);
                        contentSize += titleTextView.getMeasuredHeight();
                    } else {
                        int count = listAdapter.getCountForSection(a);
                        contentSize += AndroidUtilities.dp(32) + AndroidUtilities.dp(50) * (count - 1);
                    }
                }
                int padding = (contentSize < availableHeight ? availableHeight - contentSize : availableHeight - (availableHeight / 5 * 3)) + AndroidUtilities.dp(8);
                if (listView.getPaddingTop() != padding) {
                    ignoreLayout = true;
                    listView.setPinnedSectionOffsetY(-padding);
                    listView.setPadding(0, padding, 0, 0);
                    ignoreLayout = false;
                }
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY));
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                super.onLayout(changed, l, t, r, b);
                updateLayout(false);
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN && scrollOffsetY != 0 && ev.getY() < scrollOffsetY + AndroidUtilities.dp(12) && actionBar.getAlpha() == 0.0f) {
                    dismiss();
                    return true;
                }
                return super.onInterceptTouchEvent(ev);
            }

            @Override
            public boolean onTouchEvent(MotionEvent e) {
                return !isDismissed() && super.onTouchEvent(e);
            }

            @Override
            public void requestLayout() {
                if (ignoreLayout) {
                    return;
                }
                super.requestLayout();
            }

            @Override
            protected void onDraw(Canvas canvas) {
                int offset = AndroidUtilities.dp(13);
                int top = scrollOffsetY - backgroundPaddingTop - offset;
                if (currentSheetAnimationType == 1) {
                    top += listView.getTranslationY();
                }
                int y = top + AndroidUtilities.dp(20);

                int height = getMeasuredHeight() + AndroidUtilities.dp(15) + backgroundPaddingTop;
                float rad = 1.0f;

                if (top + backgroundPaddingTop < ActionBar.getCurrentActionBarHeight()) {
                    float toMove = offset + AndroidUtilities.dp(11 - 7);
                    float moveProgress = Math.min(1.0f, (ActionBar.getCurrentActionBarHeight() - top - backgroundPaddingTop) / toMove);
                    float availableToMove = ActionBar.getCurrentActionBarHeight() - toMove;

                    int diff = (int) (availableToMove * moveProgress);
                    top -= diff;
                    y -= diff;
                    height += diff;
                    rad = 1.0f - moveProgress;
                }

                if (Build.VERSION.SDK_INT >= 21) {
                    top += AndroidUtilities.statusBarHeight;
                    y += AndroidUtilities.statusBarHeight;
                }

                shadowDrawable.setBounds(0, top, getMeasuredWidth(), height);
                shadowDrawable.draw(canvas);

                if (rad != 1.0f) {
                    Theme.dialogs_onlineCirclePaint.setColor(Theme.getColor(Theme.key_dialogBackground));
                    rect.set(backgroundPaddingLeft, backgroundPaddingTop + top, getMeasuredWidth() - backgroundPaddingLeft, backgroundPaddingTop + top + AndroidUtilities.dp(24));
                    canvas.drawRoundRect(rect, AndroidUtilities.dp(12) * rad, AndroidUtilities.dp(12) * rad, Theme.dialogs_onlineCirclePaint);
                }

                if (rad != 0) {
                    float alphaProgress = 1.0f;
                    int w = AndroidUtilities.dp(36);
                    rect.set((getMeasuredWidth() - w) / 2, y, (getMeasuredWidth() + w) / 2, y + AndroidUtilities.dp(4));
                    int color = Theme.getColor(Theme.key_sheet_scrollUp);
                    int alpha = Color.alpha(color);
                    Theme.dialogs_onlineCirclePaint.setColor(color);
                    Theme.dialogs_onlineCirclePaint.setAlpha((int) (alpha * alphaProgress * rad));
                    canvas.drawRoundRect(rect, AndroidUtilities.dp(2), AndroidUtilities.dp(2), Theme.dialogs_onlineCirclePaint);
                }

                int color1 = Theme.getColor(Theme.key_dialogBackground);
                int finalColor = Color.argb((int) (255 * actionBar.getAlpha()), (int) (Color.red(color1) * 0.8f), (int) (Color.green(color1) * 0.8f), (int) (Color.blue(color1) * 0.8f));
                Theme.dialogs_onlineCirclePaint.setColor(finalColor);
                canvas.drawRect(backgroundPaddingLeft, 0, getMeasuredWidth() - backgroundPaddingLeft, AndroidUtilities.statusBarHeight, Theme.dialogs_onlineCirclePaint);
            }
        };
        containerView.setWillNotDraw(false);
        containerView.setPadding(backgroundPaddingLeft, 0, backgroundPaddingLeft, 0);

        listView = new RecyclerListView(context) {

            long lastUpdateTime;

            @Override
            protected boolean allowSelectChildAtPosition(float x, float y) {
                return y >= scrollOffsetY + (Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0);
            }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                if (loadingResults) {
                    long newUpdateTime = SystemClock.elapsedRealtime();
                    long dt = Math.abs(lastUpdateTime - newUpdateTime);
                    if (dt > 17) {
                        dt = 16;
                    }
                    lastUpdateTime = newUpdateTime;
                    totalTranslation += dt * gradientWidth / 1800.0f;
                    while (totalTranslation >= gradientWidth * 2) {
                        totalTranslation -= gradientWidth * 2;
                    }
                    placeholderMatrix.setTranslate(totalTranslation, 0);
                    placeholderGradient.setLocalMatrix(placeholderMatrix);
                    invalidateViews();
                    invalidate();
                }
                super.dispatchDraw(canvas);
            }
        };
        listView.setClipToPadding(false);
        listView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        listView.setHorizontalScrollBarEnabled(false);
        listView.setVerticalScrollBarEnabled(false);
        listView.setSectionsType(2);
        containerView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter = new Adapter(context));
        listView.setGlowColor(Theme.getColor(Theme.key_dialogScrollGlow));
        listView.setOnItemClickListener((view, position) -> {
            if (parentFragment == null || parentFragment.getParentActivity() == null || queries != null && !queries.isEmpty()) {
                return;
            }
            if (view instanceof TextCell) {
                int section = listAdapter.getSectionForPosition(position) - 1;
                int row = listAdapter.getPositionInSectionForPosition(position) - 1;
                if (row <= 0 || section < 0) {
                    return;
                }
                VotesList votesList = voters.get(section);
                if (row != votesList.getCount() || loadingMore.contains(votesList)) {
                    return;
                }
                if (votesList.collapsed && votesList.collapsedCount < votesList.votes.size()) {
                    votesList.collapsedCount = Math.min(votesList.collapsedCount + 50, votesList.votes.size());
                    if (votesList.collapsedCount == votesList.votes.size()) {
                        votesList.collapsed = false;
                    }
                    listAdapter.notifyDataSetChanged();
                    return;
                }
                loadingMore.add(votesList);
                TLRPC.TL_messages_getPollVotes req = new TLRPC.TL_messages_getPollVotes();
                req.peer = peer;
                req.id = messageObject.getId();
                req.limit = 50;
                req.flags |= 1;
                req.option = votesList.option;
                req.flags |= 2;
                req.offset = votesList.next_offset;
                chatActivity.getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                    if (!isShowing()) {
                        return;
                    }
                    loadingMore.remove(votesList);
                    if (response != null) {
                        TLRPC.TL_messages_votesList res = (TLRPC.TL_messages_votesList) response;
                        parentFragment.getMessagesController().putUsers(res.users, false);
                        votesList.votes.addAll(res.votes);
                        votesList.next_offset = res.next_offset;
                        listAdapter.notifyDataSetChanged();
                    }
                }));
            } else if (view instanceof UserCell) {
                UserCell userCell = (UserCell) view;
                if (userCell.currentUser == null) {
                    return;
                }
                TLRPC.User currentUser = parentFragment.getCurrentUser();
                Bundle args = new Bundle();
                args.putInt("user_id", userCell.currentUser.id);
                dismiss();
                ProfileActivity fragment = new ProfileActivity(args);
                fragment.setPlayProfileAnimation(currentUser != null && currentUser.id == userCell.currentUser.id ? 1 : 0);
                parentFragment.presentFragment(fragment);
            }
        });
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (listView.getChildCount() <= 0) {
                    return;
                }
                updateLayout(true);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int offset = AndroidUtilities.dp(13);
                    int top = scrollOffsetY - backgroundPaddingTop - offset;
                    if (top + backgroundPaddingTop < ActionBar.getCurrentActionBarHeight() && listView.canScrollVertically(1)) {
                        View child = listView.getChildAt(0);
                        RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findViewHolderForAdapterPosition(0);
                        if (holder != null && holder.itemView.getTop() > AndroidUtilities.dp(7)) {
                            listView.smoothScrollBy(0, holder.itemView.getTop() - AndroidUtilities.dp(7));
                        }
                    }
                }
            }
        });

        titleTextView = new TextView(context);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        titleTextView.setPadding(AndroidUtilities.dp(21), AndroidUtilities.dp(5), AndroidUtilities.dp(14), AndroidUtilities.dp(21));
        titleTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleTextView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        titleTextView.setText(Emoji.replaceEmoji(poll.question, titleTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(18), false));

        actionBar = new ActionBar(context) {
            @Override
            public void setAlpha(float alpha) {
                super.setAlpha(alpha);
                containerView.invalidate();
            }
        };
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setItemsColor(Theme.getColor(Theme.key_dialogTextBlack), false);
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_dialogButtonSelector), false);
        actionBar.setTitleColor(Theme.getColor(Theme.key_dialogTextBlack));
        actionBar.setSubtitleColor(Theme.getColor(Theme.key_player_actionBarSubtitle));
        actionBar.setOccupyStatusBar(false);
        actionBar.setAlpha(0.0f);
        actionBar.setTitle(LocaleController.getString("PollResults", R.string.PollResults));
        if (poll.quiz) {
            actionBar.setSubtitle(LocaleController.formatPluralString("Answer", mediaPoll.results.total_voters));
        } else {
            actionBar.setSubtitle(LocaleController.formatPluralString("Vote", mediaPoll.results.total_voters));
        }
        containerView.addView(actionBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    dismiss();
                }
            }
        });

        actionBarShadow = new View(context);
        actionBarShadow.setAlpha(0.0f);
        actionBarShadow.setBackgroundColor(Theme.getColor(Theme.key_dialogShadowLine));
        containerView.addView(actionBarShadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1));
    }

    private int getCurrentTop() {
        if (listView.getChildCount() != 0) {
            View child = listView.getChildAt(0);
            RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findContainingViewHolder(child);
            if (holder != null) {
                return listView.getPaddingTop() - (holder.getAdapterPosition() == 0 && child.getTop() >= 0 ? child.getTop() : 0);
            }
        }
        return -1000;
    }

    private void updateButtons() {
        votesPercents.clear();
        int restPercent = 100;
        boolean hasDifferent = false;
        int previousPercent = 0;
        TLRPC.TL_messageMediaPoll media = (TLRPC.TL_messageMediaPoll) messageObject.messageOwner.media;
        ArrayList<Button> sortedPollButtons = new ArrayList<>();
        int maxVote = 0;

        for (int a = 0, N = voters.size(); a < N; a++) {
            VotesList list = voters.get(a);
            Button button = new Button();
            sortedPollButtons.add(button);
            votesPercents.put(list, button);
            if (!media.results.results.isEmpty()) {
                for (int b = 0, N2 = media.results.results.size(); b < N2; b++) {
                    TLRPC.TL_pollAnswerVoters answer = media.results.results.get(b);
                    if (Arrays.equals(list.option, answer.option)) {
                        button.votesCount = answer.voters;
                        button.decimal = 100 * (answer.voters / (float) media.results.total_voters);
                        button.percent = (int) button.decimal;
                        button.decimal -= button.percent;

                        if (previousPercent == 0) {
                            previousPercent = button.percent;
                        } else if (button.percent != 0 && previousPercent != button.percent) {
                            hasDifferent = true;
                        }
                        restPercent -= button.percent;
                        maxVote = Math.max(button.percent, maxVote);
                        break;
                    }
                }
            }
        }

        if (hasDifferent && restPercent != 0) {
            Collections.sort(sortedPollButtons, (o1, o2) -> {
                if (o1.decimal > o2.decimal) {
                    return -1;
                } else if (o1.decimal < o2.decimal) {
                    return 1;
                }
                return 0;
            });
            for (int a = 0, N = Math.min(restPercent, sortedPollButtons.size()); a < N; a++) {
                sortedPollButtons.get(a).percent += 1;
            }
        }
    }

    @Override
    protected boolean canDismissWithSwipe() {
        return false;
    }

    @Override
    public void dismissInternal() {
        for (int a = 0, N = queries.size(); a < N; a++) {
            chatActivity.getConnectionsManager().cancelRequest(queries.get(a), true);
        }
        super.dismissInternal();
    }

    @SuppressLint("NewApi")
    private void updateLayout(boolean animated) {
        if (listView.getChildCount() <= 0) {
            listView.setTopGlowOffset(scrollOffsetY = listView.getPaddingTop());
            containerView.invalidate();
            return;
        }
        View child = listView.getChildAt(0);
        RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findContainingViewHolder(child);
        int top = child.getTop();
        int newOffset = AndroidUtilities.dp(7);
        if (top >= AndroidUtilities.dp(7) && holder != null && holder.getAdapterPosition() == 0) {
            newOffset = top;
        }
        boolean show = newOffset <= AndroidUtilities.dp(12);
        if (show && actionBar.getTag() == null || !show && actionBar.getTag() != null) {
            actionBar.setTag(show ? 1 : null);
            if (actionBarAnimation != null) {
                actionBarAnimation.cancel();
                actionBarAnimation = null;
            }
            actionBarAnimation = new AnimatorSet();
            actionBarAnimation.setDuration(180);
            actionBarAnimation.playTogether(
                    ObjectAnimator.ofFloat(actionBar, View.ALPHA, show ? 1.0f : 0.0f),
                    ObjectAnimator.ofFloat(actionBarShadow, View.ALPHA, show ? 1.0f : 0.0f));
            actionBarAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    actionBarAnimation = null;
                }
            });
            actionBarAnimation.start();
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
        newOffset += layoutParams.topMargin - AndroidUtilities.dp(11);
        if (scrollOffsetY != newOffset) {
            listView.setTopGlowOffset((scrollOffsetY = newOffset) - layoutParams.topMargin);
            containerView.invalidate();
        }
    }

    private void updatePlaceholder() {
        if (placeholderPaint == null) {
            return;
        }
        int color0 = Theme.getColor(Theme.key_dialogBackground);
        int color1 = Theme.getColor(Theme.key_dialogBackgroundGray);
        color0 = AndroidUtilities.getAverageColor(color1, color0);
        placeholderPaint.setColor(color1);
        placeholderGradient = new LinearGradient(0, 0, gradientWidth = AndroidUtilities.dp(500), 0, new int[]{color1, color0, color1}, new float[]{0.0f, 0.18f, 0.36f}, Shader.TileMode.REPEAT);
        placeholderPaint.setShader(placeholderGradient);
        placeholderMatrix = new Matrix();
        placeholderGradient.setLocalMatrix(placeholderMatrix);
    }

    public class Adapter extends RecyclerListView.SectionsAdapter {

        private int currentAccount = UserConfig.selectedAccount;
        private Context mContext;

        public Adapter(Context context) {
            mContext = context;
        }

        public Object getItem(int section, int position) {
            return null;
        }

        @Override
        public boolean isEnabled(int section, int row) {
            return section != 0 && row != 0 && (queries == null || queries.isEmpty());
        }

        @Override
        public int getSectionCount() {
            return voters.size() + 1;
        }

        @Override
        public int getCountForSection(int section) {
            if (section == 0) {
                return 1;
            }
            section--;
            VotesList votesList = voters.get(section);
            return votesList.getCount() + 1 + (TextUtils.isEmpty(votesList.next_offset) && !votesList.collapsed ? 0 : 1);
        }

        private SectionCell createSectionCell() {
            return new SectionCell(mContext) {
                @Override
                protected void onCollapseClick() {
                    VotesList list = (VotesList) getTag(R.id.object_tag);
                    if (list.votes.size() <= 15) {
                        return;
                    }
                    list.collapsed = !list.collapsed;
                    if (list.collapsed) {
                        list.collapsedCount = 10;
                    }
                    listAdapter.notifyDataSetChanged();
                }
            };
        }

        @Override
        public View getSectionHeaderView(int section, View view) {
            if (view == null) {
                view = createSectionCell();
            }
            SectionCell sectionCell = (SectionCell) view;
            if (section == 0) {
                sectionCell.setAlpha(0.0f);
            } else {
                section -= 1;
                view.setAlpha(1.0f);
                VotesList votesList = voters.get(section);
                TLRPC.MessageUserVote vote = votesList.votes.get(0);
                for (int a = 0, N = poll.answers.size(); a < N; a++) {
                    TLRPC.TL_pollAnswer answer = poll.answers.get(a);
                    if (Arrays.equals(answer.option, votesList.option)) {
                        Button button = votesPercents.get(votesList);
                        sectionCell.setText(answer.text, button.percent, button.votesCount, votesList.getCollapsed());
                        sectionCell.setTag(R.id.object_tag, votesList);
                        break;
                    }
                }
            }
            return view;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0: {
                    view = new UserCell(mContext);
                    break;
                }
                case 1: {
                    if (titleTextView.getParent() != null) {
                        ViewGroup p = (ViewGroup) titleTextView.getParent();
                        p.removeView(titleTextView);
                    }
                    view = titleTextView;
                    break;
                }
                case 2: {
                    view = createSectionCell();
                    break;
                }
                case 3:
                default: {
                    TextCell textCell = new TextCell(mContext, 23, true);
                    textCell.setOffsetFromImage(65);
                    textCell.setColors(Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhiteBlueText4);
                    view = textCell;
                    break;
                }
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(int section, int position, RecyclerView.ViewHolder holder) {
            switch (holder.getItemViewType()) {
                case 2: {
                    SectionCell sectionCell = (SectionCell) holder.itemView;
                    section--;
                    VotesList votesList = voters.get(section);
                    TLRPC.MessageUserVote vote = votesList.votes.get(0);
                    for (int a = 0, N = poll.answers.size(); a < N; a++) {
                        TLRPC.TL_pollAnswer answer = poll.answers.get(a);
                        if (Arrays.equals(answer.option, votesList.option)) {
                            Button button = votesPercents.get(votesList);
                            sectionCell.setText(answer.text, button.percent, button.votesCount, votesList.getCollapsed());
                            sectionCell.setTag(R.id.object_tag, votesList);
                            break;
                        }
                    }
                    break;
                }
                case 3: {
                    TextCell textCell = (TextCell) holder.itemView;
                    section--;
                    VotesList votesList = voters.get(section);
                    textCell.setTextAndIcon(LocaleController.formatPluralString("ShowVotes", votesList.count - votesList.getCount()), R.drawable.arrow_more, false);
                    break;
                }
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            if (holder.getItemViewType() == 0) {
                int position = holder.getAdapterPosition();
                int section = getSectionForPosition(position);
                position = getPositionInSectionForPosition(position);

                section--;
                position--;
                UserCell userCell = (UserCell) holder.itemView;
                VotesList votesList = voters.get(section);
                TLRPC.MessageUserVote vote = votesList.votes.get(position);
                TLRPC.User user;
                if (vote.user_id != 0) {
                    user = chatActivity.getMessagesController().getUser(vote.user_id);
                } else {
                    user = null;
                }
                userCell.setData(user, position, position != votesList.getCount() - 1 || !TextUtils.isEmpty(votesList.next_offset) || votesList.collapsed);
            }
        }

        @Override
        public int getItemViewType(int section, int position) {
            if (section == 0) {
                return 1;
            }
            if (position == 0) {
                return 2;
            }
            position--;
            section--;
            VotesList votesList = voters.get(section);
            if (position < votesList.getCount()) {
                return 0;
            }
            return 3;
        }

        @Override
        public String getLetter(int position) {
            return null;
        }

        @Override
        public int getPositionForScrollProgress(float progress) {
            return 0;
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        ThemeDescription.ThemeDescriptionDelegate delegate = this::updatePlaceholder;

        themeDescriptions.add(new ThemeDescription(containerView, 0, null, null, null, null, Theme.key_sheet_scrollUp));
        themeDescriptions.add(new ThemeDescription(containerView, 0, null, null, new Drawable[]{shadowDrawable}, null, Theme.key_dialogBackground));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_dialogBackground));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_dialogScrollGlow));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_dialogTextBlack));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_dialogTextBlack));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBTITLECOLOR, null, null, null, null, Theme.key_player_actionBarSubtitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_dialogTextBlack));

        themeDescriptions.add(new ThemeDescription(titleTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_dialogTextBlack));

        themeDescriptions.add(new ThemeDescription(actionBarShadow, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_dialogShadowLine));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, null, null, null, delegate, Theme.key_dialogBackground));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, null, null, null, delegate, Theme.key_dialogBackgroundGray));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SECTIONS, new Class[]{SectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_graySectionText));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SECTIONS, new Class[]{SectionCell.class}, new String[]{"middleTextView"}, null, null, null, Theme.key_graySectionText));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SECTIONS, new Class[]{SectionCell.class}, new String[]{"righTextView"}, null, null, null, Theme.key_graySectionText));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR | ThemeDescription.FLAG_SECTIONS, new Class[]{SectionCell.class}, null, null, null, Theme.key_graySection));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_dialogTextBlack));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueText4));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_switchTrackChecked));

        return themeDescriptions;
    }
}
