package org.lboot.s3.client;

import cn.hutool.core.lang.Validator;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.lboot.s3.api.BucketApi;
import org.lboot.s3.api.ObjectApi;
import org.lboot.s3.config.OssProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author kindear@RequiredArgsConstructor
 * S3 协议存储相关操作
 */
@Slf4j
@Data
@Component
public class S3Client implements BucketApi, ObjectApi {
    @Autowired
    OssProperties props;

    // 服务终端地址
    private String endpoint;
    // 服务公钥
    private String accessKey;
    // 服务密钥
    private String secretKey;
    // 服务地址
    private String host;


    // 最大链接
    private Integer maxConnection = 100;
    // 区域
    private String region = "";
    // 预取path形式访问
    private Boolean enablePath = true;

    private AmazonS3 amazonS3 = null;

    // Bean 初始化构建方法
    @PostConstruct
    public void init(){
        this.endpoint = props.getEndpoint();
        this.accessKey = props.getAccessKey();
        this.secretKey = props.getSecretKey();
        this.host = props.getHost();
    }
    // Bean 执行过程中重载方法 -> 例如更换 endPoint、密钥等,需要在执行setXXX 后执行 reload
    @SneakyThrows
    public void reload(){
        this.amazonS3 = null;
    }

    // 初始化构建 AWSClient\
    @SneakyThrows
    AmazonS3 clientBuild(){
        // 客户端配置，主要是全局的配置信息
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setMaxConnections(this.maxConnection);
        // url以及region配置
        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(
                this.endpoint, this.region);
        // 凭证配置
        AWSCredentials awsCredentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
        // build amazonS3Client客户端
        this.amazonS3 = AmazonS3Client.builder().withEndpointConfiguration(endpointConfiguration)
                .withClientConfiguration(clientConfiguration).withCredentials(awsCredentialsProvider)
                .disableChunkedEncoding().withPathStyleAccessEnabled(this.enablePath).build();
        return this.amazonS3;
    }
    // 获取已构建客户端
    @SneakyThrows
    AmazonS3 client(){
        if (Validator.isNotEmpty(this.amazonS3)){
            return this.amazonS3;
        }
        return clientBuild();
    }

    @Override
    @SneakyThrows
    public boolean createBucket(String bucketName) {
        AmazonS3 client = client();
        if (doesBucketExist(bucketName)){
            return false;
        }
        client.createBucket(bucketName);
        return true;
    }

    @Override
    @SneakyThrows
    public boolean doesBucketExist(String bucketName) {
        AmazonS3 client = client();
        return client.doesBucketExistV2(bucketName);
    }


}
