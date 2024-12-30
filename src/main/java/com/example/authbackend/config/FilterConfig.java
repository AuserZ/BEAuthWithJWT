package com.example.authbackend.config;

import com.example.authbackend.service.auth.JwtRequestFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

public class FilterConfig {
    @Bean
    public FilterRegistrationBean<JwtRequestFilter> jwtRequestFilter(JwtRequestFilter jwtRequestFilter) {
        FilterRegistrationBean<JwtRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtRequestFilter);
        registrationBean.addUrlPatterns("/*"); // Adjust the URL patterns as needed
        return registrationBean;
    }
}
