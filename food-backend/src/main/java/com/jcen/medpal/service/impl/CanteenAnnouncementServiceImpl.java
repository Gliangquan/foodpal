package com.jcen.medpal.service.impl;

import com.jcen.medpal.mapper.CanteenAnnouncementMapper;
import com.jcen.medpal.model.entity.food.CanteenAnnouncement;
import com.jcen.medpal.service.CanteenAnnouncementService;
import org.springframework.stereotype.Service;

@Service
public class CanteenAnnouncementServiceImpl extends BaseServiceImpl<CanteenAnnouncementMapper, CanteenAnnouncement>
        implements CanteenAnnouncementService {
}
