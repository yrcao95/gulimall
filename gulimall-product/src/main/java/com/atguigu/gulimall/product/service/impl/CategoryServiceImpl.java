package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
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
import org.springframework.util.StringUtils;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
//    @Autowired
//    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redisson;

    public static final String MY_KEY1 = "getLevel1Categories";
    public static final String MY_KEY2 = "getCatalogJson";

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
        List<Long> paths = new ArrayList<>();
        findParentPath(catelogId, paths);
        Collections.reverse(paths);
        Long[] ans = new Long[paths.size()];
        for (int i = 0; i < ans.length; i++) {
            ans[i] = paths.get(i);
        }
        return ans;
    }

    private void findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
    }


    @CacheEvict(value = "category", allEntries = true)
//    @Caching(evict = {
//            @CacheEvict(value="category", key ="#root.target.MY_KEY1"),
//            @CacheEvict(value="category", key = "#root.target.MY_KEY2")
//    })
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

        // Delete data in redis, wait for the update next time
    }

    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        System.out.println("getLevel1Categories...");
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));

    }


    @Cacheable(value = {"category"}, key = "#root.methodName", sync = true)
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> level1Categories = getParent_cid(selectList, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1Categories.stream().collect(Collectors.toMap(k -> {
            return k.getCatId().toString();
        }, v -> {
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> catelog3List = getParent_cid(selectList, l2.getCatId());
                    if (catelog3List != null) {
                        List<Catelog2Vo.Catelog3Vo> collect = catelog3List.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));


        return parent_cid;
    }

    //    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson2() {
        // Cache lock,

        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedissonLock();
            return catalogJsonFromDb;
        }
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return result;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {
        // Add a lock
        synchronized (this) {
            // Revisit the cache to check whether the records exist
            return getDataFromDb();
        }
        // Figure out all the database records

    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        // Capture a lock in redis
        RLock lock = redisson.getLock("CatalogJson-lock");
        lock.lock();

        Map<String, List<Catelog2Vo>> result;
        try {
            result = getDataFromDb();
        } finally {
            lock.unlock();
        }

//            redisTemplate.delete("lock");

        // Get the lock value for comparison and deletion should happen at the same time.
//            String lockValue = redisTemplate.opsForValue().get("lock");
//            if (token.equals(lockValue)) {
//                redisTemplate.delete("lock");
//            }

        return result;

    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        // Capture a lock in redis
        String token = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", token, 600L, TimeUnit.SECONDS);
        if (lock) {
//            redisTemplate.expire("lock", 30, TimeUnit.SECONDS);
            Map<String, List<Catelog2Vo>> result;
            try {
                result = getDataFromDb();
            } finally {
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then return redis.call(\"del\",KEYS[1]) else return 0 end";
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), token);
            }

//            redisTemplate.delete("lock");

            // Get the lock value for comparison and deletion should happen at the same time.
//            String lockValue = redisTemplate.opsForValue().get("lock");
//            if (token.equals(lockValue)) {
//                redisTemplate.delete("lock");
//            }

            return result;
        } else {
            // Retry

            return getCatalogJsonFromDbWithRedisLock();
        }

    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }
        log.info("Db checked...");

        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> level1Categories = getParent_cid(selectList, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1Categories.stream().collect(Collectors.toMap(k -> {
            return k.getCatId().toString();
        }, v -> {
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> catelog3List = getParent_cid(selectList, l2.getCatId());
                    if (catelog3List != null) {
                        List<Catelog2Vo.Catelog3Vo> collect = catelog3List.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));

        redisTemplate.opsForValue().set("catalogJSON", JSON.toJSONString(parent_cid), 1, TimeUnit.HOURS);

        return parent_cid;
    }
    // Figure out all the database records


    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        return selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
//        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
//        return categoryEntities;
    }


}
