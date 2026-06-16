package br.com.urnaeletronica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Permitir credenciais (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Permitir requisições de qualquer origem (ajuste conforme necessário)
        config.addAllowedOriginPattern("*");

        // Permitir todos os headers
        config.addAllowedHeader("*");

        // Permitir todos os métodos HTTP
        config.addAllowedMethod("*");

        // Headers expostos na resposta
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}