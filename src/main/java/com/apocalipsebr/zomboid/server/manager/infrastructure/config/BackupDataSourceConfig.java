package com.apocalipsebr.zomboid.server.manager.infrastructure.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@ConditionalOnProperty(name = "backup.datasource.enabled", havingValue = "true", matchIfMissing = false)
@EnableJpaRepositories(
    basePackages = "com.apocalipsebr.zomboid.server.manager.domain.repository.backup",
    entityManagerFactoryRef = "backupEntityManagerFactory",
    transactionManagerRef = "backupTransactionManager"
)
public class BackupDataSourceConfig {

    @Value("${backup.datasource.url}")
    private String url;

    @Value("${backup.datasource.driver-class-name}")
    private String driverClassName;

    @Bean(name = "backupDataSource")
    public DataSource backupDataSource() {
        String dbPath = url.replace("jdbc:sqlite:", "");
        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            throw new IllegalStateException(
                "Backup database not found at: " + dbPath +
                ". Make sure the backup database file exists."
            );
        }

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url + "?open_mode=1"); // Read-only mode
        return dataSource;
    }

    @Bean(name = "backupEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean backupEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("backupDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        properties.put("hibernate.show_sql", false);

        return builder
                .dataSource(dataSource)
                .packages("com.apocalipsebr.zomboid.server.manager.domain.entity.backup")
                .persistenceUnit("backup")
                .properties(properties)
                .build();
    }

    @Bean(name = "backupTransactionManager")
    public PlatformTransactionManager backupTransactionManager(
            @Qualifier("backupEntityManagerFactory") LocalContainerEntityManagerFactoryBean backupEntityManagerFactory) {
        return new JpaTransactionManager(backupEntityManagerFactory.getObject());
    }
}
