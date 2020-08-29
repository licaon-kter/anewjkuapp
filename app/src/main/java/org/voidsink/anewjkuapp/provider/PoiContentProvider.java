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

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.voidsink.anewjkuapp.PoiContentContract;

public class PoiContentProvider extends ContentProvider {

    private static final int CODE_POI = 1;
    private static final int CODE_POI_ID = 2;
    private static final int CODE_POI_SEARCH = 3;
    private static final int CODE_POI_BY_NAME = 4;

    private static final UriMatcher sUriMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PoiContentContract.AUTHORITY,
                PoiContentContract.Poi.PATH, CODE_POI);
        sUriMatcher.addURI(PoiContentContract.AUTHORITY,
                PoiContentContract.Poi.PATH + "/#", CODE_POI_ID);
        sUriMatcher.addURI(PoiContentContract.AUTHORITY,
                PoiContentContract.Poi.PATH + "/*", CODE_POI_BY_NAME);
        sUriMatcher.addURI(PoiContentContract.AUTHORITY,
                SearchManager.SUGGEST_URI_PATH_QUERY, CODE_POI_SEARCH);
        sUriMatcher.addURI(PoiContentContract.AUTHORITY,
                SearchManager.SUGGEST_URI_PATH_QUERY + "/*", CODE_POI_SEARCH);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = KusssDatabaseHelper.getInstance(getContext()).getWritableDatabase();
        int rowsDeleted;
        switch (sUriMatcher.match(uri)) {
            case CODE_POI:
                rowsDeleted = db.delete(PoiContentContract.Poi.TABLE_NAME,
                        selection, selectionArgs);
                break;
            case CODE_POI_ID:
                String whereIdClause = PoiContentContract.Poi.COL_ROWID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                rowsDeleted = db.delete(PoiContentContract.Poi.TABLE_NAME,
                        whereIdClause, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        // Notifying the changes, if there are any
        if (rowsDeleted != -1)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CODE_POI:
                return PoiContentContract.CONTENT_TYPE_DIR + "/"
                        + PoiContentContract.Poi.MIMETYPE;
            case CODE_POI_ID:
            case CODE_POI_BY_NAME:
                return PoiContentContract.CONTENT_TYPE_ITEM + "/"
                        + PoiContentContract.Poi.MIMETYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = KusssDatabaseHelper.getInstance(getContext()).getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case CODE_POI: {
                long id = db.insertOrThrow(PoiContentContract.Poi.TABLE_NAME, null, values);
                if (id != -1)
                    getContext().getContentResolver().notifyChange(uri, null);
                return PoiContentContract.Poi.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(id)).build();
            }
            default: {
                throw new IllegalArgumentException("Unsupported URI: " + uri);
            }
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

        builder.setTables(PoiContentContract.Poi.TABLE_NAME);

        String limit = null;

        /*
         * Choose the table to query and a sort order based on the code returned
         * for the incoming URI. Here, too, only the statements for table 3 are
         * shown.
         */
        switch (sUriMatcher.match(uri)) {
            case CODE_POI_ID:
                builder.appendWhere(PoiContentContract.Poi.COL_ROWID + "="
                        + uri.getLastPathSegment());
                break;
            case CODE_POI:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = PoiContentContract.Poi.COL_NAME + " ASC";
                break;
            case CODE_POI_BY_NAME:
                builder.appendWhere(PoiContentContract.Poi.COL_NAME + "='"
                        + Uri.decode(uri.getLastPathSegment()) + "'");
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = PoiContentContract.Poi.COL_NAME + " ASC";
                break;
            case CODE_POI_SEARCH:
                limit = uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT);
                try {
                    if (limit != null) {
                        Integer.parseInt(limit);
                    }
                } catch (NumberFormatException e) {
                    limit = null;
                }

                if (selection == null) {
                    selection = PoiContentContract.Poi.TABLE_NAME + " MATCH ?";
                    selectionArgs = new String[]{Uri.decode(uri.getLastPathSegment()) + "*"};
                }
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = PoiContentContract.Poi.COL_NAME + " ASC";
                }
                if (projection == null) {
                    projection = new String[]{
                            PoiContentContract.Poi.COL_ROWID + " AS "
                                    + BaseColumns._ID,
                            PoiContentContract.Poi.COL_NAME + " AS "
                                    + SearchManager.SUGGEST_COLUMN_TEXT_1,
                            PoiContentContract.Poi.COL_DESCR + " AS "
                                    + SearchManager.SUGGEST_COLUMN_TEXT_2,
                            PoiContentContract.Poi.COL_ROWID + " AS "
                                    + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};
                }
                break;
            default:
                throw new IllegalArgumentException("URI " + uri
                        + " is not supported.");
        }
        return builder.query(db, projection, selection, selectionArgs,
                null, null, sortOrder, limit);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = KusssDatabaseHelper.getInstance(getContext()).getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case CODE_POI: {
                return db.update(PoiContentContract.Poi.TABLE_NAME, values,
                        selection, selectionArgs);
            }
            case CODE_POI_ID: {
                String whereIdClause = PoiContentContract.Poi.COL_ROWID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                return db.update(PoiContentContract.Poi.TABLE_NAME, values,
                        whereIdClause, selectionArgs);
            }
            default:
                throw new IllegalArgumentException("URI " + uri
                        + " is not supported.");
        }
    }
}
