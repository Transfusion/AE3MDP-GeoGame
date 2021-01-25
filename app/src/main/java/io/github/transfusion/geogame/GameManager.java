package io.github.transfusion.geogame;

import android.content.ContentResolver;
import android.location.Location;

import java.util.List;
import io.github.transfusion.geogame.database.Task;

/**
 * This class encapsulates the task management logic.
 * Created by Bryan Kok on 17-5-10.
 *
 */

public class GameManager {
    private TaskManager taskManager;
    private SharedPrefsManager.Prefs gameSettings;
    ContentResolver cr;

    private List<Task> pendingTasks;
    public GameManager(ContentResolver cr, SharedPrefsManager.Prefs settings){
        this.cr = cr;
        taskManager = new TaskManager(cr);
        gameSettings = settings;
//        loadGame();
    }

    /*public void loadGame() {
        pendingTasks = taskManager.getPendingTasks();
    }*/

    public void refreshGame(Location location){
        pendingTasks = taskManager.getPendingTasks();
        while(pendingTasks.size() < gameSettings.simultaneousTasks){
            pendingTasks.add(taskManager.genNewTask(location.getLatitude(),
                    location.getLongitude(), gameSettings.radiusMeters));
        }
    }

    public List<Task> getPendingTasks(){
        return pendingTasks;
    }

    public void switchSettings(SharedPrefsManager.Prefs settings, Location location, boolean force){
        if (!settings.equals(gameSettings) || force){
            gameSettings = settings;
            taskManager.deleteAllPendingTasks();
            pendingTasks.clear();
            for (int i = 0; i < settings.simultaneousTasks; i++){
                pendingTasks.add(taskManager.genNewTask(location.getLatitude(),
                        location.getLongitude(), settings.radiusMeters));
            }
        }
    }

    public void markTaskCompleted(long id){
        taskManager.markTaskCompleted(id, true);
    }

    public void logTrailPoint(double latitude, double longitude) {
        taskManager.logTrailPoint(latitude, longitude);
    }
}
