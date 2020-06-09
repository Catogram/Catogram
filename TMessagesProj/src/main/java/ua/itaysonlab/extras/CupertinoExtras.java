package ua.itaysonlab.extras;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.util.Pair;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatMessageCell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.utkacraft.cupertinolib.actionsheet.ActionSheet;
import ru.utkacraft.cupertinolib.actionsheet.SimpleTextHolderProvider;
import ru.utkacraft.cupertinolib.popup.ContextMenu;
import ru.utkacraft.cupertinolib.popup.contextmenu2.ContextMenu2;

public class CupertinoExtras {
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

        ctxMenu.setBlurOverlayColor(Theme.getColor(Theme.key_windowBackgroundGray));
        ctxMenu.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));
        ctxMenu.setAccentColor(Theme.getColor(Theme.key_dialogTextBlack));
        ctxMenu.setDangerColor(Theme.getColor(Theme.key_dialogTextRed));
        ctxMenu.setMarginStartEnd(CatogramExtras.dip2px(context, isOut ? 8f : 56f));
        ctxMenu.setSheetGravity(isOut ? Gravity.END : Gravity.START);
        ctxMenu.forceMoveStrategy(ContextMenu2.MoveStrategy.MOVE_DOWN);

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
