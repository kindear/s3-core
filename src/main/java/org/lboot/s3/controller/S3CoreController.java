package org.lboot.s3.controller;

import cn.hutool.core.bean.BeanUtil;
import com.amazonaws.services.s3.model.AccessControlList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.lboot.s3.client.S3Client;
import org.lboot.s3.params.FileUploadParams;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

@RestController
@RequestMapping("s3")
@AllArgsConstructor
@Api(tags = "方法测试")
public class S3CoreController {
    S3Client s3Client;

    @PostMapping("bucket/{bucketName}")
    @ApiOperation(value = "桶创建")
    public Object bucketCreate(@PathVariable("bucketName") String bucketName){
        s3Client.createBucket(bucketName);
        return "创建成功";
    }

    @PostMapping("bucket/{bucketName}/private")
    @ApiOperation(value = "桶私有")
    public Object bucketPrivate(@PathVariable("bucketName") String bucketName){
        s3Client.setBucketPrivate(bucketName);
        return "修改成功";
    }

    @PostMapping("bucket/{bucketName}/public")
    @ApiOperation(value = "桶公开")
    public Object bucketPublic(@PathVariable("bucketName") String bucketName){
        s3Client.setBucketPublic(bucketName);
        return "修改成功";
    }

    @GetMapping("exist/{bucket}")
    @ApiOperation(value = "桶查询")
    public Object isExist(@PathVariable("bucket") String bucket){

        return s3Client.doesBucketExist(bucket);
    }

    @GetMapping("bucket/{bucketName}/acl")
    @ApiOperation(value = "桶权限")
    public Object bucketInfo(@PathVariable("bucketName") String bucketName){
        AccessControlList ctl = s3Client.getBucketACL(bucketName);
        return BeanUtil.beanToMap(ctl);
    }

    @GetMapping("buckets")
    @ApiOperation(value = "桶列表")
    public Object buckets(){
        return s3Client.listBuckets();
    }

    @DeleteMapping("bucket/{bucketName}")
    @ApiOperation(value = "桶删除",notes = "")
    public Object bucketDelete(@PathVariable("bucketName") String bucketName){
        s3Client.deleteBucket(bucketName);
        return "删除成功";
    }

    @PostMapping("object")
    @ApiOperation(value = "对象新建",notes = "")
    public Object objectCreate(FileUploadParams params){
        s3Client.putObject(params.getBucketName(), params.getFileName(), params.getFile());
        return "上传成功";
    }


    @GetMapping("object/{bucketName}/{objectName}/info")
    @ApiOperation(value = "对象信息")
    public Object objectInfo(@PathVariable("bucketName") String bucketName, @PathVariable("objectName") String objectName){
        return s3Client.getObjectInfo(bucketName, objectName);
    }

    @GetMapping("object/{bucketName}/{objectName}")
    @ApiOperation(value = "对象下载")
    @SneakyThrows
    public Object objectDownload(@PathVariable("bucketName") String bucketName, @PathVariable("objectName") String objectName){
        InputStream stream = s3Client.getObjectStream(bucketName,objectName);
        long size = stream.available();
        return "文件大小:"+size;
    }

    @DeleteMapping("object/{bucketName}/{objectName}")
    @ApiOperation(value = "对象删除",notes = "")
    public Object objectDelete(@PathVariable("bucketName") String bucketName, @PathVariable("objectName") String objectName){
        s3Client.deleteObject(bucketName, objectName);
        return "删除成功";
    }

    @GetMapping("bucket/{bucketName}/objects")
    @ApiOperation(value = "对象列表",notes = "")
    public Object listObjects(@PathVariable("bucketName") String bucketName){
        return s3Client.listObjects(bucketName);
    }

}

