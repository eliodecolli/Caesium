package org.caesium;

import org.caesium.impl.CaesiumSystem;
import org.caesium.impl.TaskFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Main.class, args);
        CaesiumSystem system = context.getBean(CaesiumSystem.class);
        MyTask taskOne = TaskFactory.createTask(MyTask.class, "taskOne", OffsetDateTime.now().plusMinutes(1));
        MyTask taskTwo = TaskFactory.createTask(MyTask.class, "taskTwo", OffsetDateTime.now().plusSeconds(10));

        system.registerTask(taskOne);
        system.registerTask(taskTwo);

        system.run();
    }
}
