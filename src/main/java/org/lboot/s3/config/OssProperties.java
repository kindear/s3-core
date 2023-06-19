package org.lboot.s3.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author kindear
 * 默认对象存储配置 -- 通用配置属性
 */
@Slf4j
@Data
@Configuration
public class OssProperties {
    /**
     * 服务API 模式
     */
    @Value("${oss.mode}")
    private String mode;
    /**
     * 服务API访问入口
     */
    @Value("${oss.endpoint}")
    private String endpoint;
    /**
     * 桶名称
     */
    @Value("${oss.bucket}")
    private String bucketName;
    /**
     * 公钥
     */
    @Value("${oss.public.key}")
    private String accessKey;
    /**
     * 私钥
     */
    @Value("${oss.private.key}")
    private String secretKey;
    /**
     * 域名绑定
     */
    @Value("${oss.host}")
    private String host;
}
