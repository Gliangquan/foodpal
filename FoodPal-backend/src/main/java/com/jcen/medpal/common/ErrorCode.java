package com.jcen.medpal.common;

/**
 * 自定义错误码
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
public enum ErrorCode {

    // 成功
    SUCCESS(0, "ok"),

    // 参数相关错误
    PARAMS_ERROR(40000, "请求参数错误"),

    // 认证与授权错误
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    INVALID_PASSWORD_ERROR(40102, "密码错误"),

    // 资源与权限错误
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    USER_NOT_FOUND_ERROR(40401, "用户不存在"),
    FILE_NOT_FOUND_ERROR(40402, "文件未找到"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    OPERATION_NOT_ALLOWED_ERROR(40301, "操作不允许"),
    ACCOUNT_LOCKED_ERROR(40302, "账户已被锁定"),

    // 请求相关错误
    REQUEST_TIMEOUT_ERROR(40800, "请求超时"),
    RATE_LIMIT_ERROR(42900, "请求过于频繁"),
    UNSUPPORTED_MEDIA_TYPE_ERROR(41500, "不支持的媒体类型"),
    VALIDATION_ERROR(42200, "数据校验失败"),

    // 业务逻辑相关错误
    DATA_CONFLICT_ERROR(40900, "数据冲突"),
    USER_ALREADY_EXISTS_ERROR(40901, "用户已存在"),
    RESOURCE_LIMIT_EXCEEDED_ERROR(41300, "资源使用超限"),

    // 第三方服务相关错误
    THIRD_PARTY_SERVICE_ERROR(50200, "第三方服务错误"),
    GATEWAY_TIMEOUT_ERROR(50400, "网关超时"),

    // 系统与操作错误
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    CONFIGURATION_ERROR(50003, "配置错误"),

    // 数据库相关错误
    DATABASE_CONNECTION_ERROR(50301, "数据库连接失败"),
    DATABASE_QUERY_ERROR(50302, "数据库查询错误"),

    // 文件相关错误
    FILE_UPLOAD_ERROR(50002, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED_ERROR(41501, "不支持的文件类型"),

    // 服务不可用错误
    SERVICE_UNAVAILABLE_ERROR(50300, "服务不可用");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
