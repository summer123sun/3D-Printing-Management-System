package com.printclub.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果
 *
 * @author D
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 数据列表 */
    private List<T> list;

    /** 总记录数 */
    private long total;

    /** 当前页（从 1 开始） */
    private long page;

    /** 每页大小 */
    private long size;

    /** 总页数 */
    private long pages;

    public static <T> PageResult<T> of(List<T> list, long total, long page, long size) {
        long pages = size == 0 ? 0 : (total + size - 1) / size;
        return new PageResult<>(list, total, page, size, pages);
    }
}