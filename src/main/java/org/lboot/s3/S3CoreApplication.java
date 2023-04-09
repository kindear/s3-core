package org.lboot.s3;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableKnife4j
@SpringBootApplication
public class S3CoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(S3CoreApplication.class, args);
    }

}

/*
 * @TODO
 * 1. 设置s3-core 自定义异常 & 自定义异常enum
 * 2. 引入 Forest Http 框架，对一些无法完成的功能（设置桶权限）完善 [待定]
 * 3. 是否保留 knife4j 依赖，决定框架是朝着全面还是轻量的方向发展
 * 4. 测试实现 reload -> 完善 reload 方案
 *    -> 定义 id -> 根据id 接口实现 获取 endpoint 等信息
 *    如何保证原子化 => 是否支持自定义调用 S3Client client = new S3Client(); ? |
 *    多例工厂 -> 根据 endpoint 判断
 *
 */