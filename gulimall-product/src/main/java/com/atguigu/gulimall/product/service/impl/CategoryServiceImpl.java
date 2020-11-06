package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
//    @Autowired
//    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 查出所有分类
        List<CategoryEntity> entities = list();

        // sort 去空
        entities.stream().filter(e -> e.getSort() == null).forEach(e -> e.setSort(0));

        // 组成成树  一级菜单
        return entities.stream()
                // 找到所有一级分类
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                // 添加上子分类
                .peek(menu -> menu.setChildren(getChildren(menu, entities)))
                // 按照 sort 排序
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                .collect(Collectors.toList());
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        //LOGGER.debug("root = {}", root);

        return all.stream()
                // 找到 all 中父分类id == root.id 的
                .filter(menu -> Objects.equals(menu.getParentCid(), (root.getCatId())))
                // 递归，为找到的子分类，再找子子分类
                .peek(menu -> menu.setChildren(getChildren(menu, all)))
                // 按照 sort 排序
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                .collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO: Check whether the categories to be deleted are referenced elsewhere
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths=new ArrayList<>();
        findParentPath(catelogId, paths);
        Collections.reverse(paths);
        Long[] ans=new Long[paths.size()];
        for (int i=0;i<ans.length;i++) {
            ans[i]=paths.get(i);
        }
        return ans;
    }

    private void findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity byId=this.getById(catelogId);
        if (byId.getParentCid()!=0) {
            findParentPath(byId.getParentCid(), paths);
        }
    }
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }
}
