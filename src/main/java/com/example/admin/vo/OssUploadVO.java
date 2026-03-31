package com.example.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OSS 上传结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OssUploadVO {

    private String objectKey;

    private String originalFilename;

    private String url;

    private Long size;

    private String contentType;
}
