package com.cleanup.todoc.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.cleanup.todoc.R;
import com.cleanup.todoc.database.TodocDatabase;
import com.cleanup.todoc.injections.Injection;
import com.cleanup.todoc.injections.ViewModelFactory;
import com.cleanup.todoc.model.Project;
import com.cleanup.todoc.model.Task;
import com.cleanup.todoc.repositories.TaskDataRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * <p>Home activity of the application which is displayed when the user opens the app.</p>
 * <p>Displays the list of tasks.</p>
 *
 * @author Gaëtan HERFRAY
 */
public class MainActivity extends AppCompatActivity implements TasksAdapter.DeleteTaskListener {

    // List of all projects available in the application
    private final Project[] allProjects = Project.getAllProjects();

    // List of all current tasks of the application
    @NonNull
    private final ArrayList<Task> tasks = new ArrayList<>();

    // The adapter which handles the list of tasks
    private final TasksAdapter adapter = new TasksAdapter(tasks, this);

    // The sort method to be used to display tasks
    @NonNull
    private SortMethod sortMethod = SortMethod.NONE;

    //Dialog to create a new task
    @Nullable
    public AlertDialog dialog = null;

    //EditText that allows user to set the name of a task
    @Nullable
    private EditText dialogEditText = null;


    //Spinner that allows the user to associate a project to a task
    @Nullable
    private Spinner dialogSpinner = null;

    // The RecyclerView which displays the list of tasks
    // Suppress warning is safe because variable is initialized in onCreate
    @SuppressWarnings("NullableProblems")
    @NonNull
    private RecyclerView listTasks;

    // The TextView displaying the empty state
    // Suppress warning is safe because variable is initialized in onCreate
    @SuppressWarnings("NullableProblems")
    @NonNull
    private TextView lblNoTasks;

    // 1 - FOR DATA
    Project mProject;
    private TaskViewModel taskViewModel;
    private long PROJECT_ID;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        listTasks = findViewById(R.id.list_tasks);
        lblNoTasks = findViewById(R.id.lbl_no_task);

        listTasks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        listTasks.setAdapter(adapter);

