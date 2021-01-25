package io.github.transfusion.geogame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.bignerdranch.expandablerecyclerview.model.Parent;

import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.transfusion.geogame.database.Task;
import io.github.transfusion.geogame.database.TrailContract;
import io.github.transfusion.geogame.database.TrailPoint;

public class GameHistoryActivity extends AppCompatActivity {

    public static final String HISTORY_DAY_TASKS = "history-day-tasks";
    public static final String HISTORY_DAY_TRAILPOINTS = "history-day-trailpoints";

    public static class HistoryDay implements Parent<Task> {
        private List<Task> dayCompletedTasks;
        private List<TrailPoint> trailEntries;

        private LocalDate ts;
        private int numCompleted;
        private long distanceTravelled;

        public HistoryDay(LocalDate ts, int numCompleted, long distanceTravelled,
                          List<Task> tasks, List<TrailPoint> trailEntries){
            this.ts = ts;
            this.numCompleted = numCompleted;
            this.distanceTravelled = distanceTravelled;
            this.dayCompletedTasks = tasks;
            this.trailEntries = trailEntries;
        }

        @Override
        public List<Task> getChildList() {
            return dayCompletedTasks;
        }

        @Override
        public boolean isInitiallyExpanded() {
            return false;
        }
    }

    public class HistoryDayItemViewHolder extends ParentViewHolder {
        private TextView day;
        private TextView numCompleted;
        private TextView distanceTravelled;

        private HistoryDay item;

