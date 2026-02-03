package com.apocalipsebr.zomboid.server.manager.infrastructure.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.sql.Connection;
import java.sql.Statement;

@Configuration
public class FlywayMigrationRunner {

    @Bean
    public CommandLineRunner runDatabaseMigrations(@Qualifier("appDataSource") DataSource dataSource) {
        return args -> {
            try (Connection conn = dataSource.getConnection()) {
                try (Statement stmt = conn.createStatement()) {
                    // Add vehicle_script column if it doesn't exist
                    try {
                        stmt.execute("ALTER TABLE cars ADD COLUMN vehicle_script TEXT NOT NULL DEFAULT 'Base.CarNormal'");
                        System.out.println("Added vehicle_script column to cars table");
                    } catch (Exception e) {
                        if (e.getMessage().contains("duplicate column name")) {
                            System.out.println("vehicle_script column already exists");
                        } else {
                            System.err.println("Error adding vehicle_script: " + e.getMessage());
                        }
                    }
                    
                    // Add trunk_size column if it doesn't exist
                    try {
                        stmt.execute("ALTER TABLE cars ADD COLUMN trunk_size INTEGER");
                        System.out.println("Added trunk_size column to cars table");
                    } catch (Exception e) {
                        if (e.getMessage().contains("duplicate column name")) {
                            System.out.println("trunk_size column already exists");
                        } else {
                            System.err.println("Error adding trunk_size: " + e.getMessage());
                        }
                    }
                    
                    // Add seats column if it doesn't exist
                    try {
                        stmt.execute("ALTER TABLE cars ADD COLUMN seats INTEGER");
                        System.out.println("Added seats column to cars table");
                    } catch (Exception e) {
                        if (e.getMessage().contains("duplicate column name")) {
                            System.out.println("seats column already exists");
                        } else {
                            System.err.println("Error adding seats: " + e.getMessage());
                        }
                    }
                    
                    // Add doors column if it doesn't exist
                    try {
                        stmt.execute("ALTER TABLE cars ADD COLUMN doors INTEGER");
                        System.out.println("Added doors column to cars table");
                    } catch (Exception e) {
                        if (e.getMessage().contains("duplicate column name")) {
                            System.out.println("doors column already exists");
                        } else {
                            System.err.println("Error adding doors: " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error during database migration: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
