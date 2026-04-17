package com.jcen.medpal.manager;

import com.jcen.medpal.common.ErrorCode;
import com.jcen.medpal.exception.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.RemoveObjectArgs;
import io.minio.messages.Item;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO 文件存储管理器
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Component
@Slf4j
public class FileStorageManager {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    @Value("${file.minio.endpoint:http://minio:9000}")
    private String endpoint;

    @Value("${file.minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${file.minio.secret-key:minioadmin}")
    private String secretKey;

    @Value("${file.minio.bucket-name:medpal-project}")
    private String bucketName;

    private MinioClient minioClient;

    @Data
    public static class MinioObjectItem {
        private String name;
        private String objectPath;
        private boolean directory;
        private long size;
        private String lastModified;
    }

    @PostConstruct
    public void init() {
        List<String> endpointCandidates = buildEndpointCandidates(endpoint);
        Exception lastException = null;
        for (String endpointCandidate : endpointCandidates) {
            try {
                MinioClient candidateClient = MinioClient.builder()
                        .endpoint(endpointCandidate)
                        .credentials(accessKey, secretKey)
                        .build();
                minioClient = candidateClient;
                ensureBucketExists();
                if (!endpointCandidate.equals(endpoint)) {
                    log.warn("MinIO endpoint switched from {} to {}", endpoint, endpointCandidate);
                }
                log.info("MinIO initialized, endpoint={}, bucket={}", endpointCandidate, bucketName);
                return;
            } catch (Exception e) {
                lastException = e;
                log.warn("MinIO initialization failed for endpoint={}: {}", endpointCandidate, e.getMessage());
            }
        }
        log.error("MinIO initialization failed after trying endpoints: {}", endpointCandidates, lastException);
        throw new IllegalStateException("MinIO 初始化失败，请确认使用的是 MinIO S3 API 端口（不是 Console 端口）", lastException);
    }

    private List<String> buildEndpointCandidates(String rawEndpoint) {
        Set<String> candidates = new LinkedHashSet<>();
        if (StringUtils.isNotBlank(rawEndpoint)) {
            candidates.add(rawEndpoint.trim());
        }
        try {
            URI uri = URI.create(rawEndpoint.trim());
            int port = uri.getPort();
            if (port == 9091 || port == 9001) {
                String apiPortEndpoint = replacePort(uri, 9000);
                if (StringUtils.isNotBlank(apiPortEndpoint)) {
                    candidates.add(apiPortEndpoint);
                }
            }
        } catch (Exception ignored) {
            // 无法解析端点时，仅使用原始配置
        }
        return new ArrayList<>(candidates);
    }

    private String replacePort(URI uri, int port) {
        try {
            URI replaced = new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    port,
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment());
            return replaced.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 列出指定目录下的文件和子目录（非递归）
     *
     * @param prefix 目录前缀，例如 "docs/" 或 "docs/images/"
     * @return 当前目录对象列表
     */
    public List<MinioObjectItem> listObjectsByPrefix(String prefix) {
        String normalizedPrefix = normalizeDirectoryPrefix(prefix);
        List<MinioObjectItem> result = new ArrayList<>();
        try {
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(normalizedPrefix)
                            .delimiter("/")
                            .build());
            for (Result<Item> objectResult : objects) {
                Item item = objectResult.get();
                String objectName = item.objectName();
                if (StringUtils.isBlank(objectName) || objectName.equals(normalizedPrefix)) {
                    continue;
                }
                boolean isDirectory = item.isDir() || objectName.endsWith("/");
                String displayName = objectName;
                if (StringUtils.isNotBlank(normalizedPrefix) && objectName.startsWith(normalizedPrefix)) {
                    displayName = objectName.substring(normalizedPrefix.length());
                }
                if (isDirectory && displayName.endsWith("/")) {
                    displayName = displayName.substring(0, displayName.length() - 1);
                }
                if (StringUtils.isBlank(displayName)) {
                    continue;
                }
                MinioObjectItem objectItem = new MinioObjectItem();
                objectItem.setName(displayName);
                objectItem.setObjectPath(objectName);
                objectItem.setDirectory(isDirectory);
                objectItem.setSize(isDirectory ? 0L : item.size());
                String lastModified = null;
                if (!isDirectory) {
                    try {
                        lastModified = item.lastModified() == null ? null : item.lastModified().toString();
                    } catch (Exception ignored) {
                        // 某些 MinIO 返回的目录对象缺少 lastModified，忽略即可
                    }
                }
                objectItem.setLastModified(lastModified);
                result.add(objectItem);
            }
        } catch (Exception e) {
            log.error("Failed to list objects for prefix={}", normalizedPrefix, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件列表获取失败");
        }
        result.sort((a, b) -> {
            if (a.isDirectory() != b.isDirectory()) {
                return a.isDirectory() ? -1 : 1;
            }
            return Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER).compare(a.getName(), b.getName());
        });
        return result;
    }

