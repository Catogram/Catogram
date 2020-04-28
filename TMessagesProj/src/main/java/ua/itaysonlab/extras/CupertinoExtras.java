package ua.itaysonlab.extras;

import android.content.Context;
import android.view.ViewGroup;

import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatMessageCell;

import java.util.ArrayList;
import java.util.List;

import ru.utkacraft.cupertinolib.popup.ContextMenu;

public class CupertinoExtras {
    public static ContextMenu initViewHolder(ArrayList<Integer> optionsID,
                                                      ArrayList<CharSequence> optionTitles,
                                                      ArrayList<Integer> optionIcons,
                                                      Context context,
                                                      RecyclerView recyclerView,
                                                      ViewGroup rootView,
                                                      RecyclerView.ViewHolder viewHolder,
                                                      boolean isOut,
                                                      boolean needLargeStartSpace,
                                                      OnClickListener onClickListener) {
        ContextMenu ctxMenu = new ContextMenu(context, recyclerView, rootView, viewHolder);

        ctxMenu.setBlurOverlayColor(Theme.getColor(Theme.key_windowBackgroundGray));
        ctxMenu.setActionsBackground(Theme.getColor(Theme.key_dialogBackground));
        ctxMenu.setAccentColor(Theme.getColor(Theme.key_dialogTextBlack));
        ctxMenu.setDangerColor(Theme.getColor(Theme.key_dialogTextRed));
        ctxMenu.setStartMargin(CatogramExtras.dip2px(context, needLargeStartSpace ? 56f : 8f));
        ctxMenu.setEndMargin(CatogramExtras.dip2px(context, 8f));

        ctxMenu.setActions(generateActionList(optionsID, optionTitles, optionIcons));
        ctxMenu.setGravity(isOut ? GravityCompat.END : GravityCompat.START);
        ctxMenu.setClickListener((action) -> {
            onClickListener.onClick(action.id);
            return true;
        });

        return ctxMenu;
    }

    private static List<ContextMenu.PopupAction> generateActionList(ArrayList<Integer> optionsID, ArrayList<CharSequence> optionTitles, ArrayList<Integer> optionIcons) {
        ArrayList<ContextMenu.PopupAction> actions = new ArrayList<>();
        for (int a = 0, N = optionsID.size(); a < N; a++) {
            ContextMenu.PopupAction action = new ContextMenu.PopupAction(optionsID.get(a), optionTitles.get(a), optionIcons.get(a), analyzeDangerousity(optionsID.get(a)));
            if (action.id == 1) action.setHasTopPadding();
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
