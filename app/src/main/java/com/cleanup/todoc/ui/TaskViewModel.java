package com.cleanup.todoc.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import com.cleanup.todoc.model.Project;
import com.cleanup.todoc.model.Task;
import com.cleanup.todoc.repositories.ProjectDataRepository;
import com.cleanup.todoc.repositories.TaskDataRepository;

import java.util.List;
import java.util.concurrent.Executor;

public class TaskViewModel extends ViewModel {

    // REPOSITORIES
    private final TaskDataRepository taskDataSource;
    private final ProjectDataRepository projectDataSource;
    private final Executor executor;
    private  LiveData<List<Task>> tasks;

    // DATA
    @Nullable
    private LiveData<Project> currentProject;

    //CONSTRUCTORS
    public TaskViewModel(TaskDataRepository itemDataSource, ProjectDataRepository userDataSource, Executor executor) {
        this.taskDataSource = itemDataSource;
        this.projectDataSource = userDataSource;
        this.executor = executor;
    }

    public void init(long userId) {
        if (this.currentProject != null) {
            return;
        }
        currentProject = projectDataSource.getProject(userId);
        tasks = taskDataSource.getTasks();
    }

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    // FOR PROJECT
    public LiveData<Project> getProject(long projectId) {
        return this.currentProject;
    }

    //FOR TASK
    public LiveData<List<Task>> getTasks(long projectId) {
        return taskDataSource.getTasks(projectId);
    }

    public void createTask(Task task) {
        executor.execute(() -> {
            taskDataSource.createTask(task);
        });
    }

    public void deleteTask(long taskId) {
        executor.execute(() -> {
            taskDataSource.deleteTask(taskId);
        });
    }

    public void updateTask(Task task) {
        executor.execute(() -> {
            taskDataSource.updateTask(task);
        });
    }


}
