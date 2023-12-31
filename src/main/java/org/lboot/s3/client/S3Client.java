package org.lboot.s3.client;

import cn.hutool.core.lang.Validator;
import cn.hutool.setting.dialect.Props;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import javassist.NotFoundException;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.lboot.s3.api.BucketApi;
import org.lboot.s3.api.ObjectApi;
import org.lboot.s3.config.OssProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author kindear
 * S3 协议存储相关操作
 */
@Slf4j
@Data
@Component
public class S3Client implements BucketApi, ObjectApi {
    @Autowired
    OssProperties props;

    // https://blog.csdn.net/qq_18300037/article/details/123795776
    private static final ConcurrentHashMap<String, AmazonS3> clients = new ConcurrentHashMap<>();

    // 服务终端地址
    private String endpoint;
    // 服务公钥
    private String accessKey;
    // 服务密钥
    private String secretKey;
    // 服务地址
    private String host;
    // 默认桶
    private String bucket;


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
        if (Validator.isNotEmpty(props)){
            this.endpoint = props.getEndpoint();
            this.accessKey = props.getAccessKey();
            this.secretKey = props.getSecretKey();
            this.host = props.getHost();
            this.bucket = props.getBucketName();
        }else {
            log.warn("S3Client Inject failed");
        }

    }
    // Bean 执行过程中重载方法 -> 例如更换 endPoint、密钥等,需要在执行setXXX 后执行 reload
    @SneakyThrows
    public void reload(){
        String clientKey = clientHash();
        this.amazonS3 = clients.get(clientKey);
    }


    // 读取OSS配置文件路径 -> reload
    /**
     * 通过配置文件重载
     * @param propsPath
     */
    @SneakyThrows
    public void reload(String propsPath){
        // 获取后缀
        Props selfProps = new Props(propsPath);
        this.endpoint = selfProps.getProperty("oss.endpoint");
        this.accessKey = selfProps.getProperty("oss.public.key");
        this.secretKey = selfProps.getProperty("oss.private.key");
        this.host = selfProps.getProperty("oss.host");
        this.bucket = selfProps.getProperty("oss.bucket");
        reload();
    }

    @SneakyThrows
    public void reload(
            String endpoint,
            String accessKey,
            String secretKey,
            String host
    ){
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.host = host;
        reload();
    }

    @SneakyThrows
    public String clientHash(){

        String calcStr = this.endpoint +
                this.accessKey +
                this.secretKey +
                this.host;
        HashFunction hf = Hashing.sha256();
        HashCode hc = hf.hashBytes(calcStr.getBytes());

        // Get the first 32 or 64 bytes of the hash code as a string
        int length = 32; // or 64
        String hashString = hc.toString().substring(0, length);
        //log.info("哈希值为{}",hashString);
        return hashString;

    }

    // 初始化构建 AWSClient
    @SneakyThrows
    AmazonS3 clientBuild(){
        // 是否执行获取
        log.info("\n执行客户端构建");
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
        //存入hash
        String clientKey = clientHash();
        clients.put(clientKey,this.amazonS3);
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
    public AccessControlList getBucketACL(String bucketName) {
        AmazonS3 client = client();
        return client.getBucketAcl(bucketName);
    }

    @Override
    @SneakyThrows
    public boolean doesBucketExist(String bucketName) {
        AmazonS3 client = client();
        return client.doesBucketExistV2(bucketName);
    }

    @Override
    @SneakyThrows
    public List<Bucket> listBuckets() {
        AmazonS3 client = client();
       return client.listBuckets();
    }

    @Override
    @SneakyThrows
    public List<String> listBucketsName() {
        List<Bucket> buckets = listBuckets();
        return buckets.stream().map(Bucket::getName).collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public boolean deleteBucket(String bucketName) {
        AmazonS3 client = client();
        client.deleteBucket(bucketName);
        return true;
    }

    @Override
    @SneakyThrows
    public boolean setBucketPrivate(String bucketName) {
        AmazonS3 client = client();
        client.setBucketAcl(bucketName, CannedAccessControlList.Private);
        return true;
    }

    @Override
    @SneakyThrows
    public boolean setBucketPublic(String bucketName) {
        AmazonS3 client = client();
        client.setBucketAcl(bucketName, CannedAccessControlList.PublicReadWrite);
        return true;
    }

    @Override
    @SneakyThrows
    public boolean setBucketPublicRead(String bucketName) {
        AmazonS3 client = client();
        client.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
        return true;
    }

    // 重写 ObjectAPI


    @Override
    @SneakyThrows
    public PutObjectResult putObject(String bucketName, String objectName, InputStream stream, long size, String contextType) {
        AmazonS3 client = client();
        createBucket(bucketName);
        byte[] bytes = IOUtils.toByteArray(stream);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(size);
        objectMetadata.setContentType(contextType);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        // 上传
        return client.putObject(bucketName, objectName, byteArrayInputStream, objectMetadata);
    }

    @Override
    @SneakyThrows
    public S3Object getObject(String bucketName, String objectName) {
        AmazonS3 client = client();
        return client.getObject(bucketName, objectName);
    }

    @Override
    @SneakyThrows
    public boolean doesObjectExist(String bucketName, String objectName) {
        AmazonS3 client = client();
        return client.doesObjectExist(bucketName,objectName);
    }

    @Override
    @SneakyThrows
    public boolean deleteObject(String bucketName, String objectName) {
        AmazonS3 client = client();
        client.deleteObject(bucketName, objectName);
        return true;
    }

    @Override
    @SneakyThrows
    public ObjectMetadata getObjectMetadata(String bucketName, String objectName) {
        AmazonS3 client = client();
        return client.getObjectMetadata(bucketName, objectName);
    }

    @Override
    @SneakyThrows
    public CopyObjectResult copyObject(String sourceBucket, String sourceObject, String targetBucket, String targetObject) {
        AmazonS3 client = client();
        if (!isExistObject(sourceObject, sourceObject)){
            throw new NotFoundException("No Object");
        }
        createBucket(targetBucket);
        return client.copyObject(sourceBucket, sourceObject, targetBucket, targetObject);
    }

    @Override
    @SneakyThrows
    public ObjectListing listObjects(String bucketName) {
        AmazonS3 client = client();
        return client.listObjects(bucketName);
    }

    @Override
    @SneakyThrows
    public ObjectListing listObjects(ListObjectsRequest request) {
        AmazonS3 client = client();
        return client.listObjects(request);
    }

    @Override
    @SneakyThrows
    public String getObjectURL(String bucketName, String objectName, Integer expires) {
        AmazonS3 client = client();
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, expires);
        URL url = client.generatePresignedUrl(bucketName, objectName, calendar.getTime());
        return url.toString();
    }
}
