package org.lboot.s3.auto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Slf4j
@Configuration
@ComponentScan(basePackages = {
        "org.lboot.s3"
})
public class S3CoreAutoConfiguration implements EnvironmentAware {

    @Override
    public void setEnvironment(Environment environment) {

    }
}
