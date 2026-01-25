package com.querymate.QueryMate.dto;

import java.util.List;

public class QueryIntent {

    private String operation;           // SELECT, INSERT, UPDATE, DELETE
    private String table;               // Single table name
    private List<String> columns;       // Columns to select
    private String whereCondition;      // WHERE clause (optional)
    private String orderBy;             // ORDER BY column (optional)
    private Integer limit;              // LIMIT value (optional)
    private boolean distinct;           // Whether to use DISTINCT
    private String aggregateFunction;   // COUNT, SUM, AVG, etc. (optional)
    private boolean possible;           // Is this query possible?
    private String reason;              // Reason if not possible

    // Getters and Setters
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public String getWhereCondition() {
        return whereCondition;
    }

    public void setWhereCondition(String whereCondition) {
        this.whereCondition = whereCondition;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public String getAggregateFunction() {
        return aggregateFunction;
    }

    public void setAggregateFunction(String aggregateFunction) {
        this.aggregateFunction = aggregateFunction;
    }

    public boolean isPossible() {
        return possible;
    }

    public void setPossible(boolean possible) {
        this.possible = possible;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
