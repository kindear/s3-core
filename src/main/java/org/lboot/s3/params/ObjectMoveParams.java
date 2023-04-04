package org.lboot.s3.params;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "对象移动参数")
public class ObjectMoveParams {
    @ApiModelProperty("源桶")
    String sourceBucket;

    @ApiModelProperty("源对象")
    String sourceObject;

    @ApiModelProperty("目标桶")
    String targetBucket;

    @ApiModelProperty("目标对象")
    String targetObject;
}
