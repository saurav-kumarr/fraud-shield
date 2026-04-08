package com.fraudshield.report.context;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Slf4j
public class UserContext {

    public String getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) return null;
        return request.getHeader("X-User-Id");
    }

    public String getCurrentUserEmail() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) return null;
        return request.getHeader("X-User-Email");
    }

    public String getCurrentUserRole() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) return null;
        return request.getHeader("X-User-Role");
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            return ((ServletRequestAttributes)
                    RequestContextHolder
                            .currentRequestAttributes())
                    .getRequest();
        } catch (Exception e) {
            log.error("Could not get current request");
            return null;
        }
    }
}