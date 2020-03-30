
package ua.itaysonlab.catogram.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import ru.utkacraft.cupertinolib.popup.ContextMenu;
import ru.utkacraft.cupertinolib.popup.ContextTouchListener;

public class SLRecyclerView extends RecyclerView implements ContextMenu.HolderBlockingView {
    private boolean disableScroll = false;
    private boolean isBlocked;
    private ContextTouchListener mTouchListener;

    public SLRecyclerView(@NonNull Context context) {
        super(context);
    }

    public SLRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SLRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (disableScroll) return false;

        return super.onInterceptTouchEvent(e);
    }

    /**
     * Disables scroll on that recyclerview
     * @param disableScroll If we need disable scroll
     */
    public void setDisableScroll(boolean disableScroll) {
        this.disableScroll = disableScroll;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isBlocked) {
            if (mTouchListener != null)
                return mTouchListener.onTouch(this, ev);

            return true;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void setBlocked(boolean bl) {
        isBlocked = bl;
    }

    @Override
    public void setRedirectTouchListener(ContextTouchListener touchListener) {
        mTouchListener = touchListener;
    }
}