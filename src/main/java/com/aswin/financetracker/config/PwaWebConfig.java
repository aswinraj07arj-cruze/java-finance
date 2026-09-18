package com.aswin.financetracker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class PwaWebConfig {

    /**
     * Registers a filter that ensures appropriate headers for PWA assets:
     * - Service-Worker-Allowed: / on /sw.js
     * - Cache-Control: no-cache on /sw.js so service worker updates are immediately detected
     * - Content-Type: application/manifest+json on /manifest.json
     */
    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> pwaHeaderFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registrationBean = new FilterRegistrationBean<>();
        
        OncePerRequestFilter filter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                String uri = request.getRequestURI();
                if ("/sw.js".equals(uri)) {
                    response.setHeader("Service-Worker-Allowed", "/");
                    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    response.setHeader("Pragma", "no-cache");
                    response.setDateHeader("Expires", 0);
                } else if ("/manifest.json".equals(uri)) {
                    response.setContentType("application/manifest+json");
                }
                filterChain.doFilter(request, response);
            }
        };

        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/sw.js", "/manifest.json");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
