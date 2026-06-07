package org.caesium.base.db;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Indexed;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents a persisted system configuration record in the SQL storage.
 */
@Entity
@Getter
@Setter
@Builder
@Table(name = "caesium_system_config")
@NoArgsConstructor
@AllArgsConstructor
public class DbSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private OffsetDateTime createdAt;

    @Column
    private boolean active;
}
