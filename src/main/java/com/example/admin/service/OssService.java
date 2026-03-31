package com.example.admin.service;

import com.example.admin.vo.OssUploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * OSS 服务
 */
public interface OssService {

    /**
     * 上传文件
     */
    OssUploadVO upload(MultipartFile file, String directory);
}
