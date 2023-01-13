package com.cleevio.vexl.common;

import com.cleevio.vexl.module.cryptocurrency.service.CryptoCurrencyServiceIntegrationTestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ContextConfiguration(
		initializers = {IntegrationTestConfig.Initializer.class},
		classes = CryptoCurrencyServiceIntegrationTestConfiguration.class
)
@ActiveProfiles("test")
public @interface IntegrationTest {
}
