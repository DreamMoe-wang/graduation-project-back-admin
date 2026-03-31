package com.example.admin.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.example.admin.config.OssProperties;
import com.example.admin.service.OssService;
import com.example.admin.vo.OssUploadVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * OSS 服务实现
 */
@Service
public class OssServiceImpl implements OssService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Resource
    private OSS ossClient;

    @Resource
    private OssProperties ossProperties;

    @Override
    public OssUploadVO upload(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
        validateFileSize(file);

        String objectKey = buildObjectKey(file.getOriginalFilename(), directory);
        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            ossClient.putObject(ossProperties.getBucketName(), objectKey, inputStream, metadata);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }

        return OssUploadVO.builder()
                .objectKey(objectKey)
                .originalFilename(file.getOriginalFilename())
                .url(buildFileUrl(objectKey))
                .size(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    private void validateFileSize(MultipartFile file) {
        long maxSize = (long) ossProperties.getMaxSizeMb() * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("文件大小不能超过 " + ossProperties.getMaxSizeMb() + "MB");
        }
    }

    private String buildObjectKey(String originalFilename, String directory) {
        String targetDir = StrUtil.blankToDefault(directory, ossProperties.getDefaultDir());
        targetDir = targetDir.replace("\\", "/");
        if (targetDir.startsWith("/")) {
            targetDir = targetDir.substring(1);
        }
        if (targetDir.endsWith("/")) {
            targetDir = targetDir.substring(0, targetDir.length() - 1);
        }

        String suffix = "";
        if (StrUtil.isNotBlank(originalFilename) && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return targetDir + "/" + LocalDate.now().format(DATE_FORMATTER) + "/" + IdUtil.simpleUUID() + suffix;
    }

    private String buildFileUrl(String objectKey) {
        String domain = StrUtil.blankToDefault(ossProperties.getPublicDomain(),
                "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint());
        if (domain.endsWith("/")) {
            return domain + objectKey;
        }
        return domain + "/" + objectKey;
    }
}
