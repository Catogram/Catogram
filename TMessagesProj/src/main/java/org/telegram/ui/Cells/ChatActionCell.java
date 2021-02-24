/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.Layout;
import android.text.Spannable;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.accessibility.AccessibilityNodeInfo;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.PhotoViewer;

public class ChatActionCell extends BaseCell implements DownloadController.FileDownloadProgressListener {

    public interface ChatActionCellDelegate {
        default void didClickImage(ChatActionCell cell) {
        }

        default void didLongPress(ChatActionCell cell, float x, float y) {
        }

        default void needOpenUserProfile(int uid) {
        }

        default void didPressBotButton(MessageObject messageObject, TLRPC.KeyboardButton button) {
        }

        default void didPressReplyMessage(ChatActionCell cell, int id) {
        }
    }

    private int TAG;

    private URLSpan pressedLink;
    private int currentAccount = UserConfig.selectedAccount;
    private ImageReceiver imageReceiver;
    private AvatarDrawable avatarDrawable;
    private StaticLayout textLayout;
    private int textWidth;
    private int textHeight;
    private int textX;
    private int textY;
    private int textXLeft;
    private int previousWidth;
    private boolean imagePressed;

    private ImageLocation currentVideoLocation;

    private float lastTouchX;
    private float lastTouchY;

    private boolean wasLayout;

    private boolean hasReplyMessage;

    private MessageObject currentMessageObject;
    private int customDate;
    private CharSequence customText;

    private String overrideBackground;
    private String overrideText;
    private ColorFilter overrideColorFilter;
    private int overrideColor;

    private ChatActionCellDelegate delegate;

    public ChatActionCell(Context context) {
        super(context);
        imageReceiver = new ImageReceiver(this);
        imageReceiver.setRoundRadius(AndroidUtilities.roundMessageSize / 2);
        avatarDrawable = new AvatarDrawable();
        TAG = DownloadController.getInstance(currentAccount).generateObserverTag();
    }

    public void setDelegate(ChatActionCellDelegate delegate) {
        this.delegate = delegate;
    }

    public void setCustomDate(int date, boolean scheduled, boolean inLayout) {
        if (customDate == date) {
            return;
        }
        CharSequence newText;
        if (scheduled) {
            if (date == 0x7ffffffe) {
                newText = LocaleController.getString("MessageScheduledUntilOnline", R.string.MessageScheduledUntilOnline);
            } else {
                newText = LocaleController.formatString("MessageScheduledOn", R.string.MessageScheduledOn, LocaleController.formatDateChat(date));
            }
        } else {
            newText = LocaleController.formatDateChat(date);
        }
        if (customText != null && TextUtils.equals(newText, customText)) {
            return;
        }
        customDate = date;
        customText = newText;
        updateTextInternal(inLayout);
    }

    private void updateTextInternal(boolean inLayout) {
        if (getMeasuredWidth() != 0) {
            createLayout(customText, getMeasuredWidth());
            invalidate();
        }
        if (!wasLayout) {
            if (inLayout) {
                AndroidUtilities.runOnUIThread(this::requestLayout);
            } else {
                requestLayout();
            }
        } else {
            buildLayout();
        }
    }

    public void setCustomText(CharSequence text) {
        customText = text;
        if (customText != null) {
            updateTextInternal(false);
        }
    }

    public void setOverrideColor(String background, String text) {
        overrideBackground = background;
        overrideText = text;
    }

