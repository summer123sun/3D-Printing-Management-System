package com.printclub.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记接口需要特定角色
 *
 * <p>用法：</p>
 * <pre>
 * {@code
 * @RequireRole({1, 2})  // 仅社长(1)/技术骨干(2)可访问
 * @PutMapping("/task/{id}/approve")
 * public Result<Void> approve(@PathVariable String id) { ... }
 * }
 * </pre>
 *
 * @author D
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /** 允许的角色值，对应 member.role 字段（1社长 2技术骨干 3普通社员 4新成员） */
    int[] value();
}