        findViewById(R.id.fab_add_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTaskDialog();
            }
        });
        //  Configure ViewModel
        Log.d("main", "onCreate");
        this.configureViewModel();

        // 9 - Get current project & tasks from Database
        this.getCurrentProject(PROJECT_ID);
        this.getTasks();
        this.taskViewModel.getTasks().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(@Nullable List<Task> task2) {
                tasks.clear();
                assert task2 != null;
                tasks.addAll(task2);
                updateTasks();


            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.refresh_button){
            Injection.provideTaskDataSource(getApplicationContext());
        }

        if (id == R.id.filter_alphabetical) {
            sortMethod = SortMethod.ALPHABETICAL;
        } else if (id == R.id.filter_alphabetical_inverted) {
            sortMethod = SortMethod.ALPHABETICAL_INVERTED;
        } else if (id == R.id.filter_oldest_first) {
            sortMethod = SortMethod.OLD_FIRST;
        } else if (id == R.id.filter_recent_first) {
            sortMethod = SortMethod.RECENT_FIRST;
        }

        updateTasks();

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onDeleteTask(Task task) {
//        tasks.remove(task);
//        updateTasks();
        taskViewModel.deleteTask(task.getId());
        updateTasksList(tasks);
        if (adapter.getItemCount() == 0) {
            Log.d("main", "onDeleteTask: ");
            refresh();
        }


    }
    // -------------------
    // DATA
    // -------------------

    // 2 - Configuring ViewModel
    private void configureViewModel(){
        ViewModelFactory mViewModelFactory = Injection.provideViewModelFactory(this);
        this.taskViewModel = ViewModelProviders.of(this, mViewModelFactory).get(TaskViewModel.class);
        this.taskViewModel.init(PROJECT_ID);
    }
    //3 - Get Current Project
    private void getCurrentProject(long projectId){
        this.taskViewModel.getProject(projectId).observe(this, this::updateHeader);
    }
    // 3 - Get all items for a user
    private void getTasks(){
        this.taskViewModel.getTasks().observe(this, this::updateTasksList);
    }

    // UI

    // 5 - Update header (username & picture)
    private void updateHeader(Project project){
        Log.d("","");
    }
 
    // 6 - Update the list of items
    private void updateTasksList(List<Task> tasks){
//        this.adapter.updateData(tasks);
        adapter.updateTasks(tasks);
    }
    /**
     * Called when the user clicks on the positive button of the Create Task Dialog.
     *
     * @param dialogInterface the current displayed dialog
     */
    private void onPositiveButtonClick(DialogInterface dialogInterface) {

        // If dialog is open
        if (dialogEditText != null && dialogSpinner != null) {
            // Get the name of the task
            String taskName = dialogEditText.getText().toString();

            // Get the selected project to be associated to the task
            Project taskProject = null;
            if (dialogSpinner.getSelectedItem() instanceof Project) {
                taskProject = (Project) dialogSpinner.getSelectedItem();
            }

            // If a name has not been set
            if (taskName.trim().isEmpty()) {
                dialogEditText.setError(getString(R.string.empty_task_name));
            }
            // If both project and name of the task have been set
            else if (taskProject != null) {
                Task task = new Task(

                        taskProject.getId(),
                        taskName,
                        new Date().getTime());

                // addTask has been changed to create new task in the db
                addTask(task);

                dialogInterface.dismiss();
            }
            // If name has been set, but project has not been set (this should never occur)
            else{
                dialogInterface.dismiss();
            }
        }
        // If dialog is aloready closed
        else {
            dialogInterface.dismiss();
        }
    }

    /**
     * Shows the Dialog for adding a Task
     */
    private void showAddTaskDialog() {
        final AlertDialog dialog = getAddTaskDialog();

        dialog.show();

        dialogEditText = dialog.findViewById(R.id.txt_task_name);
        dialogSpinner = dialog.findViewById(R.id.project_spinner);

        populateDialogSpinner();
    }

    /**
     * Adds the given task to the list of created tasks.
     *
     * @param task the task to be added to the list
     */
    private void addTask(@NonNull Task task) {
        taskViewModel.createTask(task);
        tasks.add(task);

        if(task.getProjectId() == 1)
        {
            PROJECT_ID = 1;
        }
        if(task.getProjectId() == 2) {
            PROJECT_ID = 2;
        }
        if (task.getProjectId() == 3) {
            PROJECT_ID = 3;
        }
        adapter.updateTasks(tasks);
        if(adapter.getItemCount() == 0) {
            Log.d("main", "addTask: adapter == 0 ");
            Injection.provideTaskDataSource(getApplicationContext());
        }
//        taskViewModel.updateTask(task);
//        updateTasks();
//        refresh();
    }

    public void refresh(){
        Intent i = new Intent(MainActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    public void onDestroy() {
        Log.d("main", "onDestroy");
        super.onDestroy();
    }
    @Override
    protected void onRestart() {
        Log.d("main", "onRestart");
        super.onRestart();
    }

    @Override
    protected void onStop() {

        Log.d("main", "onStop");
        super.onStop();
    }

    @Override
    protected void onResume() {

        Log.d("main", "onResume");
        super.onResume();
    }

    @Override
    protected void onStart() {

        Log.d("main", "onStart");
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.d("main", "onPause");
        super.onPause();
    }

    /**
     * Updates the list of tasks in the UI
     */
    private void updateTasks() {

        if (tasks.size() == 0) {
            lblNoTasks.setVisibility(View.VISIBLE);
            listTasks.setVisibility(View.GONE);
        } else {
                lblNoTasks.setVisibility(View.GONE);
                listTasks.setVisibility(View.VISIBLE);
            switch (sortMethod) {
                case ALPHABETICAL:
                    Collections.sort(tasks, new Task.TaskAZComparator());
                    break;
                case ALPHABETICAL_INVERTED:
                    Collections.sort(tasks, new Task.TaskZAComparator());
                    break;
                case RECENT_FIRST:
                    Collections.sort(tasks, new Task.TaskRecentComparator());
                    break;
                case OLD_FIRST:
                    Collections.sort(tasks, new Task.TaskOldComparator());
                    break;

            }
            adapter.updateTasks(tasks);
        }
    }

    /**
     * Returns the dialog allowing the user to create a new task.
     *
     * @return the dialog allowing the user to create a new task
     */
    @NonNull
    private AlertDialog getAddTaskDialog() {
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, R.style.Dialog);

        alertBuilder.setTitle(R.string.add_task);
        alertBuilder.setView(R.layout.dialog_add_task);
        alertBuilder.setPositiveButton(R.string.add, null);
        alertBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                dialogEditText = null;
                dialogSpinner = null;
                dialog = null;
            }
        });

        dialog = alertBuilder.create();

        // This instead of listener to positive button in order to avoid automatic dismiss
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        onPositiveButtonClick(dialog);
                    }
                });
            }
        });

        return dialog;
    }

    /**
     * Sets the data of the Spinner with projects to associate to a new task
     */
    private void populateDialogSpinner() {
        final ArrayAdapter<Project> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allProjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (dialogSpinner != null) {
            dialogSpinner.setAdapter(adapter);
        }
    }

    /**
     * List of all possible sort methods for task
     */
    private enum SortMethod {
        /**
         * Sort alphabetical by name
         */
        ALPHABETICAL,
        /**
         * Inverted sort alphabetical by name
         */
        ALPHABETICAL_INVERTED,
        /**
         * Lastly created first
         */
        RECENT_FIRST,
        /**
         * First created first
         */
        OLD_FIRST,
        /**
         * No sort
         */
        NONE
    }
}