    public void setMessageObject(MessageObject messageObject) {
        if (currentMessageObject == messageObject && (textLayout == null || TextUtils.equals(textLayout.getText(), messageObject.messageText)) && (hasReplyMessage || messageObject.replyMessageObject == null)) {
            return;
        }
        currentMessageObject = messageObject;
        hasReplyMessage = messageObject.replyMessageObject != null;
        DownloadController.getInstance(currentAccount).removeLoadingFileObserver(this);
        previousWidth = 0;
        if (currentMessageObject.type == 11) {
            int id = (int) messageObject.getDialogId();
            avatarDrawable.setInfo(id, null, null);
            if (currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionUserUpdatedPhoto) {
                imageReceiver.setImage(null, null, avatarDrawable, null, currentMessageObject, 0);
            } else {
                TLRPC.PhotoSize strippedPhotoSize = null;
                for (int a = 0, N = currentMessageObject.photoThumbs.size(); a < N; a++) {
                    TLRPC.PhotoSize photoSize = currentMessageObject.photoThumbs.get(a);
                    if (photoSize instanceof TLRPC.TL_photoStrippedSize) {
                        strippedPhotoSize = photoSize;
                        break;
                    }
                }
                TLRPC.PhotoSize photoSize = FileLoader.getClosestPhotoSizeWithSize(currentMessageObject.photoThumbs, 640);
                if (photoSize != null) {
                    TLRPC.Photo photo = messageObject.messageOwner.action.photo;
                    TLRPC.VideoSize videoSize = null;
                    if (!photo.video_sizes.isEmpty() && SharedConfig.autoplayGifs) {
                        videoSize = photo.video_sizes.get(0);
                        if (!messageObject.mediaExists && !DownloadController.getInstance(currentAccount).canDownloadMedia(DownloadController.AUTODOWNLOAD_TYPE_VIDEO, videoSize.size)) {
                            currentVideoLocation = ImageLocation.getForPhoto(videoSize, photo);
                            String fileName = FileLoader.getAttachFileName(videoSize);
                            DownloadController.getInstance(currentAccount).addLoadingFileObserver(fileName, currentMessageObject, this);
                            videoSize = null;
                        }
                    }
                    if (videoSize != null) {
                        imageReceiver.setImage(ImageLocation.getForPhoto(videoSize, photo), ImageLoader.AUTOPLAY_FILTER, ImageLocation.getForObject(strippedPhotoSize, currentMessageObject.photoThumbsObject), "50_50_b", avatarDrawable, 0, null, currentMessageObject, 1);
                    } else {
                        imageReceiver.setImage(ImageLocation.getForObject(photoSize, currentMessageObject.photoThumbsObject), "150_150", ImageLocation.getForObject(strippedPhotoSize, currentMessageObject.photoThumbsObject), "50_50_b", avatarDrawable, 0, null, currentMessageObject, 1);
                    }
                } else {
                    imageReceiver.setImageBitmap(avatarDrawable);
                }
            }
            imageReceiver.setVisible(!PhotoViewer.isShowingImage(currentMessageObject), false);
        } else {
            imageReceiver.setImageBitmap((Bitmap) null);
        }
        requestLayout();
    }

    public MessageObject getMessageObject() {
        return currentMessageObject;
    }

    public ImageReceiver getPhotoImage() {
        return imageReceiver;
    }

