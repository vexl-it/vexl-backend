package com.cleevio.vexl.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
public class IntegrationTestConfig {

	public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:13.2")
			.withDatabaseName("integration-tests-db")
			.withUsername("sa")
			.withPassword("sa");

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Bean
	public TransactionTemplate transactionTemplate() {
		return new TransactionTemplate(transactionManager);
	}

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			postgreSQLContainer.start();
			TestPropertyValues.of(
					"spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
					"spring.datasource.username=" + postgreSQLContainer.getUsername(),
					"spring.datasource.password=" + postgreSQLContainer.getPassword()
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}
}
