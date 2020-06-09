package ua.itaysonlab.tgkit.preference.types;

import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.List;

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

    public void callActionHueta(Activity pr, TempInterface ti) {
        ArrayList<BaseActionedSwipeFragment.Action> actions = new ArrayList<>();

        if (contract.hasIcons()) {
            for (Triple<Integer, String, Integer> act : contract.getOptionsIcons()) {
                actions.add(new BaseActionedSwipeFragment.Action(String.valueOf(act.getFirst()), act.getThird(), act.getSecond()));
            }
        } else {
            for (Pair<Integer, String> act : contract.getOptions()) {
                actions.add(new BaseActionedSwipeFragment.Action(String.valueOf(act.first), -1, act.second));
            }
        }

        TgxMessageMenuFragment menu = new TgxMessageMenuFragment(actions, (opt) -> {
            contract.setValue(opt);
            ti.update();
            return Unit.INSTANCE;
        });
        menu.setNeedToTint(false);
        menu.show(pr);
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
