package org.caesium;

import org.caesium.base.CaesiumTask;

import java.time.OffsetDateTime;

/**
 * A demo task.
 */
public class MyTask extends CaesiumTask {

    public MyTask(String id, OffsetDateTime triggerTime) {
        super(id, triggerTime);
    }

    /**
     * Implements the logic of the task.
     */
    @Override
    public void execute() {
        // this task just acts like it does something
        System.out.println("Starting my task");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            System.out.println("My task interrupted.");
        }
        System.out.println("My task completed.");
    }
}
