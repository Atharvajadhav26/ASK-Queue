package com.smartqueue.repository;

import com.smartqueue.entity.Notification;
import com.smartqueue.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    List<Notification> findByUserAndIsReadFalse(User user);
    
    long countByUserAndIsReadFalse(User user);

    void deleteByQueueId(Long queueId);
}
