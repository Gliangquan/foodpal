package com.jcen.medpal.service.impl;

import com.jcen.medpal.mapper.DishMapper;
import com.jcen.medpal.model.entity.food.Dish;
import com.jcen.medpal.service.DishService;
import org.springframework.stereotype.Service;

@Service
public class DishServiceImpl extends BaseServiceImpl<DishMapper, Dish> implements DishService {
}
