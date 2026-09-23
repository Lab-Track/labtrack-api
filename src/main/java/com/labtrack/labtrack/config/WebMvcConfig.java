package com.labtrack.labtrack.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String uploadDir;
    private final String uploadUrlPrefix;

    public WebMvcConfig(
            @Value("${app.upload-dir}") String uploadDir,
            @Value("${app.upload-url-prefix}") String uploadUrlPrefix) {
        this.uploadDir = uploadDir;
        this.uploadUrlPrefix = uploadUrlPrefix;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(uploadUrlPrefix + "/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
