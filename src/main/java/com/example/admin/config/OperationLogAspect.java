package com.example.admin.config;

import cn.hutool.core.util.StrUtil;
import com.example.admin.common.Result;
import com.example.admin.dto.LoginDTO;
import com.example.admin.entity.OperationLog;
import com.example.admin.security.SecurityUtils;
import com.example.admin.service.OperationLogService;
import com.example.admin.vo.LoginVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    private static final Map<String, MenuInfo> MENU_MAPPING = new LinkedHashMap<>();

    static {
        MENU_MAPPING.put("/trade/publish", new MenuInfo("交易发布", "/trade/publish"));
        MENU_MAPPING.put("/trade/list", new MenuInfo("交易大全", "/trade/list"));
        MENU_MAPPING.put("/trade/order", new MenuInfo("订单大全", "/trade/order"));
        MENU_MAPPING.put("/user/profile", new MenuInfo("个人中心", "/profile"));
        MENU_MAPPING.put("/user", new MenuInfo("用户管理", "/user"));
        MENU_MAPPING.put("/role", new MenuInfo("角色管理", "/role"));
        MENU_MAPPING.put("/menu", new MenuInfo("菜单管理", "/menu"));
        MENU_MAPPING.put("/notice", new MenuInfo("通知公告", "/notice"));
        MENU_MAPPING.put("/log", new MenuInfo("日志管理", "/log"));
        MENU_MAPPING.put("/setting", new MenuInfo("系统设置", "/setting"));
        MENU_MAPPING.put("/auth", new MenuInfo("认证中心", "/auth"));
        MENU_MAPPING.put("/user/login", new MenuInfo("认证中心", "/auth"));
    }

    @Resource
    private OperationLogService operationLogService;

    @Around("execution(public * com.example.admin.controller..*(..))")
    public Object aroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        String normalizedPath = normalizePath(request);
        String requestMethod = request.getMethod();

        if (!shouldLog(normalizedPath, requestMethod)) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            writeOperationLog(joinPoint, request, normalizedPath, requestMethod, result, null, startTime);
            return result;
        } catch (Throwable throwable) {
            writeOperationLog(joinPoint, request, normalizedPath, requestMethod, null, throwable, startTime);
            throw throwable;
        }
    }

    private void writeOperationLog(ProceedingJoinPoint joinPoint,
                                   HttpServletRequest request,
                                   String normalizedPath,
                                   String requestMethod,
                                   Object result,
                                   Throwable throwable,
                                   long startTime) {
        try {
            MenuInfo menuInfo = resolveMenuInfo(normalizedPath);
            String username = resolveUsername(result, joinPoint.getArgs(), normalizedPath);
            Long userId = resolveUserId(result);
            boolean success = throwable == null;
            String resultMessage = success ? "操作成功" : throwable.getMessage();

            if (result instanceof Result) {
                Result<?> resultWrapper = (Result<?>) result;
                success = resultWrapper.isSuccess();
                resultMessage = resultWrapper.getMessage();
            }

            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(userId);
            operationLog.setUsername(StrUtil.blankToDefault(username, "匿名用户"));
            operationLog.setMenuName(menuInfo.getMenuName());
            operationLog.setMenuPath(menuInfo.getMenuPath());
            operationLog.setActionName(resolveActionName(normalizedPath, requestMethod));
            operationLog.setRequestMethod(requestMethod);
            operationLog.setRequestUri(buildRequestUri(normalizedPath, request));
            operationLog.setIpAddress(resolveClientIp(request));
            operationLog.setOperationStatus(success ? 1 : 0);
            operationLog.setDurationMs(Math.max(System.currentTimeMillis() - startTime, 0));
            operationLog.setResultMessage(StrUtil.sub(StrUtil.blankToDefault(resultMessage, success ? "操作成功" : "操作失败"), 0, 255));
            operationLogService.record(operationLog);
        } catch (Exception ex) {
            log.warn("记录操作日志失败: {}", ex.getMessage());
        }
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    private String normalizePath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StrUtil.isNotBlank(contextPath) && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }

    private boolean shouldLog(String normalizedPath, String requestMethod) {
        if (StrUtil.isBlank(normalizedPath) || "OPTIONS".equalsIgnoreCase(requestMethod)) {
            return false;
        }
        if (normalizedPath.startsWith("/dashboard/")) {
            return false;
        }
        if (normalizedPath.startsWith("/auth/me")
                || normalizedPath.startsWith("/auth/menus")
                || normalizedPath.startsWith("/auth/permissions")) {
            return false;
        }
        if (normalizedPath.startsWith("/chat")) {
            return false;
        }

        if ("GET".equalsIgnoreCase(requestMethod)) {
            return normalizedPath.endsWith("/page")
                    || normalizedPath.endsWith("/list")
                    || normalizedPath.endsWith("/detail")
                    || normalizedPath.endsWith("/stats")
                    || normalizedPath.endsWith("/export")
                    || normalizedPath.endsWith("/profile/current")
                    || normalizedPath.matches(".*/\\d+$");
        }

        return true;
    }

    private MenuInfo resolveMenuInfo(String normalizedPath) {
        for (Map.Entry<String, MenuInfo> entry : MENU_MAPPING.entrySet()) {
            if (normalizedPath.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return new MenuInfo("系统操作", normalizedPath);
    }

    private String resolveActionName(String normalizedPath, String requestMethod) {
        if (normalizedPath.endsWith("/login")) {
            return "登录";
        }

        if ("GET".equalsIgnoreCase(requestMethod)) {
            if (normalizedPath.endsWith("/page") || normalizedPath.endsWith("/list")) {
                return "查询列表";
            }
            if (normalizedPath.endsWith("/stats")) {
                return "查看统计";
            }
            if (normalizedPath.endsWith("/export")) {
                return "导出数据";
            }
            return "查看详情";
        }

        if ("POST".equalsIgnoreCase(requestMethod)) {
            if (normalizedPath.endsWith("/approve")) {
                return "审核通过";
            }
            if (normalizedPath.endsWith("/reject")) {
                return "审核驳回";
            }
            if (normalizedPath.endsWith("/receive")) {
                return normalizedPath.contains("/order/") ? "接单" : "接取";
            }
            if (normalizedPath.endsWith("/complete")) {
                return "完成订单";
            }
            if (normalizedPath.endsWith("/pay")) {
                return "订单支付";
            }
            if (normalizedPath.endsWith("/cancel")) {
                return "取消订单";
            }
            return "新增";
        }

        if ("PUT".equalsIgnoreCase(requestMethod)) {
            return normalizedPath.endsWith("/detail") || normalizedPath.endsWith("/profile/current") ? "保存配置" : "编辑";
        }

        if ("DELETE".equalsIgnoreCase(requestMethod)) {
            return normalizedPath.endsWith("/clean") ? "清空日志" : "删除";
        }

        return "系统操作";
    }

    private String resolveUsername(Object result, Object[] args, String normalizedPath) {
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (StrUtil.isNotBlank(currentUsername)) {
            return currentUsername;
        }

        if (result instanceof Result) {
            Object data = ((Result<?>) result).getData();
            if (data instanceof LoginVO && StrUtil.isNotBlank(((LoginVO) data).getUsername())) {
                return ((LoginVO) data).getUsername();
            }
        }

        if (normalizedPath.endsWith("/login") && args != null && args.length > 0) {
            if (args[0] instanceof LoginDTO) {
                return ((LoginDTO) args[0]).getUsername();
            }
            if (args[0] instanceof String) {
                return (String) args[0];
            }
        }

        return null;
    }

    private Long resolveUserId(Object result) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId != null) {
            return currentUserId;
        }

        if (result instanceof Result) {
            Object data = ((Result<?>) result).getData();
            if (data instanceof LoginVO) {
                return ((LoginVO) data).getUserId();
            }
        }

        return null;
    }

    private String buildRequestUri(String normalizedPath, HttpServletRequest request) {
        if (StrUtil.isBlank(request.getQueryString())) {
            return normalizedPath;
        }
        return normalizedPath + "?" + request.getQueryString();
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
                int commaIndex = ip.indexOf(',');
                return commaIndex > -1 ? ip.substring(0, commaIndex).trim() : ip.trim();
            }
        }

        return request.getRemoteAddr();
    }

    @Getter
    @AllArgsConstructor
    private static class MenuInfo {
        private final String menuName;
        private final String menuPath;
    }
}
