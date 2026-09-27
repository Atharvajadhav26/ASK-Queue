package com.smartqueue.repository;

import com.smartqueue.entity.ServiceQueue;
import com.smartqueue.entity.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueueRepository extends JpaRepository<ServiceQueue, Long> {
    Optional<ServiceQueue> findByQueueId(String queueId);
    List<ServiceQueue> findByStatus(QueueStatus status);
    List<ServiceQueue> findByCreatedBy(Long createdBy);
    boolean existsByQueueId(String queueId);
    List<ServiceQueue> findByStatusIn(List<QueueStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM ServiceQueue q WHERE q.id = :id")
    Optional<ServiceQueue> findByIdWithLock(@Param("id") Long id);
}
