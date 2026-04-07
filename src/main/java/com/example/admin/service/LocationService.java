package com.example.admin.service;

import com.example.admin.vo.LocationResolveVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 定位服务
 */
public interface LocationService {

    LocationResolveVO reverseGeocode(Double latitude, Double longitude);

    LocationResolveVO locateByIp(HttpServletRequest request);
}
