package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.SystemConfigMapper;
import com.jcen.medpal.model.entity.SystemConfig;
import com.jcen.medpal.service.SystemConfigService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {
    
    @Override
    public Object getConfig(String key) {
        return this.lambdaQuery().eq(SystemConfig::getConfigKey, key).one();
    }
    
    @Override
    public boolean updateConfig(SystemConfig config) {
        config.setUpdateTime(LocalDateTime.now());
        return this.updateById(config);
    }
    
    @Override
    public Object listConfigs() {
        return this.list();
    }
    
    @Override
    public Object getPriceConfig() {
        Map<String, Object> priceConfig = new HashMap<>();
        priceConfig.put("duration2h", 100);
        priceConfig.put("duration4h", 180);
        priceConfig.put("duration8h", 320);
        priceConfig.put("serviceFeeRate", 0.1);
        return priceConfig;
    }
    
    @Override
    public boolean updatePriceConfig(Object priceConfig) {
        return true;
    }
}
