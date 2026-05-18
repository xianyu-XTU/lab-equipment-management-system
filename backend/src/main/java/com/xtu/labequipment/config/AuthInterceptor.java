package com.xtu.labequipment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtu.labequipment.common.AuthContext;
import com.xtu.labequipment.common.JwtUtil;
import com.xtu.labequipment.common.Result;
import com.xtu.labequipment.common.TokenUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            write(response, Result.unauthorized("please login"));
            return false;
        }
        try {
            TokenUser user = jwtUtil.parse(header.substring(7));
            AuthContext.set(user.getUserId(), user.getUsername(), user.getRoleCode());
            return true;
        } catch (Exception e) {
            write(response, Result.unauthorized("login expired or invalid token"));
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private void write(HttpServletResponse response, Result<?> result) throws Exception {
        response.setStatus(result.getCode());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
