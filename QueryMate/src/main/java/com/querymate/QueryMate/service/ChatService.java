package com.querymate.QueryMate.service;

import com.querymate.QueryMate.dto.ChatMessageDto;

import java.util.List;

public interface ChatService {

    ChatMessageDto saveMessage(Long projectId, String sender, String role, String content);

    List<ChatMessageDto> getMessagesForProject(Long projectId);

    List<ChatMessageDto> handleUserQuery(Long projectId, String userInput);
}
