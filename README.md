## S3-CORE

[中文文档](https://lucy.apisev.cn/#/s3-core/)

`s3-core` is a universal object storage solution based on the Amazon s3 protocol, supporting most bucket and object features. In addition, it also supports multiple object storage sources, and can operate and manage different object storage according to requirements. You can operate all object storage solutions that support the S3 protocol, such as Minio.

## Features

1. All object storage devices that support s3 can be accessed.
2. Standard bucket and object access methods are provided.
3. Supports switching between different object stores.

## Installation

Before introducing any Lucy series dependencies, the jitpack image repository needs to be configured.

```xml
<repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://www.jitpack.io</url>
        </repository>
</repositories>
```

Then import according to the version number.

```xml
        <dependency>
            <groupId>com.gitee.lboot</groupId>
            <artifactId>s3-core</artifactId>
            <version>${version}</version>
        </dependency>
```



## Configuration

Enable `Swagger2` documentation,` @EnableKnife4j ` needs to be configured on the start class.

```java
@EnableKnife4j
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {

        SpringApplication.run(LucyAdminApplication.class, args);
    }

}
```

The following information needs to be added to the configuration file `application.properties`  or `application.yml`.

```properties
# file upload configuration 
spring.servlet.multipart.max-file-size = 100000000
spring.servlet.multipart.max-request-size= 1000000000
###################### oss #############################
# server mode
oss.mode=minio
# api url
oss.endpoint=http://127.0.0.1:9000
# default bucket name
oss.bucket=s3-core
# public key
oss.public.key=
# private key
oss.private.key=
# domain
oss.host=http://minio.apisev.cn
```



## Use

After importing and configuring the configuration file, you can use the functions and features provided by `s3-core`.``s3-core` provides a client that can inject object storage in any place that needs to use object storage. The client automatically reads the configuration in the configuration file and prepares for access authentication.

```java
@Service
@AllArgsConstructor
public class DemoService {
    S3Client s3Client;
}
```



## API

### authorization

During system initialization, the contents in `application.properties` are automatically read and access authorization is constructed. If you need to switch to other object storage for access, you can use the following methods.

| Method Name                                                  | Feature                                                      | Remark                                                |
| :----------------------------------------------------------- | :----------------------------------------------------------- | :---------------------------------------------------- |
| reload()                                                     | Read the Bean configuration information and build the access authorization | Build an authorization mapping table based on Hash    |
| reload(String propsPath)                                     | Read the configuration file information under `resources`and build the access authorization | Multiple configuration files can exist simultaneously |
| reload( String endpoint, String accessKey,String secretKey, String host ) | Manually pass in configuration information and build access authorization |                                                       |

### bucket

| Method Name                            | Feature           | Remark                                        |
| :------------------------------------- | :---------------- | :-------------------------------------------- |
| createBucket(String bucketName)        |                   | An existing file cannot be created repeatedly |
| doesBucketExist(String bucketName)     |                   |                                               |
| listBucketsName()                      |                   | List[String]                                  |
| listBuckets()                          |                   | List[Bucket]                                  |
| deleteBucket(String bucketName)        |                   | There are no thrown exceptions                |
| setBucketPrivate(String bucketName)    |                   | not support                                   |
| setBucketPublic(String bucketName)     |                   | not support                                   |
| setBucketPublicRead(String bucketName) |                   | not support                                   |
| getBucketACL(String bucketName)        |                   |                                               |
| buckets()                              | = listBuckets     |                                               |
| isExistBucket(String bucketName)       | = doesBucketExist |                                               |
| removeBucket(String bucketName)        | = deleteBucket    |                                               |



### Object

| Method Name                                                  | Feature                                    | Return           |
| :----------------------------------------------------------- | :----------------------------------------- | :--------------- |
| putObject(String bucketName, String objectName, InputStream stream, long size, String contextType) | Object write                               | PutObjectResult  |
| putObject(String bucketName, String objectName, InputStream stream) | Object write                               | void             |
| putObject(String bucketName, String objectName, String filePath) | Local file write                           | void             |
| putObject(String bucketName, String objectName, File file)   | File write                                 | void             |
| putObject(String bucketName, String objectName, MultipartFile file) | Upload file write                          | void             |
| getObject(String bucketName, String objectName)              | Get object infomation                      | S3Object         |
| doesObjectExist(String bucketName, String objectName)        |                                            | boolean          |
| isExistObject(String bucketName, String objectName)          |                                            | boolean          |
| deleteObject(String bucketName, String objectName)           |                                            | boolean          |
| removeObject(String bucketName, String objectName)           |                                            | boolean          |
| getObjectMetadata(String bucketName, String objectName)      |                                            | ObjectMetadata   |
| getObjectInfo(String bucketName, String objectName)          |                                            | ObjectMetadata   |
| getObjectURL(String bucketName, String objectName, Integer expires) |                                            | String           |
| getObjectUrl(String bucketName, String objectName, Integer expires) |                                            | String           |
| copyObject(String sourceBucket, String sourceObject, String targetBucket, String targetObject) |                                            | CopyObjectResult |
| moveObject(String sourceBucket, String sourceObject, String targetBucket, String targetObject) |                                            | boolean          |
| listObjects(String bucketName)                               |                                            | ObjectListing    |
| listObjects(ListObjectsRequest request)                      |                                            | ObjectListing    |
| getObjectsByPrefix(String bucketName, String prefix)         | Gets a list of objects based on the prefix | ObjectListing    |
| getObjectStream(String bucketName, String objectName)        | Gets the object data flow                  | InputStream      |



## Example

Suppose the directory looks like the following.

```
|--java
|--resources
    |--application.properties
    |--qiniu.properties
    |--minio.properties

```

The qiniu.properties minio.properties configuration file content structure is similar to the following.

```properties

oss.mode=qiniu

oss.endpoint=s3-cn-north-1.qiniucs.com

oss.bucket=***

oss.public.key=***

oss.private.key=***

oss.host=***

```

To create the same bucket in two different stores, as the following code.

```java
...
@Autowried
S3Client s3client
...
public Object createBucketEvery(){
        s3Client.reload("qiniu.properties");
        s3Client.createBucket("testBucket");
        s3Client.reload("minio.properties");
        s3Client.createBucket("testBucket");
        return "success";
 }
```



