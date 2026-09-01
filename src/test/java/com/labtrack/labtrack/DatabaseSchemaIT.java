package com.labtrack.labtrack;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DatabaseSchemaIT {

    private static final List<String> EXPECTED_TABLES = List.of(
            "professor", "student", "technician", "project", "equipment",
            "status_history", "loan", "loan_item",
            "loan_return", "notification");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void allTenDomainTablesExistAfterMigration() {
        List<String> tableNames = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'",
                String.class);

        for (String expected : EXPECTED_TABLES) {
            assertTrue(tableNames.contains(expected), "Missing table: " + expected);
        }
    }
}
