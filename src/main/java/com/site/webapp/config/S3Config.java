package com.site.webapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
/**
 * Конфигурация клиента S3 (MinIO/AWS).
 * Создает бин S3Client с настройками из application.properties:
 * - endpoint URL
 * - access key / secret key
 * - region
 * - path style access для MinIO
 */
@Configuration
public class S3Config {
    @Value("${aws.s3.endpoint}")
    private String endpoint;

    @Value("{aws.s3.access-key}")
    private String accessKey;

    @Value("{aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.region}")
    private String region;
    @Bean
    public S3Client s3Client(){
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(region))
                .forcePathStyle(true)
                .build();
    }
}