    @Override
    protected void onLongPress() {
        if (delegate != null) {
            delegate.didLongPress(this, lastTouchX, lastTouchY);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        DownloadController.getInstance(currentAccount).removeLoadingFileObserver(this);
        imageReceiver.onDetachedFromWindow();
        wasLayout = false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        imageReceiver.onAttachedToWindow();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (currentMessageObject == null) {
            return super.onTouchEvent(event);
        }
        float x = lastTouchX = event.getX();
        float y = lastTouchY = event.getY();

        boolean result = false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (delegate != null) {
                if (currentMessageObject.type == 11 && imageReceiver.isInsideImage(x, y)) {
                    imagePressed = true;
                    result = true;
                }
                if (result) {
                    startCheckLongPress();
                }
            }
        } else {
            if (event.getAction() != MotionEvent.ACTION_MOVE) {
                cancelCheckLongPress();
            }
            if (imagePressed) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    imagePressed = false;
                    if (delegate != null) {
                        delegate.didClickImage(this);
                        playSoundEffect(SoundEffectConstants.CLICK);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    imagePressed = false;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!imageReceiver.isInsideImage(x, y)) {
                        imagePressed = false;
                    }
                }
            }
        }
        if (!result) {
            if (event.getAction() == MotionEvent.ACTION_DOWN || pressedLink != null && event.getAction() == MotionEvent.ACTION_UP) {
                if (x >= textX && y >= textY && x <= textX + textWidth && y <= textY + textHeight) {
                    y -= textY;
                    x -= textXLeft;

                    final int line = textLayout.getLineForVertical((int) y);
                    final int off = textLayout.getOffsetForHorizontal(line, x);
                    final float left = textLayout.getLineLeft(line);
                    if (left <= x && left + textLayout.getLineWidth(line) >= x && currentMessageObject.messageText instanceof Spannable) {
                        Spannable buffer = (Spannable) currentMessageObject.messageText;
                        URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);

                        if (link.length != 0) {
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                pressedLink = link[0];
                                result = true;
                            } else {
                                if (link[0] == pressedLink) {
                                    if (delegate != null) {
                                        String url = link[0].getURL();
                                        if (url.startsWith("game")) {
                                            delegate.didPressReplyMessage(this, currentMessageObject.getReplyMsgId());
                                            /*TLRPC.KeyboardButton gameButton = null;
                                            MessageObject messageObject = currentMessageObject.replyMessageObject;
                                            if (messageObject != null && messageObject.messageOwner.reply_markup != null) {
                                                for (int a = 0; a < messageObject.messageOwner.reply_markup.rows.size(); a++) {
                                                    TLRPC.TL_keyboardButtonRow row = messageObject.messageOwner.reply_markup.rows.get(a);
                                                    for (int b = 0; b < row.buttons.size(); b++) {
                                                        TLRPC.KeyboardButton button = row.buttons.get(b);
                                                        if (button instanceof TLRPC.TL_keyboardButtonGame && button.game_id == currentMessageObject.messageOwner.action.game_id) {
                                                            gameButton = button;
                                                            break;
                                                        }
                                                    }
                                                    if (gameButton != null) {
                                                        break;
                                                    }
                                                }
                                            }
                                            if (gameButton != null) {
                                                delegate.didPressBotButton(messageObject, gameButton);
                                            }*/
                                        } else if (url.startsWith("http")) {
                                            Browser.openUrl(getContext(), url);
                                        } else {
                                            delegate.needOpenUserProfile(Integer.parseInt(url));
                                        }
                                    }
                                    result = true;
                                }
                            }
                        } else {
                            pressedLink = null;
                        }
                    } else {
                        pressedLink = null;
                    }
                } else {
                    pressedLink = null;
                }
            }
        }

        if (!result) {
            result = super.onTouchEvent(event);
        }

        return result;
    }

    private void createLayout(CharSequence text, int width) {
        int maxWidth = width - AndroidUtilities.dp(30);
        textLayout = new StaticLayout(text, Theme.chat_actionTextPaint, maxWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        textHeight = 0;
        textWidth = 0;
        try {
            int linesCount = textLayout.getLineCount();
            for (int a = 0; a < linesCount; a++) {
                float lineWidth;
                try {
                    lineWidth = textLayout.getLineWidth(a);
                    if (lineWidth > maxWidth) {
                        lineWidth = maxWidth;
                    }
                    textHeight = (int) Math.max(textHeight, Math.ceil(textLayout.getLineBottom(a)));
                } catch (Exception e) {
                    FileLog.e(e);
                    return;
                }
                textWidth = (int) Math.max(textWidth, Math.ceil(lineWidth));
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        textX = (width - textWidth) / 2;
        textY = AndroidUtilities.dp(7);
        textXLeft = (width - textLayout.getWidth()) / 2;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (currentMessageObject == null && customText == null) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), textHeight + AndroidUtilities.dp(14));
            return;
        }
        int width = Math.max(AndroidUtilities.dp(30), MeasureSpec.getSize(widthMeasureSpec));
        if (previousWidth != width) {
            wasLayout = true;
            previousWidth = width;
            buildLayout();
        }
        setMeasuredDimension(width, textHeight + (currentMessageObject != null && currentMessageObject.type == 11 ? AndroidUtilities.roundMessageSize + AndroidUtilities.dp(10) : 0) + AndroidUtilities.dp(14));
    }

    private void buildLayout() {
        CharSequence text;
        if (currentMessageObject != null) {
            if (currentMessageObject.messageOwner != null && currentMessageObject.messageOwner.media != null && currentMessageObject.messageOwner.media.ttl_seconds != 0) {
                if (currentMessageObject.messageOwner.media.photo instanceof TLRPC.TL_photoEmpty) {
                    text = LocaleController.getString("AttachPhotoExpired", R.string.AttachPhotoExpired);
                } else if (currentMessageObject.messageOwner.media.document instanceof TLRPC.TL_documentEmpty) {
                    text = LocaleController.getString("AttachVideoExpired", R.string.AttachVideoExpired);
                } else {
                    text = currentMessageObject.messageText;
                }
            } else {
                text = currentMessageObject.messageText;
            }
        } else {
            text = customText;
        }
        createLayout(text, previousWidth);
        if (currentMessageObject != null && currentMessageObject.type == 11) {
            imageReceiver.setImageCoords((previousWidth - AndroidUtilities.roundMessageSize) / 2, textHeight + AndroidUtilities.dp(19), AndroidUtilities.roundMessageSize, AndroidUtilities.roundMessageSize);
        }
    }

    public int getCustomDate() {
        return customDate;
    }

    private int findMaxWidthAroundLine(int line) {
        int width = (int) Math.ceil(textLayout.getLineWidth(line));
        int count = textLayout.getLineCount();
        for (int a = line + 1; a < count; a++) {
            int w = (int) Math.ceil(textLayout.getLineWidth(a));
            if (Math.abs(w - width) < AndroidUtilities.dp(10)) {
                width = Math.max(w, width);
            } else {
                break;
            }
        }
        for (int a = line - 1; a >= 0; a--) {
            int w = (int) Math.ceil(textLayout.getLineWidth(a));
            if (Math.abs(w - width) < AndroidUtilities.dp(10)) {
                width = Math.max(w, width);
            } else {
                break;
            }
        }
        return width;
    }

    private boolean isLineTop(int prevWidth, int currentWidth, int line, int count, int cornerRest) {
        return line == 0 || !(line < 0 || line >= count) && findMaxWidthAroundLine(line - 1) + cornerRest * 3 < prevWidth;
    }

    private boolean isLineBottom(int nextWidth, int currentWidth, int line, int count, int cornerRest) {
        return line == count - 1 || !(line < 0 || line > count - 1) && findMaxWidthAroundLine(line + 1) + cornerRest * 3 < nextWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (currentMessageObject != null && currentMessageObject.type == 11) {
            imageReceiver.draw(canvas);
        }

        if (textLayout == null) {
            return;
        }

        if (overrideBackground != null) {
            int color = Theme.getColor(overrideBackground);
            if (color != overrideColor) {
                overrideColor = color;
                overrideColorFilter = new PorterDuffColorFilter(overrideColor, PorterDuff.Mode.MULTIPLY);
            }
            for (int a = 0; a < 4; a++) {
                Theme.chat_cornerOuter[a].setColorFilter(overrideColorFilter);
                Theme.chat_cornerInner[a].setColorFilter(overrideColorFilter);
            }
            Theme.chat_actionBackgroundPaint.setColor(overrideColor);
            Theme.chat_actionTextPaint.setColor(Theme.getColor(overrideText));
        }

        final int count = textLayout.getLineCount();
        final int corner = AndroidUtilities.dp(11);
        final int cornerOffset = AndroidUtilities.dp(6);
        final int cornerRest = corner - cornerOffset;
        final int cornerIn = AndroidUtilities.dp(8);
        int y = AndroidUtilities.dp(7);
        int previousLineBottom = 0;
        int previousLineHeight = 0;
        int dx;
        int dx2;
        int dy;
        int previousLineWidth = 0;
        for (int a = 0; a < count; a++) {
            int width = findMaxWidthAroundLine(a);
            int nextWidth = a < count - 1 ? findMaxWidthAroundLine(a + 1) : 0;
            int w1 = 0;
            int w2 = 0;
            if (previousLineWidth != 0) {
                int dw = width - previousLineWidth;
                if (dw > 0 &&  dw < AndroidUtilities.dp(15) * 2) {
                    width = w1 = previousLineWidth + AndroidUtilities.dp(15) * 2;
                }

            }
            if (nextWidth != 0) {
                int dw = width - nextWidth;
                if (dw > 0  && dw < AndroidUtilities.dp(15) * 2) {
                    width = w2 = nextWidth + AndroidUtilities.dp(15) * 2;
                }
            }
            if (w1 != 0 && w2 != 0) {
                width = Math.max(w1, w2);
            }
            previousLineWidth = width;
            int x = (getMeasuredWidth() - width - cornerRest) / 2;
            width += cornerRest;
            int lineBottom = textLayout.getLineBottom(a);
            int height = lineBottom - previousLineBottom;
            int additionalHeight = 0;
            previousLineBottom = lineBottom;

            boolean drawBottomCorners = a == count - 1;
            boolean drawTopCorners = a == 0;

            if (drawTopCorners) {
                y -= AndroidUtilities.dp(3);
                height += AndroidUtilities.dp(3);
            }
            if (drawBottomCorners) {
                height += AndroidUtilities.dp(3);
            }

            int yOld = y;
            int hOld = height;

            int drawInnerBottom = 0;
            int drawInnerTop = 0;
            int nextLineWidth = 0;
            int prevLineWidth = 0;
            if (!drawBottomCorners && a + 1 < count) {
                nextLineWidth = findMaxWidthAroundLine(a + 1) + cornerRest;
                if (nextLineWidth + cornerRest * 2 < width) {
                    drawInnerBottom = 1;
                    drawBottomCorners = true;
                } else if (width + cornerRest * 2 < nextLineWidth) {
                    drawInnerBottom = 2;
                } else {
                    drawInnerBottom = 3;
                }
            }
            if (!drawTopCorners && a > 0) {
                prevLineWidth = findMaxWidthAroundLine(a - 1) + cornerRest;
                if (prevLineWidth + cornerRest * 2 < width) {
                    drawInnerTop = 1;
                    drawTopCorners = true;
                } else if (width + cornerRest * 2 < prevLineWidth) {
                    drawInnerTop = 2;
                } else {
                    drawInnerTop = 3;
                }
            }

            if (drawInnerBottom != 0) {
                if (drawInnerBottom == 1) {
                    int nextX = (getMeasuredWidth() - nextLineWidth) / 2;
                    additionalHeight = AndroidUtilities.dp(3);

                    if (isLineBottom(nextLineWidth, width, a + 1, count, cornerRest)) {
                        canvas.drawRect(x + cornerOffset, y + height, nextX - cornerRest, y + height + AndroidUtilities.dp(3), Theme.chat_actionBackgroundPaint);
                        canvas.drawRect(nextX + nextLineWidth + cornerRest, y + height, x + width - cornerOffset, y + height + AndroidUtilities.dp(3), Theme.chat_actionBackgroundPaint);
                    } else {
                        canvas.drawRect(x + cornerOffset, y + height, nextX, y + height + AndroidUtilities.dp(3), Theme.chat_actionBackgroundPaint);
                        canvas.drawRect(nextX + nextLineWidth, y + height, x + width - cornerOffset, y + height + AndroidUtilities.dp(3), Theme.chat_actionBackgroundPaint);
                    }
                } else if (drawInnerBottom == 2) {
                    additionalHeight = AndroidUtilities.dp(3);

                    dy = y + height - AndroidUtilities.dp(11);

                    dx = x - cornerIn;
                    if (drawInnerTop != 2 && drawInnerTop != 3) {
                        dx -= cornerRest;
                    }
                    if (drawTopCorners || drawBottomCorners) {
                        canvas.drawRect(dx + cornerIn, dy + AndroidUtilities.dp(3), dx + cornerIn + corner, dy + corner, Theme.chat_actionBackgroundPaint);
                    }
                    Theme.chat_cornerInner[2].setBounds(dx, dy, dx + cornerIn, dy + cornerIn);
                    Theme.chat_cornerInner[2].draw(canvas);

                    dx = x + width;
                    if (drawInnerTop != 2 && drawInnerTop != 3) {
                        dx += cornerRest;
                    }
                    if (drawTopCorners || drawBottomCorners) {
                        canvas.drawRect(dx - corner, dy + AndroidUtilities.dp(3), dx, dy + corner, Theme.chat_actionBackgroundPaint);
                    }
                    Theme.chat_cornerInner[3].setBounds(dx, dy, dx + cornerIn, dy + cornerIn);
                    Theme.chat_cornerInner[3].draw(canvas);
                } else {
                    additionalHeight = AndroidUtilities.dp(6);
                }
            }
            if (drawInnerTop != 0) {
                if (drawInnerTop == 1) {
                    int prevX = (getMeasuredWidth() - prevLineWidth) / 2;

                    y -= AndroidUtilities.dp(3);
                    height += AndroidUtilities.dp(3);

                    if (isLineTop(prevLineWidth, width, a - 1, count, cornerRest)) {
                        canvas.drawRect(x + cornerOffset, y, prevX - cornerRest, y + AndroidUtilities.dp(3), Theme.chat_actionBackgroundPaint);
                        canvas.drawRect(prevX + prevLineWidth + cornerRest, y, x + width - cornerOffset, y + AndroidUtilities.dp(3), Theme.chat_actionBackgroundPaint);
                    } else {
                        canvas.drawRect(x + cornerOffset, y, prevX, y + AndroidUtilities.dp(3), Theme.chat_actionBackgroundPaint);
                        canvas.drawRect(prevX + prevLineWidth, y, x + width - cornerOffset, y + AndroidUtilities.dp(3), Theme.chat_actionBackgroundPaint);
                    }
                } else if (drawInnerTop == 2) {
                    y -= AndroidUtilities.dp(3);
                    height += AndroidUtilities.dp(3);

                    dy = previousLineHeight;

                    dx = x - cornerIn;
                    if (drawInnerBottom != 2 && drawInnerBottom != 3) {
                        dx -= cornerRest;
                    }
                    if (drawTopCorners || drawBottomCorners) {
                        canvas.drawRect(dx + cornerIn, y + AndroidUtilities.dp(3), dx + cornerIn + corner, y + AndroidUtilities.dp(11), Theme.chat_actionBackgroundPaint);
                    }
                    Theme.chat_cornerInner[0].setBounds(dx, dy, dx + cornerIn, dy + cornerIn);
                    Theme.chat_cornerInner[0].draw(canvas);

                    dx = x + width;
                    if (drawInnerBottom != 2 && drawInnerBottom != 3) {
                        dx += cornerRest;
                    }
                    if (drawTopCorners || drawBottomCorners) {
                        canvas.drawRect(dx - corner, y + AndroidUtilities.dp(3), dx, y + AndroidUtilities.dp(11), Theme.chat_actionBackgroundPaint);
                    }
                    Theme.chat_cornerInner[1].setBounds(dx, dy, dx + cornerIn, dy + cornerIn);
                    Theme.chat_cornerInner[1].draw(canvas);
                } else {
                    y -= AndroidUtilities.dp(6);
                    height += AndroidUtilities.dp(6);
                }
            }

            if (drawTopCorners || drawBottomCorners) {
                canvas.drawRect(x + cornerOffset, yOld, x + width - cornerOffset, yOld + hOld, Theme.chat_actionBackgroundPaint);
            } else {
                canvas.drawRect(x, yOld, x + width, yOld + hOld, Theme.chat_actionBackgroundPaint);
            }

            dx = x - cornerRest;
            dx2 = x + width - cornerOffset;
            if (drawTopCorners && !drawBottomCorners && drawInnerBottom != 2) {
                canvas.drawRect(dx, y + corner, dx + corner, y + height + additionalHeight - AndroidUtilities.dp(6), Theme.chat_actionBackgroundPaint);
                canvas.drawRect(dx2, y + corner, dx2 + corner, y + height + additionalHeight - AndroidUtilities.dp(6), Theme.chat_actionBackgroundPaint);
            } else if (drawBottomCorners && !drawTopCorners && drawInnerTop != 2) {
                canvas.drawRect(dx, y + corner - AndroidUtilities.dp(5), dx + corner, y + height + additionalHeight - corner, Theme.chat_actionBackgroundPaint);
                canvas.drawRect(dx2, y + corner - AndroidUtilities.dp(5), dx2 + corner, y + height + additionalHeight - corner, Theme.chat_actionBackgroundPaint);
            } else if (drawTopCorners || drawBottomCorners) {
                canvas.drawRect(dx, y + corner, dx + corner, y + height + additionalHeight - corner, Theme.chat_actionBackgroundPaint);
                canvas.drawRect(dx2, y + corner, dx2 + corner, y + height + additionalHeight - corner, Theme.chat_actionBackgroundPaint);
            }

            if (drawTopCorners) {
                Theme.chat_cornerOuter[0].setBounds(dx, y, dx + corner, y + corner);
                Theme.chat_cornerOuter[0].draw(canvas);
                Theme.chat_cornerOuter[1].setBounds(dx2, y, dx2 + corner, y + corner);
                Theme.chat_cornerOuter[1].draw(canvas);
            }

            if (drawBottomCorners) {
                dy = y + height + additionalHeight - corner;

                Theme.chat_cornerOuter[2].setBounds(dx2, dy, dx2 + corner, dy + corner);
                Theme.chat_cornerOuter[2].draw(canvas);
                Theme.chat_cornerOuter[3].setBounds(dx, dy, dx + corner, dy + corner);
                Theme.chat_cornerOuter[3].draw(canvas);
            }

            y += height;

            previousLineHeight = y + additionalHeight;
        }

        canvas.save();
        canvas.translate(textXLeft, textY);
        textLayout.draw(canvas);
        canvas.restore();

        if (overrideColorFilter != null) {
            for (int a = 0; a < 4; a++) {
                Theme.chat_cornerOuter[a].setColorFilter(Theme.colorFilter);
                Theme.chat_cornerInner[a].setColorFilter(Theme.colorFilter);
            }
            Theme.chat_actionBackgroundPaint.setColor(Theme.currentColor);
            Theme.chat_actionTextPaint.setColor(Theme.getColor(Theme.key_chat_serviceText));
        }
    }

    @Override
    public void onFailedDownload(String fileName, boolean canceled) {

    }

    @Override
    public void onSuccessDownload(String fileName) {
        if (currentMessageObject != null && currentMessageObject.type == 11) {
            TLRPC.PhotoSize strippedPhotoSize = null;
            for (int a = 0, N = currentMessageObject.photoThumbs.size(); a < N; a++) {
                TLRPC.PhotoSize photoSize = currentMessageObject.photoThumbs.get(a);
                if (photoSize instanceof TLRPC.TL_photoStrippedSize) {
                    strippedPhotoSize = photoSize;
                    break;
                }
            }
            imageReceiver.setImage(currentVideoLocation, ImageLoader.AUTOPLAY_FILTER, ImageLocation.getForObject(strippedPhotoSize, currentMessageObject.photoThumbsObject), "50_50_b", avatarDrawable, 0, null, currentMessageObject, 1);
            DownloadController.getInstance(currentAccount).removeLoadingFileObserver(this);
        }
    }

    @Override
    public void onProgressDownload(String fileName, long downloadSize, long totalSize) {

    }

    @Override
    public void onProgressUpload(String fileName, long downloadSize, long totalSize, boolean isEncrypted) {

    }

    @Override
    public int getObserverTag() {
        return TAG;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if (TextUtils.isEmpty(customText) && currentMessageObject == null) {
            return;
        }
        info.setText(!TextUtils.isEmpty(customText) ? customText : currentMessageObject.messageText);
        info.setEnabled(true);
    }
}
