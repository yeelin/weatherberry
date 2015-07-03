package com.example.yeelin.homework.weatherberry.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by ninjakiki on 4/10/15.
 */
public class CursorUtilities {

    /**
     *
     * @param error
     * @param valueCursor
     * @param valuesArray
     */

    public static void validateCursor(String error,
                                      Cursor valueCursor,
                                      ContentValues[] valuesArray) {
        assertNotNull("Error: Null cursor. " + error, valueCursor);
        assertEquals("Error: Unequal rows. " + error, valuesArray.length, valueCursor.getCount());

        int valueCount = 0;
        valueCursor.moveToPosition(-1); // ensure at beginning

        while (valueCursor.moveToNext()) {
            String name = valueCursor.getString(valueCursor.getColumnIndex(CurrentWeatherContract.Columns.CITY_NAME));

            for (ContentValues values : valuesArray) {
                if (values.getAsString(CurrentWeatherContract.Columns.CITY_NAME).equals(name)) {
                    ++valueCount;
                    validateCurrentRecord(error, valueCursor, values);
                    break;
                }
            }
        }
        assertEquals("Cursor doesn't contain values. " + error, valuesArray.length, valueCount);
    }


    /**
     *
     * @param error
     * @param valueCursor
     * @param expectedValues
     * @param id
     */
    public static void validateCursor(String error,
                                      Cursor valueCursor,
                                      ContentValues expectedValues,
                                      long id) {
        assertNotNull("Error: Null cursor returned." + error, valueCursor);
        assertTrue("Error: Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateId(error, valueCursor, id);
        validateCurrentRecord(error, valueCursor, expectedValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned in cursor. " + error, valueCursor.moveToNext());
    }

    /**
     *
     * @param error
     * @param valueCursor
     * @param id
     */
    private static void validateId(String error,
                                   Cursor valueCursor,
                                   long id) {
        int idx = valueCursor.getColumnIndex(BaseColumns._ID);
        assertFalse("Error: Column '" + BaseColumns._ID + "' not found. " + error, idx == -1);
        assertEquals("Error: Id does not match " + error, id, valueCursor.getLong(idx));
    }

    /**
     *
     * @param error
     * @param valueCursor
     * @param expectedValues
     */
    private static void validateCurrentRecord(String error,
                                              Cursor valueCursor,
                                              ContentValues expectedValues) {
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
