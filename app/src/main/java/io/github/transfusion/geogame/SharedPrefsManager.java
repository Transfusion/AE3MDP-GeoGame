package io.github.transfusion.geogame;

import android.content.SharedPreferences;

/**
 * Created by Bryan Kok on 17-5-5.
 * Class handling all the SharedPreference operations to avoid cluttering up the activities
 */
public class SharedPrefsManager {

    public static String MAIN_PREFS_FILE = "mainPrefs";

    private SharedPreferences mSharedPreferences;

    public static class Prefs {
        public static final int DEFAULT_SIMULTANEOUS_TASKS = 3;
        public static final long DEFAULT_TASK_RADIUS = 3000;
        public static final int DEFAULT_UPDATE_TIME_INTERVAL = 5;
        public static final int DEFAULT_UPDATE_DISTANCE_INTERVAL = 5;

        public static final long DEFAULT_NOTIFICATION_NEARBY_TASK_RADIUS = 10;
        public static final long DEFAULT_TASK_COMPLETE_RADIUS = 5;

        public int simultaneousTasks;
        public long radiusMeters;
//        Parameters to LocationManager;
        public int updateTimeInterval;
        public int updateDistanceInterval;

        public long notificationNearbyTaskRadius;
        public long taskCompleteRadius;

        public Prefs(int simultaneousTasks, long radiusMeters, int updateTimeInterval, int updateDistanceInterval,
                     long notificationNearbyTaskRadius, long taskCompleteRadius){
            this.simultaneousTasks = simultaneousTasks;
            this.radiusMeters = radiusMeters;
            this.updateDistanceInterval = updateDistanceInterval;
            this.updateTimeInterval = updateTimeInterval;

            this.notificationNearbyTaskRadius = notificationNearbyTaskRadius;
            this.taskCompleteRadius = taskCompleteRadius;
        }

        @Override
        public boolean equals(Object _other) {
            if (this == _other) {
                return true;
            }
            if (!(_other instanceof Prefs)) {
                return false;
            }
            Prefs other = (Prefs) _other;
            return (simultaneousTasks == other.simultaneousTasks) && (radiusMeters == other.radiusMeters)
                    && (updateTimeInterval == other.updateTimeInterval) &&
                    (updateDistanceInterval == other.updateDistanceInterval) &&
                    (notificationNearbyTaskRadius == other.notificationNearbyTaskRadius)&&
                    (taskCompleteRadius == other.taskCompleteRadius);
        }
    }

    public SharedPrefsManager(SharedPreferences p){
        mSharedPreferences = p;
    }

    public static String SIMUL_TASKS_KEY = "task_simul";
    public static String RADIUS_KEY = "task_radius";
    public static String UPDATE_TIME_INTERVAL_KEY = "update_interval";
    public static String UPDATE_DISTANCE_INTERVAL_KEY = "update_distance";

    public static String NOTIFICATION_NEARBY_RADIUS_KEY = "notification_distance";
    public static String TASK_COMPLETE_RADIUS_KEY = "task_complete_radius";

    public void saveSettings(Prefs p){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(SIMUL_TASKS_KEY, p.simultaneousTasks);
        editor.putLong(RADIUS_KEY, p.radiusMeters);
        editor.putInt(UPDATE_TIME_INTERVAL_KEY, p.updateTimeInterval);
        editor.putInt(UPDATE_DISTANCE_INTERVAL_KEY, p.updateDistanceInterval);

        editor.putLong(NOTIFICATION_NEARBY_RADIUS_KEY, p.notificationNearbyTaskRadius);
        editor.putLong(TASK_COMPLETE_RADIUS_KEY, p.taskCompleteRadius);
        editor.commit();
    }

    public Prefs getSettings(){
        int simulTasks = mSharedPreferences.getInt(SIMUL_TASKS_KEY, Prefs.DEFAULT_SIMULTANEOUS_TASKS);
        long radiusMeters = mSharedPreferences.getLong(RADIUS_KEY, Prefs.DEFAULT_TASK_RADIUS);
        int updateTimeInterval = mSharedPreferences.getInt(UPDATE_TIME_INTERVAL_KEY, Prefs.DEFAULT_UPDATE_TIME_INTERVAL);
        int updateDistanceInterval = mSharedPreferences.getInt(UPDATE_TIME_INTERVAL_KEY, Prefs.DEFAULT_UPDATE_DISTANCE_INTERVAL);

        long notificationNearbyTaskRadius = mSharedPreferences.getLong(NOTIFICATION_NEARBY_RADIUS_KEY, Prefs.DEFAULT_NOTIFICATION_NEARBY_TASK_RADIUS);
        long taskCompleteRadius = mSharedPreferences.getLong(TASK_COMPLETE_RADIUS_KEY, Prefs.DEFAULT_TASK_COMPLETE_RADIUS);
        return new Prefs(simulTasks, radiusMeters, updateTimeInterval, updateDistanceInterval,
        notificationNearbyTaskRadius, taskCompleteRadius);
    }
}
