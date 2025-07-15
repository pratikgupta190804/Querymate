package com.querymate.QueryMate.repo;

import com.querymate.QueryMate.entity.ChatMessage;
import com.querymate.QueryMate.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 🔍 Get all chat messages for a project, ordered by time
    List<ChatMessage> findByProjectOrderByTimestampAsc(Project project);
}
