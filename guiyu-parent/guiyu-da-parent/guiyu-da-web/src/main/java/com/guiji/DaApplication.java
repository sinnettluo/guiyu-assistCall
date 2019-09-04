package com.guiji;

import com.guiji.component.result.EnableAutoResultPack;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by ty on 2018/10/18.
 */
@SpringBootApplication
@EnableDiscoveryClient	//启用服务发现
@EnableAutoResultPack	
@EnableFeignClients	//启用feign
@EnableSwagger2	//启用swagger注解
@EnableAsync  //启用异步
@MapperScan("com.guiji.*.dao")
public class DaApplication {
    public static void main(String[] args) {
        SpringApplication.run(DaApplication.class, args);
    }
}
