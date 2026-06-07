package org.caesium.base;

import org.caesium.base.db.DbTask;

import java.util.List;
import java.util.Optional;

public interface SystemBase {
    void run();
    void registerTask(CaesiumTask task);
    List<DbTask> getCollectedTasks();
    Optional<DbTask> markTaskCompleted(String name);
    List<DbTask> getAllTasks();
}
