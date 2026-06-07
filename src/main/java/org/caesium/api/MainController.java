package org.caesium.api;

import org.caesium.base.db.DbTask;
import org.caesium.impl.CaesiumSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class MainController {

    @Autowired
    private CaesiumSystem system;

    @GetMapping
    public List<DbTask> getAllTasks() {
        return system.getAllTasks();
    }

    @GetMapping("/collected")
    public List<DbTask> getCollectedTasks() {
        return system.getCollectedTasks();
    }

    @PostMapping("/complete/{id}")
    public ResponseEntity<DbTask> markTaskCompleted(@PathVariable String id) {
        Optional<DbTask> task = system.markTaskCompleted(id);
        return task.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
