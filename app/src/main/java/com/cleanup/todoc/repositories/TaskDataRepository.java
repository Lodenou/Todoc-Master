package com.cleanup.todoc.repositories;

import android.arch.lifecycle.LiveData;

import com.cleanup.todoc.database.dao.TaskDao;
import com.cleanup.todoc.model.Task;

import java.util.List;

public class TaskDataRepository {

    private final TaskDao mTaskDao;

    public TaskDataRepository(TaskDao taskDao) {
        mTaskDao = taskDao;
    }

    public LiveData<List<Task>> getTasks(long projectId) {
        return this.mTaskDao.getTask(projectId);
    }
    // --- CREATE ---

    public void createTask(Task task){ mTaskDao.insertTask(task); }

    // --- DELETE ---
    public void deleteTask(long taskId){ mTaskDao.deleteTask(taskId); }

    // --- UPDATE ---
    public void updateTask(Task task){ mTaskDao.updateTask(task); }
}
