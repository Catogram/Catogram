package ua.itaysonlab.catogram.preferences;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;

import java.util.ArrayList;
import java.util.List;

import ua.itaysonlab.catogram.CatogramConfig;
import ua.itaysonlab.tgkit.preference.TGKitCategory;
import ua.itaysonlab.tgkit.preference.TGKitPreference;
import ua.itaysonlab.tgkit.preference.TGKitSettings;
import ua.itaysonlab.tgkit.preference.types.TGKitSliderPreference;
import ua.itaysonlab.tgkit.preference.types.TGKitSwitchPreference;

public class AppearancePreferencesEntry implements BasePreferencesEntry {
    @Override
    public TGKitSettings getPreferences() {
        List<TGKitCategory> categories = new ArrayList<>();

        List<TGKitPreference> general = new ArrayList<>();
        general.add(
                new TGKitSwitchPreference(
                        LocaleController.getString("AS_HideUserPhone", R.string.AS_HideUserPhone),
                        LocaleController.getString("AS_HideUserPhoneSummary", R.string.AS_HideUserPhoneSummary),
                        true,
                        new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return CatogramConfig.hidePhoneNumber;
                            }

                            @Override
                            public void toggleValue() {
                                CatogramConfig.toggleHidePhoneNumber();
                            }
                        })
        );
        general.add(
                new TGKitSwitchPreference(
                        LocaleController.getString("AS_NoRounding", R.string.AS_NoRounding),
                        LocaleController.getString("AS_NoRoundingSummary", R.string.AS_NoRoundingSummary),
                        true,
                        new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return CatogramConfig.noRounding;
                            }

                            @Override
                            public void toggleValue() {
                                CatogramConfig.toggleNoRounding();
                            }
                        })
        );
        general.add(
                new TGKitSwitchPreference(
                        LocaleController.getString("AS_SystemFonts", R.string.AS_SystemFonts),
                        true,
                        new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return CatogramConfig.systemFonts;
                            }

                            @Override
                            public void toggleValue() {
                                CatogramConfig.toggleSystemFonts();
                            }
                        })
        );
        general.add(
                new TGKitSwitchPreference(
                        LocaleController.getString("AS_Vibration", R.string.AS_Vibration),
                        true,
                        new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return CatogramConfig.noVibration;
                            }

                            @Override
                            public void toggleValue() {
                                CatogramConfig.toggleNoVibration();
                            }
                        })
        );
        general.add(
                new TGKitSwitchPreference(
                        LocaleController.getString("AS_FlatSB", R.string.AS_FlatSB),
                        LocaleController.getString("AS_FlatSB_Desc", R.string.AS_FlatSB_Desc),
                        true,
                        new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return CatogramConfig.flatStatusbar;
                            }

                            @Override
                            public void toggleValue() {
                                CatogramConfig.toggleFlatSB();
                            }
                        })
        );
        general.add(
                new TGKitSwitchPreference(
                        LocaleController.getString("AS_FlatAB", R.string.AS_FlatAB),
                        true,
                        new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return CatogramConfig.flatActionbar;
                            }

                            @Override
                            public void toggleValue() {
                                CatogramConfig.toggleFlatAB();
                            }
                        })
        );
        /*general.add(
                new TGKitSwitchPreference(LocaleController.getString("CG_ConfirmCalls", R.string.CG_ConfirmCalls), null, true, new TGKitSwitchPreference.TGSPContract() {
                    @Override
                    public boolean getPreferenceValue() {
                        return CatogramConfig.confirmCalls;
                    }

                    @Override
                    public void toggleValue() {
                        CatogramConfig.toggleCallConfirm();
                    }
                })
        );*/
        general.add(
                new TGKitSwitchPreference(
                        LocaleController.getString("AS_SysEmoji", R.string.AS_SysEmoji),
                        LocaleController.getString("AS_SysEmojiDesc", R.string.AS_SysEmojiDesc),
                        false,
                        new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return SharedConfig.useSystemEmoji;
                            }

                            @Override
                            public void toggleValue() {
                                SharedConfig.toggleSystemEmoji();
                            }
                        })
        );
        categories.add(new TGKitCategory(LocaleController.getString("General", R.string.General), general));

        List<TGKitPreference> notification = new ArrayList<>();
        notification.add(
                new TGKitSwitchPreference(LocaleController.getString("AS_AccentNotify", R.string.AS_AccentNotify), false, new TGKitSwitchPreference.TGSPContract() {
                    @Override
                    public boolean getPreferenceValue() {
                        return CatogramConfig.accentNotification;
                    }

                    @Override
                    public void toggleValue() {
                        CatogramConfig.toggleAccentNotification();
                    }
                })
        );
        categories.add(new TGKitCategory(LocaleController.getString("AS_Header_Notification", R.string.AS_Header_Notification), notification));

        List<TGKitPreference> drawer = new ArrayList<>();
        drawer.add(
                new TGKitSwitchPreference(
                        LocaleController.getString("AS_ForceNY_Drawer", R.string.AS_ForceNY_Drawer),
                        LocaleController.getString("AS_ForceNYSummary", R.string.AS_ForceNYSummary),
                        true,
                        new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return CatogramConfig.forceNewYearDrawer;
                            }

                            @Override
                            public void toggleValue() {
                                CatogramConfig.toggleForceNewYearDrawer();
                            }
                        })
        );
        drawer.add(
                new TGKitSwitchPreference(
                        LocaleController.getString("AS_DrawerAvatar", R.string.AS_DrawerAvatar),
                        true,
                        new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return CatogramConfig.drawerAvatar;
                            }

                            @Override
                            public void toggleValue() {
                                CatogramConfig.toggleDrawerAvatar();
                            }
                        })
        );
        drawer.add(
                new TGKitSwitchPreference(
                        LocaleController.getString("AS_DrawerBlur", R.string.AS_DrawerBlur),
                        true,
                        new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return CatogramConfig.drawerBlur;
                            }

                            @Override
                            public void toggleValue() {
                                CatogramConfig.toggleDrawerBlur();
                            }
                        })
        );
        drawer.add(
                new TGKitSwitchPreference(
                        LocaleController.getString("AS_DrawerDarken", R.string.AS_DrawerDarken),
                        false,
                        new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return CatogramConfig.drawerDarken;
                            }

                            @Override
                            public void toggleValue() {
                                CatogramConfig.toggleDrawerDarken();
                            }
                        })
        );
        categories.add(new TGKitCategory(LocaleController.getString("AS_DrawerCategory", R.string.AS_DrawerCategory), drawer));

        List<TGKitPreference> fun = new ArrayList<>();
        fun.add(
                new TGKitSwitchPreference(
                        LocaleController.getString("AS_ForceNY", R.string.AS_ForceNY),
                        LocaleController.getString("AS_ForceNYSummary", R.string.AS_ForceNYSummary),
                        true,
                        new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return CatogramConfig.forceNewYear;
                            }

                            @Override
                            public void toggleValue() {
                                CatogramConfig.toggleForceNewYear();
                            }
                        })
        );
        fun.add(
                new TGKitSwitchPreference(
                        LocaleController.getString("AS_ForcePacman", R.string.AS_ForcePacman),
                        LocaleController.getString("AS_ForcePacmanSummary", R.string.AS_ForcePacmanSummary),
                        false,
                        new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return CatogramConfig.forcePacman;
                            }

                            @Override
                            public void toggleValue() {
                                CatogramConfig.toggleForcePacman();
                            }
                        })
        );
        categories.add(new TGKitCategory(LocaleController.getString("AS_Header_Fun", R.string.AS_Header_Fun), fun));

        return new TGKitSettings(LocaleController.getString("AS_Header_Appearance", R.string.AS_Header_Appearance), categories);
    }
}
