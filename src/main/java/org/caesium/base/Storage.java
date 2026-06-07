package org.caesium.base;

import org.caesium.base.db.DbSystem;
import org.caesium.base.db.DbTask;

import java.util.List;

/**
 * The core underlying interface for implementing a Storage engine for Caesium.
 */
public interface Storage {
    List<DbTask> fetchTasksForToday();
    List<DbTask> detectMissedTasks();
    List<DbTask> fetchAllTasks();
    DbTask fetchTaskByName(String name);

    DbTask insertTask(CaesiumTask task);
    DbTask markTaskCompleted(DbTask task);

    DbSystem getOrCreateSystem();
}
