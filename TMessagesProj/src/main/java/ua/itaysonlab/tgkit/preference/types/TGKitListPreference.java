package ua.itaysonlab.tgkit.preference.types;

import android.app.Activity;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.List;

import bruhcollective.itaysonlab.airui.miui.popup.DropdownPopup;
import bruhcollective.itaysonlab.airui.miui.popup.adapter.SelectiveMenuAdapter;
import bruhcollective.itaysonlab.airui.miui.popup.adapter.content.DoubleLineContentAdapter;
import kotlin.Triple;
import kotlin.Unit;
import ua.itaysonlab.redesign.BaseActionedSwipeFragment;
import ua.itaysonlab.redesign.sheet.TgxMessageMenuSheetFragment;
import ua.itaysonlab.redesign.slides.TgxMessageMenuFragment;
import ua.itaysonlab.tgkit.preference.TGKitPreference;

public class TGKitListPreference extends TGKitPreference {
    public boolean divider = false;
    public TGTLContract contract;

    @Override
    public TGPType getType() {
        return TGPType.LIST;
    }

    public void callActionHueta(Activity pr, View view, float x, float y, TempInterface ti) {
        ArrayList<SelectiveMenuAdapter.Item> actions = new ArrayList<>();

        if (contract.hasIcons()) {
            for (Triple<Integer, String, Integer> act : contract.getOptionsIcons()) {
                actions.add(new SelectiveMenuAdapter.Item(String.valueOf(act.getFirst()), act.getSecond(), null, ContextCompat.getDrawable(pr, act.getThird()), contract.getValue().equals(act.getSecond())));
            }
        } else {
            for (Pair<Integer, String> act : contract.getOptions()) {
                actions.add(new SelectiveMenuAdapter.Item(String.valueOf(act.first), act.second, null, null, contract.getValue().equals(act.second)));
            }
        }

        DropdownPopup drp =  new DropdownPopup(pr, view, (position, item) -> {
            contract.setValue(Integer.parseInt(item.getId()));
            ti.update();
        });

        drp.setAdapter(new SelectiveMenuAdapter(pr, actions, new DoubleLineContentAdapter(pr, actions)));
        drp.show(View.TEXT_DIRECTION_INHERIT, View.TEXT_ALIGNMENT_TEXT_START, x, y);
    }

    public interface TGTLContract {
        void setValue(int id);
        String getValue();
        List<Pair<Integer, String>> getOptions();
        List<Triple<Integer, String, Integer>> getOptionsIcons();

        boolean hasIcons();
    }

    public interface TempInterface {
        void update();
    }
}
