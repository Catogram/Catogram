package ua.itaysonlab.catogram.preferences;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.util.ArrayList;
import java.util.List;

import ua.itaysonlab.catogram.CatogramConfig;
import ua.itaysonlab.tgkit.preference.TGKitCategory;
import ua.itaysonlab.tgkit.preference.TGKitPreference;
import ua.itaysonlab.tgkit.preference.TGKitSettings;
import ua.itaysonlab.tgkit.preference.types.TGKitSwitchPreference;

public class SecurityPreferencesEntry implements BasePreferencesEntry {
    @Override
    public TGKitSettings getPreferences() {
        List<TGKitCategory> categories = new ArrayList<>();
        List<TGKitPreference> auth = new ArrayList<>();
        auth.add(
                new TGKitSwitchPreference(
                        LocaleController.getString("AS_Biometric", R.string.AS_Biometric),
                        LocaleController.getString("AS_BiometricDesc", R.string.AS_BiometricDesc),
                        false,
                        new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return CatogramConfig.useBiometricPrompt;
                            }

                            @Override
                            public void toggleValue() {
                                CatogramConfig.toggleNewBiometric();
                            }
                        })
        );
        categories.add(new TGKitCategory(LocaleController.getString("AS_Header_Auth", R.string.AS_Header_Auth), auth));

        List<TGKitPreference> privacy = new ArrayList<>();
        privacy.add(
                new TGKitSwitchPreference(
                        LocaleController.getString("AS_NoProxyPromo", R.string.AS_NoProxyPromo),
                        true,
                        new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return CatogramConfig.hideProxySponsor;
                            }

                            @Override
                            public void toggleValue() {
                                CatogramConfig.toggleHideProxySponsor();
                            }
                        })
        );
        privacy.add(
                new TGKitSwitchPreference(
                        LocaleController.getString("AS_NoTyping", R.string.AS_NoTyping),
                        false,
                        new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return CatogramConfig.noTyping;
                            }

                            @Override
                            public void toggleValue() {
                                CatogramConfig.toggleNoTyping();
                            }
                        })
        );
        categories.add(new TGKitCategory(LocaleController.getString("AS_Header_Privacy", R.string.AS_Header_Privacy), privacy));

        return new TGKitSettings(LocaleController.getString("AS_Category_Security", R.string.AS_Category_Security), categories);
    }
}
