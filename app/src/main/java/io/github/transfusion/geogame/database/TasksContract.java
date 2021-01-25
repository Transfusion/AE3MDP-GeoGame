package io.github.transfusion.geogame.database;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Bryan Kok on 17-5-5.
 *
 * For storing a generated task in the DB.
 */
public class TasksContract {
    public static String AUTHORITY = "io.github.transfusion.geogame.database" +
            ".GameDBContentProvider";

    public static Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_TASKS = "tasks";

    public static final String TASKS_TABLE = "tasksTable";

    public static class TaskEntry implements BaseColumns {
        public static Uri CONTENT_URI = AUTHORITY_URI.buildUpon().appendPath(PATH_TASKS).build();
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_TS = "ts";
        public static final String COLUMN_COMPLETED = "completed";

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                CONTENT_URI + "/" + PATH_TASKS;

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_URI
                + "/"+ PATH_TASKS;

        public static Uri buildTasksUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
