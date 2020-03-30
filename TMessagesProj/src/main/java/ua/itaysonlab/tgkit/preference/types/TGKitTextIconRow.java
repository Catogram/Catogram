package ua.itaysonlab.tgkit.preference.types;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.TextCell;

import ua.itaysonlab.tgkit.preference.TGKitPreference;

public class TGKitTextIconRow extends TGKitPreference {
    public boolean divider;
    public int icon;

    @Nullable
    public String value;

    @Nullable
    public TGTIListener listener;

    public TGKitTextIconRow(String title, @Nullable String value, int icon, boolean divider, TGTIListener listener) {
        this.title = title;
        this.value = value;
        this.icon = icon;
        this.listener = listener;
        this.divider = divider;
    }

    public void bindCell(TextCell cell) {
        if (icon != -1 && value != null) {
            cell.setTextAndValueAndIcon(title, value, icon, divider);
        } else if (value != null) {
            cell.setTextAndValue(title, value, divider);
        } else if (icon != -1) {
            cell.setTextAndIcon(title, icon, divider);
        } else {
            cell.setText(title, divider);
        }
    }

    @Override
    public TGPType getType() {
        return TGPType.TEXT_ICON;
    }

    public interface TGTIListener {
        void onClick(BaseFragment bf);
    }
}
