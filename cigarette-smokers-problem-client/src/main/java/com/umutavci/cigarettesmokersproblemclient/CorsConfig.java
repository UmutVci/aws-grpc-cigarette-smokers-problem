package com.umutavci.cigarettesmokersproblemclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowCredentials(true);
        cfg.addAllowedOrigin("http://localhost:8082");
        cfg.addAllowedOrigin("http://thws-smokers-problem.eu-central-1.elasticbeanstalk.com/");
        cfg.addAllowedHeader("*");
        cfg.addAllowedMethod("*");
        cfg.addExposedHeader("Authorization"); // If needed

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
