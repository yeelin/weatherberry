package com.example.yeelin.homework2.h312yeelin.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by ninjakiki on 4/10/15.
 */
public class WeatherContentProvider extends ContentProvider {
    //logcat
    private static final String TAG = WeatherContentProvider.class.getCanonicalName();

    /**
     * UriMatcher
     */
    //UriMatcher: converts a Uri into an int/enum value for easier comparison
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //UriMatcher: possible return values for uriMatcher
    private static final int ALL_ROWS = 0;
    private static final int ROW_BY_ID = 1;
    private static final int ROW_BY_CITYID = 2;

    //UriMatcher:possible match patterns
    static {
        uriMatcher.addURI(BaseWeatherContract.AUTHORITY, BaseWeatherContract.PATH_TABLE, ALL_ROWS); //select all from a table
        uriMatcher.addURI(BaseWeatherContract.AUTHORITY, BaseWeatherContract.PATH_TABLE_ROWID, ROW_BY_ID); //select by rowId
        uriMatcher.addURI(BaseWeatherContract.AUTHORITY, BaseWeatherContract.PATH_TABLE_CITYID, ROW_BY_CITYID); //select by cityId
    }

    //helper selection strings
    private static final String WHERE_MATCHES_ID = BaseWeatherContract.whereClauseEquals(BaseWeatherContract.Columns._ID);
    private static final String WHERE_MATCHES_CITYID = BaseWeatherContract.whereClauseEquals(BaseWeatherContract.Columns.CITY_ID);

    //sqlite open helper to access the db
    private DBHelper dbHelper;

    /**
     * Create a sqlite open helper to access the db
     * @return
     */
    @Override
    public boolean onCreate() {
        dbHelper = DBHelper.getInstance(getContext());
        return true;
    }

    /**
     * Returns the type of data that will be returned given a uri.
     * @param uri
     * @return
     */
    @Override
    public String getType(Uri uri) {
        //Log.d(TAG, "getType");
        int match = uriMatcher.match(uri);
        String table;
        //UriMatchCodes code = UriMatchCodes.values()[match];
        switch (match) {
            case ALL_ROWS: //uri ends with "table"
                //return BaseWeatherContract.CONTENT_TYPE; //multiple items
                table = uri.getLastPathSegment();
                return getContentType(table);

            case ROW_BY_ID: //uri ends with "table/#"
                //return BaseWeatherContract.CONTENT_ITEM_TYPE; //specific item
                List<String> pathSegments = uri.getPathSegments();
                if (pathSegments.size() == 2) {
                    table = pathSegments.get(0);
                    return getContentItemType(table);
                }
                else {
                    throw new UnsupportedOperationException(String.format("Unknown path segments:%s in uri:%s:", pathSegments, uri));
                }

            case ROW_BY_CITYID: //uri ends with "table/cityId/#"
                //return BaseWeatherContract.CONTENT_ITEM_TYPE; //specific item
                pathSegments = uri.getPathSegments();
                if (pathSegments.size() == 3) {
                    table = pathSegments.get(0);
                    return getContentItemType(table);
                }
                else {
                    throw new UnsupportedOperationException(String.format("Unknown path segments:%s in uri:%s:", pathSegments, uri));
                }

            default:
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
        }
    }

    /**
     * Returns the content type given the last path segment in a uri.
     * @param lastPathSegment
     * @return
     */
    private String getContentType(String lastPathSegment) {
        //Log.d(TAG, "getContentType");
        switch (lastPathSegment) {
            case CurrentWeatherContract.TABLE:
                return CurrentWeatherContract.CONTENT_TYPE;
            case DailyForecastContract.TABLE:
                return DailyForecastContract.CONTENT_TYPE;
            case TriHourForecastContract.TABLE:
                return TriHourForecastContract.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown last path segment:" + lastPathSegment);
        }
    }

    /**
     * Returns the content item type given the table path segment in a uri.
     * @param pathSegment
     * @return
     */
    private String getContentItemType(String pathSegment) {
        //Log.d(TAG, "getContentItemType");
        switch (pathSegment) {
            case CurrentWeatherContract.TABLE:
                return CurrentWeatherContract.CONTENT_ITEM_TYPE;
            case DailyForecastContract.TABLE:
                return DailyForecastContract.CONTENT_ITEM_TYPE;
            case TriHourForecastContract.TABLE:
                return TriHourForecastContract.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown path segment:" + pathSegment);
        }
    }

