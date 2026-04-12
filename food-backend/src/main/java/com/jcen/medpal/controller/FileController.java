package com.jcen.medpal.controller;

import cn.hutool.core.io.FileUtil;
import com.jcen.medpal.annotation.AuthCheck;
import com.jcen.medpal.common.BaseResponse;
import com.jcen.medpal.common.ErrorCode;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.constant.UserConstant;
import com.jcen.medpal.exception.BusinessException;
import com.jcen.medpal.manager.FileStorageManager;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.model.enums.FileUploadBizEnum;
import com.jcen.medpal.service.UserService;
import com.jcen.medpal.utils.ImageUtils;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

/**
 * 文件接口
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    private static final long ONE_M = 1024 * 1024L;
    private static final long ADMIN_FILE_MAX_SIZE = 100 * ONE_M;

    @Resource
    private UserService userService;

    @Resource
    private FileStorageManager fileStorageManager;
    
    @Resource
    private com.jcen.medpal.utils.MinioUtils minioUtils;

    /**
     * 文件上传
     * 图片文件会进行压缩和水印处理
     *
     * @param multipartFile
     * @param biz
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
            @RequestParam(value = "biz", required = false) String biz, HttpServletRequest request) {
        if (biz == null || biz.isEmpty()) {
            biz = "user_avatar";
        }
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "业务类型错误");
        }
        validFile(multipartFile, fileUploadBizEnum);
        User loginUser = userService.getLoginUser(request);
        
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String originalFilename = multipartFile.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            originalFilename = "file";
        }
        String filename = uuid + "-" + originalFilename;
        String relativePath = fileUploadBizEnum.getValue() + "/" + loginUser.getId();
        
        // 获取文件后缀
        String fileSuffix = FileUtil.getSuffix(originalFilename);
        
        String fileUrl;
        
        // 检查是否为图片文件，需要进行压缩和水印处理
        if (ImageUtils.isImage(fileSuffix)) {
            try {
                fileUrl = saveImageWithCompression(multipartFile, relativePath, filename, fileSuffix);
            } catch (Exception e) {
                log.error("图片处理失败，尝试保存原文件", e);
                // 图片处理失败时，回退到原文件保存
                fileUrl = fileStorageManager.saveFile(multipartFile, relativePath, filename);
            }
        } else {
            // 非图片文件，直接保存
            fileUrl = fileStorageManager.saveFile(multipartFile, relativePath, filename);
        }
        
        return ResultUtils.success(fileUrl);
    }

    /**
     * 管理端：列出目录下对象（非递归）
     */
    @GetMapping("/admin/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<FileStorageManager.MinioObjectItem>> listMinioObjects(
            @RequestParam(value = "prefix", required = false) String prefix) {
        validateAdminPath(prefix, true);
        List<FileStorageManager.MinioObjectItem> objects = fileStorageManager.listObjectsByPrefix(prefix);
        return ResultUtils.success(objects);
    }

    /**
     * 管理端：创建文件夹
     */
    @PostMapping("/admin/folder")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> createFolder(@RequestParam(value = "prefix", required = false) String prefix,
            @RequestParam("folderName") String folderName) {
        validateAdminPath(prefix, true);
        String folderPath = fileStorageManager.createFolder(prefix, folderName);
        return ResultUtils.success(folderPath);
    }

    /**
     * 管理端：上传文件到指定目录
     */
    @PostMapping("/admin/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> uploadMinioFile(@RequestPart("file") MultipartFile multipartFile,
            @RequestParam(value = "prefix", required = false) String prefix) {
        validateAdminPath(prefix, true);
        validateAdminUploadFile(multipartFile);
        String originalFilename = multipartFile.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            originalFilename = "file";
        }
        String objectPath = fileStorageManager.saveFileToPrefix(multipartFile, prefix, originalFilename);
        return ResultUtils.success(objectPath);
    }

    /**
     * 保存图片并进行压缩和水印处理
     *
     * @param multipartFile  上传的文件
     * @param relativePath   相对路径
     * @param filename       文件名
     * @param fileSuffix     文件后缀
     * @return 文件访问URL
     * @throws Exception 处理异常
     */
    private String saveImageWithCompression(MultipartFile multipartFile, String relativePath, 
            String filename, String fileSuffix) throws Exception {
        // 处理后的文件名
        String processedFilename = ImageUtils.getCompressedFilename(filename);
        String extension = StringUtils.isBlank(fileSuffix) ? ".tmp" : "." + fileSuffix;
        Path backupFilePath = Files.createTempFile("medpal-upload-origin-", extension);
        Path processedFilePath = Files.createTempFile("medpal-upload-processed-", extension);

        try {
            try (InputStream inputStream = multipartFile.getInputStream()) {
                Files.copy(inputStream, backupFilePath, StandardCopyOption.REPLACE_EXISTING);
            }

            try {
                ImageUtils.processImage(backupFilePath.toFile(), processedFilePath.toFile());
                log.info("图片处理成功，处理后文件: {}", processedFilename);
            } catch (Exception e) {
                log.error("图片压缩或添加水印失败，回退原图", e);
                Files.copy(backupFilePath, processedFilePath, StandardCopyOption.REPLACE_EXISTING);
            }

            String contentType = multipartFile.getContentType();
            if (StringUtils.isBlank(contentType)) {
                contentType = resolveContentType(processedFilename);
            }
            try (InputStream processedInputStream = Files.newInputStream(processedFilePath)) {
                long fileSize = Files.size(processedFilePath);
                return fileStorageManager.saveInputStream(processedInputStream, fileSize, relativePath, processedFilename, contentType);
            }
        } finally {
            Files.deleteIfExists(backupFilePath);
            Files.deleteIfExists(processedFilePath);
        }
    }

    /**
     * 文件下载
     *
     * @param biz 业务类型
     * @param userId 用户 ID
     * @param filename 文件名
     * @param response 响应
     */
    @GetMapping("/download/{biz}/{userId}/{filename}")
    public void downloadFile(@PathVariable String biz, @PathVariable Long userId,
            @PathVariable String filename, HttpServletResponse response) {
        try {
            validatePreviewBiz(biz);
            String encodedFilename = UriUtils.encode(filename, StandardCharsets.UTF_8);
            
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);
            
            try (InputStream inputStream = openCompatibleFileStream(biz, userId, filename);
                    OutputStream outputStream = response.getOutputStream()) {
                writeStreamToResponse(inputStream, outputStream);
            }
        } catch (BusinessException e) {
            log.error("Business exception in download file", e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to download file", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件下载失败");
        }
    }

    /**
     * 管理端：按对象路径下载
     */
    @GetMapping("/admin/download")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void downloadMinioObject(@RequestParam("objectPath") String objectPath, HttpServletResponse response) {
        validateAdminPath(objectPath, false);
        String filename = extractFileName(objectPath);
        String encodedFilename = UriUtils.encode(filename, StandardCharsets.UTF_8);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);
        try (InputStream inputStream = fileStorageManager.getFileStream(objectPath);
                OutputStream outputStream = response.getOutputStream()) {
            writeStreamToResponse(inputStream, outputStream);
        } catch (Exception e) {
            log.error("Failed to download minio object, path={}", objectPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件下载失败");
        }
    }

    /**
     * 文件预览（用于图片等）
     *
     * @param biz 业务类型
     * @param userId 用户 ID
     * @param filename 文件名
     * @param response 响应
     */
    @GetMapping("/preview/{biz}/{userId}/{filename}")
    public void previewFile(@PathVariable String biz, @PathVariable Long userId,
            @PathVariable String filename, HttpServletResponse response) {
        try {
            log.info("Preview file request: biz={}, userId={}, filename={}", biz, userId, filename);
            validatePreviewBiz(biz);
            
            response.setContentType(resolveContentType(filename));

            try (InputStream inputStream = openCompatibleFileStream(biz, userId, filename);
                    OutputStream outputStream = response.getOutputStream()) {
                writeStreamToResponse(inputStream, outputStream);
            }
        } catch (BusinessException e) {
            log.error("Business exception in preview file", e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to preview file", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件预览失败");
        }
    }

    /**
     * 管理端：按对象路径预览
     */
    @GetMapping("/admin/preview")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void previewMinioObject(@RequestParam("objectPath") String objectPath, HttpServletResponse response) {
        validateAdminPath(objectPath, false);
        String filename = extractFileName(objectPath);
        response.setContentType(resolveContentType(filename));
        try (InputStream inputStream = fileStorageManager.getFileStream(objectPath);
                OutputStream outputStream = response.getOutputStream()) {
            writeStreamToResponse(inputStream, outputStream);
        } catch (Exception e) {
            log.error("Failed to preview minio object, path={}", objectPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件预览失败");
        }
    }

    /**
     * 管理端：删除对象（文件 / 目录）
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteMinioObject(@RequestParam("objectPath") String objectPath,
            @RequestParam(value = "directory", required = false, defaultValue = "false") boolean directory) {
        validateAdminPath(objectPath, false);
        boolean success = directory
                ? fileStorageManager.deleteDirectory(objectPath)
                : fileStorageManager.deleteObject(objectPath);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        if (fileSuffix != null) {
            fileSuffix = fileSuffix.toLowerCase();
        }
        final List<String> imageSuffix = Arrays.asList("jpeg", "jpg", "png", "webp");
        final boolean isAvatarBiz = FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)
                || FileUploadBizEnum.MERCHANT_AVATAR.equals(fileUploadBizEnum);
        final boolean isImageBiz = FileUploadBizEnum.DISH_IMAGE.equals(fileUploadBizEnum)
                || FileUploadBizEnum.DYNAMIC_IMAGE.equals(fileUploadBizEnum)
                || FileUploadBizEnum.ANNOUNCEMENT_IMAGE.equals(fileUploadBizEnum)
                || FileUploadBizEnum.COMPLAINT_EVIDENCE.equals(fileUploadBizEnum);
        if (isAvatarBiz) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!imageSuffix.contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            return;
        }
        if (isImageBiz) {
            if (fileSize > 5 * ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小不能超过 5M");
            }
            if (!imageSuffix.contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片仅支持 jpg/jpeg/png/webp");
            }
            return;
        }
        if (fileSize > 10 * ONE_M) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 10M");
        }
    }

    private void validatePreviewBiz(String biz) {
        if (StringUtils.isBlank(biz)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "业务类型错误");
        }
        if (FileUploadBizEnum.getEnumByValue(biz) != null) {
            return;
        }
        if (StringUtils.isNotBlank(mapLegacyBizToEnumBiz(biz))) {
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "业务类型错误");
    }

    private InputStream openCompatibleFileStream(String biz, Long userId, String filename) {
        BusinessException lastNotFound = null;
        for (String candidateBiz : resolveCompatibleBizCandidates(biz)) {
            String relativePath = candidateBiz + "/" + userId + "/" + filename;
            try {
                return fileStorageManager.getFileStream(relativePath);
            } catch (BusinessException e) {
                if (e.getCode() != ErrorCode.NOT_FOUND_ERROR.getCode()) {
                    throw e;
                }
                lastNotFound = e;
            }
        }
        if (lastNotFound != null) {
            throw lastNotFound;
        }
        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
    }

    private List<String> resolveCompatibleBizCandidates(String biz) {
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        if (StringUtils.isBlank(biz)) {
            return new ArrayList<>();
        }
        String normalizedBiz = biz.trim();
        candidates.add(normalizedBiz);
        String legacyBiz = mapEnumBizToLegacyBiz(normalizedBiz);
        if (StringUtils.isNotBlank(legacyBiz)) {
            candidates.add(legacyBiz);
        }
        String enumBiz = mapLegacyBizToEnumBiz(normalizedBiz);
        if (StringUtils.isNotBlank(enumBiz)) {
            candidates.add(enumBiz);
        }
        return new ArrayList<>(candidates);
    }

    private String mapLegacyBizToEnumBiz(String biz) {
        if (StringUtils.isBlank(biz)) {
            return null;
        }
        switch (biz.trim()) {
            case "user":
                return FileUploadBizEnum.USER_AVATAR.getValue();
            case "merchant":
                return FileUploadBizEnum.MERCHANT_AVATAR.getValue();
            case "dish":
                return FileUploadBizEnum.DISH_IMAGE.getValue();
            case "complaint":
                return FileUploadBizEnum.COMPLAINT_EVIDENCE.getValue();
            case "dynamic":
                return FileUploadBizEnum.DYNAMIC_IMAGE.getValue();
            case "announcement":
                return FileUploadBizEnum.ANNOUNCEMENT_IMAGE.getValue();
            default:
                return null;
        }
    }

    private String mapEnumBizToLegacyBiz(String biz) {
        if (StringUtils.isBlank(biz)) {
            return null;
        }
        switch (biz.trim()) {
            case "user_avatar":
                return "user";
            case "merchant_avatar":
                return "merchant";
            case "dish_image":
                return "dish";
            case "complaint_evidence":
                return "complaint";
            case "dynamic_image":
                return "dynamic";
            case "announcement_image":
                return "announcement";
            default:
                return null;
        }
    }

    private void validateAdminUploadFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        }
        if (multipartFile.getSize() > ADMIN_FILE_MAX_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 100M");
        }
    }

    private void validateAdminPath(String path, boolean allowEmpty) {
        if (StringUtils.isBlank(path)) {
            if (allowEmpty) {
                return;
            }
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "对象路径不能为空");
        }
        String normalized = path.trim().replace("\\", "/");
        if (normalized.contains("..")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "对象路径非法");
        }
    }

    private String extractFileName(String objectPath) {
        if (StringUtils.isBlank(objectPath)) {
            return "file";
        }
        String normalized = objectPath.replace("\\", "/");
        int lastSlashIndex = normalized.lastIndexOf("/");
        if (lastSlashIndex < 0) {
            return normalized;
        }
        String filename = normalized.substring(lastSlashIndex + 1);
        return StringUtils.isBlank(filename) ? "file" : filename;
    }

    private void writeStreamToResponse(InputStream inputStream, OutputStream outputStream) throws Exception {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
    }

    private String resolveContentType(String filename) {
        if (StringUtils.isBlank(filename)) {
            return "application/octet-stream";
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        }
        return "application/octet-stream";
    }

    /**
     * 上传用户头像
     *
     * @param file 文件
     * @param request HTTP请求
     * @return 文件URL
     */
    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadUserAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            return ResultUtils.error(400, "文件不能为空");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return ResultUtils.error(400, "头像大小不能超过5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResultUtils.error(400, "只支持图片文件");
        }

        try {
            User loginUser = userService.getLoginUser(request);
            String fileUrl = minioUtils.uploadFile(file, FileUploadBizEnum.USER_AVATAR.getValue(), loginUser.getId());
            log.info("用户头像上传成功: {}", fileUrl);
            return ResultUtils.success(fileUrl);
        } catch (Exception e) {
            log.error("用户头像上传失败", e);
            return ResultUtils.error(500, "头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传菜品图片
     *
     * @param file 文件
     * @param request HTTP请求
     * @return 文件URL
     */
    @PostMapping("/upload/dish")
    public BaseResponse<String> uploadDishImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            return ResultUtils.error(400, "文件不能为空");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return ResultUtils.error(400, "菜品图片大小不能超过10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResultUtils.error(400, "只支持图片文件");
        }

        try {
            User loginUser = userService.getLoginUser(request);
            String fileUrl = minioUtils.uploadFile(file, FileUploadBizEnum.DISH_IMAGE.getValue(), loginUser.getId());
            log.info("菜品图片上传成功: {}", fileUrl);
            return ResultUtils.success(fileUrl);
        } catch (Exception e) {
            log.error("菜品图片上传失败", e);
            return ResultUtils.error(500, "图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传商户图片
     *
     * @param file 文件
     * @param request HTTP请求
     * @return 文件URL
     */
    @PostMapping("/upload/merchant")
    public BaseResponse<String> uploadMerchantImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            return ResultUtils.error(400, "文件不能为空");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return ResultUtils.error(400, "商户图片大小不能超过10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResultUtils.error(400, "只支持图片文件");
        }

        try {
            User loginUser = userService.getLoginUser(request);
            String fileUrl = minioUtils.uploadFile(file, FileUploadBizEnum.MERCHANT_AVATAR.getValue(), loginUser.getId());
            log.info("商户图片上传成功: {}", fileUrl);
            return ResultUtils.success(fileUrl);
        } catch (Exception e) {
            log.error("商户图片上传失败", e);
            return ResultUtils.error(500, "图片上传失败: " + e.getMessage());
        }
    }
}
