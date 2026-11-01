package io.github.alphajiang.hyena.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Lets browser clients on other origins (such as the web build of wallet-app)
 * call the /hyena/** API.
 *
 * Origins come from the comma separated property {@code hyena.cors.allowed-origins}.
 * Leave it empty to disable cross-origin access. Never use "*" for a server that
 * holds real balances: the Hyena API has no authentication of its own.
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Value("${hyena.cors.allowed-origins:}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if (origins.length == 0) {
            return;
        }
        registry.addMapping("/hyena/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("Content-Type")
                .maxAge(3600);
    }
}
