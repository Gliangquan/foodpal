package com.jcen.medpal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.annotation.AuthCheck;
import com.jcen.medpal.common.BaseResponse;
import com.jcen.medpal.common.DeleteRequest;
import com.jcen.medpal.common.ErrorCode;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.constant.UserConstant;
import com.jcen.medpal.exception.BusinessException;
import com.jcen.medpal.exception.ThrowUtils;
import com.jcen.medpal.model.dto.user.UserAddRequest;
import com.jcen.medpal.model.dto.user.UserAdminPasswordResetRequest;
import com.jcen.medpal.model.dto.user.UserBatchDeleteRequest;
import com.jcen.medpal.model.dto.user.UserBatchUpdateRequest;
import com.jcen.medpal.model.dto.user.UserExportRequest;
import com.jcen.medpal.model.dto.user.UserImportRequest;
import com.jcen.medpal.model.dto.user.UserLoginRequest;
import com.jcen.medpal.model.dto.user.UserPasswordResetRequest;
import com.jcen.medpal.model.dto.user.UserQueryRequest;
import com.jcen.medpal.model.dto.user.UserRegisterRequest;
import com.jcen.medpal.model.dto.user.UserUpdateMyRequest;
import com.jcen.medpal.model.dto.user.UserUpdateRequest;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.model.entity.food.MerchantProfile;
import com.jcen.medpal.model.vo.LoginUserVO;
import com.jcen.medpal.model.vo.UserStatisticsVO;
import com.jcen.medpal.model.vo.UserVO;
import com.jcen.medpal.service.UserService;
import com.jcen.medpal.service.MerchantProfileService;

import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.jcen.medpal.service.impl.UserServiceImpl.SALT;

