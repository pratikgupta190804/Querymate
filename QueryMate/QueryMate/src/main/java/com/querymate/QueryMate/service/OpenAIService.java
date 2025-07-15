package com.querymate.QueryMate.service;

public interface OpenAIService {
    String generateSQL(String userMessage, String schemaText, String dbType);
}
