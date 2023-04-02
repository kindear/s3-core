package org.lboot.s3.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.lboot.s3.client.S3Client;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("exist/{bucket}")
    @ApiOperation(value = "桶查询")
    public Object isExist(@PathVariable("bucket") String bucket){

        return s3Client.doesBucketExist(bucket);
    }

    @DeleteMapping("bucket/{bucketName}")
    @ApiOperation(value = "桶删除",notes = "")
    public Object bucketDelete(@PathVariable("bucketName") String bucketName){
        s3Client.deleteBucket(bucketName);
        return "删除成功";
    }


}
