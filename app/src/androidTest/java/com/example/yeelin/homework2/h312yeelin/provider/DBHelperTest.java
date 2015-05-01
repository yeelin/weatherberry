package com.example.yeelin.homework2.h312yeelin.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tests DBHelper which is the SQLiteOpenHelper.
 * Created by ninjakiki on 4/9/15.
 */
public class DBHelperTest extends AndroidTestCase {
    //since we want each test to start with a clean slate
    void deleteDb() {
        getContext().deleteDatabase(DBHelper.DATABASE_NAME);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteDb();
    }

    /**
     * Test creation of the db, table, and columns.
     * @throws Throwable
     */
    public void testBuildDb() throws Throwable {
        //db shouldn't exist yet
        File file = getContext().getDatabasePath(DBHelper.DATABASE_NAME);
        assertFalse("Error: Db already exists", file.exists());

        //is the db opened?
        DBHelper dbHelper = DBHelper.getInstance(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        assertTrue("Error: Db is not open", db.isOpen());

        /**
         * Table creation verification
         */
        //have we create the tables we want?
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: Db was not created correctly with tables", cursor.moveToFirst());

        //build a hashset for all the tables we want to look for
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(CurrentWeatherContract.TABLE);
        tableNameHashSet.add(DailyForecastContract.TABLE);
        tableNameHashSet.add(TriHourForecastContract.TABLE);

        //verify that the tables were created
        do {
            tableNameHashSet.remove(cursor.getString(0));
        }
        while (cursor.moveToNext());
        cursor.close();
        assertTrue("Error: Db was created without the correct tables", tableNameHashSet.isEmpty());

        /**
         * Index creation verification
         */
        //verify that the indexes were created
        cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='index'", null);
        assertTrue("Error: Db was not created correctly with indexes", cursor.moveToFirst());

        //build a hashset of all the indexes we want to look for
        final HashSet<String> indexNameHashSet = new HashSet<>();
        indexNameHashSet.add(DailyForecastContract.INDEX_NAME);
        indexNameHashSet.add(TriHourForecastContract.INDEX_NAME);

        // verify that the indexes have been created
        do {
            indexNameHashSet.remove(cursor.getString(0));
        }
        while (cursor.moveToNext());
        cursor.close();
        assertTrue("Error: Db was created without the correct indexes", indexNameHashSet.isEmpty());

        /**
         * Current Weather Table - Column verification
         */
        //verify that the correct columns were created in the table
        cursor = db.rawQuery("PRAGMA table_info(" + CurrentWeatherContract.TABLE + ")", null);
        assertTrue("Error: Unable to query db for table info", cursor.moveToFirst());

        //build a hashset for all the columns we want to look for
        final HashSet<String> currentWeatherColumnNameHashSet = new HashSet<>();
        currentWeatherColumnNameHashSet.add(CurrentWeatherContract.Columns._ID);
        currentWeatherColumnNameHashSet.add(CurrentWeatherContract.Columns.CITY_ID);
        currentWeatherColumnNameHashSet.add(CurrentWeatherContract.Columns.CITY_NAME);

        currentWeatherColumnNameHashSet.add(CurrentWeatherContract.Columns.SUMMARY);
        currentWeatherColumnNameHashSet.add(CurrentWeatherContract.Columns.DESCRIPTION);

        currentWeatherColumnNameHashSet.add(CurrentWeatherContract.Columns.TEMPERATURE);
        currentWeatherColumnNameHashSet.add(CurrentWeatherContract.Columns.HUMIDITY);

        currentWeatherColumnNameHashSet.add(CurrentWeatherContract.Columns.WIND_SPEED);
        currentWeatherColumnNameHashSet.add(CurrentWeatherContract.Columns.UNIT);
        currentWeatherColumnNameHashSet.add(CurrentWeatherContract.Columns.TIMESTAMP);

        int columnIndex = cursor.getColumnIndex("name");
        do {
            String columnName = cursor.getString(columnIndex);
            currentWeatherColumnNameHashSet.remove(columnName);
        }
        while (cursor.moveToNext());
        cursor.close();
        assertTrue("Error: Table was created without the correct columns", currentWeatherColumnNameHashSet.isEmpty());

        /**
         * Daily Forecast Table - Column verification
         */
        //verify that the correct columns were created in the table
        cursor = db.rawQuery("PRAGMA table_info(" + DailyForecastContract.TABLE + ")", null);
        assertTrue("Error: Unable to query db for table info", cursor.moveToFirst());

        //build a hashset for all the columns we want to look for
        final HashSet<String> dailyForecastColumnNameHashSet = new HashSet<>();
        dailyForecastColumnNameHashSet.add(DailyForecastContract.Columns._ID);
        dailyForecastColumnNameHashSet.add(DailyForecastContract.Columns.CITY_ID);

        dailyForecastColumnNameHashSet.add(DailyForecastContract.Columns.FORECAST_DATETIME);
        dailyForecastColumnNameHashSet.add(DailyForecastContract.Columns.TEMPERATURE_LOW);
        dailyForecastColumnNameHashSet.add(DailyForecastContract.Columns.TEMPERATURE_HIGH);

        columnIndex = cursor.getColumnIndex("name");
        do {
            String columnName = cursor.getString(columnIndex);
            dailyForecastColumnNameHashSet.remove(columnName);
        }
        while (cursor.moveToNext());
        cursor.close();
        assertTrue("Error: Table was created without the correct columns", dailyForecastColumnNameHashSet.isEmpty());


        /**
         * Tri Hour Forecast Table - Column verification
         */
        //verify that the correct columns were created in the table
        cursor = db.rawQuery("PRAGMA table_info(" + TriHourForecastContract.TABLE + ")", null);
        assertTrue("Error: Unable to query db for table info", cursor.moveToFirst());

        //build a hashset for all the columns we want to look for
        final HashSet<String> triHourForecastColumnNameHashSet = new HashSet<>();
        triHourForecastColumnNameHashSet.add(TriHourForecastContract.Columns._ID);
        triHourForecastColumnNameHashSet.add(TriHourForecastContract.Columns.CITY_ID);

        triHourForecastColumnNameHashSet.add(TriHourForecastContract.Columns.FORECAST_DATETIME);
        triHourForecastColumnNameHashSet.add(TriHourForecastContract.Columns.TEMPERATURE);
        triHourForecastColumnNameHashSet.add(TriHourForecastContract.Columns.DESCRIPTION);

        columnIndex = cursor.getColumnIndex("name");
        do {
            String columnName = cursor.getString(columnIndex);
            triHourForecastColumnNameHashSet.remove(columnName);
        }
        while (cursor.moveToNext());
        cursor.close();
        assertTrue("Error: Table was created without the correct columns", triHourForecastColumnNameHashSet.isEmpty());

        //cleanup
        db.close();
        dbHelper.close();
    }

    /**
     * Current Weather table
     * Test all CRUD operations on the db using row id returned by insert
     * @throws Throwable
     */
    public void testCRUD_using_RowId_on_CurrentWeatherTable() throws Throwable {
        /**
         * Test Insert
         */
        DBHelper dbHelper = DBHelper.getInstance(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues insertValues = DataUtilities.CurrentWeather.insertValues_Seattle();
        long rowId = -1;
        rowId = db.insertWithOnConflict(
                CurrentWeatherContract.TABLE,
                null,
                insertValues,
                SQLiteDatabase.CONFLICT_REPLACE);
        assertTrue("Error: RowId should not be -1", rowId != -1);

        Cursor insertCursor = db.query(
                CurrentWeatherContract.TABLE,
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        insertValues.put(CurrentWeatherContract.Columns._ID, rowId);
        validateCursor("Insert Cursor", insertCursor, insertValues);
        insertCursor.close();

        /**
         * Test Update using row id
         */
        ContentValues updateValues = DataUtilities.CurrentWeather.updateValues_Seattle();
        int numRowsUpdated = db.update(
                CurrentWeatherContract.TABLE,
                updateValues,
                BaseWeatherContract.whereClauseEquals(CurrentWeatherContract.Columns._ID),
                BaseWeatherContract.whereArgs(rowId));
        assertTrue("Error: Rows updated != 1", numRowsUpdated == 1);

        Cursor updateCursor = db.query(
                CurrentWeatherContract.TABLE,
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        updateValues.put(CurrentWeatherContract.Columns._ID, rowId);
        validateCursor("Update Cursor", updateCursor, updateValues);
        updateCursor.close();

        /**
         * Test Delete using row id
         */
        int numRowsDeleted = db.delete(
                CurrentWeatherContract.TABLE,
                BaseWeatherContract.whereClauseEquals(CurrentWeatherContract.Columns._ID),
                BaseWeatherContract.whereArgs(rowId));
        assertTrue("Error: Rows deleted != 1", numRowsDeleted == 1);

        Cursor deleteCursor = db.query(
                CurrentWeatherContract.TABLE,
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        assertFalse("Error: Delete cursor is not empty", deleteCursor.moveToFirst());
        deleteCursor.close();

        //cleanup
        db.close();
        dbHelper.close();
    }

    /**
     * Current Weather table
     * Test all CRUD operations on the db using city id
     * @throws Throwable
     */
    public void testCRUD_using_CityId_on_CurrentWeatherTable() throws Throwable {
        /**
         * Test Insert
         */
        DBHelper dbHelper = DBHelper.getInstance(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues insertValues = DataUtilities.CurrentWeather.insertValues_Seattle();
        long rowId = -1;
        rowId = db.insertWithOnConflict(
                CurrentWeatherContract.TABLE,
                null,
                insertValues,
                SQLiteDatabase.CONFLICT_REPLACE);
        assertTrue("Error: RowId should not be -1", rowId != -1);

        Cursor insertCursor = db.query(
                CurrentWeatherContract.TABLE,
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        insertValues.put(CurrentWeatherContract.Columns._ID, rowId);
        validateCursor("Insert Cursor", insertCursor, insertValues);
        insertCursor.close();

        /**
         * Test Update using city id
         */
        ContentValues updateValues = DataUtilities.CurrentWeather.updateValues_Seattle();
        int numRowsUpdated = db.update(
                CurrentWeatherContract.TABLE,
                updateValues,
                BaseWeatherContract.whereClauseEquals(CurrentWeatherContract.Columns.CITY_ID),
                BaseWeatherContract.whereArgs(DataUtilities.SEATTLE_CITY_ID));
        assertTrue("Error: Rows updated != 1", numRowsUpdated == 1);

        Cursor updateCursor = db.query(
                CurrentWeatherContract.TABLE,
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        updateValues.put(CurrentWeatherContract.Columns._ID, rowId);
        validateCursor("Update Cursor", updateCursor, updateValues);
        updateCursor.close();

        /**
         * Test Delete using city id
         */
        int numRowsDeleted = db.delete(
                CurrentWeatherContract.TABLE,
                BaseWeatherContract.whereClauseEquals(CurrentWeatherContract.Columns.CITY_ID),
                BaseWeatherContract.whereArgs(DataUtilities.SEATTLE_CITY_ID));
        assertTrue("Error: Rows deleted != 1", numRowsDeleted == 1);

        Cursor deleteCursor = db.query(
                CurrentWeatherContract.TABLE,
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        assertFalse("Error: Delete cursor is not empty", deleteCursor.moveToFirst());
        deleteCursor.close();

        //cleanup
        db.close();
        dbHelper.close();
    }

    /**
     * Daily Forecast table
     * Test all CRUD operations on the db using city id
     * @throws Throwable
     */
    public void testCRUD_using_CityId_on_DailyForecastTable() throws Throwable {
        /**
         * Test Insert
         */
        DBHelper dbHelper = DBHelper.getInstance(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues insertValues = DataUtilities.DailyForecast.insertValues_Seattle1();
        long rowId = -1;
        rowId = db.insertWithOnConflict(
                DailyForecastContract.TABLE,
                null,
                insertValues,
                SQLiteDatabase.CONFLICT_REPLACE);
        assertTrue("Error: RowId should not be -1", rowId != -1);

        Cursor insertCursor = db.query(
                DailyForecastContract.TABLE,
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        insertValues.put(DailyForecastContract.Columns._ID, rowId);
        validateCursor("Insert Cursor", insertCursor, insertValues);
        insertCursor.close();

        /**
         * Test Update using city id
         */
        ContentValues updateValues = DataUtilities.DailyForecast.updateValues_Seattle();
        int numRowsUpdated = db.update(
                DailyForecastContract.TABLE,
                updateValues,
                BaseWeatherContract.whereClauseEquals(DailyForecastContract.Columns.CITY_ID),
                BaseWeatherContract.whereArgs(DataUtilities.SEATTLE_CITY_ID));
        assertTrue("Error: Rows updated != 1", numRowsUpdated == 1);

        Cursor updateCursor = db.query(
                DailyForecastContract.TABLE,
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        updateValues.put(DailyForecastContract.Columns._ID, rowId);
        validateCursor("Update Cursor", updateCursor, updateValues);
        updateCursor.close();

        /**
         * Test Delete using city id
         */
        int numRowsDeleted = db.delete(
                DailyForecastContract.TABLE,
                BaseWeatherContract.whereClauseEquals(DailyForecastContract.Columns.CITY_ID),
                BaseWeatherContract.whereArgs(DataUtilities.SEATTLE_CITY_ID));
        assertTrue("Error: Rows deleted != 1", numRowsDeleted == 1);

        Cursor deleteCursor = db.query(
                DailyForecastContract.TABLE,
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        assertFalse("Error: Delete cursor is not empty", deleteCursor.moveToFirst());
        deleteCursor.close();

        //cleanup
        db.close();
        dbHelper.close();
    }

    /**
     * Tri Hour Forecast table
     * Test all CRUD operations on the db using city id
     * @throws Throwable
     */
    public void testCRUD_using_CityId_on_TriHourForecastTable() throws Throwable {
        /**
         * Test Insert
         */
        DBHelper dbHelper = DBHelper.getInstance(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues insertValues = DataUtilities.TriHourForecast.insertValues_Seattle1();
        long rowId = -1;
        rowId = db.insertWithOnConflict(
                TriHourForecastContract.TABLE,
                null,
                insertValues,
                SQLiteDatabase.CONFLICT_REPLACE);
        assertTrue("Error: RowId should not be -1", rowId != -1);

        Cursor insertCursor = db.query(
                TriHourForecastContract.TABLE,
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        insertValues.put(TriHourForecastContract.Columns._ID, rowId);
        validateCursor("Insert Cursor", insertCursor, insertValues);
        insertCursor.close();

        /**
         * Test Update using city id
         */
        ContentValues updateValues = DataUtilities.TriHourForecast.updateValues_Seattle();
        int numRowsUpdated = db.update(
                TriHourForecastContract.TABLE,
                updateValues,
                BaseWeatherContract.whereClauseEquals(TriHourForecastContract.Columns.CITY_ID),
                BaseWeatherContract.whereArgs(DataUtilities.SEATTLE_CITY_ID));
        assertTrue("Error: Rows updated != 1", numRowsUpdated == 1);

        Cursor updateCursor = db.query(
                TriHourForecastContract.TABLE,
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        updateValues.put(TriHourForecastContract.Columns._ID, rowId);
        validateCursor("Update Cursor", updateCursor, updateValues);
        updateCursor.close();

        /**
         * Test Delete using city id
         */
        int numRowsDeleted = db.delete(
                TriHourForecastContract.TABLE,
                BaseWeatherContract.whereClauseEquals(TriHourForecastContract.Columns.CITY_ID),
                BaseWeatherContract.whereArgs(DataUtilities.SEATTLE_CITY_ID));
        assertTrue("Error: Rows deleted != 1", numRowsDeleted == 1);

        Cursor deleteCursor = db.query(
                TriHourForecastContract.TABLE,
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        assertFalse("Error: Delete cursor is not empty", deleteCursor.moveToFirst());
        deleteCursor.close();

        //cleanup
        db.close();
        dbHelper.close();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Error: Null cursor returned." + error, valueCursor != null);
        assertTrue("Error: Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned in cursor. " + error, valueCursor.moveToNext());
    }

    private static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : valueSet) {

            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Error: Column '" + columnName + "' not found. " + error, idx == -1);

            String expectedValue = entry.getValue().toString();
            assertEquals("Error: Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }
}
