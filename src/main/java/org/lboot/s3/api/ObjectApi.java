package org.lboot.s3.api;

import cn.hutool.core.io.FileUtil;
import com.amazonaws.services.s3.model.*;
import lombok.SneakyThrows;
import org.lboot.s3.params.FileUploadParams;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
     * 生成私有对象访问链接
     * @param bucketName
     * @param objectName
     * @param expires
     * @return
     */
    String getObjectURL(String bucketName, String objectName, Integer expires);

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
     * 列举 objects
     * @param request
     * @return
     */
    ObjectListing listObjects(ListObjectsRequest request);


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
    @SneakyThrows
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

    /**
     * [!Override]生成可访问地址
     * @param bucketName
     * @param objectName
     * @param expires
     * @return
     */
    @SneakyThrows
    default String getObjectUrl(String bucketName, String objectName, Integer expires){
        return getObjectURL(bucketName, objectName, expires);
    }

    /**
     * [!Override] 根据前缀获取
     * 根据前缀获取objects
     * @param bucketName
     * @param prefix
     * @return
     */
    default ObjectListing getObjectsByPrefix(String bucketName, String prefix){
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucketName);
        listObjectsRequest.setPrefix(prefix);
        return listObjects(listObjectsRequest);
    }

    /**
     * 文件下载
     * @param in
     * @param request
     * @param params
     * @return
     */
    default ResponseEntity<byte[]> downloadMethod(InputStream in, HttpServletRequest request, String params){

        HttpHeaders heads = new HttpHeaders();
        heads.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream; charset=utf-8");
        String fileName = params;
        try {
            if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
                // firefox浏览器
                fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            } else if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
                // IE浏览器
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } else if (request.getHeader("User-Agent").toUpperCase().indexOf("EDGE") > 0) {
                // WIN10浏览器
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } else if (request.getHeader("User-Agent").toUpperCase().indexOf("CHROME") > 0) {
                // 谷歌
                fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            } else {
                //万能乱码问题解决
                fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            }
        } catch (UnsupportedEncodingException e) {
            // log.error("", e);
        }
        heads.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);
        try {
            // 输入流转换为字节流
            byte[] buffer = FileCopyUtils.copyToByteArray(in);
            //file.delete();
            return new ResponseEntity<>(buffer, heads, HttpStatus.OK);
        } catch (Exception e) {
            // log.error("", e);
        }
        return null;
    }
}
