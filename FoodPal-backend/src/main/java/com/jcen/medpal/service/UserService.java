package com.jcen.medpal.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.medpal.model.dto.user.UserBatchUpdateRequest;
import com.jcen.medpal.model.dto.user.UserExportRequest;
import com.jcen.medpal.model.dto.user.UserImportRequest;
import com.jcen.medpal.model.dto.user.UserQueryRequest;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.model.vo.LoginUserVO;
import com.jcen.medpal.model.vo.UserStatisticsVO;
import com.jcen.medpal.model.vo.UserVO;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param userPhone     用户手机号
     * @param userRole      用户角色
     * @param userName      用户名称
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String userPhone, String userRole, String userName, String userEmail);

    /**
     * 用户登录（已废弃，使用 userLoginByAccount 或 userLoginByPhone）
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param userPhone    用户手机号
     * @param request
     * @return 脱敏后的用户信息
     */
    @Deprecated
    LoginUserVO userLogin(String userAccount, String userPassword, String userPhone, HttpServletRequest request);

    /**
     * 账号登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLoginByAccount(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 手机号登录
     *
     * @param userPhone    用户手机号
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLoginByPhone(String userPhone, String userPassword, HttpServletRequest request);

    /**
     * 通过账号+绑定手机号找回密码
     *
     * @param userAccount  用户账号
     * @param userPhone    绑定手机号
     * @param newPassword  新密码
     * @param checkPassword 确认密码
     * @return 是否成功
     */
    boolean resetPasswordByPhone(String userAccount, String userPhone, String newPassword, String checkPassword);

    /**
     * 管理员重置用户密码为初始密码
     *
     * @param userId 用户 id
     * @return 是否成功
     */
    boolean resetPasswordByAdmin(Long userId);

    /**
     * 微信小程序登录
     *
     * @param code     微信登录凭证 code
     * @param nickName 用户昵称（首次登录时可选）
     * @param avatarUrl 用户头像（首次登录时可选）
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLoginByWeChat(String code, String nickName, String avatarUrl, HttpServletRequest request);


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     * @param softDelete 是否软删除
     * @return 删除数量
     */
    int batchDeleteUser(List<Long> ids, Boolean softDelete);

    /**
     * 批量更新用户
     *
     * @param userBatchUpdateRequest 批量更新请求
     * @return 更新数量
     */
    int batchUpdateUser(UserBatchUpdateRequest userBatchUpdateRequest);

    /**
     * 导出用户数据
     *
     * @param userExportRequest 导出请求
     * @return 导出文件URL或Base64
     */
    String exportUser(UserExportRequest userExportRequest);

    /**
     * 导入用户数据
     *
     * @param userImportRequest 导入请求
     * @return 导入数量
     */
    int importUser(UserImportRequest userImportRequest);

    /**
     * 获取用户统计信息
     *
     * @return 统计信息
     */
    UserStatisticsVO getUserStatistics();

    /**
     * 按角色获取用户统计信息
     *
     * @param userRole 用户角色，可为空
     * @return 统计信息
     */
    UserStatisticsVO getUserStatistics(String userRole);

    /**
     * 上传用户头像
     *
     * @param file 头像文件
     * @param userId 用户ID
     * @return 头像URL
     */
    String uploadUserAvatar(org.springframework.web.multipart.MultipartFile file, Long userId);
}
