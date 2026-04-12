package com.jcen.medpal.service.impl;

import static com.jcen.medpal.constant.UserConstant.USER_LOGIN_STATE;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jcen.medpal.common.ErrorCode;
import com.jcen.medpal.constant.CommonConstant;
import com.jcen.medpal.exception.BusinessException;
import com.jcen.medpal.mapper.UserMapper;
import com.jcen.medpal.model.dto.user.UserQueryRequest;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.model.enums.UserRoleEnum;
import com.jcen.medpal.model.vo.LoginUserVO;
import com.jcen.medpal.model.vo.UserVO;
import com.jcen.medpal.service.UserService;
import com.jcen.medpal.utils.JwtTokenUtils;
import com.jcen.medpal.utils.SqlUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户服务实现
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Service
@Slf4j
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "jcenLeung";

    @Resource
    private JwtTokenUtils jwtTokenUtils;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userPhone, String userRole, String userName, String userEmail) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userPhone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 6 || checkPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (userPhone.length() != 11) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        
        // 设置默认角色
        if (StringUtils.isBlank(userRole)) {
            userRole = "student";
        }
        
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_account", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 手机号不能重复
            QueryWrapper<User> phoneWrapper = new QueryWrapper<>();
            phoneWrapper.eq("user_phone", userPhone);
            long phoneCount = this.baseMapper.selectCount(phoneWrapper);
            if (phoneCount > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号已被注册");
            }
            if (StringUtils.isNotBlank(userEmail)) {
                QueryWrapper<User> emailWrapper = new QueryWrapper<>();
                emailWrapper.eq("user_email", userEmail.trim());
                long emailCount = this.baseMapper.selectCount(emailWrapper);
                if (emailCount > 0) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱已被注册");
                }
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserPhone(userPhone);
            user.setUserEmail(StringUtils.isBlank(userEmail) ? null : userEmail.trim());
            user.setUserRole(userRole);
            user.setUserName(userName);
            user.setStatus(1);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    @Deprecated
    public LoginUserVO userLogin(String userAccount, String userPassword, String userPhone, HttpServletRequest request) {
        // 已废弃，使用 userLoginByAccount 或 userLoginByPhone
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "请使用账号登录或手机号登录");
    }

    @Override
    public LoginUserVO userLoginByAccount(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号格式错误");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        queryWrapper.eq("is_delete", 0);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "账号已被禁用");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        // 4. 生成 JWT Token
        String token = jwtTokenUtils.generateToken(user.getId(), user.getUserAccount());
        loginUserVO.setToken(token);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLoginByPhone(String userPhone, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userPhone, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userPhone.length() != 11) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_phone", userPhone);
        queryWrapper.eq("user_password", encryptPassword);
        queryWrapper.eq("is_delete", 0);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userPhone cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "账号已被禁用");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        // 4. 生成 JWT Token
        String token = jwtTokenUtils.generateToken(user.getId(), user.getUserAccount());
        loginUserVO.setToken(token);
        return loginUserVO;
    }

    @Override
    public boolean resetPasswordByPhone(String userAccount, String userPhone, String newPassword, String checkPassword) {
        if (StringUtils.isAnyBlank(userAccount, userPhone, newPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userPhone.length() != 11) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }
        if (newPassword.length() < 6 || checkPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于6位");
        }
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_phone", userPhone);
        queryWrapper.eq("is_delete", 0);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "账号与手机号不匹配");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "账号已禁用，无法找回密码");
        }

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        if (encryptPassword.equals(user.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码不能与旧密码相同");
        }
        user.setUserPassword(encryptPassword);
        return this.updateById(user);
    }

    @Override
    public boolean resetPasswordByAdmin(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        User user = this.getById(userId);
        if (user == null || user.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        String defaultPassword = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + defaultPassword).getBytes());
        user.setUserPassword(encryptPassword);
        return this.updateById(user);
    }

    @Override
    public LoginUserVO userLoginByWeChat(String code, String nickName, String avatarUrl, HttpServletRequest request) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "微信登录功能暂未开通");
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录（Session）
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        
        // 如果 Session 中没有，尝试从 Token 中获取
        if (currentUser == null || currentUser.getId() == null) {
            String token = getTokenFromRequest(request);
            if (StringUtils.isNotBlank(token) && jwtTokenUtils.validateToken(token)) {
                Long userId = jwtTokenUtils.getUserIdFromToken(token);
                currentUser = this.getById(userId);
                if (currentUser != null) {
                    return currentUser;
                }
            }
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 从请求中获取 Token
     *
     * @param request 请求
     * @return Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 先从 Authorization header 中获取
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        // 如果 header 中没有，尝试从查询参数中获取（用于图片等资源的跨域请求）
        String tokenParam = request.getParameter("token");
        if (StringUtils.isNotBlank(tokenParam)) {
            return tokenParam;
        }
        return null;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录（Session）
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        
        // 如果 Session 中没有，尝试从 Token 中获取
        if (currentUser == null || currentUser.getId() == null) {
            String token = getTokenFromRequest(request);
            if (StringUtils.isNotBlank(token) && jwtTokenUtils.validateToken(token)) {
                Long userId = jwtTokenUtils.getUserIdFromToken(token);
                currentUser = this.getById(userId);
                if (currentUser != null) {
                    return currentUser;
                }
            }
            return null;
        }
        
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userPhone = userQueryRequest.getUserPhone();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String realNameStatus = userQueryRequest.getRealNameStatus();
        String qualificationStatus = userQueryRequest.getQualificationStatus();
        String keyword = userQueryRequest.getKeyword();
        Integer status = userQueryRequest.getStatus();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", 0);
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "union_id", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mp_open_id", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "user_role", userRole);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.eq(StringUtils.isNotBlank(realNameStatus), "real_name_status", realNameStatus);
        queryWrapper.eq(StringUtils.isNotBlank(qualificationStatus), "qualification_status", qualificationStatus);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "user_profile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "user_name", userName);
        queryWrapper.like(StringUtils.isNotBlank(userAccount), "user_account", userAccount);
        queryWrapper.like(StringUtils.isNotBlank(userPhone), "user_phone", userPhone);
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like("user_name", keyword)
                    .or()
                    .like("user_account", keyword)
                    .or()
                    .like("user_phone", keyword)
                    .or()
                    .like("user_email", keyword));
        }
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public int batchDeleteUser(List<Long> ids, Boolean softDelete) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        if (softDelete == null || softDelete) {
            // 软删除
            return this.baseMapper.update(null, new UpdateWrapper<User>().in("id", ids).set("is_delete", 1));
        } else {
            // 硬删除
            return this.baseMapper.deleteBatchIds(ids);
        }
    }

    @Override
    public int batchUpdateUser(com.jcen.medpal.model.dto.user.UserBatchUpdateRequest userBatchUpdateRequest) {
        if (userBatchUpdateRequest == null || CollUtil.isEmpty(userBatchUpdateRequest.getIds())) {
            return 0;
        }
        User user = new User();
        if (StringUtils.isNotBlank(userBatchUpdateRequest.getUserName())) {
            user.setUserName(userBatchUpdateRequest.getUserName());
        }
        if (StringUtils.isNotBlank(userBatchUpdateRequest.getUserRole())) {
            user.setUserRole(userBatchUpdateRequest.getUserRole());
        }
        if (userBatchUpdateRequest.getStatus() != null) {
            user.setStatus(userBatchUpdateRequest.getStatus());
        }
        if (StringUtils.isNotBlank(userBatchUpdateRequest.getUserProfile())) {
            user.setUserProfile(userBatchUpdateRequest.getUserProfile());
        }
        return this.baseMapper.update(user, new QueryWrapper<User>().in("id", userBatchUpdateRequest.getIds()));
    }

    @Override
    public String exportUser(com.jcen.medpal.model.dto.user.UserExportRequest userExportRequest) {
        // 构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(userExportRequest.getUserName())) {
            queryWrapper.like("user_name", userExportRequest.getUserName());
        }
        if (StringUtils.isNotBlank(userExportRequest.getUserAccount())) {
            queryWrapper.like("user_account", userExportRequest.getUserAccount());
        }
        if (StringUtils.isNotBlank(userExportRequest.getUserRole())) {
            queryWrapper.eq("user_role", userExportRequest.getUserRole());
        }
        if (userExportRequest.getStatus() != null) {
            queryWrapper.eq("status", userExportRequest.getStatus());
        }
        List<User> users = this.list(queryWrapper);
        // 这里简化处理，实际应该生成真实的文件
        // 返回导出文件的URL或Base64编码
        return "export_" + System.currentTimeMillis() + ".xlsx";
    }

    @Override
    public int importUser(com.jcen.medpal.model.dto.user.UserImportRequest userImportRequest) {
        // 这里简化处理，实际应该解析文件内容
        // 返回导入的用户数量
        return 0;
    }

    @Override
    public com.jcen.medpal.model.vo.UserStatisticsVO getUserStatistics() {
        return getUserStatistics(null);
    }

    @Override
    public com.jcen.medpal.model.vo.UserStatisticsVO getUserStatistics(String userRole) {
        com.jcen.medpal.model.vo.UserStatisticsVO statistics = new com.jcen.medpal.model.vo.UserStatisticsVO();

        QueryWrapper<User> scopedBaseQuery = new QueryWrapper<User>().eq("is_delete", 0);
        if (StringUtils.isNotBlank(userRole)) {
            if ("student".equals(userRole)) {
                scopedBaseQuery.in("user_role", "student", "user");
            } else {
                scopedBaseQuery.eq("user_role", userRole);
            }
        }
        statistics.setTotalUsers(this.baseMapper.selectCount(scopedBaseQuery));

        QueryWrapper<User> adminQuery = new QueryWrapper<User>().eq("is_delete", 0).eq("user_role", "admin");
        statistics.setAdminCount(this.baseMapper.selectCount(adminQuery));

        QueryWrapper<User> studentQuery = new QueryWrapper<User>().eq("is_delete", 0).in("user_role", "student", "user");
        statistics.setStudentCount(this.baseMapper.selectCount(studentQuery));

        QueryWrapper<User> merchantQuery = new QueryWrapper<User>().eq("is_delete", 0).eq("user_role", "merchant");
        statistics.setMerchantCount(this.baseMapper.selectCount(merchantQuery));

        QueryWrapper<User> supervisorQuery = new QueryWrapper<User>().eq("is_delete", 0).eq("user_role", "supervisor");
        statistics.setSupervisorCount(this.baseMapper.selectCount(supervisorQuery));

        QueryWrapper<User> enabledQuery = new QueryWrapper<User>().eq("is_delete", 0).eq("status", 1);
        if (StringUtils.isNotBlank(userRole)) {
            if ("student".equals(userRole)) {
                enabledQuery.in("user_role", "student", "user");
            } else {
                enabledQuery.eq("user_role", userRole);
            }
        }
        statistics.setEnabledCount(this.baseMapper.selectCount(enabledQuery));

        QueryWrapper<User> disabledQuery = new QueryWrapper<User>().eq("is_delete", 0).eq("status", 0);
        if (StringUtils.isNotBlank(userRole)) {
            if ("student".equals(userRole)) {
                disabledQuery.in("user_role", "student", "user");
            } else {
                disabledQuery.eq("user_role", userRole);
            }
        }
        statistics.setDisabledCount(this.baseMapper.selectCount(disabledQuery));

        // 当前库没有独立在线/登录轨迹字段，避免返回全站错数
        statistics.setActiveUsers(0L);
        statistics.setNewUsers(0L);
        statistics.setTodayLoginCount(0L);
        statistics.setWeekLoginCount(0L);
        statistics.setMonthLoginCount(0L);

        return statistics;
    }

    @Override
    public String uploadUserAvatar(org.springframework.web.multipart.MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像大小不能超过5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持图片文件");
        }

        try {
            com.jcen.medpal.utils.MinioUtils minioUtils = new com.jcen.medpal.utils.MinioUtils();
            org.springframework.beans.factory.annotation.Autowired autowired = this.getClass().getAnnotation(org.springframework.beans.factory.annotation.Autowired.class);
            
            String avatarUrl = minioUtils.uploadFile(file, "user");

            User user = new User();
            user.setId(userId);
            user.setUserAvatar(avatarUrl);
            this.updateById(user);

            log.info("用户头像上传成功，用户ID: {}, 头像URL: {}", userId, avatarUrl);
            return avatarUrl;
        } catch (Exception e) {
            log.error("用户头像上传失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "头像上传失败: " + e.getMessage());
        }
    }
}
