package com.atguigu.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

	@Autowired
	OSSClient ossClient;

	@Test
	void testUpload() throws FileNotFoundException {
		// Upload a file stream.
		InputStream inputStream = new FileInputStream("/Users/yiruicao/Desktop/testUpload2.png");
		ossClient.putObject("gulimall-yiruicao", "testUpload2.png", inputStream);

// Shut down the OSSClient instance.
		ossClient.shutdown();
		System.out.println("Upload Success...");
	}

}
