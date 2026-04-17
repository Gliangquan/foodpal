package com.jcen.medpal.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO配置类
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file.minio")
public class MinioConfig {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}