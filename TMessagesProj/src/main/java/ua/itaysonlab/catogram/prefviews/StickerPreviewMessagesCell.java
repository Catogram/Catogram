package ua.itaysonlab.catogram.prefviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Components.BackgroundGradientDrawable;
import org.telegram.ui.Components.LayoutHelper;

import ua.itaysonlab.catogram.CatogramConfig;

public class StickerPreviewMessagesCell extends LinearLayout {
    private BackgroundGradientDrawable.Disposable backgroundGradientDisposable;
    private BackgroundGradientDrawable.Disposable oldBackgroundGradientDisposable;

    private Drawable backgroundDrawable;
    private ChatMessageCell[] cells = new ChatMessageCell[2];
    private Drawable shadowDrawable;

    public StickerPreviewMessagesCell(Context context) {
        super(context);

        setWillNotDraw(false);
        setOrientation(LinearLayout.VERTICAL);
        setPadding(0, AndroidUtilities.dp(11), 0, AndroidUtilities.dp(11));

        shadowDrawable = Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow);

        int date = (int) (System.currentTimeMillis() / 1000) - 60 * 60;

        TLRPC.Message message;

        message = new TLRPC.TL_message();
        message.message = "This is a preview of Sticker Size Amplifier.";
        message.date = date + 960;
        message.dialog_id = 1;
        message.flags = 259;
        message.from_id = Integer.MAX_VALUE;
        message.id = 1;
        message.media = new TLRPC.TL_messageMediaEmpty();
        message.out = false;
        message.to_id = new TLRPC.TL_peerUser();
        message.to_id.user_id = 0;
        MessageObject message1 = new MessageObject(UserConfig.selectedAccount, message, true);
        message1.resetLayout();
        message1.eventId = 1;

        message = new TLRPC.TL_message();
        message.message = "";
        message.date = date + 60;
        message.dialog_id = 1;
        message.flags = 257 + 8;
        message.from_id = Integer.MAX_VALUE;
        message.id = 2;
        message.media = new TLRPC.TL_messageMediaPhoto();
        message.media.flags |= 3;
        message.media.photo = new TLRPC.TL_photo();
        message.media.photo.file_reference = new byte[0];
        message.media.photo.has_stickers = false;
        message.media.photo.id = 1;
        message.media.photo.access_hash = 0;
        message.media.photo.date = date;
        TLRPC.TL_photoSize photoSize = new TLRPC.TL_photoSize();
        photoSize.size = 0;

        float modifier = CatogramConfig.slider_stickerAmplifier / 100f;
        photoSize.w = 450;
        photoSize.h = 200;

        photoSize.type = "s";
        photoSize.location = new TLRPC.TL_fileLocationUnavailable();

        message.media.photo.sizes.add(photoSize);
        message.out = false;
        message.to_id = new TLRPC.TL_peerUser();
        message.to_id.user_id = Integer.MAX_VALUE;
        MessageObject message2 = new MessageObject(UserConfig.selectedAccount, message, true);
        message2.eventId = 1;
        //message2.type = MessageObject.TYPE_STICKER;
        message2.useCustomPhoto = true;
        message2.resetLayout();

        for (int a = 0; a < cells.length; a++) {
            cells[a] = new ChatMessageCell(context);
            cells[a].setDelegate(new ChatMessageCell.ChatMessageCellDelegate() {

            });
            cells[a].isChat = false;
            cells[a].setFullyDraw(true);
            cells[a].setMessageObject(a == 0 ? message2 : message1, null, false, false);
            addView(cells[a], LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }
    }

    public ChatMessageCell[] getCells() {
        return cells;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        for (ChatMessageCell cell : cells) {
            cell.invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable newDrawable = Theme.getCachedWallpaperNonBlocking();
        if (newDrawable != backgroundDrawable && newDrawable != null) {
            if (backgroundGradientDisposable != null) {
                backgroundGradientDisposable.dispose();
                backgroundGradientDisposable = null;
            }
            backgroundDrawable = newDrawable;
        }

        Drawable drawable = backgroundDrawable;
        if (drawable != null) {
            drawable.setAlpha(255);
            if (drawable instanceof ColorDrawable || drawable instanceof GradientDrawable) {
                drawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                if (drawable instanceof BackgroundGradientDrawable) {
                    final BackgroundGradientDrawable backgroundGradientDrawable = (BackgroundGradientDrawable) drawable;
                    backgroundGradientDisposable = backgroundGradientDrawable.drawExactBoundsSize(canvas, this);
                } else {
                    drawable.draw(canvas);
                }
            } else if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if (bitmapDrawable.getTileModeX() == Shader.TileMode.REPEAT) {
                    canvas.save();
                    float scale = 2.0f / AndroidUtilities.density;
                    canvas.scale(scale, scale);
                    drawable.setBounds(0, 0, (int) Math.ceil(getMeasuredWidth() / scale), (int) Math.ceil(getMeasuredHeight() / scale));
                    drawable.draw(canvas);
                    canvas.restore();
                } else {
                    int viewHeight = getMeasuredHeight();
                    float scaleX = (float) getMeasuredWidth() / (float) drawable.getIntrinsicWidth();
                    float scaleY = (float) (viewHeight) / (float) drawable.getIntrinsicHeight();
                    float scale = scaleX < scaleY ? scaleY : scaleX;
                    int width = (int) Math.ceil(drawable.getIntrinsicWidth() * scale);
                    int height = (int) Math.ceil(drawable.getIntrinsicHeight() * scale);
                    int x = (getMeasuredWidth() - width) / 2;
                    int y = (viewHeight - height) / 2;
                    canvas.save();
                    canvas.clipRect(0, 0, width, getMeasuredHeight());
                    drawable.setBounds(x, y, x + width, y + height);
                    drawable.draw(canvas);
                    canvas.restore();
                }
            }
        }

        shadowDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
        shadowDrawable.draw(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (backgroundGradientDisposable != null) {
            backgroundGradientDisposable.dispose();
            backgroundGradientDisposable = null;
        }
        if (oldBackgroundGradientDisposable != null) {
            oldBackgroundGradientDisposable.dispose();
            oldBackgroundGradientDisposable = null;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
