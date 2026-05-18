package com.xtu.labequipment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtu.labequipment.annotation.RequireRole;
import com.xtu.labequipment.common.AuthContext;
import com.xtu.labequipment.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class RoleInterceptor implements HandlerInterceptor {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod hm)) return true;
        RequireRole requireRole = hm.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) requireRole = hm.getBeanType().getAnnotation(RequireRole.class);
        if (requireRole == null) return true;
        String role = AuthContext.getRoleCode();
        boolean ok = Arrays.asList(requireRole.value()).contains(role);
        if (!ok) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.forbidden("permission denied")));
        }
        return ok;
    }
}
