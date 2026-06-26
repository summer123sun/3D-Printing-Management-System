package com.printclub.common.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printclub.common.result.PageResult;

/**
 * 分页结果转换工具
 *
 * <p>MyBatis-Plus 的 Page 序列化后字段是 {@code records}，</p>
 * <p>前端约定的是 {@code list}。这里做转换。</p>
 *
 * @author D
 */
public class PageUtils {

    private PageUtils() {}

    /**
     * MyBatis-Plus Page → 自定义 PageResult
     */
    public static <T> PageResult<T> toResult(Page<T> page) {
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }
}