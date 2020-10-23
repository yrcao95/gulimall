package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author YiruiCao
 * @email ycao387@gatech.edu
 * @date 2020-10-22 19:02:36
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
