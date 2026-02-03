package com.apocalipsebr.zomboid.server.manager.infrastructure.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.apocalipsebr.zomboid.server.manager.domain.repository.app",
    entityManagerFactoryRef = "appEntityManagerFactory",
    transactionManagerRef = "appTransactionManager"
)
public class AppDataSourceConfig {

    @Value("${app.datasource.url}")
    private String url;

    @Value("${app.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${app.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    @Bean(name = "appDataSource")
    public DataSource appDataSource() {
        // Ensure directory exists for database file
        String dbPath = url.replace("jdbc:sqlite:", "");
        File dbFile = new File(dbPath);
        File parentDir = dbFile.getParentFile();
        
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IllegalStateException("Failed to create database directory: " + parentDir);
            }
        }

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        
        return dataSource;
    }

    @Bean(name = "appEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean appEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("appDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", ddlAuto); // Create/update tables automatically
        properties.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        properties.put("hibernate.show_sql", true);
        
        return builder
                .dataSource(dataSource)
                .packages("com.apocalipsebr.zomboid.server.manager.domain.entity.app")
                .persistenceUnit("app")
                .properties(properties)
                .build();
    }

    @Bean(name = "appTransactionManager")
    public PlatformTransactionManager appTransactionManager(
            @Qualifier("appEntityManagerFactory") LocalContainerEntityManagerFactoryBean appEntityManagerFactory) {
        return new JpaTransactionManager(appEntityManagerFactory.getObject());
    }

    @Bean(name = "appFlyway")
    public Flyway appFlyway(@Qualifier("appDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
    }
}
