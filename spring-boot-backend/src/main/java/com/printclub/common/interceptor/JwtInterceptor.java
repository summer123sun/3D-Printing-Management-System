package com.printclub.common.interceptor;

import com.printclub.common.annotation.RequireAuth;
import com.printclub.common.annotation.RequireRole;
import com.printclub.common.exception.BusinessException;
import com.printclub.common.result.ResultCode;
import com.printclub.common.util.JwtUtil;
import com.printclub.common.util.SecurityContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

/**
 * JWT 拦截器
 *
 * <p>职责：</p>
 * <ol>
 *   <li>从 {@code Authorization: Bearer <token>} 解析 JWT</li>
 *   <li>检查 {@code @RequireAuth} 注解</li>
 *   <li>检查 {@code @RequireRole} 注解</li>
 *   <li>把当前用户信息塞入 {@link SecurityContext}</li>
 * </ol>
 *
 * @author D
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Value("${jwt.header}")
    private String headerName;

    @Value("${jwt.prefix}")
    private String prefix;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 静态资源、错误页等不需要 token
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod hm = (HandlerMethod) handler;
        Method method = hm.getMethod();

        RequireAuth requireAuth = method.getAnnotation(RequireAuth.class);
        RequireRole requireRole = method.getAnnotation(RequireRole.class);

        // 没有注解默认不要求登录（公开接口）
        if (requireAuth == null && requireRole == null) {
            return true;
        }

        // 1. 取 token
        String token = request.getHeader(headerName);
        if (token == null || !token.startsWith(prefix)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        token = token.substring(prefix.length()).trim();

        // 2. 解析 token
        Claims claims;
        try {
            claims = jwtUtil.parse(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new BusinessException(ResultCode.TOKEN_EXPIRED);
        } catch (Exception e) {
            log.warn("token 解析失败：{}", e.getMessage());
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 3. 写入上下文
        String studentId = claims.getSubject();
        Integer role = claims.get("role", Integer.class);
        SecurityContext.set(studentId, role);

        // 4. 角色校验
        if (requireRole != null) {
            if (role == null) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
            boolean ok = false;
            for (int r : requireRole.value()) {
                if (r == role) { ok = true; break; }
            }
            if (!ok) {
                throw new BusinessException(ResultCode.FORBIDDEN, "需要角色：" + java.util.Arrays.toString(requireRole.value()));
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SecurityContext.clear();
    }
}