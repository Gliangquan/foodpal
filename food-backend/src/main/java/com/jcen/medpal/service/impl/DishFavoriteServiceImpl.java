package com.jcen.medpal.service.impl;

import com.jcen.medpal.mapper.DishFavoriteMapper;
import com.jcen.medpal.model.entity.food.DishFavorite;
import com.jcen.medpal.service.DishFavoriteService;
import org.springframework.stereotype.Service;

@Service
public class DishFavoriteServiceImpl extends BaseServiceImpl<DishFavoriteMapper, DishFavorite>
        implements DishFavoriteService {
}