        public HistoryDayItemViewHolder(@NonNull View itemView) {
            super(itemView);
            day = (TextView) itemView.findViewById(R.id.history_day);
            numCompleted = (TextView) itemView.findViewById(R.id.history_num_completed);
            distanceTravelled = (TextView) itemView.findViewById(R.id.history_distance_travelled);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    displayOnMainActivity(item.dayCompletedTasks, item.trailEntries);
                    return true;
                }
            });
        }

        public void bind(HistoryDay item){
            this.item = item;
            day.setText(item.ts.toString());
            numCompleted.setText("Tasks: " + String.valueOf(item.numCompleted));
            distanceTravelled.setText("Total Distance: "+String.valueOf(item.distanceTravelled) + " meters");
        }

    }
    public class HistoryDayTaskViewHolder extends ChildViewHolder{
        private TextView history_child_ts;
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");

        public HistoryDayTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            this.history_child_ts = (TextView) itemView.findViewById(R.id.history_child_ts);
        }

        public void bind(Task task){
            Date d = new Date(task.ts);
            this.history_child_ts.setText(sdf.format(d));
        }
    }

    public class HistoryDayAdapter extends ExpandableRecyclerAdapter<HistoryDay, Task, HistoryDayItemViewHolder, HistoryDayTaskViewHolder> {

        private LayoutInflater mInflater;

        public HistoryDayAdapter(Context context, @NonNull List<HistoryDay> taskList) {
            super(taskList);
            mInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public HistoryDayItemViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
            View historyDayView = mInflater.inflate(R.layout.history_item_parent, parentViewGroup, false);
            return new HistoryDayItemViewHolder(historyDayView);
        }

        @NonNull
        @Override
        public HistoryDayTaskViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
            View historyDayTaskView = mInflater.inflate(R.layout.history_item_child, childViewGroup, false);
            return new HistoryDayTaskViewHolder(historyDayTaskView);
        }

        @Override
        public void onBindParentViewHolder(@NonNull HistoryDayItemViewHolder parentViewHolder, int parentPosition, @NonNull HistoryDay parent) {
            parentViewHolder.bind(parent);
        }

        @Override
        public void onBindChildViewHolder(@NonNull HistoryDayTaskViewHolder childViewHolder, int parentPosition, int childPosition, @NonNull Task child) {
            childViewHolder.bind(child);
        }
    }

    RecyclerView mRecyclerView;
    TaskManager taskManager;
    List<Task> allCompletedTasks;
    List<TrailPoint> allTrailPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);
        setupUI();
        displayCompletedTasks();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                Intent result = new Intent();
                setResult(Activity.RESULT_CANCELED, result);
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupUI() {
        mRecyclerView = (RecyclerView) findViewById(R.id.history_recyclerview);
    }

    private void displayCompletedTasks() {
        taskManager = new TaskManager(getContentResolver());
        allCompletedTasks = taskManager.getCompletedTasks();
        allTrailPoints = taskManager.getAllTrailPoints();

//        Organize the task log into days
        HashMap<LocalDate, HistoryDay> m = new HashMap<>();
        for (Task t : allCompletedTasks){
            LocalDate d = new LocalDate(t.ts);
            if (!m.containsKey(d)){
                m.put(d, new HistoryDay(d, 0, 0, new ArrayList<Task>(),
                        new ArrayList<TrailPoint>()));
            }
            m.get(d).dayCompletedTasks.add(t);
        }
//        Get number of completed tasks per day
        for (Map.Entry<LocalDate, HistoryDay> entry : m.entrySet()){
            entry.getValue().numCompleted = entry.getValue().dayCompletedTasks.size();
        }
//        Calculate the total distance travelled
        HashMap<LocalDate, List<Location>> l = new HashMap<>();
        for (TrailPoint t : allTrailPoints) {
            LocalDate d = new LocalDate(t.ts);
            if (!l.containsKey(d)){
                l.put(d, new ArrayList<Location>());
            }
            Location loc = new Location("");
            loc.setLatitude(t.latitude);
            loc.setLongitude(t.longitude);
            l.get(d).add(loc);

            if (!m.containsKey(d)){
                m.put(d, new HistoryDay(d, 0, 0, new ArrayList<Task>(),
                        new ArrayList<TrailPoint>()));
            }
            m.get(d).trailEntries.add(t);
        }

        for (LocalDate ld : l.keySet()){
            /*if (!m.containsKey(ld)){
                m.put(ld, new HistoryDay(ld, 0, 0, new ArrayList<Task>(),
                        new ArrayList<TrailPoint>()));
            }*/
            List<Location> trailForDay = l.get(ld);
            long totalDistance = 0;
            for (int i = 1; i < trailForDay.size(); i++){
                totalDistance += trailForDay.get(i).distanceTo(trailForDay.get(i-1));
            }
            m.get(ld).distanceTravelled = totalDistance;
        }

        List<HistoryDay> historyEntries = new ArrayList<>();
//        (LocalDate ts, int numCompleted, long distanceTravelled, List<Task> tasks)
        for (Map.Entry<LocalDate, HistoryDay> e : m.entrySet()){
            historyEntries.add(e.getValue());
        }

        final HistoryDayAdapter adapter = new HistoryDayAdapter(this, historyEntries);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT ) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if (viewHolder instanceof HistoryDayItemViewHolder){
                    adapter.collapseAllParents();
//                    Toast.makeText(GameHistoryActivity.this, "on Swiped ", Toast.LENGTH_SHORT).show();
                    HistoryDayItemViewHolder hv = (HistoryDayItemViewHolder) viewHolder;
                    HistoryDay hd = adapter.getParentList().get(hv.getAdapterPosition());
                    deleteHistoryDay(hd.dayCompletedTasks, hd.trailEntries);
                }
                //Remove swiped item from list and notify the RecyclerView

            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void deleteHistoryDay(List<Task> tasks, List<TrailPoint> trailEntries){
        for (Task t : tasks){
            taskManager.deleteTask(t.id);
        }
        for (TrailPoint t : trailEntries){
            taskManager.deleteTrailPoint(t.id);
        }
    }

    private void displayOnMainActivity(List<Task> tasks, List<TrailPoint> trailEntries){
        Intent result = new Intent();
        result.putParcelableArrayListExtra(HISTORY_DAY_TASKS, new ArrayList<>(tasks));
        result.putParcelableArrayListExtra(HISTORY_DAY_TRAILPOINTS, new ArrayList<>(trailEntries));
        setResult(Activity.RESULT_OK, result);
        finish();
    }
}
