package org.caesium.base.db;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Represents a persisted task in the SQL storage.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "caesium_tasks")
public class DbTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private OffsetDateTime scheduleTime;

    @Column
    private OffsetDateTime ranTime;

    @Column(nullable = false)
    private boolean completed;
}
