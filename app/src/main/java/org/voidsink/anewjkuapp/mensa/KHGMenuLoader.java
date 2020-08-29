/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2020 Paul "Marunjar" Pretsch
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.voidsink.anewjkuapp.mensa;

import android.content.Context;
import android.text.TextUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.analytics.AnalyticsHelper;
import org.voidsink.anewjkuapp.utils.Consts;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class KHGMenuLoader extends BaseMenuLoader implements MenuLoader {

    private static final String tableSelector = "div.contentSection div.listContent table tr";
    private static final NumberFormat nf = NumberFormat.getInstance(Locale.FRENCH);

    @Override
    public IMensa getMensa(Context context) {
        Mensa mensa = new Mensa(Mensen.MENSA_KHG, "KHG");
        MensaDay day = null;
        try {
            Document doc = getData(context);
            if (doc != null) {
                Elements elements = doc.select(tableSelector);

                for (Element element : elements) {
                    Elements columns = element.children();
                    if (columns.size() == 4) {
                        day = handle4Columns(mensa, columns);
                    } else if (columns.size() == 3) {
                        handle3Columns(day, columns);
                    } else {
                        throw new IllegalStateException("Table with columns.size() = " + columns.size() + " found. Expected 3 or 4.");
                    }
                }
            }
        } catch (Exception e) {
            AnalyticsHelper.sendException(context, e, false);
            return null;
        }

        return mensa;
    }

    private void handle3Columns(MensaDay day, Elements columns) {
        String name = "Menü 2";
        String[] strings;
        String soup;
        String meal;
        double price;
        double priceBig;
        double oehBonus;
        
        if (day != null) {
            strings = columns.get(0).text().replace((char) 0xA0, ' ').trim().split(",", 2);
            if (strings.length == 2) {
                soup = strings[0].trim();
                meal = strings[1].trim();
            } else {
                soup = null;
                meal = strings[0];
            }
            if (columns.get(1).text().replace((char) 0xA0, ' ').trim().isEmpty()) {
                price = parsePrice(nf, columns.get(2).text());
                priceBig = 0;
                oehBonus = 0;
                if (price == 1.3) {
                    name = "Mehlspeise";
                }
            } else {
                price = parsePrice(nf, columns.get(1).text());
                priceBig = parsePrice(nf, columns.get(2).text());
                oehBonus = priceBig - price;
            }

            if (!TextUtils.isEmpty(meal)) {
                day.addMenu(new MensaMenu(name, soup, meal, price, priceBig, oehBonus));
            }
        }
    }

    private MensaDay handle4Columns(Mensa mensa, Elements columns) {
        String name = "Menü 1";
        MensaDay day;
        String[] strings;
        String soup;
        String meal;
        double price;
        double priceBig;
        double oehBonus;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -cal.get(Calendar.DAY_OF_WEEK));
        switch (columns.get(0).text().replace((char) 0xA0, ' ').trim()) {
            case "SO":
                cal.add(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                break;
            case "MO":
                cal.add(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                break;
            case "DI":
                cal.add(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                break;
            case "MI":
                cal.add(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                break;
            case "DO":
                cal.add(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                break;
            case "FR":
                cal.add(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                break;
            case "SA":
                cal.add(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                break;
            default:
                return null;
        }

        day = new MensaDay(cal.getTime());
        mensa.addDay(day);

        strings = columns.get(1).text().replace((char) 0xA0, ' ').trim().split(",", 2);
        if (strings.length == 2) {
            soup = strings[0].trim();
            meal = strings[1].trim();
        } else {
            soup = null;
            meal = strings[0];
        }
        if (columns.get(2).text().replace((char) 0xA0, ' ').trim().isEmpty()) {
            price = parsePrice(nf, columns.get(3).text());
            priceBig = 0;
            oehBonus = 0;
            if (price == 1.3) {
                name = "Mehlspeise";
            }
        } else {
            price = parsePrice(nf, columns.get(2).text());
            priceBig = parsePrice(nf, columns.get(3).text());
            oehBonus = priceBig - price;
        }

        if (!TextUtils.isEmpty(meal)) {
            day.addMenu(new MensaMenu(name, soup, meal, price, priceBig, oehBonus));
        }

        return day;
    }

    @Override
    protected String getCacheKey() {
        return Mensen.MENSA_KHG;
    }

    @Override
    protected String getUrl() {
        return Consts.MENSA_MENU_KHG;
    }
}
