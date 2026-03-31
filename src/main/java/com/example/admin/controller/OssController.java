package com.example.admin.controller;

import com.example.admin.common.Result;
import com.example.admin.service.OssService;
import com.example.admin.vo.OssUploadVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * OSS 文件上传控制器
 */
@RestController
@RequestMapping("/oss")
public class OssController {

    @Resource
    private OssService ossService;

    /**
     * 通用文件上传
     */
    @PostMapping("/upload")
    public Result<OssUploadVO> upload(@RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "dir", required = false) String dir) {
        return Result.success("上传成功", ossService.upload(file, dir));
    }
}
