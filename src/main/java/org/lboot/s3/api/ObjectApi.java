package org.lboot.s3.api;

import cn.hutool.core.io.FileUtil;
import com.amazonaws.services.s3.model.*;
import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;

/**
 * @author kindear
 * 对象API
 */
public interface ObjectApi {

    /**
     * 对象写入
     * @param bucketName
     * @param objectName
     * @param stream
     * @param size
     * @param contextType
     * @return
     */
    PutObjectResult putObject(String bucketName, String objectName, InputStream stream, long size, String contextType);

    /**
     * 获取 object 信息
     * @param bucketName
     * @param objectName
     * @return
     */
    S3Object getObject(String bucketName, String objectName);

    /**
     * 查询 object 是否存在
     * @param bucketName
     * @param objectName
     * @return
     */
    boolean doesObjectExist(String bucketName, String objectName);

    /**
     * 删除 object
     * @param bucketName
     * @param objectName
     * @return
     */
    boolean deleteObject(String bucketName, String objectName);

    /**
     * 获取 object 信息
     * @param bucketName
     * @param objectName
     * @return
     */
    ObjectMetadata getObjectMetadata(String bucketName, String objectName);

    /**
     * object 复制
     * @param sourceBucket
     * @param sourceObject
     * @param targetBucket
     * @param targetObject
     * @return
     */
    CopyObjectResult copyObject(String sourceBucket, String sourceObject, String targetBucket, String targetObject);


    /**
     * 列举 objects
     * @param bucketName
     * @return
     */
    ObjectListing listObjects(String bucketName);


    /**
     * [!Override]获取对象数据流
     * @param bucketName
     * @param objectName
     * @return
     */
    default InputStream getObjectStream(String bucketName, String objectName){
        S3Object nosObject = getObject(bucketName, objectName);
        // 获取文件流
        return nosObject.getObjectContent();
    }





    /**
     * [!Override]对象写入
     * @param bucketName
     * @param objectName
     * @param stream
     */
    @SneakyThrows
    default void putObject(String bucketName, String objectName, InputStream stream){

        putObject(bucketName, objectName, stream, stream.available(), "application/octet-stream");
    }

    /**
     * [!Override]本地文件对象写入
     * @param bucketName
     * @param objectName
     * @param filePath
     */
    @SneakyThrows
    default void putObject(String bucketName, String objectName, String filePath){
        InputStream stream = FileUtil.getInputStream(filePath);
        putObject(bucketName, objectName, stream);
    }

    /**
     * [!Override] 本地文件对象写入
     * @param bucketName
     * @param objectName
     * @param file
     */
    @SneakyThrows
    default void putObject(String bucketName, String objectName, File file){
        InputStream stream = FileUtil.getInputStream(file);
        putObject(bucketName, objectName, stream);
    }


    /**
     * [!Override] 上传文件对象写入
     * @param bucketName
     * @param file
     */
    @SneakyThrows
    default void putObject(String bucketName, String objectName, MultipartFile file){
        putObject(bucketName, objectName, file.getInputStream(), file.getSize(), file.getContentType());
    }

    /**
     * [!Override] 判断 object 是否存在
     * @param bucketName
     * @param objectName
     * @return
     */
    @SneakyThrows
    default Boolean isExistObject(String bucketName, String objectName){
        return doesObjectExist(bucketName, objectName);
    }

    /**
     * [!Override] 移除object
     * @param bucketName
     * @param objectName
     * @return
     */
    @SneakyThrows
    default boolean removeObject(String bucketName, String objectName){
        return deleteObject(bucketName, objectName);
    }

    /**
     * [!Override] 获取文件元信息
     * @param bucketName
     * @param objectName
     * @return
     */
    @SneakyThrows
    default ObjectMetadata getObjectInfo(String bucketName, String objectName){
        return getObjectMetadata(bucketName, objectName);
    }

    /**
     * [!Override] object 移动
     * @param sourceBucket
     * @param sourceObject
     * @param targetBucket
     * @param targetObject
     * @return
     */
    default boolean moveObject(String sourceBucket, String sourceObject, String targetBucket, String targetObject){
        if (!doesObjectExist(sourceBucket, sourceObject)){
            return false;
        }
        if (doesObjectExist(targetBucket, targetObject)){
            return false;
        }
        // 复制
        copyObject(sourceBucket, sourceObject, targetBucket, targetObject);
        // 删除
        deleteObject(sourceBucket, sourceObject);
        return true;
    }


}
