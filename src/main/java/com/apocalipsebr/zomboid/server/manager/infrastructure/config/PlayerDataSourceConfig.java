package com.apocalipsebr.zomboid.server.manager.infrastructure.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

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


@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.apocalipsebr.zomboid.server.manager.domain.repository.player",
    entityManagerFactoryRef = "playerEntityManagerFactory",
    transactionManagerRef = "playerTransactionManager"
)
public class PlayerDataSourceConfig {
    @Value("${player.datasource.url}")
    private String url;

    @Value("${player.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${player.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    @Bean(name = "playerDataSource")
    public DataSource playerDataSource() {
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
        dataSource.setUrl(url + "?open_mode=1");

        return dataSource;
    }

    @Bean(name = "playerEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean playerEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("playerDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", ddlAuto); // Create/update tables automatically
        properties.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");

        return builder
                .dataSource(dataSource)
                .packages("com.apocalipsebr.zomboid.server.manager.domain.entity.player")
                .persistenceUnit("player")
                .properties(properties)
                .build();
    }

    @Bean(name = "playerTransactionManager")
    public PlatformTransactionManager playerTransactionManager(
            @Qualifier("playerEntityManagerFactory") LocalContainerEntityManagerFactoryBean playerEntityManagerFactory) {
        return new JpaTransactionManager(playerEntityManagerFactory.getObject());
    }
}
