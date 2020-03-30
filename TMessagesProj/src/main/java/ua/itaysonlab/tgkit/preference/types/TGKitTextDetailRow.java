package ua.itaysonlab.tgkit.preference.types;

import ua.itaysonlab.tgkit.preference.TGKitPreference;

public class TGKitTextDetailRow extends TGKitPreference {
    public String detail;
    public boolean divider;

    public TGKitTextDetailRow(String title, String detail, boolean divider) {
        this.title = title;
        this.detail = detail;
        this.divider = divider;
    }

    @Override
    public TGPType getType() {
        return TGPType.TEXT_DETAIL;
    }
}
