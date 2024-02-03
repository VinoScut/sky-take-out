package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
@Api(tags = "文件上传接口")
@RestController
@RequestMapping("/admin/common/upload")
public class UploadController {

    /**
     * 文件上传
     * @param file
     * @return
     */
    @ApiOperation("文件上传")
    @PostMapping
    public Result<String> upload(MultipartFile file) {
        try {
            if(file != null) {
                String originalFilename = file.getOriginalFilename();
                String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
                String pictureURL = AliOssUtil.upload(file.getBytes(), UUID.randomUUID() + suffix);
                return Result.success(pictureURL);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
