package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author YiruiCao
 * @email ycao387@gatech.edu
 * @date 2020-10-21 23:34:16
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
