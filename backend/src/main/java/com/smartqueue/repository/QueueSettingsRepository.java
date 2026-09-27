package com.smartqueue.repository;

import com.smartqueue.entity.QueueSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QueueSettingsRepository extends JpaRepository<QueueSettings, Long> {
    @Query("SELECT qs FROM QueueSettings qs WHERE qs.queue.id = :queueId")
    Optional<QueueSettings> findByQueueId(@Param("queueId") Long queueId);
}
