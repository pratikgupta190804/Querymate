package com.querymate.QueryMate.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.querymate.QueryMate.entity.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findByProject_ProjectIdOrderByTimestampAsc(Long projectId);
    
    void deleteByProject_ProjectId(Long projectId);
}
