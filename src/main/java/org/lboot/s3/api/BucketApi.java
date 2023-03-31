package org.lboot.s3.api;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kindear
 * 桶相关接口
 */
public interface BucketApi {
    /**
     * 创建存储桶
     * @param bucketName
     * @return
     */
    default boolean createBucket(String bucketName){
        return false;
    }
    /**
     * 判断存储桶是否存在
     * @param bucketName
     * @return
     */
    default boolean doesBucketExist(String bucketName){
        return false;
    }

    /**
     * 列举桶名称
     * @return
     */
    default List<String> listBuckets(){
        return new ArrayList<>();
    }

    /**
     * 删除桶
     * @param bucketName
     * @return
     */
    default boolean deleteBucket(String bucketName){
        return false;
    };

    /**
     * 设置桶权限
     */
    default void setBucketACL(String bucketName){

    }

    /**
     * 获取桶权限
     * @param bucketName
     * @return
     */
    default String getBucketACL(String bucketName){
        return null;
    }


    /**
     * 判断存储桶是否存在
     * @param bucketName
     * @return
     */
    default boolean isExistBucket(String bucketName){
        return doesBucketExist(bucketName);
    }

}
