package com.jcen.medpal.service.impl;

import com.jcen.medpal.mapper.CanteenComplaintMapper;
import com.jcen.medpal.model.entity.food.CanteenComplaint;
import com.jcen.medpal.service.CanteenComplaintService;
import org.springframework.stereotype.Service;

@Service
public class CanteenComplaintServiceImpl extends BaseServiceImpl<CanteenComplaintMapper, CanteenComplaint>
        implements CanteenComplaintService {
}
