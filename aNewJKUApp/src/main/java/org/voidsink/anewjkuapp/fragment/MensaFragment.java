package org.voidsink.anewjkuapp.fragment;

import android.graphics.Color;

import org.voidsink.anewjkuapp.MensaDayTabItem;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.base.SlidingTabsFragment;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MensaFragment extends SlidingTabsFragment {

    @Override
    protected void fillTabs(List<SlidingTabItem> mTabs) {
        final int indicatorColor = getResources().getColor(android.R.color.white);
        final int dividerColor = getResources().getColor(R.color.primary);

        if (PreferenceWrapper.getGroupMenuByDay(getContext())) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            // jump to next day if later than 4pm
            if (cal.get(Calendar.HOUR_OF_DAY) >= 16) {
                cal.add(Calendar.DATE, 1);
            }
            // add days until next friday
            do {
                // do not add weekend (no menu)
                if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                    mTabs.add(new MensaDayTabItem(getTabTitle(cal), cal.getTime(), CalendarUtils.COLOR_DEFAULT_LVA, Color.GRAY));
                }
                // increment day
                cal.add(Calendar.DATE, 1);
            } while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY);
        } else {
            mTabs.add(new SlidingTabItem(getString(R.string.mensa_title_classic), MensaClassicFragment.class, indicatorColor, dividerColor));
            mTabs.add(new SlidingTabItem(getString(R.string.mensa_title_choice), MensaChoiceFragment.class, indicatorColor, dividerColor));
            mTabs.add(new SlidingTabItem(getString(R.string.mensa_title_khg), MensaKHGFragment.class, indicatorColor, dividerColor));
            mTabs.add(new SlidingTabItem(getString(R.string.mensa_title_raab), MensaRaabFragment.class, indicatorColor, dividerColor));
        }
    }

    private String getTabTitle(final Calendar cal) {
        final Calendar now = Calendar.getInstance();
        if (now.get(Calendar.DATE) == cal.get(Calendar.DATE)) {
            return getResources().getString(R.string.mensa_menu_today);
        } else if (cal.get(Calendar.DATE) - now.get(Calendar.DATE) == 1) {
            return getResources().getString(R.string.mensa_menu_tomorrow);
        }
        return new SimpleDateFormat("EEEE").format(cal.getTime());
    }

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_MENSA;
    }
}
