package com.apocalipsebr.zomboid.server.manager.infrastructure.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
@EnableJpaRepositories(
    basePackages = "com.apocalipsebr.zomboid.server.manager.domain.repository.zomboid",
    entityManagerFactoryRef = "zomboidEntityManagerFactory",
    transactionManagerRef = "zomboidTransactionManager"
)
public class ZomboidDataSourceConfig {

    @Value("${zomboid.datasource.url}")
    private String url;

    @Value("${zomboid.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${zomboid.datasource.read-only}")
    private boolean readOnly;

    @Primary
    @Bean(name = "zomboidDataSource")
    public DataSource zomboidDataSource() {
        // Validate database file exists
        String dbPath = url.replace("jdbc:sqlite:", "");
        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            throw new IllegalStateException(
                "Zomboid database not found at: " + dbPath + 
                ". Make sure the Zomboid server has created the database file."
            );
        }

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url + "?open_mode=1"); // Read-only mode
        
        return dataSource;
    }

    @Primary
    @Bean(name = "zomboidEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean zomboidEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("zomboidDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none"); // Never modify Zomboid DB
        properties.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        properties.put("hibernate.show_sql", true);
        
        return builder
                .dataSource(dataSource)
                .packages("com.apocalipsebr.zomboid.server.manager.domain.entity.zomboid")
                .persistenceUnit("zomboid")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "zomboidTransactionManager")
    public PlatformTransactionManager zomboidTransactionManager(
            @Qualifier("zomboidEntityManagerFactory") LocalContainerEntityManagerFactoryBean zomboidEntityManagerFactory) {
        return new JpaTransactionManager(zomboidEntityManagerFactory.getObject());
    }
}
