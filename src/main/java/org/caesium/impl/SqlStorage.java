package org.caesium.impl;

import org.caesium.base.CaesiumTask;
import org.caesium.base.Storage;
import org.caesium.base.db.DbSystem;
import org.caesium.base.db.DbTask;
import org.caesium.impl.db.SystemConfigRepository;
import org.caesium.impl.db.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
* Implements a SQL Storage to persist Tasks. The default underlying engine is Postgres.
* */
@Service
public class SqlStorage implements Storage {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SystemConfigRepository systemRepository;

    /**
     * Fetches tasks which are scheduled for today.
     * @return A list of entity Tasks.
     */
    @Override
    public List<DbTask> fetchTasksForToday() {
        // fetching today's tasks consists of querying for tasks starting at 00:00-23:59
        OffsetDateTime startTime = OffsetDateTime.now().with(LocalTime.MIN);
        OffsetDateTime endTime = OffsetDateTime.now().with(LocalTime.MAX);

        return taskRepository.getTimeRangeTasks(startTime, endTime);
    }

    /**
     * Fetches tasks that should've been run until now, but are not marked as such yet.
     * @return A list of tasks that should've been run.
     */
    @Override
    public List<DbTask> detectMissedTasks() {
        // missed tasks range from when the system was initially started up until now.
        DbSystem currentSystem = getOrCreateSystem();
        OffsetDateTime endTime = OffsetDateTime.now().with(LocalTime.MIN);

        return taskRepository.getTimeRangeTasks(currentSystem.getCreatedAt(), endTime)
                .stream()
                .filter(t -> !t.isCompleted())
                .toList();
    }

    /**
     * Fetches all the registered tasks in the SQL storage.
     * @return The full list of tasks.
     */
    @Override
    public List<DbTask> fetchAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Queries for a persisted task by the given name.
     * @param name The name of the task.
     * @return The persisted task with the given name. NULL if nothing was found.
     */
    @Override
    public DbTask fetchTaskByName(String name) {
        return taskRepository.findByName(name);
    }

    /**
     * Persists a new task into the SQL storage.
     * @param task The @CaesiumTask that should be saved.
     */
    @Override
    public DbTask insertTask(CaesiumTask task) {
        return taskRepository.save(DbTask.builder()
                .name(task.createTaskName())
                .completed(false)
                .scheduleTime(task.getTriggerTime())
                .build()
        );
    }

    /**
     * Marks a task as completed in the SQL storage.
     * @param task The task to be marked.
     */
    @Override
    public DbTask markTaskCompleted(DbTask task) {
        task.setCompleted(true);
        task.setRanTime(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));
        return taskRepository.save(task);
    }

    /**
     * Returns the current system configuration from the storage. If no configuration is found, then it creates one.
     * @return The current system configuration.
     */
    @Override
    public DbSystem getOrCreateSystem() {
        return systemRepository.getCurrentActiveSystem().orElseGet(() ->
                systemRepository.save(
                        DbSystem.builder()
                                .createdAt(OffsetDateTime.now())
                                .active(true)
                                .build()
                )
        );
    }
}
