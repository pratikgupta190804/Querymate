package com.querymate.QueryMate.dto;

import java.util.List;

public class SQLValidationResponse {
    
    private String validatedSQL;
    private boolean isValid;
    private double confidenceScore;
    private List<String> corrections;
    private List<String> warnings;
    private String recommendation;
    
    public SQLValidationResponse() {}

    public String getValidatedSQL() {
        return validatedSQL;
    }

    public void setValidatedSQL(String validatedSQL) {
        this.validatedSQL = validatedSQL;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public List<String> getCorrections() {
        return corrections;
    }

    public void setCorrections(List<String> corrections) {
        this.corrections = corrections;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
