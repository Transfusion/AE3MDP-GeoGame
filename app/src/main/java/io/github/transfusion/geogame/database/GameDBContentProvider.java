package io.github.transfusion.geogame.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by Bryan Kok on 17-5-5.
 */
public class GameDBContentProvider extends ContentProvider {
    GameDBHelper mHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int TASKS = 1000;
    private static final int TASKS_ID = TASKS + 1;

    private static final int TRAILS = 2000;
    private static final int TRAILS_ID = TRAILS+1;


    private static UriMatcher buildUriMatcher() {
        String content = TasksContract.AUTHORITY;
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(content, TasksContract.PATH_TASKS, TASKS);
        matcher.addURI(content, TasksContract.PATH_TASKS + "/#", TASKS_ID);

        matcher.addURI(content, TrailContract.PATH_TRAILS, TRAILS);
        matcher.addURI(content, TrailContract.PATH_TRAILS + "/#", TRAILS_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mHelper = new GameDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor mCursor;
        switch (sUriMatcher.match(uri)){
            case TASKS:
                mCursor = db.query(TasksContract.TASKS_TABLE,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TASKS_ID:
                long _id = ContentUris.parseId(uri);
                mCursor = db.query(TasksContract.TASKS_TABLE,
                        projection,
                        TasksContract.TaskEntry._ID + " = ?",
                        new String[] {String.valueOf(_id)},
                        null, null, sortOrder);
                break;
            case TRAILS:
                mCursor = db.query(TrailContract.TRAILS_TABLE,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TRAILS_ID:
                _id = ContentUris.parseId(uri);
                mCursor = db.query(TrailContract.TRAILS_TABLE,
                        projection,
                        TrailContract.TrailEntry._ID + " = ?",
                        new String[] {String.valueOf(_id)},
                        null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: "+uri);
        }
        return mCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch(sUriMatcher.match(uri)){
            case TASKS:
                return TasksContract.TaskEntry.CONTENT_TYPE;
            case TASKS_ID:
                return TasksContract.TaskEntry.CONTENT_ITEM_TYPE;
            case TRAILS:
                return TrailContract.TrailEntry.CONTENT_TYPE;
            case TRAILS_ID:
                return TrailContract.TrailEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        long _id;
        Uri returnUri = null;
        switch(sUriMatcher.match(uri)){
            case TASKS:
                _id = db.insert(TasksContract.TASKS_TABLE, null, values);
                if (_id > 0){
                    returnUri = TasksContract.TaskEntry.buildTasksUri(_id);
                }
                break;

            case TRAILS:
                _id = db.insert(TrailContract.TRAILS_TABLE, null, values);
                if (_id > 0){
                    returnUri = TrailContract.TrailEntry.buildTrailsUri(_id);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int rows;
        switch(sUriMatcher.match(uri)){
            case TASKS:
                rows = db.delete(TasksContract.TASKS_TABLE, selection, selectionArgs);
                break;
            case TASKS_ID:
                long _id = ContentUris.parseId(uri);
                String sel = TasksContract.TaskEntry._ID + " = ?";
                rows = db.delete(TasksContract.TASKS_TABLE, sel, new String[]{String.valueOf(_id)});
                break;
            case TRAILS:
                rows = db.delete(TrailContract.TRAILS_TABLE, selection, selectionArgs);
                break;
            case TRAILS_ID:
                _id = ContentUris.parseId(uri);
                sel = TrailContract.TrailEntry._ID + " = ?";
                rows = db.delete(TrailContract.TRAILS_TABLE, sel, new String[]{String.valueOf(_id)});
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        return rows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        int rows;
        switch(sUriMatcher.match(uri)){
            case TASKS:
                rows = db.update(TasksContract.TASKS_TABLE, values, selection, selectionArgs);
                break;
            case TASKS_ID:
                long _id = ContentUris.parseId(uri);
                String sel = TasksContract.TaskEntry._ID + " = ?";
                rows = db.update(TasksContract.TASKS_TABLE, values, sel, new String[]{String.valueOf(_id)});
                break;

            case TRAILS:
                rows = db.update(TrailContract.TRAILS_TABLE, values, selection, selectionArgs);
                break;
            case TRAILS_ID:
                _id = ContentUris.parseId(uri);
                sel = TasksContract.TaskEntry._ID + " = ?";
                rows = db.update(TrailContract.TRAILS_TABLE, values, sel, new String[]{String.valueOf(_id)});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return rows;
    }
}
