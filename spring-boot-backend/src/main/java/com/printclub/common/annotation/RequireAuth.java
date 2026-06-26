package com.printclub.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记接口需要登录
 *
 * <p>用法：</p>
 * <pre>
 * {@code
 * @RequireAuth
 * @GetMapping("/user/info")
 * public Result<UserVO> getUserInfo() { ... }
 * }
 * </pre>
 *
 * @author D
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuth {
}