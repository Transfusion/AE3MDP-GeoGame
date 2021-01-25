package io.github.transfusion.geogame.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.transfusion.geogame.GameManager;
import io.github.transfusion.geogame.GeoGameLocationListener;
import io.github.transfusion.geogame.R;
import io.github.transfusion.geogame.SharedPrefsManager;
import io.github.transfusion.geogame.database.Task;

/**
 * Purpose of this service is so the user's game is still effectively running in the background;
 * they don't actually need to have the game application open to be playing.
 */
public class GameLocationService extends Service {

    public static final String TASK_LIST_BROADCAST_CODE = "task-list";
    public static final String OWN_POSITION_BROADCAST_CODE = "own-location";
    public static final String TASK_COMPLETED_BROADCAST_CODE = "task-completed";

    public static final String BROADCAST_PAYLOAD_CODE = "message";

    private LocationManager locationManager;
    private GeoGameLocationListener mLocationListener;
    private boolean activityInForeground = false;

    SharedPrefsManager sharedPrefsManager;
    SharedPrefsManager.Prefs settings;

    GameManager gameManager;
    private int RANDOM_NOTIFICATION_ID = 1;
    private NotificationManager nm;


    public GameLocationService() {
    }

    /**
     * Binder here is strictly used for Activity-initiated communication.
     */
    public class GameLocationBinder extends Binder {
        /**
         * Stop all location updates; user wants to quit the game.
         */
        public void stopGame(){
            locationManager.removeUpdates(mLocationListener);
        }

        public void activityInForeground(boolean in){
            activityInForeground = in;
        }

        public void notifyGameSettingsChanged(boolean force){
            Location loc = getCurrentLocation();
            settings = sharedPrefsManager.getSettings();
            gameManager.switchSettings(settings, loc, force);
            sendCurrentLocationToActivity(loc);
            sendTaskListToActivity();
        }

        public Location getCurrentLocation(){
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        public List<Task> getPendingTasks(){
            return gameManager.getPendingTasks();
        }
    }

    private final GameLocationBinder mBinder = new GameLocationBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate(){
        sharedPrefsManager = new SharedPrefsManager(getSharedPreferences(SharedPrefsManager.MAIN_PREFS_FILE,
                MODE_PRIVATE));
        settings = sharedPrefsManager.getSettings();
        gameManager = new GameManager(getContentResolver(), settings);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        setupLocationServices();
        setupGame();
    }

    private void setupGame() {
//        called after FIRST_FIX, guaranteed to return something
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
        if (location != null){
            gameManager.switchSettings(settings, location, false);
            gameManager.refreshGame(location);
            sendTaskListToActivity();
        }
    }

    private void setupLocationServices() {
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission (check was already done in the MainActivity; service won't even have been started)
        locationManager.addGpsStatusListener(new GpsStatus.Listener() {
            @SuppressWarnings("MissingPermission")
            @Override
            public void onGpsStatusChanged(int event) {
                switch(event){
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        sendCurrentLocationToActivity(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                        setupGame();
                        break;
                    default:
                        break;
                }
            }
        });

        mLocationListener = new GeoGameLocationListener();
        mLocationListener.addLocationChangedCallback(new GeoGameLocationListener.OnLocationChangedCallback() {
            @Override
            public void handle(Location location) {
                Log.d(getClass().getName(), location.getLatitude() + " " + location.getLongitude());
                gameManager.logTrailPoint(location.getLatitude(), location.getLongitude());
                checkReachedTasks(location);
                sendCurrentLocationToActivity(location);
                sendTaskListToActivity();
            }
        });

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    settings.updateTimeInterval, // minimum time interval between updates
                    settings.updateDistanceInterval, // minimum distance between updates, in metres
                    mLocationListener);
        } catch(SecurityException e) {
            Log.d(getClass().getName(), "Unable to request location updates!");
        }
    }

    private Set<Long> sentNotifications = new HashSet<>();
    /* Game logic is here */
    protected void checkReachedTasks(Location location){
        for (Task t : gameManager.getPendingTasks()){
            float[] diff = new float[1];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                    t.latitude, t.longitude, diff);
            if (diff[0] <= settings.taskCompleteRadius){
                handleFinishTask(location, t.id);
            }
            else if (diff[0] <= settings.notificationNearbyTaskRadius){
                if (!sentNotifications.contains(t.id)){
                    handleNearbyTask(diff[0]);
                    sentNotifications.add(t.id);
                }

            }
        }
        gameManager.refreshGame(location);
        sendTaskListToActivity();
    }

    private void handleNearbyTask(float v) {
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_near_me_black_24dp)
                        .setContentTitle("Task Nearby!")
                        .setContentText("You are " + v + " meters away from a task");

        int unqInt = RANDOM_NOTIFICATION_ID++;
        nm.notify(unqInt, mBuilder.build());
    }

    private void handleFinishTask(Location loc, long id) {
        gameManager.markTaskCompleted(id);
        sendTaskCompletedToActivity(loc);
        displayTaskCompleted(loc);
    }

    private void displayTaskCompleted(Location loc) {
        String notifText = "Lat: " + loc.getLatitude() + " Long: " + loc.getLongitude();
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_near_me_black_24dp)
                        .setContentTitle("Task Completed").setStyle(new Notification.BigTextStyle().bigText(notifText))
                        .setContentText(notifText);

        int unqInt = RANDOM_NOTIFICATION_ID++;
        nm.notify(unqInt, mBuilder.build());
    }

    private void sendCurrentLocationToActivity(Location loc) {
        Intent intent = new Intent(OWN_POSITION_BROADCAST_CODE);
        intent.putExtra(BROADCAST_PAYLOAD_CODE, loc);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendTaskListToActivity(){
        Intent intent = new Intent(TASK_LIST_BROADCAST_CODE);
        intent.putParcelableArrayListExtra(BROADCAST_PAYLOAD_CODE, new ArrayList<>(gameManager.getPendingTasks()));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendTaskCompletedToActivity(Location loc){
        Intent intent = new Intent(TASK_COMPLETED_BROADCAST_CODE);
        intent.putExtra(BROADCAST_PAYLOAD_CODE, loc);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
