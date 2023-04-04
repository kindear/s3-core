package org.lboot.s3.controller;


import com.amazonaws.services.s3.model.AccessControlList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.lboot.s3.client.S3Client;
import org.lboot.s3.params.FileUploadParams;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

    /**
     * 不支持: minio
     * @param bucketName
     * @return
     */
    @PostMapping("bucket/{bucketName}/private")
    @ApiOperation(value = "桶私有")
    public Object bucketPrivate(@PathVariable("bucketName") String bucketName){
        s3Client.setBucketPrivate(bucketName);
        return "修改成功";
    }

    /**
     * 不支持: minio
     * @param bucketName
     * @return
     */
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
        return ctl;
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

    @GetMapping("object/{bucketName}/{objectName}/url")
    @ApiOperation(value = "对象访问地址")
    public Object objectUrl(@PathVariable("bucketName") String bucketName, @PathVariable("objectName") String objectName){
        return s3Client.getObjectURL(bucketName, objectName, 3);
    }

    @GetMapping("object/{bucketName}/{objectName}")
    @ApiOperation(value = "对象下载")
    @SneakyThrows
    public ResponseEntity<byte[]> objectDownload(@PathVariable("bucketName") String bucketName, HttpServletRequest request, @PathVariable("objectName") String objectName){
        InputStream stream = s3Client.getObjectStream(bucketName,objectName);
        return downloadMethod(stream,request,bucketName);
    }

    @DeleteMapping("object/{bucketName}/{objectName}")
    @ApiOperation(value = "对象删除",notes = "")
    public Object objectDelete(@PathVariable("bucketName") String bucketName, @PathVariable("objectName") String objectName){
        if (s3Client.doesObjectExist(bucketName, objectName)){
            s3Client.deleteObject(bucketName, objectName);
            return true;
        }
        return "桶或对象不存在";
    }

    @GetMapping("bucket/{bucketName}/objects")
    @ApiOperation(value = "对象列表",notes = "")
    public Object listObjects(@PathVariable("bucketName") String bucketName){
        return s3Client.listObjects(bucketName);
    }


    /**
     * 文件下载
     * @param in
     * @param request
     * @param params
     * @return
     */
     ResponseEntity<byte[]> downloadMethod(InputStream in, HttpServletRequest request, String params){

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

