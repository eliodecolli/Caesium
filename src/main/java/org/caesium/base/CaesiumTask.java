package org.caesium.base;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public abstract class CaesiumTask {
    private final String id;
    private final OffsetDateTime triggerTime;

    private boolean isCompleted;
    private boolean running;

    private Runnable completeCallback;

    public CaesiumTask(String id, OffsetDateTime triggerTime) {
        this.id = id;
        this.triggerTime = triggerTime;
        this.isCompleted = false;
    }

    public String createTaskName() {
        return String.format("%s-%d", id, triggerTime.toEpochSecond());
    }

    public abstract void execute();

    public void run() {
        running = true;
        execute();
        if (completeCallback != null) {
            completeCallback.run();
        }
        isCompleted = true;
        running = false;
    }
}
