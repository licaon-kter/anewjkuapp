package org.voidsink.anewjkuapp.fragment;

import android.graphics.Color;

import org.voidsink.anewjkuapp.MensaDayTabItem;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.base.SlidingTabsFragment;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MensaFragment extends SlidingTabsFragment {

    @Override
    protected void fillTabs(List<SlidingTabItem> mTabs) {

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
                    mTabs.add(new MensaDayTabItem(cal.getTime(), CalendarUtils.COLOR_DEFAULT_LVA, Color.GRAY));
                }
                // increment day
                cal.add(Calendar.DATE, 1);
            } while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY);
        } else {
            mTabs.add(new SlidingTabItem("Classic", MensaClassicFragment.class, CalendarUtils.COLOR_DEFAULT_LVA, Color.GRAY));
            mTabs.add(new SlidingTabItem("Choice", MensaChoiceFragment.class, CalendarUtils.COLOR_DEFAULT_EXAM, Color.GRAY));
            mTabs.add(new SlidingTabItem("KHG", MensaKHGFragment.class, CalendarUtils.COLOR_DEFAULT_LVA, Color.GRAY));
            mTabs.add(new SlidingTabItem("Raab", MensaRaabFragment.class, CalendarUtils.COLOR_DEFAULT_EXAM, Color.GRAY));
        }
    }
}