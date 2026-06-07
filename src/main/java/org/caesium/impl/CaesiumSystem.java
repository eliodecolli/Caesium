package org.caesium.impl;

import jakarta.transaction.Transactional;
import org.caesium.base.CaesiumTask;
import org.caesium.base.Clock;
import org.caesium.base.Storage;
import org.caesium.base.SystemBase;
import org.caesium.base.db.DbTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * The main implementation of the Caesium Task Scheduler.
 */
@Service
public class CaesiumSystem implements SystemBase {
    @Autowired
    private Clock internalClock;

    @Autowired
    private Storage taskStorage;

    private OffsetDateTime currentDate;

    private final List<DbTask> todayTasks;
    private volatile boolean started;

    private final Map<String, CaesiumTask> registeredTasks;

    private static final Logger log = LoggerFactory.getLogger(CaesiumSystem.class);

    public CaesiumSystem() {
        started = false;
        registeredTasks = new HashMap<>();
        todayTasks = new ArrayList<>();
    }

    private synchronized void populateTodayTasks() {
        // we need to run today's tasks, as well as the ones that have been scheduled but did not run
        List<DbTask> newTasks = taskStorage.fetchTasksForToday();
        List<DbTask> pendingMissed = taskStorage.detectMissedTasks();

        // reset the current tasks so we make sure we clear the memory
        todayTasks.clear();

        todayTasks.addAll(newTasks);
        todayTasks.addAll(0, pendingMissed);

        // to avoid unnecessary logging, only log if you have more than 0 tasks
        // this prevents entering an infinite loop if you run the system with 0 tasks registered
        int totalTasks = todayTasks.size();
        if (totalTasks > 0) {
            log.info("[x] Fetched today's tasks: {}", todayTasks.size());
        }
    }

    private void tryPopulatingTasks() {
        if (started && todayTasks.isEmpty()) {
            // check again if we have any new tasks registered
            populateTodayTasks();
        }
    }

    private String extractTaskName(DbTask task) {
        // the task name in the DB is saved as:
        // {task_id}-{scheduled_time}
        // the convention is that a CaesiumTask's ID is its name
        // in the DB the name is post-fixed with the schedule time
        String name = task.getName();
        return name.substring(0, name.lastIndexOf('-'));
    }

    private synchronized void step(long timestamp) {
        // what's the time according to the @AtomicClock instance?
        OffsetDateTime currentTime = Instant.ofEpochSecond(timestamp).atOffset(ZoneOffset.UTC);

        if (currentTime.getDayOfYear() == currentDate.getDayOfYear()) {
            // we're still in the current day here
            tryPopulatingTasks();

            // iterating over and over in memory is much faster than querying the DB every time
            List<DbTask> scheduledToRun = todayTasks.stream()
                    .filter(t -> !t.isCompleted() && (t.getScheduleTime().isBefore(currentTime) || t.getScheduleTime().isEqual(currentTime)))
                    .toList();

            // iterate over each task
            scheduledToRun.forEach(t -> {
                CaesiumTask task  = registeredTasks.get(extractTaskName(t));

                if (task == null) {
                    log.info("[x] Task {} was found in the storage but it is not registered in the system.", t.getName());
                }
                else {
                    if (task.isCompleted()) {
                        log.info("[x] Task {} has been completed but not updated in the storage. Updating now...", t.getName());
                        taskStorage.markTaskCompleted(t);
                        task.setRunning(false);
                    }
                    else {
                        if (task.isRunning()) {
                            log.info("[x] Skipping task {}: Currently being run.", t.getName());
                        }
                        else {
                            // since we run each task in its own virtual thread, we need a way to mark the task
                            // as "completed" once it finishes running,
                            // to do this we set a callback function in the task itself, and once it's done,
                            // it will run this callback
                            task.setCompleteCallback(() -> {
                                taskStorage.markTaskCompleted(t);
                                log.info("[x]\t-> Completed task {}", t.getName());
                            });

                            Thread.startVirtualThread(task::run);
                        }
                    }
                }
            });
        }
        else {
            // the day has changed, now reload the tasks
            currentDate = currentTime;
            populateTodayTasks();
        }
    }

    /**
     * Runs the current Caesium Task Scheduler.
     */
    @Override
    public void run() {
        currentDate = OffsetDateTime.now();
        populateTodayTasks();
        internalClock.start(1, this::step);
        started = true;

        log.info("Started Caesium System");
    }

    /**
     * Registers a new task in the system.
     * @param task The @CaesiumTask to register.
     */
    @Override
    @Transactional  // <- this annotation might not be needed?
    public void registerTask(CaesiumTask task) {
        DbTask persisted = taskStorage.insertTask(task);
        registeredTasks.put(task.getId(), task);
    }

    /**
     * Gets the currently collected (today's) tasks.
     * @return A list of collected tasks.
     */
    @Override
    public synchronized List<DbTask> getCollectedTasks() {
        return List.copyOf(todayTasks);
    }

    /**
     * Marks a task as completed.
     * @param name The name of the task to mark as completed.
     */
    @Override
    public synchronized Optional<DbTask> markTaskCompleted(String name) {
        Optional<DbTask> retval = Optional.empty();

        for (int i = 0; i < todayTasks.size(); i++) {
            DbTask task = todayTasks.get(i);

            if (task.getName().compareTo(name) == 0) {
                retval = Optional.ofNullable(taskStorage.markTaskCompleted(task));
                log.info("Task {} has been marked as completed", name);
            }
        }
        return retval;
    }

    /**
     * Fetches all the tasks registered in the system.
     * @return A complete list of all the tasks.
     */
    @Override
    public List<DbTask> getAllTasks() {
        return taskStorage.fetchAllTasks();
    }
}
