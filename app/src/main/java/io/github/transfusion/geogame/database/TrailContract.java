package io.github.transfusion.geogame.database;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Bryan Kok on 17-5-11.
 *
 * For logging the user's movement through gameplay.
 */

public class TrailContract {
    public static String AUTHORITY = "io.github.transfusion.geogame.database" +
            ".GameDBContentProvider";

    public static Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_TRAILS = "trails";

    public static final String TRAILS_TABLE = "trailsTable";

    public static class TrailEntry implements BaseColumns {
        public static Uri CONTENT_URI = AUTHORITY_URI.buildUpon().appendPath(PATH_TRAILS).build();
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_TS = "ts";

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                CONTENT_URI + "/" + PATH_TRAILS;

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_URI
                + "/"+ PATH_TRAILS;

        public static Uri buildTrailsUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
