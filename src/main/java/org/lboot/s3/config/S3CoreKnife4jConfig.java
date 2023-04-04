package org.lboot.s3.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author  : Kindear
 * @date : 2021-11-15
 * Knife4j 文档配置 基于 SpringFox
 */
@Configuration
@EnableSwagger2
@AllArgsConstructor
public class S3CoreKnife4jConfig {

    @Bean(value = "s3Api")
    public Docket s3Api() {
        String groupName="S3-API";
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("s3-core")
                .description("AWS S3协议访问方案")
                .termsOfServiceUrl("http://localhost:8080")
                .version("1.0.0")
                .build();
        return new Docket(DocumentationType.SWAGGER_2)
                .host("http://localhost:8080/")
                .apiInfo(apiInfo)
                .groupName(groupName)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.lboot.s3.controller"))
                .paths(PathSelectors.any())
                .build();
    }

}
