package io.github.transfusion.geogame;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import io.github.transfusion.geogame.database.Task;
import io.github.transfusion.geogame.database.TrailPoint;
import io.github.transfusion.geogame.services.GameLocationService;

public class MainActivity extends AppCompatActivity {

    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int LOCATION_REQUEST_CODE = 1;

    private static final int GAME_SETTINGS_REQUEST_CODE = 1;
//    The user may select logs from previous days and view paths previously travelled and the
//
    private static final int GAME_HISTORY_REQUEST_CODE = 2;

//    MapBox library
    MapView mapView;
    MapboxMap map;


    private FloatingActionButton ownPositionBtn;
    private FloatingActionButton closeHistoryBtn;

    private ServiceConnection mServiceConnection;
    private GameLocationService.GameLocationBinder mBinder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Mapbox.getInstance(this, getString(R.string.MAPBOX_API_KEY));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*sharedPrefsManager = new SharedPrefsManager(getSharedPreferences(SharedPrefsManager.MAIN_PREFS_FILE, MODE_PRIVATE));
        settings = sharedPrefsManager.getSettings();
        gameManager = new GameManager(getContentResolver(), settings);*/

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                map.setMyLocationEnabled(true);
                if (!canAccessLocation()){
                    ActivityCompat.requestPermissions(MainActivity.this, LOCATION_PERMS, LOCATION_REQUEST_CODE);
                }
                else {
                    establishServiceConnection();
                }
            }
        });

        setupUI();

    }


    interface ExecuteOnServiceConnected {
        void execute();
    }

    private Queue<ExecuteOnServiceConnected> tasks = new ArrayDeque<>();
    private void establishServiceConnection() {
        this.mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(getClass().getName(), "GameLocationService Connected");
                mBinder = (GameLocationService.GameLocationBinder) service;
                mBinder.activityInForeground(true);
                LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(gameTasksListReceiver,
                        new IntentFilter(GameLocationService.TASK_LIST_BROADCAST_CODE));
                LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(ownLocationReceiver,
                        new IntentFilter(GameLocationService.OWN_POSITION_BROADCAST_CODE));
                LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(taskCompletedReceiver,
                        new IntentFilter(GameLocationService.TASK_COMPLETED_BROADCAST_CODE));
                displayLastLocation();
                while(!tasks.isEmpty()){
                    tasks.remove().execute();
                }
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBinder = null;
            }
        };
        startService(new Intent(this, GameLocationService.class));
        this.bindService(new Intent(this, GameLocationService.class),
                this.mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case GAME_SETTINGS_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(getClass().getName(), "returned from GameControlActivity");
                    MainActivity.this.tasks.add(new ExecuteOnServiceConnected() {
                        @Override
                        public void execute() {
                            mBinder.notifyGameSettingsChanged(false);
                            map.clear();
                            displayLastLocation();
                        }
                    });

                }
                break;
            case GAME_HISTORY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    final List<Task> tasks = data.getParcelableArrayListExtra(GameHistoryActivity.HISTORY_DAY_TASKS);
                    final List<TrailPoint> points = data.getParcelableArrayListExtra(GameHistoryActivity.HISTORY_DAY_TRAILPOINTS);

                    MainActivity.this.tasks.add(new ExecuteOnServiceConnected() {
                        @Override
                        public void execute() {
                            displayHistoryTrail(tasks, points);
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.activity_main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_game_settings:
                startSettingsActivity();
                break;
            case R.id.action_task_history:
                startGameHistoryActivity();
                break;
            case R.id.action_refresh_tasks:
                map.clear();
                mBinder.notifyGameSettingsChanged(true);
                break;
            case R.id.action_quit_game:
                mBinder.stopGame();
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    private void startGameHistoryActivity() {
        Intent intent = new Intent(this, GameHistoryActivity.class);
        startActivityForResult(intent, GAME_HISTORY_REQUEST_CODE);
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, GameControlActivity.class);
        startActivityForResult(intent, GAME_SETTINGS_REQUEST_CODE);
    }

    private void setupUI() {
        ownPositionBtn = (FloatingActionButton) findViewById(R.id.own_position_btn);
        ownPositionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayCurrentLocation(mBinder.getCurrentLocation());
            }
        });
        closeHistoryBtn = (FloatingActionButton) findViewById(R.id.close_history_btn);
        closeHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.clear();
                displayLastLocation();
                closeHistoryBtn.setVisibility(View.INVISIBLE);
            }
        });
    }

    private boolean canAccessLocation() {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /* https://github.com/commonsguy/cw-omnibus/blob/master/Permissions/PermissionMonger/app/src/main/java/com/commonsware/android/permmonger/MainActivity.java*/
    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch(requestCode){
            case LOCATION_REQUEST_CODE:
                Log.d(getClass().getName(), "location permissions granted");
                map.clear();
                map.setMyLocationEnabled(true);
                break;
            default:
                break;
        }
    }

    private BroadcastReceiver gameTasksListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Task> tasks = intent.getParcelableArrayListExtra(GameLocationService.BROADCAST_PAYLOAD_CODE);
            Log.d(getClass().getName(), "List of tasks received in bcast receiver " + tasks.size());
            displayTasks(tasks);
        }
    };
    private BroadcastReceiver ownLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location loc = intent.getParcelableExtra(GameLocationService.BROADCAST_PAYLOAD_CODE);
            displayCurrentLocation(loc);
        }
    };

    private BroadcastReceiver taskCompletedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location loc = intent.getParcelableExtra(GameLocationService.BROADCAST_PAYLOAD_CODE);
            displayLastLocation();
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                if (canAccessLocation()) {
                    establishServiceConnection();
                }
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(gameTasksListReceiver);
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(ownLocationReceiver);
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(taskCompletedReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }


    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServiceConnection != null){
            mBinder.activityInForeground(false);
            unbindService(mServiceConnection);
            mServiceConnection = null;
        }
        mapView.onDestroy();
    }

    /* methods related to the MapView are below */

    private void displayLastLocation(){
        displayCurrentLocation(mBinder.getCurrentLocation());
        displayTasks(mBinder.getPendingTasks());
    }

    /**
     * Moves the map to the current location in case the user pans too far away
     */
    private void displayCurrentLocation(Location location){
        if (map != null && location != null) {
//                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
        }
    }

    private void displayTasks(List<Task> tasks){
        if (map == null || tasks == null){
            return;
        }
        MarkerViewOptions m;
        for (Task t : tasks){
            m = new MarkerViewOptions()
                    .position(new LatLng(t.latitude, t.longitude));
            Location location = mBinder.getCurrentLocation();
            if (location != null){
                float[] diff = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        t.latitude, t.longitude, diff);
                m.title(String.valueOf(diff[0]) + " meters away");
            }

            map.addMarker(m);
        }
    }


    private void displayHistoryTrail(List<Task> tasks, List<TrailPoint> points) {
        IconFactory iconFactory = IconFactory.getInstance(this);
        Icon icon = iconFactory.fromResource(R.drawable.purple_marker);

        MarkerViewOptions m;
        for (Task t : tasks){
            m = new MarkerViewOptions()
                    .position(new LatLng(t.latitude, t.longitude));
            Location location = mBinder.getCurrentLocation();
            if (location != null){
                float[] diff = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        t.latitude, t.longitude, diff);
                m.title(String.valueOf(diff[0]) + " meters away");
            }
            m.icon(icon);
            map.addMarker(m);
        }

        List<LatLng> latLngs = new ArrayList<>();
        for (TrailPoint point : points){
            LatLng latLng = new LatLng(point.latitude, point.longitude);
            latLngs.add(latLng);
        }
        map.addPolyline(new PolylineOptions()
                .addAll(latLngs)
                .color(Color.MAGENTA)
                .width(10));
        closeHistoryBtn.setVisibility(View.VISIBLE);
    }

}