/**
 * 用户接口
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Resource
    private UserService userService;

    @Resource
    private MerchantProfileService merchantProfileService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userPhone = userRegisterRequest.getUserPhone();
        String userRole = userRegisterRequest.getUserRole();
        String userName = userRegisterRequest.getUserName();
        String userEmail = userRegisterRequest.getUserEmail();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userPhone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validateUserPhoneAndEmail(userPhone, userEmail);
        ensurePhoneAndEmailUnique(userPhone, userEmail, null);

        long result = userService.userRegister(userAccount, userPassword, checkPassword, userPhone, userRole, userName, userEmail);
        if ("merchant".equalsIgnoreCase(userRole)) {
            MerchantProfile existedProfile = merchantProfileService.lambdaQuery()
                    .eq(MerchantProfile::getUserId, result)
                    .last("limit 1")
                    .one();
            if (existedProfile == null) {
                MerchantProfile profile = new MerchantProfile();
                profile.setUserId(result);
                profile.setMerchantName(StringUtils.defaultIfBlank(userName, userAccount));
                profile.setContactName(StringUtils.defaultIfBlank(userName, userAccount));
                profile.setContactPhone(userPhone);
                profile.setStatus(1);
                profile.setAuditStatus("approved");
                profile.setCreateTime(java.time.LocalDateTime.now());
                profile.setUpdateTime(java.time.LocalDateTime.now());
                profile.setIsDelete(0);
                merchantProfileService.save(profile);
            }
        }
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String loginType = userLoginRequest.getLoginType();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isBlank(loginType) || StringUtils.isBlank(userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录类型和密码不能为空");
        }

        LoginUserVO loginUserVO;
        if ("phone".equals(loginType)) {
            String userPhone = userLoginRequest.getUserPhone();
            if (StringUtils.isBlank(userPhone)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号不能为空");
            }
            loginUserVO = userService.userLoginByPhone(userPhone, userPassword, request);
        } else if ("account".equals(loginType)) {
            String userAccount = userLoginRequest.getUserAccount();
            if (StringUtils.isBlank(userAccount)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能为空");
            }
            loginUserVO = userService.userLoginByAccount(userAccount, userPassword, request);
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录类型不支持");
        }
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 忘记密码（账号 + 绑定手机号找回）
     */
    @PostMapping("/password/reset")
    public BaseResponse<Boolean> resetPasswordByPhone(@RequestBody UserPasswordResetRequest resetRequest) {
        if (resetRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.resetPasswordByPhone(
                resetRequest.getUserAccount(),
                resetRequest.getUserPhone(),
                resetRequest.getNewPassword(),
                resetRequest.getCheckPassword());
        return ResultUtils.success(result);
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(userAddRequest.getUserAccount())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能为空");
        }
        validateUserPhoneAndEmail(userAddRequest.getUserPhone(), userAddRequest.getUserEmail());
        User existed = userService.lambdaQuery()
                .eq(User::getUserAccount, userAddRequest.getUserAccount().trim())
                .eq(User::getIsDelete, 0)
                .one();
        if (existed != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        ensurePhoneAndEmailUnique(userAddRequest.getUserPhone(), userAddRequest.getUserEmail(), null);
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        String defaultPassword = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + defaultPassword).getBytes());
        user.setUserPassword(encryptPassword);
        user.setUserAccount(userAddRequest.getUserAccount().trim());
        user.setUserRole(StringUtils.defaultIfBlank(userAddRequest.getUserRole(), "student"));
        user.setStatus(userAddRequest.getStatus() == null ? 1 : userAddRequest.getStatus());
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validateUserPhoneAndEmail(userUpdateRequest.getUserPhone(), userUpdateRequest.getUserEmail());
        ensurePhoneAndEmailUnique(userUpdateRequest.getUserPhone(), userUpdateRequest.getUserEmail(), userUpdateRequest.getId());
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员重置用户密码
     */
    @PostMapping("/password/reset/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> resetPasswordByAdmin(
            @RequestBody UserAdminPasswordResetRequest resetRequest,
            HttpServletRequest request) {
        if (resetRequest == null || resetRequest.getId() == null || resetRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.resetPasswordByAdmin(resetRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 100, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 更新个人信息
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
            HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        validateUserPhoneAndEmail(userUpdateMyRequest.getUserPhone(), userUpdateMyRequest.getUserEmail());
        ensurePhoneAndEmailUnique(userUpdateMyRequest.getUserPhone(), userUpdateMyRequest.getUserEmail(), loginUser.getId());

        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 批量删除用户
     */
    @PostMapping("/batch-delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> batchDeleteUser(@RequestBody UserBatchDeleteRequest userBatchDeleteRequest,
            HttpServletRequest request) {
        if (userBatchDeleteRequest == null || userBatchDeleteRequest.getIds() == null || userBatchDeleteRequest.getIds().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.batchDeleteUser(userBatchDeleteRequest.getIds(), userBatchDeleteRequest.getSoftDelete());
        return ResultUtils.success(result);
    }

    /**
     * 批量更新用户
     */
    @PostMapping("/batch-update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> batchUpdateUser(@RequestBody UserBatchUpdateRequest userBatchUpdateRequest,
            HttpServletRequest request) {
        if (userBatchUpdateRequest == null || userBatchUpdateRequest.getIds() == null || userBatchUpdateRequest.getIds().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.batchUpdateUser(userBatchUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 导出用户数据
     */
    @PostMapping("/export")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> exportUser(@RequestBody UserExportRequest userExportRequest,
            HttpServletRequest request) {
        if (userExportRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String result = userService.exportUser(userExportRequest);
        return ResultUtils.success(result);
    }

    /**
     * 导入用户数据
     */
    @PostMapping("/import")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> importUser(@RequestBody UserImportRequest userImportRequest,
            HttpServletRequest request) {
        if (userImportRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.importUser(userImportRequest);
        return ResultUtils.success(result);
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserStatisticsVO> getUserStatistics(String userRole, HttpServletRequest request) {
        UserStatisticsVO result = userService.getUserStatistics(userRole);
        return ResultUtils.success(result);
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar/upload")
    public BaseResponse<String> uploadAvatar(@org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String avatarUrl = userService.uploadUserAvatar(file, loginUser.getId());
        return ResultUtils.success(avatarUrl);
    }

    /**
     * 更新用户头像
     */
    @PostMapping("/avatar/update")
    public BaseResponse<Boolean> updateAvatar(@RequestBody com.jcen.medpal.model.dto.user.UserAvatarUpdateRequest avatarUpdateRequest, HttpServletRequest request) {
        if (avatarUpdateRequest == null || org.apache.commons.lang3.StringUtils.isBlank(avatarUpdateRequest.getAvatarUrl())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像URL不能为空");
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserAvatar(avatarUpdateRequest.getAvatarUrl());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    private void validateUserPhoneAndEmail(String userPhone, String userEmail) {
        if (StringUtils.isNotBlank(userPhone) && !PHONE_PATTERN.matcher(userPhone.trim()).matches()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }
        if (StringUtils.isNotBlank(userEmail) && !EMAIL_PATTERN.matcher(userEmail.trim()).matches()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }
    }

    private void ensurePhoneAndEmailUnique(String userPhone, String userEmail, Long excludeUserId) {
        if (StringUtils.isNotBlank(userPhone)) {
            User existingUserByPhone = userService.lambdaQuery()
                    .eq(User::getUserPhone, userPhone.trim())
                    .ne(excludeUserId != null, User::getId, excludeUserId)
                    .eq(User::getIsDelete, 0)
                    .one();
            if (existingUserByPhone != null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该手机号已被使用");
            }
        }
        if (StringUtils.isNotBlank(userEmail)) {
            User existingUserByEmail = userService.lambdaQuery()
                    .eq(User::getUserEmail, userEmail.trim())
                    .ne(excludeUserId != null, User::getId, excludeUserId)
                    .eq(User::getIsDelete, 0)
                    .one();
            if (existingUserByEmail != null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已被使用");
            }
        }
    }
}
