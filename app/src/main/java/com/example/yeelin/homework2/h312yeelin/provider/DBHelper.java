package com.example.yeelin.homework2.h312yeelin.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

/**
 * Created by ninjakiki on 4/9/15.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "Weather.db";

    //singleton
    private static DBHelper dbHelper;

    /**
     * Use this instead of calling constructor since we want only 1 SQLiteOpenHelper per database.
     * @param context
     * @return
     */
    public static synchronized DBHelper getInstance(Context context) {
        if (dbHelper == null) {
            //use application context since this is a singleton and will outlive any activity
            dbHelper = new DBHelper(context.getApplicationContext());
        }
        return dbHelper;
    }

    /**
     * Private constructor since we should be using getInstance
     * @param context
     */
    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        //enable write ahead logging
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setWriteAheadLoggingEnabled(true);
        }
    }

    /**
     * Create all the tables in the db
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        //create the current weather table
        db.execSQL(CurrentWeatherContract.CREATE_TABLE);

        //create the daily forecast table along with the index
        db.execSQL(DailyForecastContract.CREATE_TABLE);
        db.execSQL(DailyForecastContract.CREATE_INDEX);

        //create the tri hour forecast table along with the index
        db.execSQL(TriHourForecastContract.CREATE_TABLE);
        db.execSQL(TriHourForecastContract.CREATE_INDEX);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && !db.isReadOnly()) {
            db.enableWriteAheadLogging();
        }

        if (!db.isReadOnly()) {
            //reduce auto checkpoint size
            Cursor cursor = db.rawQuery("PRAGMA wal_autocheckpoint=100", null);
            if (cursor != null) cursor.close();

            //force a checkpoint now
            cursor = db.rawQuery("PRAGMA wal_checkpoint", null);
            if (cursor!= null) cursor.close();
        }
    }

    /**
     * Rebuilds the db if the version changes. Nothing fancy.
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        cleanupDatabase(db);
        onCreate(db);
    }

    /**
     * Drops all the tables in the db.
     * @param db
     */
    private void cleanupDatabase(SQLiteDatabase db) {
        //drop the current weather table
        db.execSQL(BaseWeatherContract.dropTable(CurrentWeatherContract.TABLE));

        //drop the daily forecast table
        db.execSQL(BaseWeatherContract.dropTable(DailyForecastContract.TABLE));

        //drop the tri hour forecast table
        db.execSQL(BaseWeatherContract.dropTable(TriHourForecastContract.TABLE));
    }
}
