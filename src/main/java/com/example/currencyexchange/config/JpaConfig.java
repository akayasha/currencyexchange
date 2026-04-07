package com.example.currencyexchange.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA configuration to enable repositories and transaction management.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.example.currencyexchange.repository")
@EnableTransactionManagement
public class JpaConfig {
}