    /**
     * Query operation. Supports select all, by row id and by city id.
     * Steps:
     * 1. get a readable db
     * 2. validate uri
     * 3. try to query
     * 4. register cursor with the uri so that caller will be notified of changes in the future
     * 5. return cursor
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        Log.d(TAG, String.format("query: Uri:%s, Projection:%s, Selection:%s, SelectionArgs:%s, Sort:%s", uri.toString(), Arrays.toString(projection), selection, Arrays.toString(selectionArgs), sortOrder));
        //get a readable db
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //validate uri
        int match = uriMatcher.match(uri);
        String useSelection = selection;
        String[] useSelectionArgs = selectionArgs;
        String table;

        switch (match) {
            case ALL_ROWS: //uri ends with "table"
                table = uri.getLastPathSegment();
                break;

            case ROW_BY_ID: //uri ends with "table/#"
                List<String> pathSegments = uri.getPathSegments();
                if (pathSegments.size() == 2) {
                    table = pathSegments.get(0);
                    useSelection = WHERE_MATCHES_ID;
                    useSelectionArgs = new String[]{ pathSegments.get(1) };
                }
                else {
                    throw new UnsupportedOperationException(String.format("Unknown path segments:%s in uri:%s:", pathSegments, uri));
                }
                break;

            case ROW_BY_CITYID: //uri ends with "table/cityId/#"
                pathSegments = uri.getPathSegments();
                if (pathSegments.size() == 3) {
                    table = pathSegments.get(0);

                    useSelection = WHERE_MATCHES_CITYID;
                    useSelectionArgs = new String[]{ pathSegments.get(2) };

                    //Hack to support date selection for trihour forecast
                    if (selection != null && selectionArgs != null) {
                        //append to the end
                        useSelection = useSelection + " AND " + selection;
                        useSelectionArgs = mergeSelectionArgs(useSelectionArgs, selectionArgs);
                    }
                }
                else {
                    throw new UnsupportedOperationException(String.format("Unknown path segments:%s in uri:%s:", pathSegments, uri));
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
        }

        //try to query
        Cursor cursor = db.query(
                table,
                projection,
                useSelection,
                useSelectionArgs,
                null, //groupby
                null, //having
                sortOrder);

        //register cursor to watch uri for changes so that caller will know if the data changes later
        Log.d(TAG, "query: setNotificationUri:" + uri.toString());
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        //return cursor
        return cursor;
    }

    /**
     * Insert operation.
     * @param uri
     * @param values
     * @return
     */
    @Override
    public Uri insert(Uri uri,
                      ContentValues values) {
        Log.d(TAG, String.format("insert: Uri:%s", uri.toString()));
        //get a writable db
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //validate uri
        int match = uriMatcher.match(uri);
        String table;

        switch (match) {
            case ALL_ROWS: //uri ends with "table"
                table = uri.getLastPathSegment();
                break;

            case ROW_BY_ID: //uri ends with "table/#"
                throw new UnsupportedOperationException("Unable to insert by row id. Uri: " + uri);

            case ROW_BY_CITYID: //uri ends with "table/cityId/#"
                List<String>pathSegments = uri.getPathSegments();
                if (pathSegments.size() == 3) {
                    table = pathSegments.get(0);
                }
                else {
                    throw new UnsupportedOperationException(String.format("Unknown path segments:%s in uri:%s:", pathSegments, uri));
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
        }
        //try to insert
        long id = -1;
        db.beginTransactionNonExclusive();
        try {
            id = db.insertWithOnConflict(
                    table,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
        //notify users with active cursors to reload data
        Log.d(TAG, "insert: notifyChange:" + uri.toString());
        getContext().getContentResolver().notifyChange(uri, null, false);
        //return uri with appended row id
        Log.d(TAG, "insert: Row inserted:" + id);
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Delete operation. Supports delete all, by row id and by city id.
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int delete(Uri uri,
                      String selection,
                      String[] selectionArgs) {
        Log.d(TAG, String.format("delete: Uri: %s, Selection:%s, SelectionArgs:%s", uri.toString(), selection, Arrays.toString(selectionArgs)));
        //get a writable db
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //validate uri
        int match = uriMatcher.match(uri);
        String useSelection = selection;
        String[] useSelectionArgs = selectionArgs;
        String table;

        switch (match) {
            case ALL_ROWS: //uri ends with "table"
                table = uri.getLastPathSegment();
                break;

            case ROW_BY_ID: //uri ends with "table/#"
                List<String> pathSegments = uri.getPathSegments();
                if (pathSegments.size() == 2) {
                    table = pathSegments.get(0);
                    useSelection = WHERE_MATCHES_ID;
                    useSelectionArgs = new String[]{ pathSegments.get(1) };
                }
                else {
                    throw new UnsupportedOperationException(String.format("Unknown path segments:%s in uri:%s:", pathSegments, uri));
                }
                break;

            case ROW_BY_CITYID: //uri ends with "table/cityId/#"
                pathSegments = uri.getPathSegments();
                if (pathSegments.size() == 3) {
                    table = pathSegments.get(0);
                    useSelection = WHERE_MATCHES_CITYID;
                    useSelectionArgs = new String[]{ pathSegments.get(2) };
                }
                else {
                    throw new UnsupportedOperationException(String.format("Unknown path segments:%s in uri:%s:", pathSegments, uri));
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
        }
        //try to delete
        int rowsDeleted = 0;
        db.beginTransactionNonExclusive();
        try {
            rowsDeleted = db.delete(
                    table,
                    useSelection,
                    useSelectionArgs);
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
        //notify users with active cursors to reload data
        Log.d(TAG, "delete: notifyChange:" + uri.toString());
        getContext().getContentResolver().notifyChange(uri, null, false);
        //return count of rows deleted
        Log.d(TAG, "delete: Rows deleted:" + rowsDeleted);
        return rowsDeleted;
    }

    /**
     * Update operation. Supports update all, by row id, and by city id.
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(Uri uri,
                      ContentValues values,
                      String selection,
                      String[] selectionArgs) {
        Log.d(TAG, String.format("update: Uri:%s Selection:%s SelectionArgs:%s", uri.toString(), selection, Arrays.toString(selectionArgs)));
        //get a writable db
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //validate uri
        int match = uriMatcher.match(uri);
        String useSelection = selection;
        String[] useSelectionArgs = selectionArgs;
        String table;

        switch (match) {
            case ALL_ROWS: //ends with "table"
                table = uri.getLastPathSegment();

                break;

            case ROW_BY_ID: //ends with "table/#"
                List<String> pathSegments = uri.getPathSegments();
                if (pathSegments.size() == 2) {
                    table = pathSegments.get(0);
                    useSelection = WHERE_MATCHES_ID;
                    useSelectionArgs = new String[]{ pathSegments.get(1) };
                }
                else {
                    throw new UnsupportedOperationException(String.format("Unknown path segments:%s in uri:%s:", pathSegments, uri));
                }
                break;

            case ROW_BY_CITYID: //ends with "table/cityId/#"
                pathSegments = uri.getPathSegments();
                if (pathSegments.size() == 3) {
                    table = pathSegments.get(0);
                    useSelection = WHERE_MATCHES_CITYID;
                    useSelectionArgs = new String[]{ pathSegments.get(2) };
                }
                else {
                    throw new UnsupportedOperationException(String.format("Unknown path segments:%s in uri:%s:", pathSegments, uri));
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
        }
        //try to update
        int rowsUpdated = 0;
        db.beginTransactionNonExclusive();
        try {
            rowsUpdated = db.update(
                    table,
                    values,
                    useSelection,
                    useSelectionArgs);
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
        //notify users with active cursors to reload data
        Log.d(TAG, "update: notifyChange:" + uri.toString());
        getContext().getContentResolver().notifyChange(uri, null, false);
        //return count of rows updated
        Log.d(TAG, "update: Rows updated:" + rowsUpdated);
        return rowsUpdated;
    }

    /**
     * Bulk insert operation
     * @param uri
     * @param valuesArray
     * @return
     */
    @Override
    public int bulkInsert(Uri uri,
                          @NonNull ContentValues[] valuesArray) {
        Log.d(TAG, String.format("bulkInsert: Uri:%s", uri.toString()));
        //get writable db
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //validate uri
        int match = uriMatcher.match(uri);
        String table;

        switch (match) {
            case ALL_ROWS: //uri ends with "table"
                table = uri.getLastPathSegment();
                break;

            case ROW_BY_ID: //uri ends with "table/#"
                throw new UnsupportedOperationException("Unable to bulk insert by row id. Uri: " + uri);

            case ROW_BY_CITYID:
                throw new UnsupportedOperationException("Unable to bulk insert by city id. Uri: " + uri);

            default:
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
        }
        //try to bulk insert
        int rowsInserted = 0;
        db.beginTransactionNonExclusive();
        try {
            for (ContentValues values : valuesArray) {
                long id = db.insertWithOnConflict(
                        table,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE);
                if (id != -1) {
                    ++rowsInserted;
                }
                else {
                    Log.d(TAG, "Bulk insert failed for row:" + values);
                }
            }
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
        //notify users with active cursors to reload data
        Log.d(TAG, "bulkInsert: notifyChange:" + uri.toString());
        getContext().getContentResolver().notifyChange(uri, null, false);
        //return count of rows inserted
        Log.d(TAG, "bulkInsert: Rows inserted:" + rowsInserted);
        return rowsInserted;
    }

    /**
     * Helper method to merge two selection args string arrays together.
     * @param firstSelectionArgs
     * @param secondSelectionArgs
     * @return
     */
    private String[] mergeSelectionArgs (String[] firstSelectionArgs, String[] secondSelectionArgs) {
        List<String> bothSelectionArgs = new ArrayList<>(firstSelectionArgs.length + secondSelectionArgs.length);
        Collections.addAll(bothSelectionArgs, firstSelectionArgs);
        Collections.addAll(bothSelectionArgs, secondSelectionArgs);

        return bothSelectionArgs.toArray(new String[bothSelectionArgs.size()]);
    }
}
