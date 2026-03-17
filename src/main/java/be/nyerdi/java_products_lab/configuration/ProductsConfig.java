package be.nyerdi.java_products_lab.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ProductsConfig {
    @Bean
    public OpenAPI productCatalogOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Management API")
                        .description("API REST pour la gestion d'un catalogue de produits avec Spring Boot 3 et PostgreSQL.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("nyerdi"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}
