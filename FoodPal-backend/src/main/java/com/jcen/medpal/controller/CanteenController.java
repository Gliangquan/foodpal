package com.jcen.medpal.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.annotation.AuthCheck;
import com.jcen.medpal.common.BaseResponse;
import com.jcen.medpal.common.ErrorCode;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.constant.UserConstant;
import com.jcen.medpal.exception.BusinessException;
import com.jcen.medpal.model.dto.food.*;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.model.entity.food.CanteenAnnouncement;
import com.jcen.medpal.model.entity.food.CanteenComplaint;
import com.jcen.medpal.model.entity.food.CanteenDynamicComment;
import com.jcen.medpal.model.entity.food.CanteenDynamicLike;
import com.jcen.medpal.model.entity.food.CanteenDynamicShare;
import com.jcen.medpal.model.entity.food.CanteenDynamic;
import com.jcen.medpal.model.entity.food.Dish;
import com.jcen.medpal.model.entity.food.DishFavorite;
import com.jcen.medpal.model.entity.food.DishLike;
import com.jcen.medpal.model.entity.food.MerchantProfile;
import com.jcen.medpal.service.CanteenAnnouncementService;
import com.jcen.medpal.service.CanteenComplaintService;
import com.jcen.medpal.service.CanteenDynamicCommentService;
import com.jcen.medpal.service.CanteenDynamicLikeService;
import com.jcen.medpal.service.CanteenDynamicShareService;
import com.jcen.medpal.service.CanteenDynamicService;
import com.jcen.medpal.service.DishFavoriteService;
import com.jcen.medpal.service.DishLikeService;
import com.jcen.medpal.service.DishService;
import com.jcen.medpal.service.MerchantProfileService;
import com.jcen.medpal.service.NotificationService;
import com.jcen.medpal.service.UserService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/canteen")
@Slf4j
public class CanteenController {

    private static final Set<String> STUDENT_ROLES = new HashSet<>();
    private static final Set<String> SUPERVISOR_ROLES = new HashSet<>();
    private static final Set<String> MERCHANT_ROLES = new HashSet<>();
    private static final Set<String> COMPLAINT_STATUS_SET = new HashSet<>();
    private static final DateTimeFormatter COMPLAINT_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    static {
        STUDENT_ROLES.add("student");
        STUDENT_ROLES.add("user");
        SUPERVISOR_ROLES.add("supervisor");
        SUPERVISOR_ROLES.add("admin");
        MERCHANT_ROLES.add("merchant");
        COMPLAINT_STATUS_SET.add("pending_review");
        COMPLAINT_STATUS_SET.add("pending_rectify");
        COMPLAINT_STATUS_SET.add("rectified");
        COMPLAINT_STATUS_SET.add("completed");
        COMPLAINT_STATUS_SET.add("rejected");
    }

    @Resource
    private UserService userService;

    @Resource
    private MerchantProfileService merchantProfileService;

    @Resource
    private DishService dishService;

    @Resource
    private DishFavoriteService dishFavoriteService;

    @Resource
    private DishLikeService dishLikeService;

    @Resource
    private CanteenComplaintService canteenComplaintService;

    @Resource
    private CanteenDynamicService canteenDynamicService;

    @Resource
    private CanteenDynamicLikeService canteenDynamicLikeService;

    @Resource
    private CanteenDynamicCommentService canteenDynamicCommentService;

    @Resource
    private CanteenDynamicShareService canteenDynamicShareService;

    @Resource
    private CanteenAnnouncementService canteenAnnouncementService;

    @Resource
    private NotificationService notificationService;

    @GetMapping("/dashboard/summary")
    public BaseResponse<Map<String, Object>> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        List<MerchantProfile> activeMerchants = merchantProfileService.lambdaQuery()
                .eq(MerchantProfile::getIsDelete, 0)
                .list();
        Set<Long> activeMerchantIds = activeMerchants.stream()
                .map(MerchantProfile::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        long dishCount = activeMerchantIds.isEmpty()
                ? 0
                : dishService.lambdaQuery()
                        .eq(Dish::getIsDelete, 0)
                        .in(Dish::getMerchantId, activeMerchantIds)
                        .count();
        long complaintCount = activeMerchantIds.isEmpty()
                ? 0
                : canteenComplaintService.lambdaQuery()
                        .eq(CanteenComplaint::getIsDelete, 0)
                        .in(CanteenComplaint::getMerchantId, activeMerchantIds)
                        .count();
        long pendingComplaintCount = activeMerchantIds.isEmpty()
                ? 0
                : canteenComplaintService.lambdaQuery()
                        .eq(CanteenComplaint::getIsDelete, 0)
                        .in(CanteenComplaint::getMerchantId, activeMerchantIds)
                        .in(CanteenComplaint::getStatus, "pending_review", "pending_rectify")
                        .count();

        summary.put("merchantCount", activeMerchants.size());
        summary.put("dishCount", dishCount);
        summary.put("complaintCount", complaintCount);
        summary.put("pendingComplaintCount", pendingComplaintCount);
        summary.put("dynamicCount", canteenDynamicService.lambdaQuery().eq(CanteenDynamic::getIsDelete, 0)
                .eq(CanteenDynamic::getStatus, "published").count());
        summary.put("announcementCount", canteenAnnouncementService.lambdaQuery().eq(CanteenAnnouncement::getIsDelete, 0)
                .eq(CanteenAnnouncement::getStatus, "published").count());
        return ResultUtils.success(summary);
    }

    @GetMapping("/merchant/list")
    public BaseResponse<Page<MerchantProfile>> listMerchants(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String auditStatus) {
        List<User> matchedUsers = StringUtils.isBlank(keyword)
                ? Collections.emptyList()
                : userService.lambdaQuery()
                        .eq(User::getIsDelete, 0)
                        .eq(User::getUserRole, "merchant")
                        .and(wrapper -> wrapper.like(User::getUserName, keyword)
                                .or().like(User::getUserAccount, keyword)
                                .or().like(User::getUserPhone, keyword)
                                .or().like(User::getUserEmail, keyword))
                        .list();
        Set<Long> matchedUserIds = matchedUsers.stream()
                .map(User::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Page<MerchantProfile> page = merchantProfileService.lambdaQuery()
                .eq(MerchantProfile::getIsDelete, 0)
                .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper
                        .like(MerchantProfile::getMerchantName, keyword)
                        .or().like(MerchantProfile::getContactName, keyword)
                        .or().like(MerchantProfile::getContactPhone, keyword)
                        .or().in(!matchedUserIds.isEmpty(), MerchantProfile::getUserId, matchedUserIds))
                .eq(status != null, MerchantProfile::getStatus, status)
                .eq(StringUtils.isNotBlank(auditStatus), MerchantProfile::getAuditStatus, auditStatus)
                .orderByDesc(MerchantProfile::getUpdateTime)
                .page(new Page<>(current, size));
        return ResultUtils.success(page);
    }

    @GetMapping("/merchant/my")
    public BaseResponse<MerchantProfile> getMyMerchant(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        MerchantProfile profile = getMerchantByUserId(loginUser.getId());
        if (profile == null) {
            return ResultUtils.success(null);
        }
        MerchantProfile view = new MerchantProfile();
        BeanUtils.copyProperties(profile, view);
        if (StringUtils.isNotBlank(profile.getPendingData())) {
            applyPendingMerchantData(view);
            view.setPendingData(profile.getPendingData());
        }
        return ResultUtils.success(view);
    }

