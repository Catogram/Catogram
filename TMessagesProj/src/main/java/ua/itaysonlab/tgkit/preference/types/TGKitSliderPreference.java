package ua.itaysonlab.tgkit.preference.types;

import androidx.annotation.Nullable;

import ua.itaysonlab.tgkit.preference.TGKitPreference;

public class TGKitSliderPreference extends TGKitPreference {
    public TGSLContract contract;

    @Nullable
    public String summary;

    public TGKitSliderPreference(TGSLContract contract) {
        this.contract = contract;
    }

    @Override
    public TGPType getType() {
        return TGPType.SLIDER;
    }

    public interface TGSLContract {
        void setValue(int value);

        int getPreferenceValue();
        int getMin();
        int getMax();
    }
}
