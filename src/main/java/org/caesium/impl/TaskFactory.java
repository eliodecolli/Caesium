package org.caesium.impl;

import org.caesium.base.CaesiumTask;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.OffsetDateTime;

/**
 * A Task Factory used to build tasks.
 */
public class TaskFactory {

    /**
     * Creates a new instance of a defined derived @CaesiumTask class.
     * @param taskClass The type of the derived task.
     * @param id The ID of the task.
     * @param scheduleTime The scheduled time to run this task.
     * @return A newly created instance of T.
     * @param <T> The derived type from @CaesiumTask.
     */
    public static <T extends CaesiumTask> T createTask(Class<T> taskClass, String id, OffsetDateTime scheduleTime) {
        try {
            // we use reflection to programmatically instantiate a new object of the given type
            // first we get a hold of the constructor for the object, based on its given type
            // then we use the constructor to generate a new instance
            Constructor<T> constructor = taskClass.getDeclaredConstructor(String.class, OffsetDateTime.class);
            return constructor.newInstance(id, scheduleTime);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Task class " + taskClass.getSimpleName() + " must have a constructor (String, OffsetDateTime)", e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to instantiate task " + taskClass.getSimpleName(), e);
        }
    }
}
