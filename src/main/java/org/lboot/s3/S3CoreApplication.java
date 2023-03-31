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
