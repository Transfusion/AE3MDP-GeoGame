package io.github.transfusion.geogame;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.transfusion.geogame.database.Task;
import io.github.transfusion.geogame.database.TasksContract;
import io.github.transfusion.geogame.database.TrailContract;
import io.github.transfusion.geogame.database.TrailPoint;

/**
 * Created by Bryan Kok on 17-5-5.
 *
 * Task generation and management logic is encapsulated in this file;
 * don't want to clutter up MainActivity.java
 *
 */
public class TaskManager {
    Random random;
    ContentResolver mContentResolver;

    public TaskManager(ContentResolver cr){
        mContentResolver = cr;
        random = new Random();
    }

    public List<Task> getCompletedTasks(){
        ArrayList<Task> list = new ArrayList<>();
        final Cursor queryCursor = mContentResolver.query(TasksContract.TaskEntry.CONTENT_URI, null,
                TasksContract.TaskEntry.COLUMN_COMPLETED + " = 1", null, null);

        if (queryCursor != null) {
//            queryCursor.moveToFirst();
            while(queryCursor.moveToNext()){
                Task t = new Task(
                        queryCursor.getInt(queryCursor.getColumnIndex(TasksContract.TaskEntry._ID)),
                        queryCursor.getDouble(queryCursor.getColumnIndex(TasksContract.TaskEntry.COLUMN_LATITUDE)),
                        queryCursor.getDouble(queryCursor.getColumnIndex(TasksContract.TaskEntry.COLUMN_LONGITUDE)),
                        queryCursor.getLong(queryCursor.getColumnIndex(TasksContract.TaskEntry.COLUMN_TS)),
                        queryCursor.getInt(queryCursor.getColumnIndex(TasksContract.TaskEntry.COLUMN_COMPLETED))
                );
                list.add(t);
            }

        }
        return list;
    }

    public List<Task> getPendingTasks(){
        ArrayList<Task> list = new ArrayList<>();
        final Cursor queryCursor = mContentResolver.query(TasksContract.TaskEntry.CONTENT_URI, null,
                TasksContract.TaskEntry.COLUMN_COMPLETED + " = 0", null, null);

        Log.d(getClass().getName(), "pendingTasksCount "+queryCursor.getCount());
        if (queryCursor != null) {
//            queryCursor.moveToFirst();
            while(queryCursor.moveToNext()){
                Task t = new Task(
                        queryCursor.getInt(queryCursor.getColumnIndex(TasksContract.TaskEntry._ID)),
                        queryCursor.getDouble(queryCursor.getColumnIndex(TasksContract.TaskEntry.COLUMN_LATITUDE)),
                        queryCursor.getDouble(queryCursor.getColumnIndex(TasksContract.TaskEntry.COLUMN_LONGITUDE)),
                        queryCursor.getLong(queryCursor.getColumnIndex(TasksContract.TaskEntry.COLUMN_TS)),
                        queryCursor.getInt(queryCursor.getColumnIndex(TasksContract.TaskEntry.COLUMN_COMPLETED))
                );
                list.add(t);
            }
            queryCursor.close();
        }
        return list;
    }

    public List<Task> getAllTasks(){
        ArrayList<Task> list = new ArrayList<>();
        final Cursor queryCursor = mContentResolver.query(TasksContract.TaskEntry.CONTENT_URI, null,
                null, null, null);

        if (queryCursor != null) {
//            queryCursor.moveToFirst();
            while(queryCursor.moveToNext()){
                Task t = new Task(
                        queryCursor.getInt(queryCursor.getColumnIndex(TasksContract.TaskEntry._ID)),
                        queryCursor.getDouble(queryCursor.getColumnIndex(TasksContract.TaskEntry.COLUMN_LATITUDE)),
                        queryCursor.getDouble(queryCursor.getColumnIndex(TasksContract.TaskEntry.COLUMN_LONGITUDE)),
                        queryCursor.getLong(queryCursor.getColumnIndex(TasksContract.TaskEntry.COLUMN_TS)),
                        queryCursor.getInt(queryCursor.getColumnIndex(TasksContract.TaskEntry.COLUMN_COMPLETED))
                );
                list.add(t);
            }
            queryCursor.close();
        }
        return list;
    }

