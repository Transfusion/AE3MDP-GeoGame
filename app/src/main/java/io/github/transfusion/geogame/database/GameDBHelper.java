package io.github.transfusion.geogame.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Bryan Kok on 17-5-5.
 */
public class GameDBHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "geoGameDatabase.db";
    public static final int DATABASE_VERSION = 1;

    public GameDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TasksContract.TASKS_TABLE + " (" +
        TasksContract.TaskEntry._ID + " INTEGER PRIMARY KEY, " +
        TasksContract.TaskEntry.COLUMN_LATITUDE + " DECIMAL (10, 6) NOT NULL, " +
        TasksContract.TaskEntry.COLUMN_LONGITUDE + " DECIMAL (10,6 ) NOT NULL, "+
        TasksContract.TaskEntry.COLUMN_TS + " UNSIGNED BIG INT NOT NULL, "+
        TasksContract.TaskEntry.COLUMN_COMPLETED + " BOOLEAN NOT NULL" + "); ");


        db.execSQL("CREATE TABLE " + TrailContract.TRAILS_TABLE + " (" +
                TrailContract.TrailEntry._ID + " INTEGER PRIMARY KEY, " +
                TrailContract.TrailEntry.COLUMN_LATITUDE + " DECIMAL (10, 6) NOT NULL, " +
                TrailContract.TrailEntry.COLUMN_LONGITUDE + " DECIMAL (10,6 ) NOT NULL, "+
                TrailContract.TrailEntry.COLUMN_TS + " UNSIGNED BIG INT NOT NULL" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
