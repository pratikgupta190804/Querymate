package com.querymate.QueryMate.service;

import com.querymate.QueryMate.dto.QueryIntent;
import org.springframework.stereotype.Service;

@Service
public class SqlBuilderService {

    public String buildSQL(QueryIntent intent) {
        if (!intent.isPossible()) {
            return "-- Error: " + intent.getReason();
        }

        StringBuilder sql = new StringBuilder();
        
        // SELECT clause
        sql.append("SELECT ");
        
        if (intent.isDistinct()) {
            sql.append("DISTINCT ");
        }
        
        if (intent.getAggregateFunction() != null) {
            sql.append(intent.getAggregateFunction()).append("(");
            if (intent.getColumns() != null && !intent.getColumns().isEmpty()) {
                sql.append(intent.getColumns().get(0));
            } else {
                sql.append("*");
            }
            sql.append(")");
        } else {
            if (intent.getColumns() != null && !intent.getColumns().isEmpty()) {
                sql.append(String.join(", ", intent.getColumns()));
            } else {
                sql.append("*");
            }
        }
        
        // FROM clause
        sql.append(" FROM ").append(intent.getTable());
        
        // WHERE clause
        if (intent.getWhereCondition() != null && !intent.getWhereCondition().isEmpty()) {
            sql.append(" WHERE ").append(intent.getWhereCondition());
        }
        
        // ORDER BY clause
        if (intent.getOrderBy() != null && !intent.getOrderBy().isEmpty()) {
            sql.append(" ORDER BY ").append(intent.getOrderBy());
        }
        
        // LIMIT clause
        if (intent.getLimit() != null && intent.getLimit() > 0) {
            sql.append(" LIMIT ").append(intent.getLimit());
        }
        
        sql.append(";");
        
        return sql.toString();
    }
}
