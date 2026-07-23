package com.ailisting.config;

import com.ailisting.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;

    @Pointcut("execution(* com.ailisting.controller.*.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String action = deriveAction(joinPoint);
        String method = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getMethod();
        String endpoint = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getRequestURI();
        String ipAddress = getClientIp(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest());
        String userAgent = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getHeader("User-Agent");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        String username = null;
        if (auth != null && auth.getPrincipal() instanceof com.ailisting.model.entity.User user) {
            userId = user.getId();
            username = user.getUsername();
        }

        Object result;
        boolean success = true;
        String errorMessage = null;
        Integer statusCode = 200;

        try {
            result = joinPoint.proceed();
        } catch (Throwable t) {
            success = false;
            errorMessage = t.getMessage();
            statusCode = 500;
            throw t;
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            auditService.log(action, null, null, method, endpoint, userId, username,
                    ipAddress, userAgent, statusCode, success, errorMessage, durationMs);
        }

        return result;
    }

    private String deriveAction(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        return switch (methodName) {
            case "login" -> "LOGIN";
            case "register" -> "REGISTER";
            case "refreshToken" -> "REFRESH_TOKEN";
            case "logout", "logoutAll" -> "LOGOUT";
            case "forgotPassword" -> "FORGOT_PASSWORD";
            case "resetPassword" -> "RESET_PASSWORD";
            case "verifyEmail" -> "VERIFY_EMAIL";
            case "createListing" -> "CREATE_LISTING";
            case "updateListing" -> "UPDATE_LISTING";
            case "deleteListing" -> "DELETE_LISTING";
            case "getListing", "getListingById" -> "VIEW_LISTING";
            case "generateContent" -> "AI_GENERATE";
            case "clearAllCaches" -> "CACHE_CLEAR";
            case "getCacheStats" -> "CACHE_STATS";
            case "getUsers", "getUserById", "toggleUserEnabled", "updateUserRole", "getAnalytics", "healthCheck" -> "ADMIN_" + methodName.toUpperCase();
            default -> className.replace("Controller", "") + "_" + methodName.toUpperCase();
        };
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
