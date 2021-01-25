package io.github.transfusion.geogame;

import android.app.Activity;
import android.content.Intent;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class GameControlActivity extends AppCompatActivity {


    private GamePreferencesFragment mFragment;

    public static class GamePreferencesFragment extends PreferenceFragment {
        SharedPrefsManager sharedPrefsManager;
        SharedPrefsManager.Prefs settings;

        public SharedPrefsManager.Prefs getPrefs(){
            return settings;
        }

        public void savePrefs(){
            sharedPrefsManager.saveSettings(settings);
        }

        EditTextPreference task_simul_pref;
        EditTextPreference task_radius_pref;
        EditTextPreference update_interval_pref;
        EditTextPreference update_distance_pref;

        EditTextPreference notification_distance;
        EditTextPreference task_complete_radius;

        @Override
        public void onCreate(final Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            /*PreferenceManager manager = getPreferenceManager();
            manager.setSharedPreferencesName(SharedPrefsManager.MAIN_PREFS_FILE);*/
            sharedPrefsManager = new SharedPrefsManager(GamePreferencesFragment.this
                    .getActivity().getSharedPreferences(SharedPrefsManager.MAIN_PREFS_FILE, MODE_PRIVATE));
            settings = sharedPrefsManager.getSettings();

            addPreferencesFromResource(R.xml.game_preferences);
            setupUI();
        }

//        Want to save the sharedpreferences as ints/longs via sharedprefsmanager
        private void setupUI() {
            task_simul_pref = (EditTextPreference) findPreference(SharedPrefsManager.SIMUL_TASKS_KEY);
            task_simul_pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    settings.simultaneousTasks = Integer.parseInt((String) newValue);
                    task_simul_pref.setSummary((String) newValue);
                    return true;
                }
            });
            task_simul_pref.setText(String.valueOf(settings.simultaneousTasks));

            task_radius_pref = (EditTextPreference) findPreference(SharedPrefsManager.RADIUS_KEY);
            task_radius_pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    settings.radiusMeters = Long.parseLong((String) newValue);
                    task_radius_pref.setSummary((String) newValue);
                    return true;
                }
            });
            task_radius_pref.setText(String.valueOf(settings.radiusMeters));

            update_interval_pref = (EditTextPreference) findPreference(SharedPrefsManager.UPDATE_TIME_INTERVAL_KEY);
            update_interval_pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    settings.updateTimeInterval = Integer.parseInt((String) newValue);
                    update_distance_pref.setSummary((String) newValue);
                    return true;
                }
            });
            update_interval_pref.setText(String.valueOf(settings.updateTimeInterval));

            update_distance_pref = (EditTextPreference) findPreference(SharedPrefsManager.UPDATE_DISTANCE_INTERVAL_KEY);
            update_distance_pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    settings.updateDistanceInterval = Integer.parseInt((String) newValue);
                    update_distance_pref.setSummary((String) newValue);
                    return true;
                }
            });
            update_distance_pref.setText(String.valueOf(settings.updateDistanceInterval));

            notification_distance = (EditTextPreference) findPreference(SharedPrefsManager.NOTIFICATION_NEARBY_RADIUS_KEY);
            notification_distance.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    settings.notificationNearbyTaskRadius = Long.parseLong((String) newValue);
                    notification_distance.setSummary((String) newValue);
                    return true;
                }
            });
            notification_distance.setText(String.valueOf(settings.notificationNearbyTaskRadius));

            task_complete_radius = (EditTextPreference) findPreference(SharedPrefsManager.TASK_COMPLETE_RADIUS_KEY);
            task_complete_radius.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    settings.taskCompleteRadius = Long.parseLong((String) newValue);
                    task_complete_radius.setSummary((String)newValue);
                    return true;
                }
            });
            task_complete_radius.setText(String.valueOf(settings.taskCompleteRadius));

        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_game_control);
        mFragment = new GamePreferencesFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, mFragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            /*
            Only save settings when the toolbar back button is used to return to the parent activity (MainActivity)
               https://developer.android.com/training/basics/fragments/communicating.html Activity -> Fragment commms
               can be done directly using public methods; vice versa requires defining interfaces.
            */
            case android.R.id.home:
                mFragment.savePrefs();
                Intent result = new Intent();
                setResult(Activity.RESULT_OK, result);
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
