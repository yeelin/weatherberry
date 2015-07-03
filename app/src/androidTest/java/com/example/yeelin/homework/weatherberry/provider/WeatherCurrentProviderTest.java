package com.example.yeelin.homework.weatherberry.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Tests the current weather provider.
 * Created by ninjakiki on 4/10/15.
 */
public class WeatherCurrentProviderTest extends AndroidTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cleanupDb();
    }

    private void cleanupDb() {
        DBHelper dbHelper = DBHelper.getInstance(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(CurrentWeatherContract.TABLE, null, null);
        db.close();
    }

    /**
     * Tests provider's getType method
     * @throws Exception
     */
    public void testGetType() throws Throwable {
        //test using base uri
        Uri uriWithoutId = CurrentWeatherContract.URI;
        String type = getContext().getContentResolver().getType(uriWithoutId);
        assertEquals(CurrentWeatherContract.CONTENT_TYPE, type);

        //test using base uri and city_id
        Uri uriWithId = ContentUris.withAppendedId(CurrentWeatherContract.URI, DataUtilities.SEATTLE_CITY_ID);
        type = getContext().getContentResolver().getType(uriWithId);
        assertEquals(CurrentWeatherContract.CONTENT_ITEM_TYPE, type);
    }

    /**
     * Tests provider's CRUD operations using row id returned by insert
     * @throws java.lang.Throwable
     */
    public void testCRUD_usingRowId() throws Throwable {
        /**
         * Test insert
         */
        ContentValues insertValues = DataUtilities.CurrentWeather.insertValues_Seattle();
        Uri uriWithRowId = getContext().getContentResolver().insert(
                CurrentWeatherContract.URI, //uri
                insertValues); //values
        assertNotNull("Error: Uri returned by insert is null", uriWithRowId);

        long rowId = ContentUris.parseId(uriWithRowId);
        Cursor insertCursor = getContext().getContentResolver().query(
                uriWithRowId, //uri
                null, //projection
                null, //selection
                null, //selectionArgs
                null);//sortOrder
        CursorUtilities.validateCursor("Insert Cursor", insertCursor, insertValues, rowId);
        insertCursor.close();

        /**
         * Test update using row id
         */
        ContentValues updateValues = DataUtilities.CurrentWeather.updateValues_Seattle();
        int numRowsUpdated = getContext().getContentResolver().update(
                uriWithRowId, //uri
                updateValues, //values
                BaseWeatherContract.whereClauseEquals(CurrentWeatherContract.Columns._ID), //selection
                BaseWeatherContract.whereArgs(rowId)); //selectionArgs
        assertTrue("Error: Rows updated != 1", numRowsUpdated == 1);

        Cursor updateCursor = getContext().getContentResolver().query(
                uriWithRowId, //uri
                null, //projection
                null, //selection
                null, //selectionArgs
                null);//sortOrder
        CursorUtilities.validateCursor("Update Cursor", updateCursor, updateValues, rowId);
        updateCursor.close();

        /**
         * Test delete using row id
         */
        int numRowsDeleted = getContext().getContentResolver().delete(
                uriWithRowId, //uri
                BaseWeatherContract.whereClauseEquals(CurrentWeatherContract.Columns._ID), //selection
                BaseWeatherContract.whereArgs(rowId)); //selectionArgs
        assertTrue("Error: Rows deleted != 1", numRowsDeleted == 1);

        Cursor deleteCursor = getContext().getContentResolver().query(
                uriWithRowId, //uri
                null, //projection
                null, //selection
                null, //selectionArgs
                null);//sortOrder
        assertFalse("Error: Delete cursor is not empty", deleteCursor.moveToFirst());
        deleteCursor.close();
    }

    /**
     * Tests provider's CRUD operations using city id
     * @throws Throwable
     */
    public void testCRUD_UsingCityId() throws Throwable {
        /**
         * Test insert
         */
        ContentValues insertValues = DataUtilities.CurrentWeather.insertValues_Seattle();
        Uri uriWithRowId = getContext().getContentResolver().insert(
                CurrentWeatherContract.URI, //uri
                insertValues); //values
        assertNotNull("Error: Uri returned by insert is null", uriWithRowId);

        long rowId = ContentUris.parseId(uriWithRowId);
        Uri uriWithCityId = ContentUris.withAppendedId(CurrentWeatherContract.URI, DataUtilities.SEATTLE_CITY_ID);
        Cursor insertCursor = getContext().getContentResolver().query(
                uriWithCityId, //uri
                null, //projection
                null, //selection
                null, //selectionArgs
                null);//sortOrder
        CursorUtilities.validateCursor("Insert Cursor", insertCursor, insertValues, rowId);
        insertCursor.close();

        /**
         * Test update using city id
         */
        ContentValues updateValues = DataUtilities.CurrentWeather.updateValues_Seattle();
        int numRowsUpdated = getContext().getContentResolver().update(
                uriWithCityId, //uri
                updateValues, //values
                BaseWeatherContract.whereClauseEquals(CurrentWeatherContract.Columns.CITY_ID), //selection
                BaseWeatherContract.whereArgs(DataUtilities.SEATTLE_CITY_ID)); //selectionArgs
        assertTrue("Error: Rows updated != 1", numRowsUpdated == 1);

        Cursor updateCursor = getContext().getContentResolver().query(
                uriWithCityId, //uri
                null, //projection
                null, //selection
                null, //selectionArgs
                null);//sortOrder
        CursorUtilities.validateCursor("Update Cursor", updateCursor, updateValues, rowId);
        updateCursor.close();

        /**
         * Test delete using city id
         */
        int numRowsDeleted = getContext().getContentResolver().delete(
                uriWithCityId, //uri
                BaseWeatherContract.whereClauseEquals(CurrentWeatherContract.Columns.CITY_ID), //selection
                BaseWeatherContract.whereArgs(DataUtilities.SEATTLE_CITY_ID)); //selectionArgs
        assertTrue("Error: Rows deleted != 1", numRowsDeleted == 1);

        Cursor deleteCursor = getContext().getContentResolver().query(
                uriWithCityId, //uri
                null, //projection
                null, //selection
                null, //selectionArgs
                null);//sortOrder
        assertFalse("Error: Delete cursor is not empty", deleteCursor.moveToFirst());
        deleteCursor.close();
    }

    /**
     * Tests provider's bulk insert method
     * @throws Throwable
     */
    public void testBulkCRUD() throws Throwable {
        /**
         * Test bulk insert
         */
        ContentValues[] bulkInsertValues = DataUtilities.CurrentWeather.insertBulkValues();
        int numRowsInserted = getContext().getContentResolver().bulkInsert(
                CurrentWeatherContract.URI, //uri
                bulkInsertValues); //values
        assertTrue("Error: Rows bulk inserted != 2", numRowsInserted == 2);

        Cursor bulkInsertCursor = getContext().getContentResolver().query(
                CurrentWeatherContract.URI, //uri
                null, //projection
                null, //selection
                null, //selectionArgs
                null); //sortOrder
        CursorUtilities.validateCursor("Bulk Insert Cursor", bulkInsertCursor, bulkInsertValues);
        bulkInsertCursor.close();

        /**
         * Test bulk update
         */
        ContentValues updateValues = DataUtilities.updateBulkCurrentWeatherTimestamp();
        int numRowsUpdated = getContext().getContentResolver().update(
                CurrentWeatherContract.URI, //uri
                updateValues, //values
                null, //selection
                null); //selectionArgs
        assertTrue("Error: Rows bulk updated != 2", numRowsUpdated == 2);

        ContentValues[] mergedValues = DataUtilities.mergeValues(bulkInsertValues, updateValues);
        Cursor bulkUpdateCursor = getContext().getContentResolver().query(
                CurrentWeatherContract.URI, //uri
                null, //projection
                null, //selection
                null, //selectionArgs
                null); //sortOrder
        CursorUtilities.validateCursor("Bulk Update Cursor", bulkUpdateCursor, mergedValues);
        bulkUpdateCursor.close();

        /**
         * Test bulk delete
         */
        int numRowsDeleted = getContext().getContentResolver().delete(
                CurrentWeatherContract.URI, //uri
                null, //selection
                null); //selectionArgs
        assertTrue("Error: Rows bulk deleted != 2", numRowsDeleted == 2);

        Cursor bulkDeleteCursor = getContext().getContentResolver().query(
                CurrentWeatherContract.URI, //uri
                null, //projection
                null, //selection
                null, //selectionArgs
                null); //sortOrder
        assertFalse("Error: Bulk delete cursor is not empty", bulkDeleteCursor.moveToFirst());
        bulkDeleteCursor.close();
    }

    /**
     * Tests all errors from insert, update, query, and delete methods
     * @throws Throwable
     */
    public void testErrors() throws Throwable {
        ContentValues insertValues = DataUtilities.CurrentWeather.insertValues_Seattle();
        Uri uri = getContext().getContentResolver().insert(CurrentWeatherContract.URI, insertValues);
        assertNotNull("Error: Insert Uri should not be null", uri);

        /**
         * Error case 1: attempt to re-insert using returned Uri
         */
        boolean hadException = false;
        try {
            uri = getContext().getContentResolver().insert(uri, insertValues);
        }
        catch (UnsupportedOperationException e) {
            hadException = true;
        }
        assertTrue(hadException);

        //create a bad uri
        Uri badUri = CurrentWeatherContract.URI.buildUpon().appendPath("error").build();

        /**
         * Error case 2: use a bad uri for insertion
         */
        hadException = false;
        try {
            uri = getContext().getContentResolver().insert(badUri, insertValues);
        }
        catch (UnsupportedOperationException e) {
            hadException = true;
        }
        assertTrue(hadException);

        /**
         * Error case 3: use a bad uri for update
         */
        hadException = false;
        try {
            int rows = getContext().getContentResolver().update(badUri, insertValues, null, null);
        }
        catch (UnsupportedOperationException e) {
            hadException = true;
        }
        assertTrue(hadException);

        /**
         * Error case 4: use a bad uri for query
         */
        hadException = false;
        try {
            Cursor c = getContext().getContentResolver().query(badUri, null, null, null, null);
        }
        catch (UnsupportedOperationException e) {
            hadException = true;
        }
        assertTrue(hadException);

        /**
         * Error case 5: use a bad uri for delete
         */
        hadException = false;
        try {
            int rows = getContext().getContentResolver().delete(badUri, null, null);
        }
        catch (UnsupportedOperationException e) {
            hadException = true;
        }
        assertTrue(hadException);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