    public Task genNewTask(double startLat, double startLong, long metersRadius){
//        111.111 km / degree of *longitude*, *1000 to convert to meters
//        Conversion of meters to degrees inspired by
// https://gis.stackexchange.com/questions/25877/generating-random-locations-nearby.
        double toDegrees = metersRadius / 111111f;

        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = toDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        double newX = x / Math.cos(Math.toRadians(startLat));
        double foundLongitude = newX + startLong;
        double foundLatitude = y + startLat;

        long ts = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(TasksContract.TaskEntry.COLUMN_LATITUDE, foundLatitude);
        values.put(TasksContract.TaskEntry.COLUMN_LONGITUDE, foundLongitude);
        values.put(TasksContract.TaskEntry.COLUMN_TS, ts);
        values.put(TasksContract.TaskEntry.COLUMN_COMPLETED, 0);
        Uri inserted = mContentResolver.insert(TasksContract.TaskEntry.CONTENT_URI, values);
        long id = Long.parseLong(inserted.getLastPathSegment());
        return new Task(id, foundLatitude, foundLongitude, ts, 0);
    }

    public int deleteTask(long id){
        return mContentResolver.delete(TasksContract.TaskEntry.buildTasksUri(id), null, null);
    }

    public int deleteAllPendingTasks(){
        String sel = TasksContract.TaskEntry.COLUMN_COMPLETED + " = ?";
        String[] selArgs = new String[] {"0"};
        return mContentResolver.delete(TasksContract.TaskEntry.CONTENT_URI, sel, selArgs);
    }

    public int markTaskCompleted(long id, boolean _completed){
        int completed = _completed ? 1 : 0;
//        public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
        ContentValues values = new ContentValues();
//        values.put(TasksContract.TaskEntry._ID, id);
        Uri uri = ContentUris.withAppendedId(TasksContract.TaskEntry.CONTENT_URI, id);
        values.put(TasksContract.TaskEntry.COLUMN_COMPLETED, completed);
        return mContentResolver.update(uri, values, null, null);
    }
    /*public List<Task> getPendingTasks(long tsStart, long tsEnd){

    }

    public int getNumPendingTasks(){

    }*/

    /* Tracking the movement of the user is done below */
    public void logTrailPoint(double latitude, double longitude){
        ContentValues values = new ContentValues();
        values.put(TrailContract.TrailEntry.COLUMN_LATITUDE, latitude);
        values.put(TrailContract.TrailEntry.COLUMN_LONGITUDE, longitude);
        values.put(TrailContract.TrailEntry.COLUMN_TS, System.currentTimeMillis());
        Uri inserted = mContentResolver.insert(TrailContract.TrailEntry.CONTENT_URI, values);
    }

    public List<TrailPoint> getAllTrailPoints(){
        ArrayList<TrailPoint> list = new ArrayList<>();
        final Cursor queryCursor = mContentResolver.query(TrailContract.TrailEntry.CONTENT_URI, null,
                null, null, TrailContract.TrailEntry._ID + " DESC ");

        if (queryCursor != null) {
            while(queryCursor.moveToNext()){
                TrailPoint t = new TrailPoint(
                        queryCursor.getInt(queryCursor.getColumnIndex(TrailContract.TrailEntry._ID)),
                        queryCursor.getDouble(queryCursor.getColumnIndex(TrailContract.TrailEntry.COLUMN_LATITUDE)),
                        queryCursor.getDouble(queryCursor.getColumnIndex(TrailContract.TrailEntry.COLUMN_LONGITUDE)),
                        queryCursor.getLong(queryCursor.getColumnIndex(TrailContract.TrailEntry.COLUMN_TS))
                );
                list.add(t);
            }
            queryCursor.close();
        }
        return list;
    }

    public int deleteTrailPoint(long id){
        return mContentResolver.delete(TrailContract.TrailEntry.buildTrailsUri(id), null, null);
    }
}
