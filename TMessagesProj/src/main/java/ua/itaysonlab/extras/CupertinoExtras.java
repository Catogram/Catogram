package ua.itaysonlab.extras;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.List;

import ru.utkacraft.cupertinolib.popup.contextmenu2.ContextMenu2;

public class CupertinoExtras {
    public static int lighten(int color, float factor) {
        int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
        int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
        int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
        return Color.argb(Color.alpha(color), red, green, blue);
    }

    public static int darken(int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a, Math.max((int) (r * factor), 0), Math.max((int) (g * factor), 0), Math.max((int) (b * factor), 0));
    }

    public static ContextMenu2 initViewHolder(ArrayList<Integer> optionsID,
                                              ArrayList<CharSequence> optionTitles,
                                              ArrayList<Integer> optionIcons,
                                              Context context,
                                              RecyclerView recyclerView,
                                              ViewGroup rootView,
                                              RecyclerView.ViewHolder viewHolder,
                                              boolean isOut,
                                              boolean needLargeStartSpace,
                                              OnClickListener onClickListener) {
        ContextMenu2 ctxMenu = new ContextMenu2(context, recyclerView, rootView, viewHolder.itemView);

        int color = Theme.getColor(Theme.key_windowBackgroundWhite);
        if (ColorUtils.calculateLuminance(color) >= 0.8) {
            color = darken(color, 0.85f);
        } else {
            color = darken(color, 0.90f);
        }

        ctxMenu.setBlurOverlayColor(Theme.getColor(Theme.key_windowBackgroundGray));
        ctxMenu.setBackgroundColor(color);
        ctxMenu.setAccentColor(Theme.getColor(Theme.key_dialogTextBlack));
        ctxMenu.setDangerColor(Theme.getColor(Theme.key_dialogTextRed));
        ctxMenu.setMarginStartEnd(CatogramExtras.dip2px(context, (isOut || !needLargeStartSpace) ? 8f : 56f));
        ctxMenu.setSheetGravity(isOut ? Gravity.END : Gravity.START);
        //ctxMenu.forceMoveStrategy(ContextMenu2.MoveStrategy.MOVE_UP_CONTENT);

        ctxMenu.setActions(generateActionList(optionsID, optionTitles, optionIcons));
        ctxMenu.setClickListener((action) -> {
            onClickListener.onClick(action.id);
            return true;
        });

        ctxMenu.setClickListener(new ContextMenu2.OnActionClickListener() {
            @Override
            public boolean onClick(ContextMenu2.Action action) {
                return true;
            }

            @Override
            public void afterClosed(ContextMenu2.Action action) {
                onClickListener.onClick(action.id);
            }
        });

        AndroidUtilities.hideKeyboard(rootView);
        ctxMenu.createAndShow();
        return ctxMenu;
    }

    private static List<ContextMenu2.Action> generateActionList(ArrayList<Integer> optionsID, ArrayList<CharSequence> optionTitles, ArrayList<Integer> optionIcons) {
        ArrayList<ContextMenu2.Action> actions = new ArrayList<>();
        for (int a = 0, N = optionsID.size(); a < N; a++) {
            ContextMenu2.Action action = new ContextMenu2.Action(optionsID.get(a), optionTitles.get(a), optionIcons.get(a), analyzeDangerousity(optionsID.get(a)));
            //if (action.id == 1) action.setHasTopPadding();
            actions.add(action);
        }
        //actions.add(new ContextMenu.PopupAction(999, "Select", R.drawable.arrow_more, false).setHasTopPadding());
        return actions;
    }

    private static boolean analyzeDangerousity(int actionID) {
        return actionID == 1; // Delete
    }

    public interface OnClickListener {
        void onClick(int i);
    }
}
