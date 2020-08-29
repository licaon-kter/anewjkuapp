/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2018 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp.kusss;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.analytics.AnalyticsHelper;

import java.util.regex.Pattern;

public class Course {

    private static final Pattern courseIdPattern = Pattern
            .compile(KusssHandler.PATTERN_LVA_NR_WITH_DOT);

    private final Term term;
    private String courseId;
    private String title;
    private int cid;
    private String teacher;
    private double ects;
    private String lvaType;
    private String code;

    private Course(@NonNull Term term, @NonNull String courseId) {
        this.term = term;
        this.courseId = courseId;
    }

    public Course(Context c, @NonNull Term term, Element row) {
        this(term, "");

        Elements columns = row.getElementsByTag("td");

        if (columns.size() >= 11) {
            try {
                boolean active = columns.get(9)
                        .getElementsByClass("assignment-active").size() == 1;
                String courseIdText = columns.get(6).text();
                if (active && courseIdPattern.matcher(courseIdText).matches()) {
                    this.courseId = courseIdText.toUpperCase().replace(".", "");
                    setTitle(columns.get(5).text());
                    setLvaType(columns.get(4).text()); // type (UE, ...)
                    setTeacher(columns.get(7).text()); // Leiter
                    String cidText = columns.get(2).text();
                    if (!TextUtils.isEmpty(cidText)) {
                        setCid(Integer.parseInt(cidText)); // curricula id
                    }
                    setECTS(Double.parseDouble(columns.get(8).text()
                            .replace(",", "."))); // ECTS
                    setCode(columns.get(3).text());
                }
            } catch (Exception e) {
                AnalyticsHelper.sendException(c, e, true, row.text());
            }
        }
    }

    public Course(@NonNull Term term, @NonNull String courseId, @NonNull String title, int cid, String teacher, double sws, double ects, @NonNull String lvaType, @NonNull String code) {
        this.term = term;
        this.courseId = courseId;
        this.title = title;
        this.cid = cid;
        this.teacher = teacher;
        this.ects = ects;
        this.lvaType = lvaType;
        this.code = code;
    }

    private void setCode(@NonNull String code) {
        this.code = code;
    }

    private void setCid(int cid) {
        this.cid = cid;
    }

    public int getCid() {
        return this.cid;
    }

    private void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getTeacher() {
        return this.teacher;
    }

    public double getSws() {
        return this.ects * 2 / 3;
    }

    private void setECTS(double ects) {
        this.ects = ects;
    }

    public double getEcts() {
        return this.ects;
    }

    private void setLvaType(@NonNull String type) {
        this.lvaType = type;
    }

    @NonNull
    public String getLvaType() {
        return this.lvaType;
    }

    private void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getTitle() {
        return this.title;
    }

    @NonNull
    public Term getTerm() {
        return this.term;
    }

    @NonNull
    public String getCourseId() {
        return this.courseId;
    }

    public boolean isInitialized() {
        return !term.isEmpty() && !courseId.isEmpty();
    }

    @NonNull
    public String getCode() {
        return this.code;
    }

}