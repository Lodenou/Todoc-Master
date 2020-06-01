package com.cleanup.todoc;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.cleanup.todoc.database.TodocDatabase;
import com.cleanup.todoc.model.Project;
import com.cleanup.todoc.model.Task;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class TaskDaoTest {

    // FOR DATA
    private TodocDatabase database;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void initDb() throws Exception {
        this.database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                TodocDatabase.class)
                .allowMainThreadQueries()
                .build();
    }

    @After
    public void closeDb() throws Exception {
        database.close();
    }

    // DATA SET FOR TEST
    private Project mProject;
    private  long PROJECT_ID = 1;
    private Project PROJECT_DEMO = new Project(PROJECT_ID, "Projet Tartampion", 0xFFEADAD1);
    //FIXME LE LONG creationTimestamp est bricolé + changer les textes
    final Task NEW_TASK_0 = new Task((long) 1, 1L, "task 1", new Date().getTime());
    final Task NEW_TASK_1 = new Task((long) 2, 2L, "task 2", new Date().getTime());
    final Task NEW_TASK_2 = new Task((long) 3, 3L, "task 3", new Date().getTime());



    @Test
    public void insertAndGetProject() throws InterruptedException {
        // BEFORE : Adding a new project
        this.database.projectDao().createProject(PROJECT_DEMO);
        // TEST
        Project project = LiveDataTestUtil.getValue(this.database.projectDao().getProject(PROJECT_ID));
        assertTrue(project.getName().equals(PROJECT_DEMO.getName()) && project.getId() == PROJECT_ID);
    }

    @Test
    public void getTasksWhenNoTaskInserted() throws InterruptedException {
        // TEST
        List<Task> tasks = LiveDataTestUtil.getValue(this.database.taskDao().getTask(PROJECT_ID));
        assertTrue(tasks.isEmpty());
    }

    @Test
    public void insertAndGetTasks() throws InterruptedException {
        // BEFORE : Adding demo project & demo tasks

        this.database.projectDao().createProject(PROJECT_DEMO);
        this.database.taskDao().insertTask(NEW_TASK_0);
        this.database.taskDao().insertTask(NEW_TASK_1);
        this.database.taskDao().insertTask(NEW_TASK_2);

        // TEST
        List<Task> tasks = LiveDataTestUtil.getValue(this.database.taskDao().getTask(PROJECT_ID));
        assertTrue(tasks.size() == 3);
    }

    @Test
    public void insertAndUpdateTask() throws InterruptedException {
        // BEFORE : Adding demo project & demo tasks. Next, update task added & re-save it
        this.database.projectDao().createProject(PROJECT_DEMO);
        this.database.taskDao().insertTask(NEW_TASK_0);
        Task taskAdded = LiveDataTestUtil.getValue(this.database.taskDao().getTask(PROJECT_ID)).get(0);
//        taskAdded.setSelected(true);
        this.database.taskDao().updateTask(taskAdded);

//        //TEST
//        List<Item> items = LiveDataTestUtil.getValue(this.database.itemDao().getItems(USER_ID));
//        assertTrue(items.size() == 1 && items.get(0).getSelected());
    }

    @Test
    public void insertAndDeleteTask() throws InterruptedException {
        // BEFORE : Adding demo user & demo item. Next, get the item added & delete it.
        this.database.projectDao().createProject(PROJECT_DEMO);
//        this.database.taskDao().insertTask(NEW_TASK_0);
        Task taskAdded = LiveDataTestUtil.getValue(this.database.taskDao().getTask(PROJECT_ID)).get(0);
        this.database.taskDao().deleteTask(taskAdded.getId());

        //TEST
        List<Task> tasks = LiveDataTestUtil.getValue(this.database.taskDao().getTask(PROJECT_ID));
        assertTrue(tasks.isEmpty());
    }
}


