package ua.itaysonlab.tgkit.preference.types;

import androidx.annotation.Nullable;

import ua.itaysonlab.tgkit.preference.TGKitPreference;

public class TGKitSwitchPreference extends TGKitPreference {
    public TGSPContract contract;
    public boolean divider;

    @Nullable
    public String summary;

    public TGKitSwitchPreference(String title, boolean divider, TGSPContract contract) {
        this.contract = contract;
        this.title = title;
        this.divider = divider;
    }

    public TGKitSwitchPreference(String title, String summary, boolean divider, TGSPContract contract) {
        this.contract = contract;
        this.title = title;
        this.summary = summary;
        this.divider = divider;
    }

    @Override
    public TGPType getType() {
        return TGPType.SWITCH;
    }

    public interface TGSPContract {
        boolean getPreferenceValue();
        void toggleValue();
    }
}
