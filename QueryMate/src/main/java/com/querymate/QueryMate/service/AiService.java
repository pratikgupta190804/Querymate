package com.querymate.QueryMate.service;

public interface AiService {
    String generateSQL(String userMessage, String schemaText, String dbType);
}
