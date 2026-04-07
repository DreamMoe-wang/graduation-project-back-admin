package com.example.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 定位解析结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResolveVO {

    private Double longitude;

    private Double latitude;

    private String provinceName;

    private String cityName;

    private String areaName;

    private String address;
}
