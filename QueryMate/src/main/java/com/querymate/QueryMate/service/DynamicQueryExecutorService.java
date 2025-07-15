package com.querymate.QueryMate.service;

import com.querymate.QueryMate.entity.Project;
import com.querymate.QueryMate.utils.CryptoUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Service
public class DynamicQueryExecutorService {

    private final CryptoUtils cryptoUtils;

    public DynamicQueryExecutorService(CryptoUtils cryptoUtils) {
        this.cryptoUtils = cryptoUtils;
    }

    public List<Map<String, Object>> executeSQL(Project project, String sql) {
        if (!sql.trim().toLowerCase().startsWith("select")) {
            throw new IllegalArgumentException("Only SELECT queries are allowed.");
        }

        DataSource dataSource = createDataSource(project);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute SQL: " + e.getMessage());
        }
    }

    private DataSource createDataSource(Project project) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        String dbType = project.getDbType().toLowerCase();
        String url;

        switch (dbType) {
            case "mysql":
                dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
                url = "jdbc:mysql://" + project.getDbHost() + ":" + project.getDbPort() + "/" + project.getDbName() +
                        "?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true";
                break;

            case "postgresql":
                dataSource.setDriverClassName("org.postgresql.Driver");
                url = "jdbc:postgresql://" + project.getDbHost() + ":" + project.getDbPort() + "/" + project.getDbName();
                break;

            default:
                throw new UnsupportedOperationException("Unsupported DB type: " + dbType);
        }

        dataSource.setUrl(url);
        dataSource.setUsername(cryptoUtils.decrypt(project.getDbUsername()));
        dataSource.setPassword(cryptoUtils.decrypt(project.getDbPassword()));

        return dataSource;
    }
}