    /**
     * 在指定目录下创建子目录
     *
     * @param prefix 目录前缀
     * @param folderName 目录名
     * @return 创建后的目录对象路径（以 / 结尾）
     */
    public String createFolder(String prefix, String folderName) {
        String safeFolderName = sanitizeFolderName(folderName);
        String normalizedPrefix = normalizeDirectoryPrefix(prefix);
        String folderObjectPath = normalizedPrefix + safeFolderName + "/";
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0])) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(folderObjectPath)
                            .stream(inputStream, 0, -1)
                            .contentType("application/x-directory")
                            .build());
            return folderObjectPath;
        } catch (Exception e) {
            log.error("Failed to create folder in MinIO: {}", folderObjectPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建文件夹失败");
        }
    }

    /**
     * 上传文件到指定目录
     *
     * @param file 文件
     * @param prefix 目录前缀
     * @param filename 文件名
     * @return 对象路径
     */
    public String saveFileToPrefix(MultipartFile file, String prefix, String filename) {
        String safeFileName = sanitizeFileName(filename);
        String normalizedPrefix = normalizeDirectoryPrefix(prefix);
        String objectName = normalizedPrefix + safeFileName;
        String contentType = StringUtils.isBlank(file.getContentType()) ? DEFAULT_CONTENT_TYPE : file.getContentType();
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build());
            return objectName;
        } catch (Exception e) {
            log.error("Failed to save file to prefix, object={}", objectName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }
    }

    /**
     * 删除对象（文件或空目录对象）
     *
     * @param objectPath 对象路径
     * @return 是否成功
     */
    public boolean deleteObject(String objectPath) {
        String normalizedObjectPath = normalizePath(objectPath);
        if (StringUtils.isBlank(normalizedObjectPath)) {
            return false;
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(normalizedObjectPath)
                            .build());
            return true;
        } catch (Exception e) {
            log.error("Failed to delete object from MinIO: {}", normalizedObjectPath, e);
            return false;
        }
    }

    /**
     * 删除目录（递归删除目录下全部对象）
     *
     * @param prefix 目录前缀
     * @return 是否成功
     */
    public boolean deleteDirectory(String prefix) {
        String normalizedPrefix = normalizeDirectoryPrefix(prefix);
        try {
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(normalizedPrefix)
                            .recursive(true)
                            .build());
            for (Result<Item> objectResult : objects) {
                Item item = objectResult.get();
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(item.objectName())
                                .build());
            }
            // 删除目录标记对象（如果存在）
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(normalizedPrefix)
                            .build());
            return true;
        } catch (Exception e) {
            log.error("Failed to delete directory from MinIO: {}", normalizedPrefix, e);
            return false;
        }
    }

    /**
     * 保存文件（原文件名）
     *
     * @param file 文件
     * @param relativePath 相对路径
     * @return 文件访问路径
     */
    public String saveFile(MultipartFile file, String relativePath) {
        return saveFile(file, relativePath, file.getOriginalFilename());
    }

    /**
     * 保存文件（指定文件名）
     *
     * @param file 文件
     * @param relativePath 相对路径
     * @param filename 文件名
     * @return 文件访问路径
     */
    public String saveFile(MultipartFile file, String relativePath, String filename) {
        try (InputStream inputStream = file.getInputStream()) {
            return saveInputStream(inputStream, file.getSize(), relativePath, filename, file.getContentType());
        } catch (Exception e) {
            log.error("Failed to save file to MinIO", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件保存失败");
        }
    }

    /**
     * 保存输入流到 MinIO
     *
     * @param inputStream 输入流
     * @param size 文件大小
     * @param relativePath 相对路径（biz/userId）
     * @param filename 文件名
     * @param contentType 内容类型
     * @return 统一预览路径
     */
    public String saveInputStream(InputStream inputStream, long size, String relativePath, String filename, String contentType) {
        String safeFileName = sanitizeFileName(filename);
        String objectName = buildObjectName(relativePath, safeFileName);
        String finalContentType = StringUtils.isBlank(contentType) ? DEFAULT_CONTENT_TYPE : contentType;
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(finalContentType)
                            .build());
            log.info("File saved to MinIO: bucket={}, object={}", bucketName, objectName);
            return buildPreviewUrl(objectName);
        } catch (Exception e) {
            log.error("Failed to put object to MinIO: {}", objectName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件保存失败");
        }
    }

    /**
     * 删除文件
     *
     * @param relativePath 相对路径（biz/userId/filename）
     * @return 是否删除成功
     */
    public boolean deleteFile(String relativePath) {
        return deleteObject(relativePath);
    }

    /**
     * 获取文件输入流
     *
     * @param relativePath 相对路径（biz/userId/filename）
     * @return 文件输入流
     */
    public InputStream getFileStream(String relativePath) {
        String objectName = normalizePath(relativePath);
        try {
            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            return response;
        } catch (Exception e) {
            log.error("Failed to get object from MinIO: {}", objectName, e);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build());
        if (!bucketExists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Created MinIO bucket: {}", bucketName);
        }
    }

    private String buildObjectName(String relativePath, String filename) {
        String normalizedPath = normalizePath(relativePath);
        if (StringUtils.isBlank(normalizedPath)) {
            return filename;
        }
        return normalizedPath + "/" + filename;
    }

    private String normalizeDirectoryPrefix(String prefix) {
        String normalized = normalizePath(prefix);
        if (StringUtils.isBlank(normalized)) {
            return "";
        }
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }

    private String normalizePath(String path) {
        if (path == null) {
            return "";
        }
        String normalized = path.trim().replace("\\", "/");
        validatePath(normalized);
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.startsWith("files/")) {
            normalized = normalized.substring("files/".length());
        }
        return normalized;
    }

    private void validatePath(String path) {
        if (StringUtils.isBlank(path)) {
            return;
        }
        if (path.contains("..")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件路径非法");
        }
    }

    private String sanitizeFileName(String filename) {
        if (StringUtils.isBlank(filename)) {
            return "file";
        }
        return filename.replace("\\", "_").replace("/", "_");
    }

    private String sanitizeFolderName(String folderName) {
        if (StringUtils.isBlank(folderName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件夹名称不能为空");
        }
        String safeName = folderName.trim().replace("\\", "/");
        while (safeName.startsWith("/")) {
            safeName = safeName.substring(1);
        }
        while (safeName.endsWith("/")) {
            safeName = safeName.substring(0, safeName.length() - 1);
        }
        if (StringUtils.isBlank(safeName) || safeName.contains("/") || safeName.contains("..")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件夹名称非法");
        }
        return safeName;
    }

    private String buildPreviewUrl(String objectName) {
        String[] segments = objectName.split("/", 3);
        if (segments.length < 3) {
            return "/api/file/preview/" + objectName;
        }
        return "/api/file/preview/" + segments[0] + "/" + segments[1] + "/" + segments[2];
    }
}
