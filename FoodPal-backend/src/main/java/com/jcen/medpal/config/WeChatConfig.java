package com.jcen.medpal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wechat")
public class WeChatConfig {

    private String appId;
    private String secret;
    private String loginUrl;
    private String sessionKeyUrl;

    public String getSessionKeyUrl() {
        return sessionKeyUrl != null ? sessionKeyUrl :
            "https://api.weixin.qq.com/sns/oauth2/access_token";
    }

    public String getLoginUrl() {
        return loginUrl != null ? loginUrl :
            "https://api.weixin.qq.com/sns/jscode2session";
    }
}
