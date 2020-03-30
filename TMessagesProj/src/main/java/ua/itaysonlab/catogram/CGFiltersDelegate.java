package ua.itaysonlab.catogram;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.DialogsActivity;

public class CGFiltersDelegate {
    public static CatogramFilter DEFAULT_FILTER = extractFilterByString(CatogramConfig.savedFilter);

    private static CatogramFilter extractFilterByString(String savedFilter) {
        for (CatogramFilter filter: CatogramFilter.values()) {
            if (savedFilter.equals(filter.savePath)) return filter;
        }
        return CatogramFilter.ALL;
    }

    public static String ARG_ALL = "counter_all",
            ARG_PM = "counter_pms", ARG_SECRET = "counter_secrets",
            ARG_CHATS = "counter_chats", ARG_CHANNELS = "counter_channels",
            ARG_BOTS = "counter_bots";

    public enum CatogramFilter {
        ALL(LocaleController.getString("CG_Filters_All", R.string.CG_Filters_All), 0, "all"),
        PM(LocaleController.getString("CG_Filters_PM", R.string.CG_Filters_PM), 9, "pm"),
        SECRETS(LocaleController.getString("CG_Filters_Secret", R.string.CG_Filters_Secret), 7, "secret"),
        CHANNELS(LocaleController.getString("CG_Filters_Channels", R.string.CG_Filters_Channels), 10, "channels"),
        CHATS(LocaleController.getString("CG_Filters_Chats", R.string.CG_Filters_Chats), 11, "chats"),
        BOTS(LocaleController.getString("CG_Filters_Bots", R.string.CG_Filters_Bots), 8, "bots");

        public String title;
        public int dialogsType;
        public String savePath;

        CatogramFilter(String title, int dialogsType, String savePath) {
            this.title = title;
            this.dialogsType = dialogsType;
            this.savePath = savePath;
        }
    }

    public static void createFilterSheet(DialogsActivity act, Bundle counters) {
        createFilterSheet(act, counters, false);
    }

    public static void createFilterSheet(DialogsActivity act, Bundle counters, Boolean shouldShowHint) {
        BottomSheet.Builder builder = new BottomSheet.Builder(act.getParentActivity());
        builder.setApplyBottomPadding(false);

        long[] countersRaw = new long[]{
                counters.getLong(ARG_ALL),
                counters.getLong(ARG_PM),
                counters.getLong(ARG_SECRET),
                counters.getLong(ARG_CHANNELS),
                counters.getLong(ARG_CHATS),
                counters.getLong(ARG_BOTS)
        };

        int[] icons = new int[]{
                R.drawable.menu_saved,
                R.drawable.menu_contacts,
                R.drawable.menu_secret,
                R.drawable.menu_broadcast,
                R.drawable.menu_groups,
                R.drawable.menu_jobtitle
        };

        LinearLayout containerView = new LinearLayout(act.getParentActivity());
        builder.setCustomView(containerView);
        containerView.setOrientation(LinearLayout.VERTICAL);

        TextView titleView = new TextView(act.getParentActivity());
        titleView.setLines(1);
        titleView.setSingleLine(true);
        titleView.setText(LocaleController.getString("CG_Filters_Header", R.string.CG_Filters_Header));
        titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        titleView.setTypeface(ua.itaysonlab.extras.CatogramExtras.getBold());
        titleView.setPadding(AndroidUtilities.dp(21), AndroidUtilities.dp(6), AndroidUtilities.dp(21), AndroidUtilities.dp(8));
        titleView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        containerView.addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48));

        int length = CatogramFilter.values().length;
        for (int i = 0; i < length; i++) {
            CatogramFilter filter = CatogramFilter.values()[i];

            TextCell cell = new TextCell(act.getParentActivity());
            cell.setBackground(Theme.getRoundRectSelectorDrawable(Theme.getColor(Theme.key_dialogButton)));
            cell.setTextAndValueAndIcon(filter.title, LocaleController.formatShortNumber(countersRaw[i], null), icons[i], i < (length - 1));
            cell.setOnClickListener(v -> {
                act.dismissCurrentDialig();
                applyFilters(act, filter);
            });

            containerView.addView(cell);
        }

        if (shouldShowHint && CatogramConfig.filterHintFAB) {
            TextInfoPrivacyCell hintCell = new TextInfoPrivacyCell(act.getParentActivity());
            hintCell.setText(LocaleController.getString("CG_Filters_AlsoFABHint", R.string.CG_Filters_AlsoFABHint));
            containerView.addView(hintCell);
        } else {
            TextInfoPrivacyCell hintCell = new TextInfoPrivacyCell(act.getParentActivity());
            hintCell.setText(LocaleController.getString("CG_Filters_GenericHint", R.string.CG_Filters_GenericHint));
            containerView.addView(hintCell);
        }

        act.showDialog(builder.create());
        CatogramConfig.shownFABHint();
    }

    private static void applyFilters(DialogsActivity act, CatogramFilter filter) {
        CatogramConfig.setSavedFilter(filter.savePath);
    }
}
