package com.lopjv.qlhoctap.config;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FlywayMigrationRunner implements CommandLineRunner {

    private final DataSource dataSource;

    public FlywayMigrationRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        try (var connection = dataSource.getConnection()) {
            if (connection.getMetaData().getDatabaseProductName().toLowerCase().contains("h2")) {
                return;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể xác định loại cơ sở dữ liệu", exception);
        }

        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("2")
                .load()
                .migrate();
    }
}
