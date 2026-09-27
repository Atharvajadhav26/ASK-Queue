package com.smartqueue.repository;

import com.smartqueue.entity.ServiceQueue;
import com.smartqueue.entity.Token;
import com.smartqueue.entity.TokenStatus;
import com.smartqueue.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findByQueueAndStatusInOrderByPriorityDescQueuePositionAsc(ServiceQueue queue, List<TokenStatus> statuses);
    
    List<Token> findByUserAndStatusIn(User user, List<TokenStatus> statuses);
    
    List<Token> findByQueueAndStatusOrderByQueuePositionAsc(ServiceQueue queue, TokenStatus status);
    
    long countByQueueAndStatus(ServiceQueue queue, TokenStatus status);
    
    long countByQueueAndStatusIn(ServiceQueue queue, List<TokenStatus> statuses);
    
    List<Token> findByStatusAndHoldExpiryTimeBefore(TokenStatus status, LocalDateTime time);
    
    List<Token> findByQueueAndUserAndStatusIn(ServiceQueue queue, User user, List<TokenStatus> statuses);
    
    @Query("SELECT COUNT(t) FROM Token t WHERE t.queue = :queue AND t.status = :status AND t.queuePosition < :queuePosition")
    long countByQueueAndStatusAndQueuePositionLessThan(@Param("queue") ServiceQueue queue, @Param("status") TokenStatus status, @Param("queuePosition") int queuePosition);
    
    List<Token> findByQueue(ServiceQueue queue);
    
    List<Token> findByUserOrderByCreatedAtDesc(User user);
    
    List<Token> findByUserIdAndStatusIn(Long userId, List<TokenStatus> statuses);
    
    List<Token> findByUserIdOrderByCreatedAtDesc(Long userId);
}
