package com.cleevio.vexl.common.integration.swagger.config;

import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    @Value("${springdoc.swagger-server}")
    private final String server;

    @Bean
    public OpenAPI openApiConfiguration() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Vexl offer-microservice")
                                .version("v0.0.1")
                                .description("Vexl offer-ms API Documentation")
                                .contact(
                                        new Contact()
                                                .email("david.tilser@cleevio.com")
                                                .name("David Tilšer")
                                )
                )
                .addServersItem(
                        new Server()
                                .url(server)
                                .description("Server")
                )
                .components(
                        new Components()
                                .addSecuritySchemes(SecurityFilter.HEADER_HASH, new SecurityScheme().type(SecurityScheme.Type.APIKEY).
                                        description("SHA-256 hash of phone number").in(SecurityScheme.In.HEADER).name(SecurityFilter.HEADER_HASH))
                                .addSecuritySchemes(SecurityFilter.HEADER_PUBLIC_KEY, new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                                        .description("public key of user").in(SecurityScheme.In.HEADER).name(SecurityFilter.HEADER_PUBLIC_KEY))
                                .addSecuritySchemes(SecurityFilter.HEADER_SIGNATURE, new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                                        .description("signature").in(SecurityScheme.In.HEADER).name(SecurityFilter.HEADER_SIGNATURE))
                );
    }

    @Bean
    public GroupedOpenApi v1Api() {
        return GroupedOpenApi.builder().group("v1").pathsToMatch("/api/v1/**", "/settings/**", "/ws").build();
    }

    @Bean
    public GroupedOpenApi v2Api() {
        return GroupedOpenApi.builder().group("v2").pathsToMatch("/api/v2/**", "/settings/**", "/ws").build();
    }

    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(new Parameter().in("header").required(true).example("Td7wKTAOSa8YfGD2FC6rD8tZ7RtVjamqlfZ3qrC6ucs=").name(SecurityFilter.HEADER_HASH));
            operation.addParametersItem(new Parameter().in("header").required(true).example("Zjk1MmY1OTJnNTlzZDJnNTlzZGE0NTZzZA==").name(SecurityFilter.HEADER_PUBLIC_KEY));
            operation.addParametersItem(new Parameter().in("header").required(true).example("31U5s5v+oM1yP0twRRfTzNEN9oW98HIB3mSheXKXGlavZWpROUxl0VXxhfYal1oLLGxnR7Be8AYT34UV9EICw==").name(SecurityFilter.HEADER_SIGNATURE));
            return operation;
        };

    }

    /**
     * Map custom mapper to support snake/camel case
     *
     * @param objectMapper Project object mapper
     * @return Model resolver
     */
    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper) {
        return new ModelResolver(objectMapper);
    }


}
