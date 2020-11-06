package com.atguigu.gulimall.product;


import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallProductApplicationTests {
	@Autowired
	BrandService brandService;

	@Autowired
	CategoryService categoryService;

	@Test
	public void testFindPath() {

		Long[] fullPath = categoryService.findCatelogPath(225L);
		log.info("Full path: {}", Arrays.asList(fullPath));
	}

//	@Test
//	public void testUpload() throws FileNotFoundException {
//		// This example uses the endpoint of the China (Hangzhou) region. Specify the actual endpoint based on your requirements.
//		String endpoint = "oss-us-west-1.aliyuncs.com";
//// Security risks may arise if you use the AccessKey pair of an Alibaba Cloud account to log on to OSS, because the account has permissions on all API operations. To ensure cloud security, we recommend that you follow best practices of Resource Access Management and use a RAM user account to call API operations or perform routine operations and maintenance. To create a RAM user account, log on to https://ram.console.aliyun.com.
//		String accessKeyId = "LTAI4FzB7ZxciCcXYJxrg6eq";
//		String accessKeySecret = "r0vgMyNbCFmsdfiMUnZbG2Ny4lWVGA";
//
//// Create an OSSClient instance.
//		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//// Upload a file stream.
//		InputStream inputStream = new FileInputStream("/Users/yiruicao/Desktop/testUpload.png");
//		ossClient.putObject("gulimall-yiruicao", "testUpload.png", inputStream);
//
//// Shut down the OSSClient instance.
//		ossClient.shutdown();
//		System.out.println("Upload Success...");
//	}

//	@Test
	public void contextLoads() {
		BrandEntity brandEntity = new BrandEntity();
		brandEntity.setBrandId(1L);
		brandEntity.setDescript("Oneplus");
//		brandEntity.setDescript("Important");
//		brandEntity.setName("Apple");
//		brandService.save(brandEntity);
//		System.out.println("Saved successful...");

		brandService.updateById(brandEntity);
		List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
		for (BrandEntity each:list) {
			System.out.println(each.toString());
		}
	}

}
