package com.jcen.medpal.model.dto.food;

import lombok.Data;

@Data
public class DynamicShareRequest {

    /**
     * 分享渠道，例如 wechat / qq / moments
     */
    private String shareChannel;
}
