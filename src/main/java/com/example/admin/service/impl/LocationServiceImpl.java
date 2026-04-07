package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.admin.config.BaiduMapProperties;
import com.example.admin.service.LocationService;
import com.example.admin.vo.LocationResolveVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 定位服务实现
 */
@Service
public class LocationServiceImpl implements LocationService {

    private static final String REVERSE_GEOCODE_URL = "https://api.map.baidu.com/reverse_geocoding/v3/";
    private static final String IP_LOCATE_URL = "https://api.map.baidu.com/location/ip";

    @Resource
    private BaiduMapProperties baiduMapProperties;

    @Override
    public LocationResolveVO reverseGeocode(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new RuntimeException("缺少定位坐标");
        }

        String requestUrl = REVERSE_GEOCODE_URL + "?" + createReverseQuery(latitude, longitude);
        JSONObject payload = requestBaiduApi(requestUrl, "百度逆地理编码失败");
        validateBaiduResponse(payload, "百度逆地理编码失败");

        JSONObject result = payload.getJSONObject("result");
        JSONObject addressComponent = result == null ? null : result.getJSONObject("addressComponent");

        return LocationResolveVO.builder()
                .latitude(latitude)
                .longitude(longitude)
                .provinceName(readString(addressComponent, "province"))
                .cityName(trimCitySuffix(readString(addressComponent, "city")))
                .areaName(readString(addressComponent, "district"))
                .address(readString(result, "formatted_address"))
                .build();
    }

    @Override
    public LocationResolveVO locateByIp(HttpServletRequest request) {
        String clientIp = resolveClientIp(request);
        if (StrUtil.isBlank(clientIp) || "127.0.0.1".equals(clientIp) || "::1".equals(clientIp)) {
            throw new RuntimeException("当前环境无法通过 IP 获取准确位置，请允许浏览器定位");
        }

        String requestUrl = String.format("%s?ak=%s&coor=bd09ll&ip=%s", IP_LOCATE_URL, getRequiredAk(), clientIp);
        JSONObject payload = requestBaiduApi(requestUrl, "百度 IP 定位失败");
        validateBaiduResponse(payload, "百度 IP 定位失败");

        JSONObject content = payload.getJSONObject("content");
        JSONObject point = content == null ? null : content.getJSONObject("point");
        JSONObject addressDetail = content == null ? null : content.getJSONObject("address_detail");

        return LocationResolveVO.builder()
                .longitude(point == null ? null : point.getDouble("x"))
                .latitude(point == null ? null : point.getDouble("y"))
                .provinceName(readString(addressDetail, "province"))
                .cityName(trimCitySuffix(readString(addressDetail, "city")))
                .areaName(readString(addressDetail, "district"))
                .address(readString(content, "address"))
                .build();
    }

    private String createReverseQuery(Double latitude, Double longitude) {
        return String.format(
                "ak=%s&output=json&coordtype=wgs84ll&extensions_poi=0&location=%s,%s",
                getRequiredAk(),
                latitude,
                longitude
        );
    }

    private void validateBaiduResponse(JSONObject payload, String defaultMessage) {
        if (payload == null) {
            throw new RuntimeException(defaultMessage);
        }

        Integer status = payload.getInt("status", -1);
        if (status != null && status == 0) {
            return;
        }

        String message = payload.getStr("message");
        throw new RuntimeException(StrUtil.blankToDefault(message, defaultMessage));
    }

    private JSONObject requestBaiduApi(String requestUrl, String defaultMessage) {
        try {
            String responseBody = HttpRequest.get(requestUrl)
                    .timeout(getTimeoutMs())
                    .execute()
                    .body();
            return JSONUtil.parseObj(responseBody);
        } catch (Exception error) {
            throw new RuntimeException(defaultMessage, error);
        }
    }

    private String getRequiredAk() {
        String ak = baiduMapProperties.getAk();
        if (StrUtil.isBlank(ak)) {
            throw new RuntimeException("后端未配置百度地图 AK");
        }
        return ak.trim();
    }

    private int getTimeoutMs() {
        Integer timeoutMs = baiduMapProperties.getTimeoutMs();
        return timeoutMs == null || timeoutMs < 1000 ? 5000 : timeoutMs;
    }

    private String trimCitySuffix(String cityName) {
        if (StrUtil.isBlank(cityName)) {
            return "";
        }
        return cityName.endsWith("市") ? cityName.substring(0, cityName.length() - 1) : cityName;
    }

    private String readString(JSONObject object, String key) {
        return object == null ? "" : StrUtil.blankToDefault(object.getStr(key), "");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String[] headers = new String[] {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
                String[] ipList = ip.split(",");
                for (String candidate : ipList) {
                    String normalized = StrUtil.trim(candidate);
                    if (StrUtil.isNotBlank(normalized) && !"unknown".equalsIgnoreCase(normalized)) {
                        return normalized;
                    }
                }
            }
        }

        return request.getRemoteAddr();
    }
}
