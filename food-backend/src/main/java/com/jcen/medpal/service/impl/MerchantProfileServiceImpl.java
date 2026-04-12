package com.jcen.medpal.service.impl;

import com.jcen.medpal.mapper.MerchantProfileMapper;
import com.jcen.medpal.model.entity.food.MerchantProfile;
import com.jcen.medpal.service.MerchantProfileService;
import org.springframework.stereotype.Service;

@Service
public class MerchantProfileServiceImpl extends BaseServiceImpl<MerchantProfileMapper, MerchantProfile>
        implements MerchantProfileService {
}
