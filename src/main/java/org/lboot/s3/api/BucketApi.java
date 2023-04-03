package org.lboot.s3.api;

import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;

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
    default List<String> listBucketsName(){
        return new ArrayList<>();
    }

    /**
     * 列举桶信息
     * @return
     */
    List<Bucket> listBuckets();

    /**
     * 删除桶
     * @param bucketName
     * @return
     */
    default boolean deleteBucket(String bucketName){
        return false;
    };


    //--------bucket-acl--------------//

    /**
     * 设置桶私有 -
     * @param bucketName
     * @return
     */
    boolean setBucketPrivate(String bucketName);

    /**
     * 设置桶公开 rw
     * @param bucketName
     * @return
     */
    boolean setBucketPublic(String bucketName);

    /**
     * 设置桶公开读 私有写 r
     * @param bucketName
     * @return
     */
    boolean setBucketPublicRead(String bucketName);

    /**
     * 获取桶权限
     * @param bucketName
     * @return
     */
    AccessControlList getBucketACL(String bucketName);

    //----非重写API----//

    /**
     * [!Override]判断存储桶是否存在
     * @param bucketName
     * @return
     */
    default boolean isExistBucket(String bucketName){
        return doesBucketExist(bucketName);
    }

    /**
     * [!Override]删除桶
     * @param bucketName
     * @return
     */
    default boolean removeBucket(String bucketName) {
        return deleteBucket(bucketName);
    }



}
