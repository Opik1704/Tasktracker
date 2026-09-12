package com.site.webapp.config;

import com.site.webapp.interceptor.MdcLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    private final MdcLoggingInterceptor mdcLoggingInterceptor;

    public MvcConfig(MdcLoggingInterceptor mdcLoggingInterceptor) {
        this.mdcLoggingInterceptor = mdcLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mdcLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/error");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry){
        registry.addViewController("/authorization").setViewName("authorization");
    }

    @Value("${app.upload.dir}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String rootPath = System.getProperty("user.dir");
        Path path = Paths.get(rootPath, uploadPath).toAbsolutePath();

        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + path.toString() + "/");
    }
}
