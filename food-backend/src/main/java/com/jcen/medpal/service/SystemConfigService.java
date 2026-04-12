package com.jcen.medpal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.medpal.model.entity.SystemConfig;

public interface SystemConfigService extends IService<SystemConfig> {
    
    Object getConfig(String key);
    
    boolean updateConfig(SystemConfig config);
    
    Object listConfigs();
    
    Object getPriceConfig();
    
    boolean updatePriceConfig(Object priceConfig);
}
