package com.jcen.medpal.utils;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO工具类
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Slf4j
@Component
public class MinioUtils {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private com.jcen.medpal.config.MinioConfig minioConfig;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());

    @PostConstruct
    public void init() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .build()
                );
                log.info("MinIO bucket创建成功: {}", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("MinIO bucket初始化失败", e);
        }
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @param folder 文件夹名称（如: user, dish, merchant）
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        return uploadFile(file, folder, null);
    }

    /**
     * 上传文件（带用户ID）
     *
     * @param file 文件
     * @param folder 文件夹名称（如: user, dish, merchant）
     * @param userId 用户ID（可选）
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String folder, Long userId) {
        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uuid = UUID.randomUUID().toString();
            
            String objectName;
            if (userId != null) {
                // 兼容现有格式: folder/userId/filename
                objectName = folder + "/" + userId + "/" + uuid + suffix;
            } else {
                // 新格式: folder/date/uuid.ext
                objectName = folder + "/" + DATE_FORMATTER.format(new Date().toInstant()) + "/" + uuid + suffix;
            }

            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件访问URL
     *
     * @param objectName 对象名称
     * @return 文件访问URL
     */
    public String getFileUrl(String objectName) {
        try {
            // objectName格式: folder/userId/filename 或 folder/date/filename
            // 转换为预览接口格式: /api/file/preview/folder/userId/filename
            return "/api/file/preview/" + objectName;
        } catch (Exception e) {
            log.error("获取文件URL失败", e);
            throw new RuntimeException("获取文件URL失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从URL中提取objectName
     *
     * @param url 文件URL
     * @return 对象名称
     */
    public String extractObjectName(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        int bucketIndex = url.indexOf(minioConfig.getBucketName());
        if (bucketIndex == -1) {
            return null;
        }
        return url.substring(bucketIndex + minioConfig.getBucketName().length() + 1);
    }
}