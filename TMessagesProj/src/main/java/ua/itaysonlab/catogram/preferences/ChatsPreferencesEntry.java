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

public class ChatsPreferencesEntry implements BasePreferencesEntry {
    @Override
    public TGKitSettings getPreferences() {
        List<TGKitCategory> categories = new ArrayList<>();

        List<TGKitPreference> redesign = new ArrayList<>();
        redesign.add(
                new TGKitSwitchPreference(LocaleController.getString("AS_CupertinoLib", R.string.AS_CupertinoLib), LocaleController.getString("AS_CupertinoLibSummary", R.string.AS_CupertinoLibSummary), true, new TGKitSwitchPreference.TGSPContract() {
                    @Override
                    public boolean getPreferenceValue() {
                        return CatogramConfig.useCupertinoLib;
                    }

                    @Override
                    public void toggleValue() {
                        CatogramConfig.toggleCupertinoLib();
                    }
                })
        );
        redesign.add(
                new TGKitSwitchPreference(LocaleController.getString("CG_NewMsgAnim", R.string.CG_NewMsgAnim), false, new TGKitSwitchPreference.TGSPContract() {
                    @Override
                    public boolean getPreferenceValue() {
                        return CatogramConfig.newMessageAnimation;
                    }

                    @Override
                    public void toggleValue() {
                        CatogramConfig.toggleNewMessageAnimation();
                    }
                })
        );
        categories.add(new TGKitCategory(LocaleController.getString("AS_RedesignCategory", R.string.AS_RedesignCategory), redesign));

        List<TGKitPreference> stickerSize = new ArrayList<>();
        stickerSize.add(
                new TGKitSliderPreference(new TGKitSliderPreference.TGSLContract() {
                    @Override
                    public void setValue(int value) {
                        CatogramConfig.setStickerAmplifier(value);
                    }

                    @Override
                    public int getPreferenceValue() {
                        return CatogramConfig.slider_stickerAmplifier;
                    }

                    @Override
                    public int getMin() {
                        return 50;
                    }

                    @Override
                    public int getMax() {
                        return 100;
                    }
                })
        );
        categories.add(new TGKitCategory(LocaleController.getString("CG_Slider_StickerAmplifier", R.string.CG_Slider_StickerAmplifier), stickerSize));

        List<TGKitPreference> filters = new ArrayList<>();
        filters.add(
                new TGKitSwitchPreference(LocaleController.getString("CG_NewTabs_NoCounter", R.string.CG_NewTabs_NoCounter), true, new TGKitSwitchPreference.TGSPContract() {
                    @Override
                    public boolean getPreferenceValue() {
                        return CatogramConfig.newTabs_noUnread;
                    }

                    @Override
                    public void toggleValue() {
                        CatogramConfig.toggleTabCount();
                    }
                })
        );
        filters.add(
                new TGKitSwitchPreference(LocaleController.getString("CG_NewTabs_RemoveAllChats", R.string.CG_NewTabs_RemoveAllChats), LocaleController.getString("CG_NewTabs_RemoveAllChats_Desc", R.string.CG_NewTabs_RemoveAllChats_Desc), true, new TGKitSwitchPreference.TGSPContract() {
                    @Override
                    public boolean getPreferenceValue() {
                        return CatogramConfig.newTabs_hideAllChats;
                    }

                    @Override
                    public void toggleValue() {
                        CatogramConfig.toggleAllChats();
                    }
                })
        );
        filters.add(
                new TGKitSwitchPreference(LocaleController.getString("CG_NewTabs_Emoticons", R.string.CG_NewTabs_Emoticons), LocaleController.getString("CG_NewTabs_Emoticons_Desc", R.string.CG_NewTabs_Emoticons_Desc), true, new TGKitSwitchPreference.TGSPContract() {
                    @Override
                    public boolean getPreferenceValue() {
                        return CatogramConfig.newTabs_emoji_insteadOfName;
                    }

                    @Override
                    public void toggleValue() {
                        CatogramConfig.toggleEmojiIN();
                    }
                })
        );
        filters.add(
                new TGKitSwitchPreference(LocaleController.getString("CG_NewTabs_EmoticonsAppend", R.string.CG_NewTabs_EmoticonsAppend), LocaleController.getString("CG_NewTabs_EmoticonsAppend_Desc", R.string.CG_NewTabs_EmoticonsAppend_Desc), false, new TGKitSwitchPreference.TGSPContract() {
                    @Override
                    public boolean getPreferenceValue() {
                        return CatogramConfig.newTabs_emoji_appendToName;
                    }

                    @Override
                    public void toggleValue() {
                        CatogramConfig.toggleEmojiAN();
                    }
                })
        );
        categories.add(new TGKitCategory(LocaleController.getString("AS_Filters_Header", R.string.AS_Filters_Header), filters));

        List<TGKitPreference> dialogs = new ArrayList<>();
        dialogs.add(
                new TGKitSwitchPreference(LocaleController.getString("CG_NewRepostUI", R.string.CG_NewRepostUI), LocaleController.getString("CG_NewRepostUI_Desc", R.string.CG_NewRepostUI_Desc), true, new TGKitSwitchPreference.TGSPContract() {
                    @Override
                    public boolean getPreferenceValue() {
                        return CatogramConfig.newRepostUI;
                    }

                    @Override
                    public void toggleValue() {
                        CatogramConfig.toggleNewRepostUI();
                    }
                })
        );
        dialogs.add(
                new TGKitSwitchPreference(LocaleController.getString("CG_SmoothKbd", R.string.CG_SmoothKbd), LocaleController.getString("CG_SmoothKbd_Desc", R.string.CG_SmoothKbd_Desc), true, new TGKitSwitchPreference.TGSPContract() {
                    @Override
                    public boolean getPreferenceValue() {
                        return SharedConfig.smoothKeyboard;
                    }

                    @Override
                    public void toggleValue() {
                        SharedConfig.toggleSmoothKeyboard();
                    }
                })
        );
        dialogs.add(
                new TGKitSwitchPreference(LocaleController.getString("CG_HideKbdOnScroll", R.string.CG_HideKbdOnScroll), false, new TGKitSwitchPreference.TGSPContract() {
                    @Override
                    public boolean getPreferenceValue() {
                        return CatogramConfig.hideKeyboardOnScroll;
                    }

                    @Override
                    public void toggleValue() {
                        CatogramConfig.toggleHideKbdOnScroll();
                    }
                })
        );
        categories.add(new TGKitCategory(LocaleController.getString("AS_Header_Chats", R.string.AS_Header_Chats), dialogs));

        List<TGKitPreference> profiles = new ArrayList<>();
        profiles.add(
                new TGKitSwitchPreference(LocaleController.getString("CG_NewAvaHeader_NoEdgeTapping", R.string.CG_NewAvaHeader_NoEdgeTapping), LocaleController.getString("CG_NewAvaHeader_NoEdgeTapping", R.string.CG_NewAvaHeader_NoEdgeTapping_Desc), true, new TGKitSwitchPreference.TGSPContract() {
                    @Override
                    public boolean getPreferenceValue() {
                        return CatogramConfig.profiles_noEdgeTapping;
                    }

                    @Override
                    public void toggleValue() {
                        CatogramConfig.toggleProfilesNoEdgeTapping();
                    }
                })
        );
        profiles.add(
                new TGKitSwitchPreference(LocaleController.getString("CG_NewAvaHeader_OpenOnTap", R.string.CG_NewAvaHeader_OpenOnTap), LocaleController.getString("CG_NewAvaHeader_OpenOnTap_Desc", R.string.CG_NewAvaHeader_OpenOnTap_Desc), true, new TGKitSwitchPreference.TGSPContract() {
                    @Override
                    public boolean getPreferenceValue() {
                        return CatogramConfig.profiles_openOnTap;
                    }

                    @Override
                    public void toggleValue() {
                        CatogramConfig.toggleProfilesOpenOnTap();
                    }
                })
        );
        profiles.add(
                new TGKitSwitchPreference(LocaleController.getString("CG_NewAvaHeader_AlwaysExpand", R.string.CG_NewAvaHeader_AlwaysExpand), LocaleController.getString("CG_NewAvaHeader_AlwaysExpand", R.string.CG_NewAvaHeader_AlwaysExpand_Desc), false, new TGKitSwitchPreference.TGSPContract() {
                    @Override
                    public boolean getPreferenceValue() {
                        return CatogramConfig.profiles_alwaysExpand;
                    }

                    @Override
                    public void toggleValue() {
                        CatogramConfig.toggleProfilesAlwaysExpand();
                    }
                })
        );
        categories.add(new TGKitCategory(LocaleController.getString("CG_Header_Profile", R.string.CG_Header_Profile), profiles));

        return new TGKitSettings(LocaleController.getString("AS_Header_Chats", R.string.AS_Header_Chats), categories);
    }
}
