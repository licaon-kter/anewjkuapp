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

package org.voidsink.anewjkuapp.provider;

import android.accounts.Account;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.analytics.AnalyticsHelper;
import org.voidsink.anewjkuapp.kusss.Assessment;
import org.voidsink.anewjkuapp.kusss.Course;
import org.voidsink.anewjkuapp.kusss.Curriculum;
import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class KusssContentProvider extends ContentProvider {

    private static final int CODE_COURSE = 1;
    private static final int CODE_COURSE_ID = 2;
    private static final int CODE_EXAM = 3;
    private static final int CODE_EXAM_ID = 4;
    private static final int CODE_GRADE = 5;
    private static final int CODE_GRADE_ID = 6;
    private static final int CODE_CURRICULA = 7;
    private static final int CODE_CURRICULA_ID = 8;

    private static final UriMatcher sUriMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Course.PATH, CODE_COURSE);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Course.PATH + "/#", CODE_COURSE_ID);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Exam.PATH, CODE_EXAM);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Exam.PATH + "/#", CODE_EXAM_ID);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Assessment.PATH, CODE_GRADE);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Assessment.PATH + "/#", CODE_GRADE_ID);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Curricula.PATH, CODE_CURRICULA);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Curricula.PATH + "/#", CODE_CURRICULA_ID);
    }

    private String[] getAdditionalData(ContentValues values, String selection, String[] selectionArgs) {
        List<String> additionalDataList = new ArrayList<>();
        if (values != null) {
            additionalDataList.add(values.toString());
        }
        if (!TextUtils.isEmpty(selection)) {
            additionalDataList.add(selection);
        }
        if (selectionArgs != null) {
            additionalDataList.addAll(Arrays.asList(selectionArgs));
        }
        return additionalDataList.toArray(new String[]{});
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = KusssDatabaseHelper.getInstance(getContext()).getWritableDatabase();

        try {
            String whereIdClause;
            int rowsDeleted;

            switch (sUriMatcher.match(uri)) {
                case CODE_COURSE:
                    rowsDeleted = db.delete(KusssContentContract.Course.TABLE_NAME,
                            selection, selectionArgs);
                    break;
                case CODE_EXAM:
                    rowsDeleted = db.delete(KusssContentContract.Exam.TABLE_NAME,
                            selection, selectionArgs);
                    break;
                case CODE_GRADE:
                    rowsDeleted = db.delete(
                            KusssContentContract.Assessment.TABLE_NAME, selection,
                            selectionArgs);
                    break;
                case CODE_CURRICULA:
                    rowsDeleted = db.delete(
                            KusssContentContract.Curricula.TABLE_NAME, selection,
                            selectionArgs);
                    break;
                case CODE_COURSE_ID:
                    whereIdClause = KusssContentContract.Course.COL_ID + "="
                            + uri.getLastPathSegment();
                    if (!TextUtils.isEmpty(selection))
                        whereIdClause += " AND " + selection;
                    rowsDeleted = db.delete(KusssContentContract.Course.TABLE_NAME,
                            whereIdClause, selectionArgs);
                    break;
                case CODE_EXAM_ID:
                    whereIdClause = KusssContentContract.Exam.COL_ID + "="
                            + uri.getLastPathSegment();
                    if (!TextUtils.isEmpty(selection))
                        whereIdClause += " AND " + selection;
                    rowsDeleted = db.delete(KusssContentContract.Exam.TABLE_NAME,
                            whereIdClause, selectionArgs);
                    break;
                case CODE_GRADE_ID:
                    whereIdClause = KusssContentContract.Assessment.COL_ID + "="
                            + uri.getLastPathSegment();
                    if (!TextUtils.isEmpty(selection))
                        whereIdClause += " AND " + selection;
                    rowsDeleted = db.delete(
                            KusssContentContract.Assessment.TABLE_NAME, whereIdClause,
                            selectionArgs);
                    break;
                case CODE_CURRICULA_ID:
                    whereIdClause = KusssContentContract.Curricula.COL_ID + "="
                            + uri.getLastPathSegment();
                    if (!TextUtils.isEmpty(selection))
                        whereIdClause += " AND " + selection;
                    rowsDeleted = db.delete(
                            KusssContentContract.Curricula.TABLE_NAME, whereIdClause,
                            selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported URI: " + uri);
            }
            // Notifying the changes, if there are any
            if (rowsDeleted != -1)
                getContext().getContentResolver().notifyChange(uri, null);
            return rowsDeleted;
        } catch (Exception e) {
            AnalyticsHelper.sendException(getContext(), e, true, getAdditionalData(null, selection, selectionArgs));
            throw e;
        }
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CODE_COURSE:
                return KusssContentContract.CONTENT_TYPE_DIR + "/"
                        + KusssContentContract.Course.PATH;
            case CODE_COURSE_ID:
                return KusssContentContract.CONTENT_TYPE_ITEM + "/"
                        + KusssContentContract.Course.PATH;
            case CODE_EXAM:
                return KusssContentContract.CONTENT_TYPE_DIR + "/"
                        + KusssContentContract.Exam.PATH;
            case CODE_EXAM_ID:
                return KusssContentContract.CONTENT_TYPE_ITEM + "/"
                        + KusssContentContract.Exam.PATH;
            case CODE_GRADE:
                return KusssContentContract.CONTENT_TYPE_DIR + "/"
                        + KusssContentContract.Assessment.PATH;
            case CODE_GRADE_ID:
                return KusssContentContract.CONTENT_TYPE_ITEM + "/"
                        + KusssContentContract.Assessment.PATH;
            case CODE_CURRICULA:
                return KusssContentContract.CONTENT_TYPE_DIR + "/"
                        + KusssContentContract.Curricula.PATH;
            case CODE_CURRICULA_ID:
                return KusssContentContract.CONTENT_TYPE_ITEM + "/"
                        + KusssContentContract.Curricula.PATH;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = KusssDatabaseHelper.getInstance(getContext()).getWritableDatabase();
        try {
            switch (sUriMatcher.match(uri)) {
                case CODE_COURSE: {
                    long id = db.insertOrThrow(KusssContentContract.Course.TABLE_NAME, null,
                            values);
                    if (id != -1)
                        getContext().getContentResolver().notifyChange(uri, null);
                    return KusssContentContract.Course.CONTENT_URI.buildUpon()
                            .appendPath(String.valueOf(id)).build();
                }
                case CODE_EXAM: {
                    long id = db.insertOrThrow(KusssContentContract.Exam.TABLE_NAME,
                            null, values);
                    if (id != -1)
                        getContext().getContentResolver().notifyChange(uri, null);
                    return KusssContentContract.Exam.CONTENT_URI.buildUpon()
                            .appendPath(String.valueOf(id)).build();
                }
                case CODE_GRADE: {
                    long id = db.insertOrThrow(KusssContentContract.Assessment.TABLE_NAME,
                            null, values);
                    if (id != -1)
                        getContext().getContentResolver().notifyChange(uri, null);
                    return KusssContentContract.Assessment.CONTENT_URI.buildUpon()
                            .appendPath(String.valueOf(id)).build();
                }
                case CODE_CURRICULA: {
                    long id = db.insertOrThrow(KusssContentContract.Curricula.TABLE_NAME,
                            null, values);
                    if (id != -1)
                        getContext().getContentResolver().notifyChange(uri, null);
                    return KusssContentContract.Curricula.CONTENT_URI.buildUpon()
                            .appendPath(String.valueOf(id)).build();
                }
                default: {
                    throw new IllegalArgumentException("Unsupported URI: " + uri);
                }
            }
        } catch (Exception e) {
            AnalyticsHelper.sendException(getContext(), e, true, getAdditionalData(values, null, null));
            throw e;
        }
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = KusssDatabaseHelper.getInstance(getContext()).getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        String table;

        /*
         * Choose the table to query and a sort order based on the code returned
         * for the incoming URI. Here, too, only the statements for table 3 are
         * shown.
         */
        switch (sUriMatcher.match(uri)) {
            case CODE_COURSE_ID:
                table = KusssContentContract.Course.TABLE_NAME;
                builder.appendWhere(KusssContentContract.Course.COL_ID + "="
                        + uri.getLastPathSegment());
                break;
            case CODE_COURSE:
                table = KusssContentContract.Course.TABLE_NAME;
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = KusssContentContract.Course.COL_ID + " ASC";
                break;
            case CODE_EXAM_ID:
                table = KusssContentContract.Exam.TABLE_NAME;
                builder.appendWhere(KusssContentContract.Exam.COL_ID + "="
                        + uri.getLastPathSegment());
                break;
            case CODE_EXAM:
                table = KusssContentContract.Exam.TABLE_NAME;
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = KusssContentContract.Exam.COL_ID + " ASC";
                break;
            case CODE_GRADE_ID:
                table = KusssContentContract.Assessment.TABLE_NAME;
                builder.appendWhere(KusssContentContract.Assessment.COL_ID + "="
                        + uri.getLastPathSegment());
                break;
            case CODE_GRADE:
                table = KusssContentContract.Assessment.TABLE_NAME;
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = KusssContentContract.Assessment.COL_ID + " ASC";
                break;
            case CODE_CURRICULA_ID:
                table = KusssContentContract.Curricula.TABLE_NAME;
                builder.appendWhere(KusssContentContract.Curricula.COL_ID + "="
                        + uri.getLastPathSegment());
                break;
            case CODE_CURRICULA:
                table = KusssContentContract.Curricula.TABLE_NAME;
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = KusssContentContract.Curricula.COL_ID + " ASC";
                break;
            default:
                throw new IllegalArgumentException("URI " + uri
                        + " is not supported.");
        }
        builder.setTables(table);
        return builder.query(db, projection, selection, selectionArgs,
                null, null, sortOrder);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = KusssDatabaseHelper.getInstance(getContext()).getWritableDatabase();

        try {
            switch (sUriMatcher.match(uri)) {
                case CODE_COURSE: {
                    return db.update(KusssContentContract.Course.TABLE_NAME, values,
                            selection, selectionArgs);
                }
                case CODE_EXAM: {
                    return db.update(KusssContentContract.Exam.TABLE_NAME, values,
                            selection, selectionArgs);
                }
                case CODE_GRADE: {
                    return db.update(KusssContentContract.Assessment.TABLE_NAME,
                            values, selection, selectionArgs);
                }
                case CODE_CURRICULA: {
                    return db.update(KusssContentContract.Curricula.TABLE_NAME,
                            values, selection, selectionArgs);
                }
                case CODE_COURSE_ID: {
                    String whereIdClause = KusssContentContract.Course.COL_ID + "="
                            + uri.getLastPathSegment();
                    if (!TextUtils.isEmpty(selection))
                        whereIdClause += " AND " + selection;
                    return db.update(KusssContentContract.Course.TABLE_NAME, values,
                            whereIdClause, selectionArgs);
                }
                case CODE_EXAM_ID: {
                    String whereIdClause = KusssContentContract.Exam.COL_ID + "="
                            + uri.getLastPathSegment();
                    if (!TextUtils.isEmpty(selection))
                        whereIdClause += " AND " + selection;
                    return db.update(KusssContentContract.Exam.TABLE_NAME, values,
                            whereIdClause, selectionArgs);
                }
                case CODE_GRADE_ID: {
                    String whereIdClause = KusssContentContract.Assessment.COL_ID + "="
                            + uri.getLastPathSegment();
                    if (!TextUtils.isEmpty(selection))
                        whereIdClause += " AND " + selection;
                    return db.update(KusssContentContract.Assessment.TABLE_NAME,
                            values, whereIdClause, selectionArgs);
                }
                case CODE_CURRICULA_ID: {
                    String whereIdClause = KusssContentContract.Curricula.COL_ID + "="
                            + uri.getLastPathSegment();
                    if (!TextUtils.isEmpty(selection))
                        whereIdClause += " AND " + selection;
                    return db.update(KusssContentContract.Curricula.TABLE_NAME,
                            values, whereIdClause, selectionArgs);
                }
                default:
                    throw new IllegalArgumentException("URI " + uri
                            + " is not supported.");
            }
        } catch (Exception e) {
            AnalyticsHelper.sendException(getContext(), e, true, getAdditionalData(values, selection, selectionArgs));
            throw e;
        }
    }

    public static List<Assessment> getAssessmentsFromCursor(Context context, Cursor data) {
        List<Assessment> mAssessments = new ArrayList<>();

        if (data != null) {
            data.moveToFirst();
            data.moveToPrevious();
            try {
                while (data.moveToNext()) {
                    mAssessments.add(KusssHelper.createAssessment(data));
                }
            } catch (ParseException e) {
                AnalyticsHelper.sendException(context, e, false);
                mAssessments.clear();
            }
        }
        return mAssessments;
    }

    private static List<Assessment> getAssessments(Context context) {
        List<Assessment> mAssessments = new ArrayList<>();

        Account mAccount = AppUtils.getAccount(context);
        if (mAccount != null) {
            ContentResolver cr = context.getContentResolver();
            try (Cursor c = cr.query(KusssContentContract.Assessment.CONTENT_URI,
                    KusssContentContract.Assessment.DB.PROJECTION, null, null,
                    KusssContentContract.Assessment.TABLE_NAME + "."
                            + KusssContentContract.Assessment.COL_TYPE
                            + " ASC,"
                            + KusssContentContract.Assessment.TABLE_NAME + "."
                            + KusssContentContract.Assessment.COL_DATE
                            + " DESC")) {
                if (c != null) {
                    mAssessments = getAssessmentsFromCursor(context, c);
                }
            }
        }
        return mAssessments;
    }

    public static List<Course> getCoursesFromCursor(Context context, Cursor c) {
        List<Course> courses = new ArrayList<>();

        if (c != null) {
            c.moveToFirst();
            c.moveToPrevious();
            try {
                while (c.moveToNext()) {
                    courses.add(KusssHelper.createCourse(c));
                }
            } catch (ParseException e) {
                AnalyticsHelper.sendException(context, e, false);
                courses.clear();
            }
        }
        return courses;
    }

    public static List<Curriculum> getCurriculaFromCursor(Cursor c) {
        List<Curriculum> mCurriculum = new ArrayList<>();
        if (c != null) {
            c.moveToFirst();
            c.moveToPrevious();
            while (c.moveToNext()) {
                mCurriculum.add(KusssHelper.createCurricula(c));
            }
            AppUtils.sortCurricula(mCurriculum);
        }
        return mCurriculum;
    }

    private static List<Curriculum> getCurricula(Context context) {
        List<Curriculum> mCurriculum = new ArrayList<>();
        Account mAccount = AppUtils.getAccount(context);
        if (mAccount != null) {
            ContentResolver cr = context.getContentResolver();

            try (Cursor c = cr.query(KusssContentContract.Curricula.CONTENT_URI,
                    KusssContentContract.Curricula.DB.PROJECTION, null, null,
                    KusssContentContract.Curricula.COL_DT_START + " DESC")) {
                if (c != null) {
                    mCurriculum = getCurriculaFromCursor(c);
                }
            }
        }

        return mCurriculum;
    }

    public static List<Term> getTerms(Context context) {
        List<String> terms = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        List<Curriculum> mCurriculum = getCurricula(context);

        if (mCurriculum == null) {
            mCurriculum = new ArrayList<>();
        }

        if (mCurriculum.size() == 0) {
            AppUtils.triggerSync(context, false, Consts.ARG_WORKER_KUSSS_CURRICULA);

            try {
                List<Assessment> assessments = getAssessments(context);

                Date dtStart = null;

                for (Assessment assessment : assessments) {
                    if (assessment.isInitialized()) {
                        Date date = assessment.getDate();
                        if (dtStart == null || date.before(dtStart)) {
                            dtStart = date;
                        }
                    }
                }

                if (dtStart != null) {
                    // subtract -1 term for sure
                    cal.setTime(dtStart);
                    cal.add(Calendar.MONTH, -6);
                    dtStart = cal.getTime();

                    mCurriculum.add(new Curriculum(dtStart, null));
                }
            } catch (Exception e) {
                AnalyticsHelper.sendException(context, e, false);
            }
        }

        // always load current term, subtract -1 term for sure
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, -6);
        mCurriculum.add(new Curriculum(cal.getTime(), null));

        if (mCurriculum.size() > 0) {
            // calculate terms from curricula duration
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, 1);
            Date then = cal.getTime();

            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            int year = 2010;

            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, Calendar.MARCH);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Date startSS = cal.getTime(); // 1.3.

            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, Calendar.OCTOBER);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Date startWS = cal.getTime(); // 1.10.

            while (startSS.before(then) || startWS.before(then)) {
                if (startSS.before(then) && dateInRange(startSS, mCurriculum)) {
                    terms.add(String.format(Locale.GERMAN, "%dS", year));
                }
                if (startWS.before(then) && dateInRange(startWS, mCurriculum)) {
                    terms.add(String.format(Locale.GERMAN, "%dW", year));
                }

                // inc year
                year++;

                cal.setTime(startSS);
                cal.set(Calendar.YEAR, year);
                startSS.setTime(cal.getTimeInMillis());

                cal.setTime(startWS);
                cal.set(Calendar.YEAR, year);
                startWS.setTime(cal.getTimeInMillis());
            }
        }

        /*
        if (terms.size() == 0) {
            // get Terms from Data, may take a little bit longer
        }
        */

        Collections.sort(terms, (lhs, rhs) -> rhs.compareTo(lhs));

        List<Term> objects = new ArrayList<>();
        try {
            for (String term : terms) {
                objects.add(Term.parseTerm(term));
            }
        } catch (ParseException e) {
            AnalyticsHelper.sendException(context, e, true);
            objects.clear();
        }

        return Collections.unmodifiableList(objects);
    }

    public static Term getLastTerm(Context c) {
        List<Term> terms = getTerms(c);
        if (terms.size() == 0) {
            return null;
        }
        return terms.get(0);
    }

    private static boolean dateInRange(Date date, List<Curriculum> curricula) {
        for (Curriculum curriculum : curricula) {
            if (curriculum.dateInRange(date)) {
                return true;
            }
        }
        return false;
    }
}
