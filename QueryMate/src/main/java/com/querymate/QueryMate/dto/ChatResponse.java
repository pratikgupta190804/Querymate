package com.querymate.QueryMate.dto;

import java.util.List;

public class ChatResponse {
    
    private String userQuery;
    private String generatedSQL;
    private String validatedSQL;
    private boolean sqlExecuted;
    private Object queryResults;
    private List<String> errors;
    private String aiExplanation;
    private Double confidenceScore;
    private String timestamp;
    
    public ChatResponse() {}

    public String getUserQuery() {
        return userQuery;
    }

    public void setUserQuery(String userQuery) {
        this.userQuery = userQuery;
    }

    public String getGeneratedSQL() {
        return generatedSQL;
    }

    public void setGeneratedSQL(String generatedSQL) {
        this.generatedSQL = generatedSQL;
    }

    public String getValidatedSQL() {
        return validatedSQL;
    }

    public void setValidatedSQL(String validatedSQL) {
        this.validatedSQL = validatedSQL;
    }

    public boolean isSqlExecuted() {
        return sqlExecuted;
    }

    public void setSqlExecuted(boolean sqlExecuted) {
        this.sqlExecuted = sqlExecuted;
    }

    public Object getQueryResults() {
        return queryResults;
    }

    public void setQueryResults(Object queryResults) {
        this.queryResults = queryResults;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getAiExplanation() {
        return aiExplanation;
    }

    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
