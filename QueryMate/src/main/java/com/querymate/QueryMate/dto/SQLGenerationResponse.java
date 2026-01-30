package com.querymate.QueryMate.dto;

import java.util.List;

public class SQLGenerationResponse {
    
    private String generatedSQL;
    private String explanation;
    private List<String> tablesUsed;
    private String reasoning;
    
    public SQLGenerationResponse() {}

    public String getGeneratedSQL() {
        return generatedSQL;
    }

    public void setGeneratedSQL(String generatedSQL) {
        this.generatedSQL = generatedSQL;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<String> getTablesUsed() {
        return tablesUsed;
    }

    public void setTablesUsed(List<String> tablesUsed) {
        this.tablesUsed = tablesUsed;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }
}
