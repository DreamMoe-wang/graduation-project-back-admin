package com.example.admin.controller;

import com.example.admin.common.Result;
import com.example.admin.service.LocationService;
import com.example.admin.vo.LocationResolveVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 定位控制器
 */
@RestController
@RequestMapping("/location")
public class LocationController {

    @Resource
    private LocationService locationService;

    @GetMapping("/reverse-geocode")
    public Result<LocationResolveVO> reverseGeocode(@RequestParam Double latitude,
                                                    @RequestParam Double longitude) {
        return Result.success(locationService.reverseGeocode(latitude, longitude));
    }

    @GetMapping("/ip")
    public Result<LocationResolveVO> locateByIp(HttpServletRequest request) {
        return Result.success(locationService.locateByIp(request));
    }
}
