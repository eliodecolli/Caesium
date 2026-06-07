package org.caesium.impl.db;

import org.caesium.base.db.DbTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Represents a JPA Repository for DbTasks.
 */
@Repository
public interface TaskRepository extends JpaRepository<DbTask, Long> {
    @Query("SELECT t FROM DbTask t WHERE (t.scheduleTime BETWEEN :start AND :end) AND t.completed = FALSE")
    List<DbTask> getTimeRangeTasks(
            @Param("start")
            OffsetDateTime start,

            @Param("end")
            OffsetDateTime end
    );

    @Query("SELECT t FROM DbTask t WHERE t.name = :name")
    DbTask findByName(
            @Param("name")
            String name
    );
}
