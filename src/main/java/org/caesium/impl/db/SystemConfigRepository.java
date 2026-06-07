package org.caesium.impl.db;

import org.caesium.base.db.DbSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents a System Configuration JPA Repository.
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<DbSystem, UUID> {

    @Query("SELECT s FROM DbSystem s WHERE s.active = true")
    Optional<DbSystem> getCurrentActiveSystem();

}
