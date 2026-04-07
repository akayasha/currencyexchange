package com.example.currencyexchange.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger configuration.
 * Accessible at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI currencyExchangeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Currency Exchange API")
                        .description("""
                                REST API for currency exchange operations.

                                **Features:**
                                - Full CRUD for currencies (GET, POST, PUT, PATCH, DELETE)
                                - Full CRUD for exchange rates
                                - Paginated search across currencies and exchange rates
                                - Currency conversion using stored rates or live external API fallback
                                - JOIN query endpoint returning enriched exchange rate data
                                - All requests/responses logged via AspectJ

                                **H2 Console:** http://localhost:8080/h2-console
                                (JDBC URL: `jdbc:h2:mem:currencyexchangedb`, Username: `sa`, Password: empty)
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Currency Exchange Team")
                                .email("support@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ));
    }
}