    @PostMapping("/merchant/save")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> saveMerchant(@RequestBody MerchantProfile merchantProfile) {
        if (merchantProfile == null || merchantProfile.getUserId() == null || StringUtils.isBlank(merchantProfile.getMerchantName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商户参数不完整");
        }
        User bindUser = userService.getById(merchantProfile.getUserId());
        if (bindUser == null || bindUser.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "关联商户账号不存在");
        }
        if (!"merchant".equalsIgnoreCase(bindUser.getUserRole())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能绑定商户角色账号");
        }
        merchantProfile.setId(null);
        merchantProfile.setStatus(merchantProfile.getStatus() == null ? 1 : merchantProfile.getStatus());
        merchantProfile.setAuditStatus(StringUtils.defaultIfBlank(merchantProfile.getAuditStatus(), "approved"));
        merchantProfile.setContactName(StringUtils.defaultIfBlank(merchantProfile.getContactName(), bindUser.getUserName()));
        merchantProfile.setContactPhone(StringUtils.defaultIfBlank(merchantProfile.getContactPhone(), bindUser.getUserPhone()));
        merchantProfile.setCreateTime(LocalDateTime.now());
        merchantProfile.setUpdateTime(LocalDateTime.now());
        merchantProfile.setIsDelete(0);
        boolean saved = merchantProfileService.save(merchantProfile);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "商户保存失败");
        }
        return ResultUtils.success(merchantProfile.getId());
    }

    @PostMapping("/merchant/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateMerchant(@RequestBody MerchantProfile merchantProfile) {
        if (merchantProfile == null || merchantProfile.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商户ID不能为空");
        }
        MerchantProfile existed = merchantProfileService.getById(merchantProfile.getId());
        if (existed == null || existed.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商户不存在");
        }

        boolean auditOnlyRequest = merchantProfile.getStatus() == null
                && merchantProfile.getUserId() == null
                && StringUtils.isBlank(merchantProfile.getMerchantName())
                && StringUtils.isBlank(merchantProfile.getContactName())
                && StringUtils.isBlank(merchantProfile.getContactPhone())
                && StringUtils.isBlank(merchantProfile.getAvatar())
                && StringUtils.isBlank(merchantProfile.getBusinessHours())
                && StringUtils.isBlank(merchantProfile.getLocation())
                && StringUtils.isBlank(merchantProfile.getDescription())
                && StringUtils.isNotBlank(merchantProfile.getAuditStatus());

        if (merchantProfile.getUserId() != null && !merchantProfile.getUserId().equals(existed.getUserId())) {
            User bindUser = userService.getById(merchantProfile.getUserId());
            if (bindUser == null || bindUser.getIsDelete() != 0) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "关联商户账号不存在");
            }
            if (!"merchant".equalsIgnoreCase(bindUser.getUserRole())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能绑定商户角色账号");
            }
            MerchantProfile duplicated = merchantProfileService.lambdaQuery()
                    .eq(MerchantProfile::getUserId, merchantProfile.getUserId())
                    .eq(MerchantProfile::getIsDelete, 0)
                    .ne(MerchantProfile::getId, existed.getId())
                    .one();
            if (duplicated != null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该商户账号已绑定其他商户");
            }
            existed.setUserId(merchantProfile.getUserId());
            if (StringUtils.isBlank(merchantProfile.getContactName())) {
                existed.setContactName(StringUtils.defaultIfBlank(existed.getContactName(), bindUser.getUserName()));
            }
            if (StringUtils.isBlank(merchantProfile.getContactPhone())) {
                existed.setContactPhone(StringUtils.defaultIfBlank(existed.getContactPhone(), bindUser.getUserPhone()));
            }
        }

        if (merchantProfile.getStatus() != null) {
            existed.setStatus(merchantProfile.getStatus());
        }
        if (StringUtils.isNotBlank(merchantProfile.getAuditStatus())) {
            existed.setAuditStatus(merchantProfile.getAuditStatus());
        }
        if (StringUtils.isNotBlank(merchantProfile.getMerchantName())) {
            existed.setMerchantName(merchantProfile.getMerchantName().trim());
        }
        if (merchantProfile.getContactName() != null) {
            existed.setContactName(StringUtils.trimToNull(merchantProfile.getContactName()));
        }
        if (merchantProfile.getContactPhone() != null) {
            existed.setContactPhone(StringUtils.trimToNull(merchantProfile.getContactPhone()));
        }
        if (merchantProfile.getAvatar() != null) {
            existed.setAvatar(StringUtils.trimToNull(merchantProfile.getAvatar()));
        }
        if (merchantProfile.getBusinessHours() != null) {
            existed.setBusinessHours(StringUtils.trimToNull(merchantProfile.getBusinessHours()));
        }
        if (merchantProfile.getLocation() != null) {
            existed.setLocation(StringUtils.trimToNull(merchantProfile.getLocation()));
        }
        if (merchantProfile.getDescription() != null) {
            existed.setDescription(StringUtils.trimToNull(merchantProfile.getDescription()));
        }

        if (auditOnlyRequest) {
            if ("approved".equals(existed.getAuditStatus())) {
                applyPendingMerchantData(existed);
            } else if ("rejected".equals(existed.getAuditStatus())) {
                existed.setPendingData(null);
            }
        } else {
            existed.setPendingData(null);
        }

        existed.setUpdateTime(LocalDateTime.now());
        return ResultUtils.success(merchantProfileService.updateById(existed));
    }

    @PostMapping("/merchant/my/update")
    public BaseResponse<Boolean> updateMyMerchant(@RequestBody MerchantProfile requestBody, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        requireMerchant(loginUser);

        MerchantProfile existed = getMerchantByUserId(loginUser.getId());
        if (existed == null) {
            existed = new MerchantProfile();
            existed.setUserId(loginUser.getId());
            existed.setCreateTime(LocalDateTime.now());
            existed.setIsDelete(0);
            existed.setStatus(1);
        }
        existed.setAuditStatus("pending");
        existed.setPendingData(buildPendingMerchantData(requestBody));
        existed.setUpdateTime(LocalDateTime.now());

        if (existed.getId() == null) {
            applyPendingMerchantData(existed);
        }
        boolean ok = existed.getId() == null ? merchantProfileService.save(existed) : merchantProfileService.updateById(existed);
        return ResultUtils.success(ok);
    }

    @PostMapping("/merchant/image/upload")
    public BaseResponse<String> uploadMerchantImage(@org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        requireMerchant(loginUser);
        
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商户图片大小不能超过10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持图片文件");
        }

        try {
            com.jcen.medpal.utils.MinioUtils minioUtils = new com.jcen.medpal.utils.MinioUtils();
            String imageUrl = minioUtils.uploadFile(file, "merchant", loginUser.getId());
            log.info("商户图片上传成功，用户ID: {}, 图片URL: {}", loginUser.getId(), imageUrl);
            return ResultUtils.success(imageUrl);
        } catch (Exception e) {
            log.error("商户图片上传失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/merchant/delete/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteMerchant(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        MerchantProfile profile = merchantProfileService.getById(id);
        if (profile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        profile.setIsDelete(1);
        profile.setUpdateTime(LocalDateTime.now());
        return ResultUtils.success(merchantProfileService.updateById(profile));
    }

    @PostMapping("/merchant/violation/{merchantId}")
    public BaseResponse<Boolean> sendViolationNotice(@PathVariable Long merchantId, @RequestBody Map<String, String> body, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        requireSupervisor(loginUser);

        if (merchantId == null || merchantId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商户ID不能为空");
        }
        String content = body.get("content");
        if (StringUtils.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提醒内容不能为空");
        }

        MerchantProfile merchant = merchantProfileService.getById(merchantId);
        if (merchant == null || merchant.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商户不存在");
        }

        // 发送通知给商户
        notificationService.createNotification(
                merchant.getUserId(),
                "system",
                "违规提醒",
                content,
                "merchant",
                merchantId);

        return ResultUtils.success(true);
    }

    @GetMapping("/dish/list")
    public BaseResponse<Page<Map<String, Object>>> listDish(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Integer supplyStatus,
            @RequestParam(defaultValue = "false") boolean onlySpecial,
            HttpServletRequest request) {

        List<MerchantProfile> activeMerchants = merchantProfileService.lambdaQuery()
                .eq(MerchantProfile::getIsDelete, 0)
                .list();
        Set<Long> activeMerchantIds = activeMerchants.stream()
                .map(MerchantProfile::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (activeMerchantIds.isEmpty()) {
            Page<Map<String, Object>> emptyPage = new Page<>(current, size, 0);
            emptyPage.setRecords(Collections.emptyList());
            return ResultUtils.success(emptyPage);
        }
        Set<Long> matchedMerchantIds = activeMerchants.stream()
                .filter(item -> StringUtils.isNotBlank(item.getMerchantName()) && StringUtils.containsIgnoreCase(item.getMerchantName(), keyword))
                .map(MerchantProfile::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Page<Dish> dishPage = dishService.lambdaQuery()
                .eq(Dish::getIsDelete, 0)
                .eq(StringUtils.isNotBlank(category), Dish::getCategory, category)
                .eq(merchantId != null, Dish::getMerchantId, merchantId)
                .eq(supplyStatus != null, Dish::getSupplyStatus, supplyStatus)
                .in(Dish::getMerchantId, activeMerchantIds)
                .and(StringUtils.isNotBlank(keyword), wrapper -> {
                    wrapper.like(Dish::getDishName, keyword);
                    if (!matchedMerchantIds.isEmpty()) {
                        wrapper.or().in(Dish::getMerchantId, matchedMerchantIds);
                    }
                })
                .orderByDesc(Dish::getUpdateTime)
                .page(new Page<>(current, size));

        List<Dish> dishes = dishPage.getRecords();
        if (onlySpecial) {
            LocalDateTime now = LocalDateTime.now();
            dishes = dishes.stream().filter(d -> isSpecialDish(d, now)).collect(Collectors.toList());
        }

        Map<Long, MerchantProfile> merchantMap = buildMerchantMap(dishes.stream().map(Dish::getMerchantId).collect(Collectors.toSet()));
        User loginUser = tryGetLoginUser(request);
        Set<Long> likedDishIds = new HashSet<>();
        Set<Long> favoriteDishIds = new HashSet<>();
        if (loginUser != null && !dishes.isEmpty()) {
            List<Long> dishIds = dishes.stream().map(Dish::getId).collect(Collectors.toList());
            likedDishIds = dishLikeService.lambdaQuery().eq(DishLike::getUserId, loginUser.getId()).eq(DishLike::getIsDelete, 0)
                    .in(DishLike::getDishId, dishIds).list().stream().map(DishLike::getDishId).collect(Collectors.toSet());
            favoriteDishIds = dishFavoriteService.lambdaQuery().eq(DishFavorite::getUserId, loginUser.getId()).eq(DishFavorite::getIsDelete, 0)
                    .in(DishFavorite::getDishId, dishIds).list().stream().map(DishFavorite::getDishId).collect(Collectors.toSet());
        }

        List<Map<String, Object>> records = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Dish dish : dishes) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", dish.getId());
            record.put("dishName", dish.getDishName());
            record.put("dishPrice", dish.getDishPrice());
            record.put("ingredients", dish.getIngredients());
            record.put("nutritionInfo", dish.getNutritionInfo());
            record.put("dishImage", dish.getDishImage());
            record.put("category", dish.getCategory());
            record.put("supplyStatus", dish.getSupplyStatus());
            record.put("merchantId", dish.getMerchantId());
            record.put("likeCount", dish.getLikeCount());
            record.put("favoriteCount", dish.getFavoriteCount());
            record.put("createTime", dish.getCreateTime());
            record.put("updateTime", dish.getUpdateTime());

            MerchantProfile merchant = merchantMap.get(dish.getMerchantId());
            record.put("merchantName", merchant == null ? "" : merchant.getMerchantName());

            boolean special = isSpecialDish(dish, now);
            record.put("isSpecial", special);
            record.put("currentPrice", special ? dish.getSpecialPrice() : dish.getDishPrice());
            record.put("specialPrice", dish.getSpecialPrice());
            record.put("specialStartTime", dish.getSpecialStartTime());
            record.put("specialEndTime", dish.getSpecialEndTime());

            record.put("liked", likedDishIds.contains(dish.getId()));
            record.put("favorited", favoriteDishIds.contains(dish.getId()));
            records.add(record);
        }

        Page<Map<String, Object>> page = new Page<>(dishPage.getCurrent(), dishPage.getSize(), dishPage.getTotal());
        page.setRecords(records);
        return ResultUtils.success(page);
    }

    @GetMapping("/dish/recommend")
    public BaseResponse<List<Map<String, Object>>> recommendDish(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        User loginUser = tryGetLoginUser(request);
        LocalDateTime now = LocalDateTime.now();

        List<MerchantProfile> activeMerchants = merchantProfileService.lambdaQuery()
                .eq(MerchantProfile::getIsDelete, 0)
                .list();
        Set<Long> activeMerchantIds = activeMerchants.stream()
                .map(MerchantProfile::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (activeMerchantIds.isEmpty()) {
            return ResultUtils.success(Collections.emptyList());
        }

        List<Dish> activeDishes = dishService.lambdaQuery()
                .eq(Dish::getIsDelete, 0)
                .eq(Dish::getSupplyStatus, 1)
                .in(Dish::getMerchantId, activeMerchantIds)
                .list();

        if (activeDishes.isEmpty()) {
            return ResultUtils.success(Collections.emptyList());
        }

        // 获取用户偏好数据
        Set<String> preferredCategories = new HashSet<>();
        Set<Long> preferredMerchantIds = new HashSet<>();
        Set<Long> likedDishIds = new HashSet<>();
        Set<Long> favoriteDishIds = new HashSet<>();

        if (loginUser != null) {
            // 获取用户点赞的菜品
            likedDishIds = dishLikeService.lambdaQuery()
                    .eq(DishLike::getUserId, loginUser.getId())
                    .eq(DishLike::getIsDelete, 0)
                    .list().stream().map(DishLike::getDishId).collect(Collectors.toSet());

            // 获取用户收藏的菜品
            favoriteDishIds = dishFavoriteService.lambdaQuery()
                    .eq(DishFavorite::getUserId, loginUser.getId())
                    .eq(DishFavorite::getIsDelete, 0)
                    .list().stream().map(DishFavorite::getDishId).collect(Collectors.toSet());

            // 获取用户偏好的分类和商户
            Set<Long> preferredDishIds = new HashSet<>();
            preferredDishIds.addAll(likedDishIds);
            preferredDishIds.addAll(favoriteDishIds);

            for (Dish dish : activeDishes) {
                if (preferredDishIds.contains(dish.getId())) {
                    if (StringUtils.isNotBlank(dish.getCategory())) {
                        preferredCategories.add(dish.getCategory());
                    }
                    if (dish.getMerchantId() != null) {
                        preferredMerchantIds.add(dish.getMerchantId());
                    }
                }
            }
        }

        // 计算每个菜品的推荐分数
        final Set<String> finalPreferredCategories = preferredCategories;
        final Set<Long> finalPreferredMerchantIds = preferredMerchantIds;
        final Set<Long> finalLikedDishIds = likedDishIds;
        final Set<Long> finalFavoriteDishIds = favoriteDishIds;

        List<Map<String, Object>> scoredDishes = new ArrayList<>();
        for (Dish dish : activeDishes) {
            int score = calculateRecommendScore(dish, finalPreferredCategories, finalPreferredMerchantIds,
                    finalLikedDishIds, finalFavoriteDishIds, now, loginUser);
            Map<String, Object> item = new HashMap<>();
            item.put("dish", dish);
            item.put("score", score);
            scoredDishes.add(item);
        }

        // 热门推荐按点赞数优先排序，同分再按创建顺序倒序
        scoredDishes.sort((o1, o2) -> {
            Dish d1 = (Dish) o1.get("dish");
            Dish d2 = (Dish) o2.get("dish");
            int likeCompare = Integer.compare(nvl(d2.getLikeCount()), nvl(d1.getLikeCount()));
            if (likeCompare != 0) {
                return likeCompare;
            }
            return Long.compare(d2.getId() == null ? 0L : d2.getId(), d1.getId() == null ? 0L : d1.getId());
        });

        // 去重后取前 limit 个，避免重复菜品出现在推荐区
        Map<Long, Dish> pickedMap = new LinkedHashMap<>();
        for (Map<String, Object> scoredDish : scoredDishes) {
            Dish dish = (Dish) scoredDish.get("dish");
            if (dish == null || dish.getId() == null || pickedMap.containsKey(dish.getId())) {
                continue;
            }
            pickedMap.put(dish.getId(), dish);
            if (pickedMap.size() >= limit) {
                break;
            }
        }
        List<Dish> picked = new ArrayList<>(pickedMap.values());

        Map<Long, MerchantProfile> merchantMap = buildMerchantMap(picked.stream().map(Dish::getMerchantId).collect(Collectors.toSet()));
        List<Map<String, Object>> data = picked.stream().map(dish -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", dish.getId());
            row.put("dishName", dish.getDishName());
            row.put("dishPrice", dish.getDishPrice());
            row.put("dishImage", dish.getDishImage());
            row.put("category", dish.getCategory());
            row.put("merchantId", dish.getMerchantId());
            row.put("merchantName", merchantMap.get(dish.getMerchantId()) == null ? "" : merchantMap.get(dish.getMerchantId()).getMerchantName());
            row.put("likeCount", dish.getLikeCount());
            row.put("favoriteCount", dish.getFavoriteCount());
            boolean special = isSpecialDish(dish, now);
            row.put("isSpecial", special);
            row.put("currentPrice", special ? dish.getSpecialPrice() : dish.getDishPrice());
            row.put("score", nvl(dish.getLikeCount()) + nvl(dish.getFavoriteCount()));
            return row;
        }).collect(Collectors.toList());

        return ResultUtils.success(data);
    }

    @PostMapping("/dish/save")
    public BaseResponse<Long> saveDish(@RequestBody Dish dish, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (dish == null || StringUtils.isBlank(dish.getDishName()) || dish.getDishPrice() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "菜品参数不完整");
        }

        fillDishByRole(dish, loginUser, true);
        dish.setId(null);
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        dish.setIsDelete(0);
        dish.setSupplyStatus(dish.getSupplyStatus() == null ? 1 : dish.getSupplyStatus());
        dish.setLikeCount(dish.getLikeCount() == null ? 0 : dish.getLikeCount());
        dish.setFavoriteCount(dish.getFavoriteCount() == null ? 0 : dish.getFavoriteCount());

        boolean saved = dishService.save(dish);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "新增菜品失败");
        }
        return ResultUtils.success(dish.getId());
    }

    @PostMapping("/dish/image/upload")
    public BaseResponse<String> uploadDishImage(@org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        requireMerchant(loginUser);
        
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "菜品图片大小不能超过10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持图片文件");
        }

        try {
            com.jcen.medpal.utils.MinioUtils minioUtils = new com.jcen.medpal.utils.MinioUtils();
            String imageUrl = minioUtils.uploadFile(file, "dish", loginUser.getId());
            log.info("菜品图片上传成功，用户ID: {}, 图片URL: {}", loginUser.getId(), imageUrl);
            return ResultUtils.success(imageUrl);
        } catch (Exception e) {
            log.error("菜品图片上传失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/dish/update")
    public BaseResponse<Boolean> updateDish(@RequestBody Dish dish, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (dish == null || dish.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "菜品ID不能为空");
        }
        Dish existed = dishService.getById(dish.getId());
        if (existed == null || existed.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "菜品不存在");
        }
        checkDishOwnership(existed, loginUser);

        fillDishByRole(dish, loginUser, false);
        dish.setUpdateTime(LocalDateTime.now());
        return ResultUtils.success(dishService.updateById(dish));
    }

    @PostMapping("/dish/delete/{id}")
    public BaseResponse<Boolean> deleteDish(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Dish existed = dishService.getById(id);
        if (existed == null || existed.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "菜品不存在");
        }
        checkDishOwnership(existed, loginUser);
        existed.setIsDelete(1);
        existed.setUpdateTime(LocalDateTime.now());
        return ResultUtils.success(dishService.updateById(existed));
    }

    @PostMapping("/dish/special/set")
    public BaseResponse<Boolean> setSpecialDish(@RequestBody DishSpecialRequest specialRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        requireMerchant(loginUser);
        
        if (specialRequest == null || specialRequest.getDishId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "菜品ID不能为空");
        }
        if (specialRequest.getSpecialPrice() == null || specialRequest.getSpecialPrice().signum() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "特价价格必须大于0");
        }
        if (specialRequest.getSpecialStartTime() == null || specialRequest.getSpecialEndTime() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "特价开始时间和结束时间不能为空");
        }
        if (!specialRequest.getSpecialStartTime().isBefore(specialRequest.getSpecialEndTime())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "特价开始时间必须早于结束时间");
        }

        Dish dish = dishService.getById(specialRequest.getDishId());
        if (dish == null || dish.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "菜品不存在");
        }
        
        MerchantProfile merchant = getMerchantByUserId(loginUser.getId());
        if (merchant == null || !Objects.equals(dish.getMerchantId(), merchant.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能操作自己商户的菜品");
        }

        if (specialRequest.getSpecialPrice().compareTo(dish.getDishPrice()) >= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "特价价格必须低于原价");
        }

        dish.setSpecialPrice(specialRequest.getSpecialPrice());
        dish.setSpecialStartTime(specialRequest.getSpecialStartTime());
        dish.setSpecialEndTime(specialRequest.getSpecialEndTime());
        dish.setPurchaseLimit(specialRequest.getPurchaseLimit());
        dish.setUpdateTime(LocalDateTime.now());
        
        boolean ok = dishService.updateById(dish);
        if (ok) {
            notificationService.createNotification(
                    dish.getMerchantId(),
                    "merchant",
                    "特价活动设置成功",
                    "菜品「" + dish.getDishName() + "」特价活动已设置，特价: ¥" + specialRequest.getSpecialPrice(),
                    "dish",
                    dish.getId());
        }
        return ResultUtils.success(ok);
    }

    @PostMapping("/dish/special/cancel/{dishId}")
    public BaseResponse<Boolean> cancelSpecialDish(@PathVariable Long dishId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        requireMerchant(loginUser);
        
        if (dishId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "菜品ID不能为空");
        }

        Dish dish = dishService.getById(dishId);
        if (dish == null || dish.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "菜品不存在");
        }
        
        MerchantProfile merchant = getMerchantByUserId(loginUser.getId());
        if (merchant == null || !Objects.equals(dish.getMerchantId(), merchant.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能操作自己商户的菜品");
        }

        if (dish.getSpecialPrice() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该菜品未设置特价");
        }

        dish.setSpecialPrice(null);
        dish.setSpecialStartTime(null);
        dish.setSpecialEndTime(null);
        dish.setPurchaseLimit(null);
        dish.setUpdateTime(LocalDateTime.now());
        
        boolean ok = dishService.updateById(dish);
        return ResultUtils.success(ok);
    }

    @GetMapping("/dish/detail/{id}")
    public BaseResponse<Dish> getDishDetail(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "菜品ID不能为空");
        }
        Dish dish = dishService.getById(id);
        if (dish == null || dish.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "菜品不存在");
        }
        return ResultUtils.success(dish);
    }

    @GetMapping("/dish/my/list")
    public BaseResponse<Page<Map<String, Object>>> listMyDish(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer supplyStatus,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        requireMerchant(loginUser);

        MerchantProfile merchant = getMerchantByUserId(loginUser.getId());
        if (merchant == null) {
            Page<Map<String, Object>> emptyPage = new Page<>(current, size);
            emptyPage.setRecords(Collections.emptyList());
            return ResultUtils.success(emptyPage);
        }

        Page<Dish> dishPage = dishService.lambdaQuery()
                .eq(Dish::getIsDelete, 0)
                .eq(Dish::getMerchantId, merchant.getId())
                .eq(supplyStatus != null, Dish::getSupplyStatus, supplyStatus)
                .like(StringUtils.isNotBlank(keyword), Dish::getDishName, keyword)
                .orderByDesc(Dish::getUpdateTime)
                .page(new Page<>(current, size));
        
        List<Dish> dishes = dishPage.getRecords();
        LocalDateTime now = LocalDateTime.now();
        
        List<Map<String, Object>> records = new ArrayList<>();
        for (Dish dish : dishes) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", dish.getId());
            record.put("dishName", dish.getDishName());
            record.put("dishPrice", dish.getDishPrice());
            record.put("ingredients", dish.getIngredients());
            record.put("nutritionInfo", dish.getNutritionInfo());
            record.put("dishImage", dish.getDishImage());
            record.put("category", dish.getCategory());
            record.put("supplyStatus", dish.getSupplyStatus());
            record.put("merchantId", dish.getMerchantId());
            record.put("likeCount", dish.getLikeCount());
            record.put("favoriteCount", dish.getFavoriteCount());
            record.put("createTime", dish.getCreateTime());
            record.put("updateTime", dish.getUpdateTime());
            
            boolean special = isSpecialDish(dish, now);
            record.put("isSpecial", special);
            record.put("currentPrice", special ? dish.getSpecialPrice() : dish.getDishPrice());
            record.put("specialPrice", dish.getSpecialPrice());
            record.put("specialStartTime", dish.getSpecialStartTime());
            record.put("specialEndTime", dish.getSpecialEndTime());
            record.put("purchaseLimit", dish.getPurchaseLimit());
            
            records.add(record);
        }
        
        Page<Map<String, Object>> page = new Page<>(dishPage.getCurrent(), dishPage.getSize(), dishPage.getTotal());
        page.setRecords(records);
        return ResultUtils.success(page);
    }

    @PostMapping("/dish/like/toggle/{dishId}")
    public BaseResponse<Boolean> toggleLike(@PathVariable Long dishId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Dish dish = getDishOrThrow(dishId);

        DishLike existed = dishLikeService.lambdaQuery().eq(DishLike::getDishId, dishId)
                .eq(DishLike::getUserId, loginUser.getId()).orderByDesc(DishLike::getId).last("limit 1").one();
        if (existed == null) {
            DishLike dishLike = new DishLike();
            dishLike.setDishId(dishId);
            dishLike.setUserId(loginUser.getId());
            dishLike.setCreateTime(LocalDateTime.now());
            dishLike.setIsDelete(0);
            dishLikeService.save(dishLike);
            dish.setLikeCount(Math.max(0, nvl(dish.getLikeCount())) + 1);
        } else if (existed.getIsDelete() != null && existed.getIsDelete() == 1) {
            existed.setIsDelete(0);
            dishLikeService.updateById(existed);
            dish.setLikeCount(Math.max(0, nvl(dish.getLikeCount())) + 1);
        } else {
            existed.setIsDelete(1);
            dishLikeService.updateById(existed);
            dish.setLikeCount(Math.max(0, nvl(dish.getLikeCount()) - 1));
        }
        dish.setUpdateTime(LocalDateTime.now());
        dishService.updateById(dish);
        return ResultUtils.success(true);
    }

    @PostMapping("/dish/favorite/toggle/{dishId}")
    public BaseResponse<Boolean> toggleFavorite(@PathVariable Long dishId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Dish dish = getDishOrThrow(dishId);

        DishFavorite existed = dishFavoriteService.lambdaQuery().eq(DishFavorite::getDishId, dishId)
                .eq(DishFavorite::getUserId, loginUser.getId()).orderByDesc(DishFavorite::getId).last("limit 1").one();
        if (existed == null) {
            DishFavorite favorite = new DishFavorite();
            favorite.setDishId(dishId);
            favorite.setUserId(loginUser.getId());
            favorite.setCreateTime(LocalDateTime.now());
            favorite.setIsDelete(0);
            dishFavoriteService.save(favorite);
            dish.setFavoriteCount(Math.max(0, nvl(dish.getFavoriteCount())) + 1);
        } else if (existed.getIsDelete() != null && existed.getIsDelete() == 1) {
            existed.setIsDelete(0);
            dishFavoriteService.updateById(existed);
            dish.setFavoriteCount(Math.max(0, nvl(dish.getFavoriteCount())) + 1);
        } else {
            existed.setIsDelete(1);
            dishFavoriteService.updateById(existed);
            dish.setFavoriteCount(Math.max(0, nvl(dish.getFavoriteCount()) - 1));
        }
        dish.setUpdateTime(LocalDateTime.now());
        dishService.updateById(dish);
        return ResultUtils.success(true);
    }

    @GetMapping("/dish/favorite/list")
    public BaseResponse<List<Map<String, Object>>> listMyFavoriteDish(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<DishFavorite> favorites = dishFavoriteService.lambdaQuery().eq(DishFavorite::getUserId, loginUser.getId())
                .eq(DishFavorite::getIsDelete, 0).orderByDesc(DishFavorite::getCreateTime).list();
        if (favorites.isEmpty()) {
            return ResultUtils.success(Collections.emptyList());
        }
        List<Long> dishIds = favorites.stream().map(DishFavorite::getDishId).collect(Collectors.toList());
        List<Dish> dishes = dishService.lambdaQuery().in(Dish::getId, dishIds).eq(Dish::getIsDelete, 0).list();
        Map<Long, Dish> dishMap = dishes.stream().collect(Collectors.toMap(Dish::getId, dish -> dish));
        Map<Long, MerchantProfile> merchantMap = buildMerchantMap(dishes.stream().map(Dish::getMerchantId).collect(Collectors.toSet()));
        LocalDateTime now = LocalDateTime.now();

        List<Map<String, Object>> data = new ArrayList<>();
        for (DishFavorite favorite : favorites) {
            Dish dish = dishMap.get(favorite.getDishId());
            if (dish == null) {
                continue;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("id", dish.getId());
            row.put("dishName", dish.getDishName());
            row.put("dishImage", dish.getDishImage());
            row.put("dishPrice", dish.getDishPrice());
            row.put("category", dish.getCategory());
            row.put("supplyStatus", dish.getSupplyStatus());
            boolean special = isSpecialDish(dish, now);
            row.put("isSpecial", special);
            row.put("currentPrice", special ? dish.getSpecialPrice() : dish.getDishPrice());
            MerchantProfile merchant = merchantMap.get(dish.getMerchantId());
            row.put("merchantName", merchant == null ? "" : merchant.getMerchantName());
            row.put("favoriteTime", favorite.getCreateTime());
            data.add(row);
        }
        return ResultUtils.success(data);
    }

    @PostMapping("/complaint/create")
    public BaseResponse<Long> createComplaint(@RequestBody ComplaintCreateRequest complaintRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        requireStudent(loginUser);

        if (complaintRequest == null || complaintRequest.getMerchantId() == null
                || StringUtils.isBlank(complaintRequest.getComplaintTitle())
                || StringUtils.isBlank(complaintRequest.getComplaintContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "投诉参数不完整");
        }

        MerchantProfile merchantProfile = merchantProfileService.getById(complaintRequest.getMerchantId());
        if (merchantProfile == null || merchantProfile.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "投诉对象不存在");
        }

        CanteenComplaint complaint = new CanteenComplaint();
        complaint.setComplaintNo(generateComplaintNo());
        complaint.setUserId(loginUser.getId());
        complaint.setMerchantId(complaintRequest.getMerchantId());
        complaint.setComplaintTitle(complaintRequest.getComplaintTitle());
        complaint.setComplaintContent(complaintRequest.getComplaintContent());
        complaint.setEvidenceUrls(normalizeEvidenceUrls(complaintRequest.getEvidenceUrls()));
        complaint.setStatus("pending_review");
        complaint.setProcessProgress(processStatusText("pending_review"));
        complaint.setCreateTime(LocalDateTime.now());
        complaint.setUpdateTime(LocalDateTime.now());
        complaint.setIsDelete(0);

        boolean saved = canteenComplaintService.save(complaint);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "投诉提交失败");
        }

        notifySupervisor("新投诉待处理", "新的食堂投诉已提交，请及时审核", "complaint", complaint.getId());
        return ResultUtils.success(complaint.getId());
    }

    @GetMapping("/complaint/my/list")
    public BaseResponse<Page<Map<String, Object>>> listMyComplaint(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        Page<CanteenComplaint> page;
        if (isMerchant(loginUser)) {
            MerchantProfile merchant = getMerchantByUserId(loginUser.getId());
            if (merchant == null) {
                return ResultUtils.success(new Page<>(current, size));
            }
            page = canteenComplaintService.lambdaQuery()
                    .eq(CanteenComplaint::getIsDelete, 0)
                    .eq(CanteenComplaint::getMerchantId, merchant.getId())
                    .eq(StringUtils.isNotBlank(status), CanteenComplaint::getStatus, status)
                    .orderByDesc(CanteenComplaint::getCreateTime)
                    .page(new Page<>(current, size));
        } else {
            page = canteenComplaintService.lambdaQuery()
                    .eq(CanteenComplaint::getIsDelete, 0)
                    .eq(CanteenComplaint::getUserId, loginUser.getId())
                    .eq(StringUtils.isNotBlank(status), CanteenComplaint::getStatus, status)
                    .orderByDesc(CanteenComplaint::getCreateTime)
                    .page(new Page<>(current, size));
        }

        List<CanteenComplaint> complaints = page.getRecords();
        Set<Long> merchantIds = complaints.stream().map(CanteenComplaint::getMerchantId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> userIds = new HashSet<>();
        userIds.addAll(complaints.stream().map(CanteenComplaint::getUserId).filter(Objects::nonNull).collect(Collectors.toSet()));
        userIds.addAll(complaints.stream().map(CanteenComplaint::getProcessBy).filter(Objects::nonNull).collect(Collectors.toSet()));
        Map<Long, MerchantProfile> merchantMap = buildMerchantMap(merchantIds);
        Map<Long, User> userMap = buildUserMap(userIds);

        List<Map<String, Object>> data = new ArrayList<>();
        for (CanteenComplaint complaint : complaints) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", complaint.getId());
            row.put("complaintNo", complaint.getComplaintNo());
            row.put("userId", complaint.getUserId());
            row.put("merchantId", complaint.getMerchantId());
            row.put("complaintTitle", complaint.getComplaintTitle());
            row.put("complaintContent", complaint.getComplaintContent());
            row.put("evidenceUrls", complaint.getEvidenceUrls());
            row.put("status", complaint.getStatus());
            row.put("processProgress", complaint.getProcessProgress());
            row.put("rectifyRequirement", complaint.getRectifyRequirement());
            row.put("rectifyResult", complaint.getRectifyResult());
            row.put("feedback", complaint.getFeedback());
            row.put("resultRating", complaint.getResultRating());
            row.put("processBy", complaint.getProcessBy());
            row.put("processTime", complaint.getProcessTime());
            row.put("createTime", complaint.getCreateTime());
            row.put("updateTime", complaint.getUpdateTime());

            MerchantProfile merchant = merchantMap.get(complaint.getMerchantId());
            row.put("merchantName", merchant == null ? "" : merchant.getMerchantName());
            User submitUser = userMap.get(complaint.getUserId());
            row.put("studentName", submitUser == null ? "" : submitUser.getUserName());
            User processUser = userMap.get(complaint.getProcessBy());
            row.put("processByName", processUser == null ? "" : processUser.getUserName());
            data.add(row);
        }

        Page<Map<String, Object>> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(data);
        return ResultUtils.success(result);
    }

    @GetMapping("/complaint/list")
    public BaseResponse<Page<Map<String, Object>>> listComplaint(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long merchantId,
            HttpServletRequest request) {
        // 允许所有登录用户查看投诉列表（公开透明）
        User loginUser = tryGetLoginUser(request);

        List<MerchantProfile> activeMerchants = merchantProfileService.lambdaQuery()
                .eq(MerchantProfile::getIsDelete, 0)
                .list();
        Set<Long> activeMerchantIds = activeMerchants.stream()
                .map(MerchantProfile::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (activeMerchantIds.isEmpty()) {
            return ResultUtils.success(new Page<>(current, size, 0));
        }

        Map<Long, MerchantProfile> allMerchantMap = activeMerchants.stream()
                .collect(Collectors.toMap(MerchantProfile::getId, item -> item, (o1, o2) -> o1));
        Set<Long> matchedMerchantIds = activeMerchantIds;
        if (StringUtils.isNotBlank(keyword)) {
            String normalizedKeyword = keyword.trim();
            Set<Long> merchantIdsByKeyword = activeMerchants.stream()
                    .filter(item -> StringUtils.containsIgnoreCase(StringUtils.defaultString(item.getMerchantName()), normalizedKeyword))
                    .map(MerchantProfile::getId)
                    .collect(Collectors.toSet());
            Set<Long> studentIdsByKeyword = userService.lambdaQuery()
                    .eq(User::getIsDelete, 0)
                    .and(wrapper -> wrapper.like(User::getUserName, normalizedKeyword)
                            .or().like(User::getUserAccount, normalizedKeyword)
                            .or().like(User::getUserPhone, normalizedKeyword))
                    .list()
                    .stream()
                    .map(User::getId)
                    .collect(Collectors.toSet());
            List<CanteenComplaint> keywordComplaints = canteenComplaintService.lambdaQuery()
                    .eq(CanteenComplaint::getIsDelete, 0)
                    .in(CanteenComplaint::getMerchantId, activeMerchantIds)
                    .and(wrapper -> wrapper.like(CanteenComplaint::getComplaintNo, normalizedKeyword)
                            .or().like(CanteenComplaint::getComplaintTitle, normalizedKeyword)
                            .or().like(CanteenComplaint::getComplaintContent, normalizedKeyword)
                            .or().in(!studentIdsByKeyword.isEmpty(), CanteenComplaint::getUserId, studentIdsByKeyword)
                            .or().in(!merchantIdsByKeyword.isEmpty(), CanteenComplaint::getMerchantId, merchantIdsByKeyword))
                    .list();
            Page<Map<String, Object>> result = buildComplaintPage(current, size, keywordComplaints, allMerchantMap);
            return ResultUtils.success(result);
        }

        Page<CanteenComplaint> page = canteenComplaintService.lambdaQuery()
                .eq(CanteenComplaint::getIsDelete, 0)
                .in(CanteenComplaint::getMerchantId, matchedMerchantIds)
                .eq(StringUtils.isNotBlank(status), CanteenComplaint::getStatus, status)
                .eq(merchantId != null, CanteenComplaint::getMerchantId, merchantId)
                .orderByDesc(CanteenComplaint::getCreateTime)
                .page(new Page<>(current, size));

        List<CanteenComplaint> complaints = page.getRecords();
        Set<Long> merchantIds = complaints.stream().map(CanteenComplaint::getMerchantId).collect(Collectors.toSet());
        Set<Long> userIds = new HashSet<>();
        userIds.addAll(complaints.stream().map(CanteenComplaint::getUserId).collect(Collectors.toSet()));
        userIds.addAll(complaints.stream().map(CanteenComplaint::getProcessBy).filter(Objects::nonNull).collect(Collectors.toSet()));

        Map<Long, MerchantProfile> merchantMap = buildMerchantMap(merchantIds);
        Map<Long, User> userMap = buildUserMap(userIds);

        List<Map<String, Object>> data = new ArrayList<>();
        for (CanteenComplaint complaint : complaints) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", complaint.getId());
            row.put("complaintNo", complaint.getComplaintNo());
            row.put("userId", complaint.getUserId());
            row.put("merchantId", complaint.getMerchantId());
            row.put("complaintTitle", complaint.getComplaintTitle());
            row.put("complaintContent", complaint.getComplaintContent());
            row.put("evidenceUrls", complaint.getEvidenceUrls());
            row.put("status", complaint.getStatus());
            row.put("processProgress", complaint.getProcessProgress());
            row.put("rectifyRequirement", complaint.getRectifyRequirement());
            row.put("rectifyResult", complaint.getRectifyResult());
            row.put("feedback", complaint.getFeedback());
            row.put("resultRating", complaint.getResultRating());
            row.put("processBy", complaint.getProcessBy());
            row.put("processTime", complaint.getProcessTime());
            row.put("createTime", complaint.getCreateTime());
            row.put("updateTime", complaint.getUpdateTime());

            MerchantProfile merchant = merchantMap.get(complaint.getMerchantId());
            row.put("merchantName", merchant == null ? "" : merchant.getMerchantName());
            User submitUser = userMap.get(complaint.getUserId());
            row.put("studentName", submitUser == null ? "" : submitUser.getUserName());
            User processUser = userMap.get(complaint.getProcessBy());
            row.put("processByName", processUser == null ? "" : processUser.getUserName());
            data.add(row);
        }

        Page<Map<String, Object>> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(data);
        return ResultUtils.success(result);
    }

    @GetMapping("/complaint/detail/{id}")
    public BaseResponse<Map<String, Object>> getComplaintDetail(@PathVariable Long id, HttpServletRequest request) {
        // 允许所有登录用户查看投诉详情（公开透明）
        User loginUser = tryGetLoginUser(request);
        
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "投诉ID不能为空");
        }
        
        CanteenComplaint complaint = canteenComplaintService.getById(id);
        if (complaint == null || complaint.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "投诉不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", complaint.getId());
        result.put("complaintNo", complaint.getComplaintNo());
        result.put("userId", complaint.getUserId());
        result.put("merchantId", complaint.getMerchantId());
        result.put("complaintTitle", complaint.getComplaintTitle());
        result.put("complaintContent", complaint.getComplaintContent());
        result.put("evidenceUrls", complaint.getEvidenceUrls());
        result.put("status", complaint.getStatus());
        result.put("processProgress", complaint.getProcessProgress());
        result.put("rectifyRequirement", complaint.getRectifyRequirement());
        result.put("rectifyResult", complaint.getRectifyResult());
        result.put("feedback", complaint.getFeedback());
        result.put("resultRating", complaint.getResultRating());
        result.put("processBy", complaint.getProcessBy());
        result.put("processTime", complaint.getProcessTime());
        result.put("createTime", complaint.getCreateTime());
        result.put("updateTime", complaint.getUpdateTime());
        
        // 获取商户信息
        MerchantProfile merchant = merchantProfileService.getById(complaint.getMerchantId());
        result.put("merchantName", merchant == null || merchant.getIsDelete() != 0 ? "" : merchant.getMerchantName());
        
        // 获取用户信息
        User submitUser = userService.getById(complaint.getUserId());
        result.put("studentName", submitUser == null ? "" : submitUser.getUserName());
        
        if (complaint.getProcessBy() != null) {
            User processUser = userService.getById(complaint.getProcessBy());
            result.put("processByName", processUser == null ? "" : processUser.getUserName());
        }
        
        return ResultUtils.success(result);
    }

    @PostMapping("/complaint/process")
    public BaseResponse<Boolean> processComplaint(@RequestBody ComplaintProcessRequest processRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        requireSupervisor(loginUser);

        if (processRequest == null || processRequest.getComplaintId() == null || StringUtils.isBlank(processRequest.getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "处理参数不完整");
        }
        String targetStatus = processRequest.getStatus().trim();
        if (!COMPLAINT_STATUS_SET.contains(targetStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "投诉状态错误");
        }

        CanteenComplaint complaint = canteenComplaintService.getById(processRequest.getComplaintId());
        if (complaint == null || complaint.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "投诉不存在");
        }
        validateComplaintProcessTransition(complaint.getStatus(), targetStatus);
        if ("pending_rectify".equals(targetStatus) && StringUtils.isBlank(processRequest.getRectifyRequirement())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "下达整改时必须填写整改要求");
        }

        complaint.setStatus(targetStatus);
        complaint.setProcessProgress(StringUtils.defaultIfBlank(processRequest.getProcessProgress(), processStatusText(targetStatus)));
        complaint.setRectifyRequirement(processRequest.getRectifyRequirement());
        complaint.setRectifyResult(processRequest.getRectifyResult());
        complaint.setFeedback(processRequest.getFeedback());
        complaint.setProcessBy(loginUser.getId());
        complaint.setProcessTime(LocalDateTime.now());
        complaint.setUpdateTime(LocalDateTime.now());
        boolean ok = canteenComplaintService.updateById(complaint);

        if (ok) {
            notificationService.createNotification(
                    complaint.getUserId(),
                    "system",
                    "投诉进度更新",
                    "投诉 " + complaint.getComplaintNo() + " 状态更新为：" + processStatusText(complaint.getStatus()),
                    "complaint",
                    complaint.getId());
            if ("pending_rectify".equals(targetStatus)) {
                notifyMerchant(complaint, "投诉整改通知", "投诉 " + complaint.getComplaintNo() + " 已下发整改要求，请尽快整改");
            }
        }
        return ResultUtils.success(ok);
    }

    @PostMapping("/complaint/rectify/submit")
    public BaseResponse<Boolean> submitRectify(@RequestBody ComplaintRectifyRequest rectifyRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        requireMerchant(loginUser);

        if (rectifyRequest == null || rectifyRequest.getComplaintId() == null || StringUtils.isBlank(rectifyRequest.getRectifyResult())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "整改参数不完整");
        }

        MerchantProfile merchant = getMerchantByUserId(loginUser.getId());
        if (merchant == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "商户资料不存在");
        }

        CanteenComplaint complaint = canteenComplaintService.getById(rectifyRequest.getComplaintId());
        if (complaint == null || complaint.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "投诉不存在");
        }
        if (!Objects.equals(complaint.getMerchantId(), merchant.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权处理该投诉");
        }
        if (!"pending_rectify".equals(complaint.getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前投诉状态不允许提交整改");
        }

        complaint.setRectifyResult(rectifyRequest.getRectifyResult());
        String normalizedEvidenceUrls = normalizeEvidenceUrls(rectifyRequest.getEvidenceUrls());
        if (normalizedEvidenceUrls != null) {
            complaint.setEvidenceUrls(normalizedEvidenceUrls);
        }
        complaint.setStatus("rectified");
        complaint.setProcessProgress(processStatusText("rectified"));
        complaint.setUpdateTime(LocalDateTime.now());
        boolean ok = canteenComplaintService.updateById(complaint);

        if (ok) {
            notifySupervisor(
                    "投诉整改提交",
                    "投诉 " + complaint.getComplaintNo() + " 已提交整改，请审核",
                    "complaint",
                    complaint.getId());
            notificationService.createNotification(
                    complaint.getUserId(),
                    "system",
                    "投诉整改进度更新",
                    "投诉 " + complaint.getComplaintNo() + " 商户已提交整改，等待复核",
                    "complaint",
                    complaint.getId());
        }
        return ResultUtils.success(ok);
    }

    @PostMapping("/complaint/evaluate")
    public BaseResponse<Boolean> evaluateComplaint(@RequestBody ComplaintEvaluateRequest evaluateRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        requireStudent(loginUser);
        if (evaluateRequest == null || evaluateRequest.getComplaintId() == null || evaluateRequest.getResultRating() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评价参数不完整");
        }
        Integer resultRating = evaluateRequest.getResultRating();
        if (resultRating < 1 || resultRating > 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评价分值仅支持 1-3");
        }

        CanteenComplaint complaint = canteenComplaintService.getById(evaluateRequest.getComplaintId());
        if (complaint == null || complaint.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "投诉不存在");
        }
        if (!Objects.equals(complaint.getUserId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能评价自己的投诉");
        }
        if (!"completed".equals(complaint.getStatus()) && !"rejected".equals(complaint.getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "投诉未完成，暂不可评价");
        }
        if (complaint.getResultRating() != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该投诉已评价");
        }

        complaint.setResultRating(resultRating);
        if (StringUtils.isNotBlank(evaluateRequest.getFeedback())) {
            complaint.setFeedback(mergeFeedback(complaint.getFeedback(), evaluateRequest.getFeedback(), resultRating));
        }
        complaint.setUpdateTime(LocalDateTime.now());
        boolean ok = canteenComplaintService.updateById(complaint);
        if (ok) {
            notifySupervisor(
                    "投诉结果评价",
                    "投诉 " + complaint.getComplaintNo() + " 已收到学生处理评价",
                    "complaint",
                    complaint.getId());
            notifyMerchant(complaint, "投诉处理评价", "投诉 " + complaint.getComplaintNo() + " 已收到学生处理评价");
        }
        return ResultUtils.success(ok);
    }

    @GetMapping("/complaint/ranking")
    public BaseResponse<List<Map<String, Object>>> complaintRanking(@RequestParam(defaultValue = "10") int limit) {
        List<MerchantProfile> activeMerchants = merchantProfileService.lambdaQuery()
                .eq(MerchantProfile::getIsDelete, 0)
                .list();
        Set<Long> activeMerchantIds = activeMerchants.stream()
                .map(MerchantProfile::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (activeMerchantIds.isEmpty()) {
            return ResultUtils.success(Collections.emptyList());
        }

        List<CanteenComplaint> complaints = canteenComplaintService.lambdaQuery()
                .eq(CanteenComplaint::getIsDelete, 0)
                .in(CanteenComplaint::getMerchantId, activeMerchantIds)
                .list();
        if (complaints.isEmpty()) {
            return ResultUtils.success(Collections.emptyList());
        }

        Map<Long, Long> countMap = complaints.stream().collect(Collectors.groupingBy(CanteenComplaint::getMerchantId, Collectors.counting()));
        Map<Long, MerchantProfile> merchantMap = buildMerchantMap(countMap.keySet());

        List<Map<String, Object>> ranking = countMap.entrySet().stream()
                .sorted((o1, o2) -> Long.compare(o2.getValue(), o1.getValue()))
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> row = new HashMap<>();
                    MerchantProfile merchant = merchantMap.get(entry.getKey());
                    row.put("merchantId", entry.getKey());
                    row.put("merchantName", merchant == null ? "未知商户" : merchant.getMerchantName());
                    row.put("complaintCount", entry.getValue());
                    return row;
                })
                .collect(Collectors.toList());

        return ResultUtils.success(ranking);
    }

    @GetMapping("/complaint/export")
    public void exportComplaint(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long merchantId,
            HttpServletRequest request,
            HttpServletResponse response) {
        User loginUser = userService.getLoginUser(request);
        requireSupervisor(loginUser);

        List<CanteenComplaint> complaints = canteenComplaintService.lambdaQuery()
                .eq(CanteenComplaint::getIsDelete, 0)
                .eq(StringUtils.isNotBlank(status), CanteenComplaint::getStatus, status)
                .eq(merchantId != null, CanteenComplaint::getMerchantId, merchantId)
                .orderByDesc(CanteenComplaint::getCreateTime)
                .list();

        Set<Long> merchantIds = complaints.stream().map(CanteenComplaint::getMerchantId).collect(Collectors.toSet());
        Set<Long> userIds = complaints.stream().map(CanteenComplaint::getUserId).collect(Collectors.toSet());
        Map<Long, MerchantProfile> merchantMap = buildMerchantMap(merchantIds);
        Map<Long, User> userMap = buildUserMap(userIds);

        StringBuilder csv = new StringBuilder();
        csv.append('\ufeff');
        csv.append("投诉编号,商户,学生,状态,处理进度,整改要求,整改结果,反馈,创建时间,处理时间\n");
        for (CanteenComplaint complaint : complaints) {
            MerchantProfile merchant = merchantMap.get(complaint.getMerchantId());
            User student = userMap.get(complaint.getUserId());
            appendCsvCell(csv, complaint.getComplaintNo());
            appendCsvCell(csv, merchant == null ? "" : merchant.getMerchantName());
            appendCsvCell(csv, student == null ? "" : student.getUserName());
            appendCsvCell(csv, processStatusText(complaint.getStatus()));
            appendCsvCell(csv, complaint.getProcessProgress());
            appendCsvCell(csv, complaint.getRectifyRequirement());
            appendCsvCell(csv, complaint.getRectifyResult());
            appendCsvCell(csv, complaint.getFeedback());
            appendCsvCell(csv, formatDate(complaint.getCreateTime()));
            appendCsvCell(csv, formatDate(complaint.getProcessTime()));
            csv.append('\n');
        }

        String fileName = "complaint_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        try {
            response.getWriter().write(csv.toString());
            response.getWriter().flush();
        } catch (Exception e) {
            log.error("投诉导出失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "投诉导出失败");
        }
    }

    @GetMapping("/complaint/export/excel")
    public void exportComplaintExcel(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long merchantId,
            HttpServletRequest request,
            HttpServletResponse response) {
        User loginUser = userService.getLoginUser(request);
        requireSupervisor(loginUser);

        List<CanteenComplaint> complaints = canteenComplaintService.lambdaQuery()
                .eq(CanteenComplaint::getIsDelete, 0)
                .eq(StringUtils.isNotBlank(status), CanteenComplaint::getStatus, status)
                .eq(merchantId != null, CanteenComplaint::getMerchantId, merchantId)
                .orderByDesc(CanteenComplaint::getCreateTime)
                .list();

        Set<Long> merchantIds = complaints.stream().map(CanteenComplaint::getMerchantId).collect(Collectors.toSet());
        Set<Long> userIds = complaints.stream().map(CanteenComplaint::getUserId).collect(Collectors.toSet());
        Map<Long, MerchantProfile> merchantMap = buildMerchantMap(merchantIds);
        Map<Long, User> userMap = buildUserMap(userIds);

        List<ComplaintExportVO> exportData = complaints.stream().map(complaint -> {
            ComplaintExportVO vo = new ComplaintExportVO();
            vo.setComplaintNo(complaint.getComplaintNo());
            vo.setComplaintTitle(complaint.getComplaintTitle());
            MerchantProfile merchant = merchantMap.get(complaint.getMerchantId());
            vo.setMerchantName(merchant == null ? "" : merchant.getMerchantName());
            User student = userMap.get(complaint.getUserId());
            vo.setStudentName(student == null ? "" : student.getUserName());
            vo.setStatus(processStatusText(complaint.getStatus()));
            vo.setProcessProgress(complaint.getProcessProgress());
            vo.setRectifyRequirement(complaint.getRectifyRequirement());
            vo.setRectifyResult(complaint.getRectifyResult());
            vo.setFeedback(complaint.getFeedback());
            vo.setResultRating(complaint.getResultRating() != null ? 
                    (complaint.getResultRating() == 1 ? "不满意" : complaint.getResultRating() == 2 ? "一般" : "满意") : "");
            vo.setCreateTime(complaint.getCreateTime());
            vo.setProcessTime(complaint.getProcessTime());
            return vo;
        }).collect(Collectors.toList());

        String fileName = "投诉记录_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        try {
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            com.alibaba.excel.EasyExcel.write(response.getOutputStream(), ComplaintExportVO.class)
                    .sheet("投诉记录")
                    .doWrite(exportData);
        } catch (Exception e) {
            log.error("投诉Excel导出失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "投诉导出失败");
        }
    }

    @GetMapping("/complaint/ranking/export")
    public void exportComplaintRanking(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request,
            HttpServletResponse response) {
        User loginUser = userService.getLoginUser(request);
        requireSupervisor(loginUser);

        List<CanteenComplaint> complaints = canteenComplaintService.lambdaQuery()
                .eq(CanteenComplaint::getIsDelete, 0)
                .list();

        if (complaints.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "暂无投诉数据");
        }

        Map<Long, Long> countMap = complaints.stream()
                .collect(Collectors.groupingBy(CanteenComplaint::getMerchantId, Collectors.counting()));
        Map<Long, MerchantProfile> merchantMap = buildMerchantMap(countMap.keySet());

        List<ComplaintRankingExportVO> ranking = countMap.entrySet().stream()
                .sorted((o1, o2) -> Long.compare(o2.getValue(), o1.getValue()))
                .limit(limit)
                .map(entry -> {
                    ComplaintRankingExportVO vo = new ComplaintRankingExportVO();
                    MerchantProfile merchant = merchantMap.get(entry.getKey());
                    vo.setMerchantId(entry.getKey());
                    vo.setMerchantName(merchant == null ? "未知商户" : merchant.getMerchantName());
                    vo.setComplaintCount(entry.getValue().intValue());
                    vo.setContactPerson(merchant == null ? "" : merchant.getContactName());
                    vo.setContactPhone(merchant == null ? "" : merchant.getContactPhone());
                    return vo;
                })
                .collect(Collectors.toList());

        // 设置排名
        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setRank(i + 1);
        }

        String fileName = "投诉排行榜_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        try {
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            com.alibaba.excel.EasyExcel.write(response.getOutputStream(), ComplaintRankingExportVO.class)
                    .sheet("投诉排行榜")
                    .doWrite(ranking);
        } catch (Exception e) {
            log.error("投诉排行榜导出失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "投诉排行榜导出失败");
        }
    }



    @GetMapping("/dynamic/list")
    public BaseResponse<Page<CanteenDynamic>> listDynamic(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String newsType,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "true") boolean onlyPublished) {
        Page<CanteenDynamic> page = canteenDynamicService.lambdaQuery()
                .eq(CanteenDynamic::getIsDelete, 0)
                .eq(StringUtils.isNotBlank(newsType), CanteenDynamic::getNewsType, newsType)
                .eq(merchantId != null, CanteenDynamic::getMerchantId, merchantId)
                .eq(onlyPublished, CanteenDynamic::getStatus, "published")
                .eq(onlyPublished, CanteenDynamic::getAuditStatus, "approved")
                .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper
                        .like(CanteenDynamic::getTitle, keyword)
                        .or().like(CanteenDynamic::getContent, keyword))
                .orderByDesc(CanteenDynamic::getPublishTime)
                .orderByDesc(CanteenDynamic::getCreateTime)
                .page(new Page<>(current, size));
        Map<Long, MerchantProfile> merchantMap = buildMerchantMap(page.getRecords().stream()
                .map(CanteenDynamic::getMerchantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        page.getRecords().forEach(item -> {
            MerchantProfile merchant = merchantMap.get(item.getMerchantId());
            item.setMerchantName(merchant == null ? "" : merchant.getMerchantName());
        });
        return ResultUtils.success(page);
    }

    @GetMapping("/dynamic/my/list")
    public BaseResponse<Page<CanteenDynamic>> listMyDynamic(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        Long merchantId = null;
        if (isMerchant(loginUser)) {
            MerchantProfile merchant = getMerchantByUserId(loginUser.getId());
            if (merchant != null) {
                merchantId = merchant.getId();
            }
        } else if (!isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        Page<CanteenDynamic> page = canteenDynamicService.lambdaQuery()
                .eq(CanteenDynamic::getIsDelete, 0)
                .eq(merchantId != null, CanteenDynamic::getMerchantId, merchantId)
                .eq(CanteenDynamic::getPublisherId, loginUser.getId())
                .orderByDesc(CanteenDynamic::getCreateTime)
                .page(new Page<>(current, size));
        return ResultUtils.success(page);
    }

    @PostMapping("/dynamic/save")
    public BaseResponse<Long> saveDynamic(@RequestBody CanteenDynamic dynamic, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (dynamic == null || StringUtils.isBlank(dynamic.getTitle()) || StringUtils.isBlank(dynamic.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "动态参数不完整");
        }

        dynamic.setId(null);
        dynamic.setPublisherId(loginUser.getId());
        dynamic.setCreateTime(LocalDateTime.now());
        dynamic.setUpdateTime(LocalDateTime.now());
        dynamic.setIsDelete(0);
        dynamic.setLikeCount(dynamic.getLikeCount() == null ? 0 : dynamic.getLikeCount());
        dynamic.setCommentCount(dynamic.getCommentCount() == null ? 0 : dynamic.getCommentCount());
        dynamic.setShareCount(dynamic.getShareCount() == null ? 0 : dynamic.getShareCount());

        if (isAdmin(loginUser)) {
            dynamic.setPublisherType("admin");
            if (dynamic.getMerchantId() != null) {
                MerchantProfile merchant = merchantProfileService.getById(dynamic.getMerchantId());
                if (merchant == null || merchant.getIsDelete() != 0) {
                    throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商户不存在");
                }
            }
            dynamic.setAuditStatus(StringUtils.defaultIfBlank(dynamic.getAuditStatus(), "approved"));
            dynamic.setStatus(StringUtils.defaultIfBlank(dynamic.getStatus(), "published"));
            LocalDateTime publishTime = dynamic.getPublishTime() == null ? LocalDateTime.now() : dynamic.getPublishTime();
            dynamic.setPublishTime(publishTime);
            if (dynamic.getExpireTime() != null && dynamic.getExpireTime().isBefore(publishTime)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "截止时间不能早于发布时间");
            }
        } else if (isMerchant(loginUser)) {
            MerchantProfile merchant = getMerchantByUserId(loginUser.getId());
            if (merchant == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "未绑定商户资料");
            }
            dynamic.setPublisherType("merchant");
            dynamic.setMerchantId(merchant.getId());
            dynamic.setAuditStatus("pending");
            dynamic.setStatus("draft");
        } else {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        boolean saved = canteenDynamicService.save(dynamic);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "动态保存失败");
        }
        return ResultUtils.success(dynamic.getId());
    }

    @PostMapping("/dynamic/update")
    public BaseResponse<Boolean> updateDynamic(@RequestBody CanteenDynamic dynamic, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (dynamic == null || dynamic.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        CanteenDynamic existed = canteenDynamicService.getById(dynamic.getId());
        if (existed == null || existed.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (!isAdmin(loginUser) && !Objects.equals(existed.getPublisherId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        dynamic.setUpdateTime(LocalDateTime.now());
        if (isMerchant(loginUser)) {
            dynamic.setAuditStatus("pending");
            dynamic.setStatus("draft");
            dynamic.setPublishTime(null);
        } else if (dynamic.getPublishTime() != null && dynamic.getExpireTime() != null && dynamic.getExpireTime().isBefore(dynamic.getPublishTime())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "截止时间不能早于发布时间");
        }
        return ResultUtils.success(canteenDynamicService.updateById(dynamic));
    }

    @PostMapping("/dynamic/like/toggle/{id}")
    public BaseResponse<Boolean> toggleDynamicLike(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        CanteenDynamic dynamic = getPublishedDynamicOrThrow(id);
        CanteenDynamicLike existed = canteenDynamicLikeService.lambdaQuery()
                .eq(CanteenDynamicLike::getDynamicId, id)
                .eq(CanteenDynamicLike::getUserId, loginUser.getId())
                .orderByDesc(CanteenDynamicLike::getId)
                .last("limit 1")
                .one();
        if (existed == null) {
            CanteenDynamicLike like = new CanteenDynamicLike();
            like.setDynamicId(id);
            like.setUserId(loginUser.getId());
            like.setCreateTime(LocalDateTime.now());
            like.setIsDelete(0);
            canteenDynamicLikeService.save(like);
            dynamic.setLikeCount(Math.max(0, nvl(dynamic.getLikeCount())) + 1);
        } else if (existed.getIsDelete() != null && existed.getIsDelete() == 1) {
            existed.setIsDelete(0);
            canteenDynamicLikeService.updateById(existed);
            dynamic.setLikeCount(Math.max(0, nvl(dynamic.getLikeCount())) + 1);
        } else {
            existed.setIsDelete(1);
            canteenDynamicLikeService.updateById(existed);
            dynamic.setLikeCount(Math.max(0, nvl(dynamic.getLikeCount()) - 1));
        }
        dynamic.setUpdateTime(LocalDateTime.now());
        canteenDynamicService.updateById(dynamic);
        return ResultUtils.success(true);
    }

    @GetMapping("/dynamic/comment/list")
    public BaseResponse<Page<Map<String, Object>>> listDynamicComment(
            @RequestParam Long dynamicId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        if (dynamicId == null || dynamicId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "动态ID不能为空");
        }
        CanteenDynamic dynamic = canteenDynamicService.getById(dynamicId);
        if (dynamic == null || dynamic.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "动态不存在");
        }
        Page<CanteenDynamicComment> page = canteenDynamicCommentService.lambdaQuery()
                .eq(CanteenDynamicComment::getDynamicId, dynamicId)
                .eq(CanteenDynamicComment::getIsDelete, 0)
                .orderByDesc(CanteenDynamicComment::getCreateTime)
                .page(new Page<>(current, size));

        List<CanteenDynamicComment> comments = page.getRecords();
        Map<Long, User> userMap = buildUserMap(comments.stream().map(CanteenDynamicComment::getUserId).collect(Collectors.toSet()));
        List<Map<String, Object>> data = new ArrayList<>();
        for (CanteenDynamicComment comment : comments) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", comment.getId());
            row.put("dynamicId", comment.getDynamicId());
            row.put("userId", comment.getUserId());
            row.put("content", comment.getContent());
            row.put("parentId", comment.getParentId());
            row.put("createTime", comment.getCreateTime());
            User user = userMap.get(comment.getUserId());
            row.put("userName", user == null ? "" : user.getUserName());
            row.put("userAvatar", user == null ? "" : user.getUserAvatar());
            data.add(row);
        }
        Page<Map<String, Object>> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(data);
        return ResultUtils.success(result);
    }

    @PostMapping("/dynamic/comment/save")
    public BaseResponse<Long> saveDynamicComment(@RequestBody DynamicCommentCreateRequest commentRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (commentRequest == null || commentRequest.getDynamicId() == null || StringUtils.isBlank(commentRequest.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论参数不完整");
        }
        String content = commentRequest.getContent().trim();
        if (content.length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容不能超过500字符");
        }

        CanteenDynamic dynamic = getPublishedDynamicOrThrow(commentRequest.getDynamicId());
        CanteenDynamicComment comment = new CanteenDynamicComment();
        comment.setDynamicId(commentRequest.getDynamicId());
        comment.setUserId(loginUser.getId());
        comment.setContent(content);
        comment.setParentId(commentRequest.getParentId());
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        comment.setIsDelete(0);
        boolean saved = canteenDynamicCommentService.save(comment);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "评论失败");
        }
        dynamic.setCommentCount(Math.max(0, nvl(dynamic.getCommentCount())) + 1);
        dynamic.setUpdateTime(LocalDateTime.now());
        canteenDynamicService.updateById(dynamic);
        return ResultUtils.success(comment.getId());
    }

    @PostMapping("/dynamic/comment/delete/{id}")
    public BaseResponse<Boolean> deleteDynamicComment(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        CanteenDynamicComment comment = canteenDynamicCommentService.getById(id);
        if (comment == null || comment.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }
        CanteenDynamic dynamic = canteenDynamicService.getById(comment.getDynamicId());
        if (dynamic == null || dynamic.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "动态不存在");
        }
        boolean owner = Objects.equals(comment.getUserId(), loginUser.getId());
        boolean admin = isAdmin(loginUser);
        boolean publisher = Objects.equals(dynamic.getPublisherId(), loginUser.getId());
        if (!owner && !admin && !publisher) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权删除该评论");
        }
        comment.setIsDelete(1);
        comment.setUpdateTime(LocalDateTime.now());
        boolean ok = canteenDynamicCommentService.updateById(comment);
        if (ok) {
            dynamic.setCommentCount(Math.max(0, nvl(dynamic.getCommentCount()) - 1));
            dynamic.setUpdateTime(LocalDateTime.now());
            canteenDynamicService.updateById(dynamic);
        }
        return ResultUtils.success(ok);
    }

    @PostMapping("/dynamic/share/{id}")
    public BaseResponse<Boolean> shareDynamic(@PathVariable Long id, @RequestBody(required = false) DynamicShareRequest shareRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        CanteenDynamic dynamic = getPublishedDynamicOrThrow(id);
        CanteenDynamicShare share = new CanteenDynamicShare();
        share.setDynamicId(id);
        share.setUserId(loginUser.getId());
        share.setShareChannel(shareRequest == null ? null : StringUtils.substring(shareRequest.getShareChannel(), 0, 32));
        share.setCreateTime(LocalDateTime.now());
        share.setIsDelete(0);
        boolean ok = canteenDynamicShareService.save(share);
        if (!ok) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "分享记录失败");
        }
        dynamic.setShareCount(Math.max(0, nvl(dynamic.getShareCount())) + 1);
        dynamic.setUpdateTime(LocalDateTime.now());
        canteenDynamicService.updateById(dynamic);
        return ResultUtils.success(true);
    }

    @PostMapping("/dynamic/audit/{id}")
    public BaseResponse<Boolean> auditDynamic(
            @PathVariable Long id,
            @RequestBody DynamicAuditRequest auditRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        requireSupervisor(loginUser);

        if (auditRequest == null || StringUtils.isBlank(auditRequest.getAuditStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "审核状态不能为空");
        }

        CanteenDynamic dynamic = canteenDynamicService.getById(id);
        if (dynamic == null || dynamic.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        if (!"approved".equals(auditRequest.getAuditStatus()) && !"rejected".equals(auditRequest.getAuditStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "审核状态错误");
        }

        // 驳回时必须填写审核意见
        if ("rejected".equals(auditRequest.getAuditStatus()) && StringUtils.isBlank(auditRequest.getAuditReason())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "驳回时必须填写审核意见");
        }

        dynamic.setAuditStatus(auditRequest.getAuditStatus());
        dynamic.setStatus("approved".equals(auditRequest.getAuditStatus()) ? "published" : "draft");
        dynamic.setPublishTime("approved".equals(auditRequest.getAuditStatus()) ? LocalDateTime.now() : null);
        dynamic.setAuditReason(auditRequest.getAuditReason());
        dynamic.setUpdateTime(LocalDateTime.now());

        boolean ok = canteenDynamicService.updateById(dynamic);
        if (ok && dynamic.getPublisherId() != null) {
            notificationService.createNotification(
                    dynamic.getPublisherId(),
                    "system",
                    "动态审核结果",
                    "你的动态《" + dynamic.getTitle() + "》审核结果：" + ("approved".equals(auditRequest.getAuditStatus()) ? "通过" : "驳回")
                    + (StringUtils.isNotBlank(auditRequest.getAuditReason()) ? "，审核意见：" + auditRequest.getAuditReason() : ""),
                    "dynamic",
                    dynamic.getId());
        }
        return ResultUtils.success(ok);
    }

    @PostMapping("/dynamic/delete/{id}")
    public BaseResponse<Boolean> deleteDynamic(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        CanteenDynamic dynamic = canteenDynamicService.getById(id);
        if (dynamic == null || dynamic.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (!isAdmin(loginUser) && !Objects.equals(dynamic.getPublisherId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        dynamic.setIsDelete(1);
        dynamic.setUpdateTime(LocalDateTime.now());
        return ResultUtils.success(canteenDynamicService.updateById(dynamic));
    }

    @GetMapping("/announcement/list")
    public BaseResponse<Page<CanteenAnnouncement>> listAnnouncement(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String announcementType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "true") boolean onlyPublished) {
        Page<CanteenAnnouncement> page = canteenAnnouncementService.lambdaQuery()
                .eq(CanteenAnnouncement::getIsDelete, 0)
                .eq(StringUtils.isNotBlank(announcementType), CanteenAnnouncement::getAnnouncementType, announcementType)
                .eq(onlyPublished, CanteenAnnouncement::getStatus, "published")
                .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper
                        .like(CanteenAnnouncement::getTitle, keyword)
                        .or().like(CanteenAnnouncement::getContent, keyword))
                .orderByDesc(CanteenAnnouncement::getIsTop)
                .orderByDesc(CanteenAnnouncement::getSortNo)
                .orderByDesc(CanteenAnnouncement::getPublishTime)
                .orderByDesc(CanteenAnnouncement::getCreateTime)
                .page(new Page<>(current, size));
        return ResultUtils.success(page);
    }

    @PostMapping("/announcement/save")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> saveAnnouncement(@RequestBody CanteenAnnouncement announcement) {
        if (announcement == null || StringUtils.isBlank(announcement.getTitle()) || StringUtils.isBlank(announcement.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告参数不完整");
        }
        announcement.setId(null);
        announcement.setIsTop(announcement.getIsTop() == null ? 0 : announcement.getIsTop());
        announcement.setSortNo(announcement.getSortNo() == null ? 0 : announcement.getSortNo());
        announcement.setStatus(StringUtils.defaultIfBlank(announcement.getStatus(), "published"));
        announcement.setPublishTime("published".equals(announcement.getStatus()) ? LocalDateTime.now() : announcement.getPublishTime());
        announcement.setCreateTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());
        announcement.setIsDelete(0);
        boolean saved = canteenAnnouncementService.save(announcement);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "公告保存失败");
        }
        return ResultUtils.success(announcement.getId());
    }

    @PostMapping("/announcement/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAnnouncement(@RequestBody CanteenAnnouncement announcement) {
        if (announcement == null || announcement.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        announcement.setUpdateTime(LocalDateTime.now());
        if ("published".equals(announcement.getStatus()) && announcement.getPublishTime() == null) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        return ResultUtils.success(canteenAnnouncementService.updateById(announcement));
    }

    @PostMapping("/announcement/delete/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAnnouncement(@PathVariable Long id) {
        CanteenAnnouncement announcement = canteenAnnouncementService.getById(id);
        if (announcement == null || announcement.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        announcement.setIsDelete(1);
        announcement.setUpdateTime(LocalDateTime.now());
        return ResultUtils.success(canteenAnnouncementService.updateById(announcement));
    }

    @PostMapping("/announcement/pin/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> pinAnnouncement(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean top) {
        CanteenAnnouncement announcement = canteenAnnouncementService.getById(id);
        if (announcement == null || announcement.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        announcement.setIsTop(top ? 1 : 0);
        announcement.setUpdateTime(LocalDateTime.now());
        return ResultUtils.success(canteenAnnouncementService.updateById(announcement));
    }

    @PostMapping("/announcement/publish/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> publishAnnouncement(@PathVariable Long id, @RequestParam String status) {
        CanteenAnnouncement announcement = canteenAnnouncementService.getById(id);
        if (announcement == null || announcement.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (!"published".equals(status) && !"unpublished".equals(status)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "发布状态错误");
        }
        announcement.setStatus(status);
        announcement.setPublishTime("published".equals(status) ? LocalDateTime.now() : announcement.getPublishTime());
        announcement.setUpdateTime(LocalDateTime.now());
        return ResultUtils.success(canteenAnnouncementService.updateById(announcement));
    }

    @PostMapping("/announcement/withdraw/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> withdrawAnnouncement(@PathVariable Long id) {
        CanteenAnnouncement announcement = canteenAnnouncementService.getById(id);
        if (announcement == null || announcement.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        announcement.setStatus("withdrawn");
        announcement.setUpdateTime(LocalDateTime.now());
        return ResultUtils.success(canteenAnnouncementService.updateById(announcement));
    }

    @PostMapping("/dynamic/withdraw/{id}")
    public BaseResponse<Boolean> withdrawDynamic(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "动态ID不能为空");
        }
        CanteenDynamic dynamic = canteenDynamicService.getById(id);
        if (dynamic == null || dynamic.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "动态不存在");
        }
        // 只有发布者或管理员可以撤回
        if (!isAdmin(loginUser) && !Objects.equals(dynamic.getPublisherId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权撤回该动态");
        }
        // 只有已发布的动态可以撤回
        if (!"published".equals(dynamic.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只有已发布的动态可以撤回");
        }
        dynamic.setStatus("unpublished");
        dynamic.setUpdateTime(LocalDateTime.now());
        return ResultUtils.success(canteenDynamicService.updateById(dynamic));
    }

    private String processStatusText(String status) {
        if ("pending_review".equals(status)) {
            return "待监督员处理";
        }
        if ("pending_rectify".equals(status)) {
            return "已通知商户整改";
        }
        if ("rectified".equals(status)) {
            return "商户已提交整改结果，请监督员确认是否通过";
        }
        if ("completed".equals(status)) {
            return "投诉已处理完成";
        }
        if ("rejected".equals(status)) {
            return "投诉已驳回";
        }
        return "处理中";
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "" : value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private void appendCsvCell(StringBuilder csv, String value) {
        String text = StringUtils.defaultString(value).replace("\"", "\"\"");
        csv.append('"').append(text).append('"').append(',');
    }

    private void validateComplaintProcessTransition(String currentStatus, String targetStatus) {
        String current = StringUtils.defaultIfBlank(currentStatus, "pending_review");
        if ("completed".equals(current) || "rejected".equals(current)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "投诉已完结，不可重复处理");
        }
        if ("pending_review".equals(current)
                && ("pending_rectify".equals(targetStatus) || "completed".equals(targetStatus) || "rejected".equals(targetStatus))) {
            return;
        }
        if ("pending_rectify".equals(current)
                && ("rectified".equals(targetStatus) || "completed".equals(targetStatus) || "rejected".equals(targetStatus))) {
            return;
        }
        if ("rectified".equals(current)
                && ("pending_rectify".equals(targetStatus) || "completed".equals(targetStatus) || "rejected".equals(targetStatus))) {
            return;
        }
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "投诉状态流转不合法");
    }

    private void notifyMerchant(CanteenComplaint complaint, String title, String content) {
        if (complaint == null || complaint.getMerchantId() == null) {
            return;
        }
        MerchantProfile merchantProfile = merchantProfileService.getById(complaint.getMerchantId());
        if (merchantProfile == null || merchantProfile.getUserId() == null) {
            return;
        }
        notificationService.createNotification(
                merchantProfile.getUserId(),
                "system",
                title,
                content,
                "complaint",
                complaint.getId());
    }

    private String mergeFeedback(String oldFeedback, String studentFeedback, Integer resultRating) {
        String ratingText;
        if (resultRating == 1) {
            ratingText = "不满意";
        } else if (resultRating == 2) {
            ratingText = "一般";
        } else {
            ratingText = "满意";
        }
        String newLine = "学生评价(" + ratingText + ")：" + studentFeedback.trim();
        if (StringUtils.isBlank(oldFeedback)) {
            return newLine;
        }
        return oldFeedback + "\n" + newLine;
    }

    private String buildPendingMerchantData(MerchantProfile requestBody) {
        JSONObject payload = new JSONObject();
        if (requestBody != null) {
            if (StringUtils.isNotBlank(requestBody.getMerchantName())) {
                payload.put("merchantName", requestBody.getMerchantName().trim());
            }
            if (StringUtils.isNotBlank(requestBody.getContactName())) {
                payload.put("contactName", requestBody.getContactName().trim());
            }
            if (StringUtils.isNotBlank(requestBody.getContactPhone())) {
                payload.put("contactPhone", requestBody.getContactPhone().trim());
            }
            if (StringUtils.isNotBlank(requestBody.getAvatar())) {
                payload.put("avatar", requestBody.getAvatar().trim());
            }
            if (StringUtils.isNotBlank(requestBody.getBusinessHours())) {
                payload.put("businessHours", requestBody.getBusinessHours().trim());
            }
            if (StringUtils.isNotBlank(requestBody.getLocation())) {
                payload.put("location", requestBody.getLocation().trim());
            }
            if (StringUtils.isNotBlank(requestBody.getDescription())) {
                payload.put("description", requestBody.getDescription().trim());
            }
        }
        return payload.isEmpty() ? null : payload.toJSONString();
    }

    private void applyPendingMerchantData(MerchantProfile merchantProfile) {
        if (merchantProfile == null || StringUtils.isBlank(merchantProfile.getPendingData())) {
            return;
        }
        JSONObject payload = JSON.parseObject(merchantProfile.getPendingData());
        if (payload == null || payload.isEmpty()) {
            merchantProfile.setPendingData(null);
            return;
        }
        if (StringUtils.isNotBlank(payload.getString("merchantName"))) {
            merchantProfile.setMerchantName(payload.getString("merchantName"));
        }
        if (StringUtils.isNotBlank(payload.getString("contactName"))) {
            merchantProfile.setContactName(payload.getString("contactName"));
        }
        if (StringUtils.isNotBlank(payload.getString("contactPhone"))) {
            merchantProfile.setContactPhone(payload.getString("contactPhone"));
        }
        if (StringUtils.isNotBlank(payload.getString("avatar"))) {
            merchantProfile.setAvatar(payload.getString("avatar"));
        }
        if (StringUtils.isNotBlank(payload.getString("businessHours"))) {
            merchantProfile.setBusinessHours(payload.getString("businessHours"));
        }
        if (StringUtils.isNotBlank(payload.getString("location"))) {
            merchantProfile.setLocation(payload.getString("location"));
        }
        if (StringUtils.isNotBlank(payload.getString("description"))) {
            merchantProfile.setDescription(payload.getString("description"));
        }
        merchantProfile.setPendingData(null);
    }

    private MerchantProfile getMerchantByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return merchantProfileService.lambdaQuery()
                .eq(MerchantProfile::getUserId, userId)
                .eq(MerchantProfile::getIsDelete, 0)
                .orderByDesc(MerchantProfile::getUpdateTime)
                .orderByDesc(MerchantProfile::getId)
                .last("limit 1")
                .one();
    }

    private Dish getDishOrThrow(Long dishId) {
        Dish dish = dishService.getById(dishId);
        if (dish == null || dish.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "菜品不存在");
        }
        return dish;
    }

    private CanteenDynamic getPublishedDynamicOrThrow(Long dynamicId) {
        CanteenDynamic dynamic = canteenDynamicService.getById(dynamicId);
        if (dynamic == null || dynamic.getIsDelete() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "动态不存在");
        }
        if (!"published".equals(dynamic.getStatus()) || !"approved".equals(dynamic.getAuditStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "动态未发布，暂不可互动");
        }
        return dynamic;
    }

    private int getDishScore(Dish dish) {
        return nvl(dish.getLikeCount()) * 2 + nvl(dish.getFavoriteCount()) * 3;
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private int calculateRecommendScore(Dish dish, Set<String> preferredCategories,
                                        Set<Long> preferredMerchantIds, Set<Long> likedDishIds,
                                        Set<Long> favoriteDishIds, LocalDateTime now, User currentUser) {
        int score = 0;

        // 基础分数：点赞和收藏权重
        score += nvl(dish.getLikeCount()) * 2;
        score += nvl(dish.getFavoriteCount()) * 3;

        // 个性化加分
        if (currentUser != null) {
            // 用户偏好分类匹配 +20分
            if (StringUtils.isNotBlank(dish.getCategory()) && preferredCategories.contains(dish.getCategory())) {
                score += 20;
            }

            // 用户偏好商户匹配 +15分
            if (dish.getMerchantId() != null && preferredMerchantIds.contains(dish.getMerchantId())) {
                score += 15;
            }
        }

        // 特价菜品加分 +25分
        if (isSpecialDish(dish, now)) {
            score += 25;
        }

        // 新品加分（7天内创建）+10分
        if (dish.getCreateTime() != null) {
            long days = java.time.Duration.between(dish.getCreateTime(), now).toDays();
            if (days <= 7) {
                score += 10;
            }
        }

        return score;
    }

    private boolean isSpecialDish(Dish dish, LocalDateTime now) {
        if (dish.getSpecialPrice() == null || dish.getSpecialPrice().signum() <= 0) {
            return false;
        }
        if (dish.getSpecialStartTime() == null || dish.getSpecialEndTime() == null) {
            return false;
        }
        return !now.isBefore(dish.getSpecialStartTime()) && !now.isAfter(dish.getSpecialEndTime());
    }

    private void fillDishByRole(Dish dish, User loginUser, boolean isCreate) {
        if (isAdmin(loginUser)) {
            if (isCreate && dish.getMerchantId() == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "管理员新增菜品必须指定商户");
            }
            return;
        }

        requireMerchant(loginUser);
        MerchantProfile merchant = getMerchantByUserId(loginUser.getId());
        if (merchant == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "未绑定商户资料");
        }
        dish.setMerchantId(merchant.getId());
    }

    private void checkDishOwnership(Dish dish, User loginUser) {
        if (isAdmin(loginUser)) {
            return;
        }
        requireMerchant(loginUser);
        MerchantProfile merchant = getMerchantByUserId(loginUser.getId());
        if (merchant == null || !Objects.equals(merchant.getId(), dish.getMerchantId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "不能操作其他商户菜品");
        }
    }

    private Map<Long, MerchantProfile> buildMerchantMap(Collection<Long> merchantIds) {
        if (merchantIds == null || merchantIds.isEmpty()) {
            return new HashMap<>();
        }
        return merchantProfileService.lambdaQuery().in(MerchantProfile::getId, merchantIds)
                .eq(MerchantProfile::getIsDelete, 0)
                .list()
                .stream()
                .collect(Collectors.toMap(MerchantProfile::getId, item -> item, (o1, o2) -> o1));
    }

    private Page<Map<String, Object>> buildComplaintPage(long current, long size, List<CanteenComplaint> complaintList,
                                                         Map<Long, MerchantProfile> merchantMap) {
        if (complaintList == null) {
            complaintList = Collections.emptyList();
        }
        List<CanteenComplaint> filtered = complaintList.stream()
                .filter(item -> item.getMerchantId() != null && merchantMap.containsKey(item.getMerchantId()))
                .sorted(Comparator.comparing(CanteenComplaint::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        Set<Long> userIds = new HashSet<>();
        userIds.addAll(filtered.stream().map(CanteenComplaint::getUserId).filter(Objects::nonNull).collect(Collectors.toSet()));
        userIds.addAll(filtered.stream().map(CanteenComplaint::getProcessBy).filter(Objects::nonNull).collect(Collectors.toSet()));
        Map<Long, User> userMap = buildUserMap(userIds);

        int start = (int) Math.max((current - 1) * size, 0);
        int end = (int) Math.min(start + size, filtered.size());
        List<CanteenComplaint> pageItems = start >= filtered.size() ? Collections.emptyList() : filtered.subList(start, end);

        List<Map<String, Object>> data = new ArrayList<>();
        for (CanteenComplaint complaint : pageItems) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", complaint.getId());
            row.put("complaintNo", complaint.getComplaintNo());
            row.put("userId", complaint.getUserId());
            row.put("merchantId", complaint.getMerchantId());
            row.put("complaintTitle", complaint.getComplaintTitle());
            row.put("complaintContent", complaint.getComplaintContent());
            row.put("evidenceUrls", complaint.getEvidenceUrls());
            row.put("status", complaint.getStatus());
            row.put("processProgress", complaint.getProcessProgress());
            row.put("rectifyRequirement", complaint.getRectifyRequirement());
            row.put("rectifyResult", complaint.getRectifyResult());
            row.put("feedback", complaint.getFeedback());
            row.put("resultRating", complaint.getResultRating());
            row.put("processBy", complaint.getProcessBy());
            row.put("processTime", complaint.getProcessTime());
            row.put("createTime", complaint.getCreateTime());
            row.put("updateTime", complaint.getUpdateTime());

            MerchantProfile merchant = merchantMap.get(complaint.getMerchantId());
            row.put("merchantName", merchant == null ? "" : merchant.getMerchantName());
            User submitUser = userMap.get(complaint.getUserId());
            row.put("studentName", submitUser == null ? "" : submitUser.getUserName());
            row.put("studentAccount", submitUser == null ? "" : submitUser.getUserAccount());
            User processUser = userMap.get(complaint.getProcessBy());
            row.put("processByName", processUser == null ? "" : processUser.getUserName());
            data.add(row);
        }

        Page<Map<String, Object>> result = new Page<>(current, size, filtered.size());
        result.setRecords(data);
        return result;
    }

    private Map<Long, User> buildUserMap(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }
        return userService.lambdaQuery().in(User::getId, userIds).eq(User::getIsDelete, 0).list().stream()
                .collect(Collectors.toMap(User::getId, item -> item, (o1, o2) -> o1));
    }

    private User tryGetLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        try {
            return userService.getLoginUser(request);
        } catch (Exception e) {
            return null;
        }
    }

    private void requireStudent(User user) {
        if (user == null || !STUDENT_ROLES.contains(normalizeRole(user.getUserRole()))) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅学生可操作");
        }
    }

    private void requireSupervisor(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅监督管理员可操作");
        }
        String role = normalizeRole(user.getUserRole());
        if (!SUPERVISOR_ROLES.contains(role)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅监督管理员可操作");
        }
    }

    private void requireMerchant(User user) {
        if (user == null || !MERCHANT_ROLES.contains(normalizeRole(user.getUserRole()))) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅商户可操作");
        }
    }

    private boolean isAdmin(User user) {
        return user != null && UserConstant.ADMIN_ROLE.equals(normalizeRole(user.getUserRole()));
    }

    private boolean isMerchant(User user) {
        return user != null && MERCHANT_ROLES.contains(normalizeRole(user.getUserRole()));
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        String value = role.trim().toLowerCase();
        if ("user".equals(value)) {
            return "student";
        }
        return value;
    }

    private String generateComplaintNo() {
        return "CP" + LocalDateTime.now().format(COMPLAINT_NO_FMT)
                + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private String normalizeEvidenceUrls(String raw) {
        if (raw == null) {
            return null;
        }
        String source = raw.trim();
        if (source.isEmpty()) {
            return null;
        }

        List<String> values = new ArrayList<>();
        if (source.startsWith("[") && source.endsWith("]")) {
            try {
                List<String> parsed = JSON.parseArray(source, String.class);
                if (parsed != null) {
                    values.addAll(parsed);
                }
            } catch (Exception e) {
                log.warn("Failed to parse complaint evidence urls as json array: {}", source, e);
            }
        }
        if (values.isEmpty()) {
            String[] items = source.split(",");
            Collections.addAll(values, items);
        }

        List<String> normalized = values.stream()
                .map(item -> item == null ? "" : item.trim())
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (normalized.isEmpty()) {
            return null;
        }
        return JSON.toJSONString(normalized);
    }

    private void notifySupervisor(String title, String content, String relatedType, Long relatedId) {
        List<User> supervisors = userService.lambdaQuery()
                .in(User::getUserRole, "supervisor", "admin")
                .eq(User::getStatus, 1)
                .eq(User::getIsDelete, 0)
                .list();
        for (User supervisor : supervisors) {
            notificationService.createNotification(supervisor.getId(), "system", title, content, relatedType, relatedId);
        }
    }
}
