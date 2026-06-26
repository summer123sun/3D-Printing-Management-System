package com.printclub.common.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户上下文（基于 ThreadLocal）
 * <p>由 JwtInterceptor 在解析 token 后写入，由 Service 层读取</p>
 *
 * @author D
 */
public class SecurityContext {

    private static final ThreadLocal<CurrentUser> USER = new ThreadLocal<>();

    private SecurityContext() {}

    /** 设置当前登录用户 */
    public static void set(String studentId, Integer role) {
        USER.set(new CurrentUser(studentId, role));
    }

    /** 获取当前登录用户的学号 */
    public static String getCurrentUserId() {
        CurrentUser u = USER.get();
        return u == null ? null : u.getStudentId();
    }

    /** 获取当前登录用户的角色 */
    public static Integer getCurrentRole() {
        CurrentUser u = USER.get();
        return u == null ? null : u.getRole();
    }

    /** 获取当前登录用户的全部信息 */
    public static CurrentUser getCurrentUser() {
        return USER.get();
    }

    /** 清除（必须在拦截器 afterCompletion 中调用） */
    public static void clear() {
        USER.remove();
    }

    /** 当前用户 DTO */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentUser {
        private String studentId;
        private Integer role;
    }
}