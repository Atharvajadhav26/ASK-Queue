package com.smartqueue.repository;

import com.smartqueue.entity.Token;
import com.smartqueue.entity.TokenHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenHistoryRepository extends JpaRepository<TokenHistory, Long> {
    List<TokenHistory> findByTokenOrderByCreatedAtDesc(Token token);
    
    List<TokenHistory> findByTokenIdOrderByCreatedAtDesc(Long tokenId);
    
    Optional<TokenHistory> findTopByToken_Queue_IdAndActionOrderByCreatedAtDesc(Long queueId, String action);

    void deleteByToken(Token token);

    void deleteByTokenIn(List<Token> tokens);
}
