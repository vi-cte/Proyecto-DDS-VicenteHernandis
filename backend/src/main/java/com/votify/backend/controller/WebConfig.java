package com.votify.backend.controller;

import com.votify.backend.security.SecurityInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
// Configura interceptores y CORS para la API del backend.
public class WebConfig implements WebMvcConfigurer {
    private final @NonNull SecurityInterceptor securityInterceptor;

    // Inyecta el interceptor que aplicará las reglas de acceso.
    public WebConfig(@NonNull SecurityInterceptor securityInterceptor) {
        this.securityInterceptor = securityInterceptor;
    }

    @Override
    // Registra el interceptor para todas las rutas de la API.
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(securityInterceptor).addPathPatterns("/api/**");
    }

    @Override
    // Permite peticiones CORS hacia la API desde cualquier origen.
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
