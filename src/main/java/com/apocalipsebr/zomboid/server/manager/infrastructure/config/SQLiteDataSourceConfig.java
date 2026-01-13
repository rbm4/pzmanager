// package com.apocalipsebr.zomboid.server.manager.infrastructure.config;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.jdbc.datasource.DriverManagerDataSource;
// import org.sqlite.SQLiteConfig;

// import javax.sql.DataSource;
// import java.util.Properties;

// @Configuration
// public class SQLiteDataSourceConfig {

//     @Value("${zomboid.database.path}")
//     private String databasePath;

//     @Bean
//     public DataSource dataSource() {
//         SQLiteConfig config = new SQLiteConfig();
        
//         // Set read-only mode
//         config.setReadOnly(true);
        
//         // Set busy timeout (30 seconds)
//         config.setBusyTimeout(30000);
        
//         // Enable WAL mode for better concurrency
//         config.setJournalMode(SQLiteConfig.JournalMode.WAL);
        
//         // Set locking mode
//         config.setLockingMode(SQLiteConfig.LockingMode.NORMAL);
        
//         // Set synchronous mode
//         config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
        
//         // Create datasource with SQLite configuration
//         DriverManagerDataSource dataSource = new DriverManagerDataSource();
//         dataSource.setDriverClassName("org.sqlite.JDBC");
//         dataSource.setUrl("jdbc:sqlite:" + databasePath);
        
//         // Apply SQLite config properties
//         Properties properties = config.toProperties();
//         dataSource.setConnectionProperties(properties);
        
//         return dataSource;
//     }
// }