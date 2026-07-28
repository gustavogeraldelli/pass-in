package dev.gustavo.passin.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "pass.in API",
                version = "v1",
                description = "Event registration and check-in API"
        )
)
public class OpenApiConfig {
}
